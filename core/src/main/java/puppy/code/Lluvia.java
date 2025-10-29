package puppy.code;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

public class Lluvia {
	private Array<Rectangle> rainDropsPos;
	private Array<Integer> rainDropsType;
    private long lastDropTime;
    private Texture gotaBuena;
    private Texture gotaMala;
    private Sound dropSound;
    private Music rainMusic;

    // Valores base / ajustables
    private int difficultyLevel = 0; // sube cada 50 gotas recogidas
    private final long BASE_MIN_SPAWN_MS = 400;  // inicio: gotas menos frecuentes
    private final long BASE_MAX_SPAWN_MS = 1200;
    private final int BASE_MAX_DROPS = 6;        // inicio: pocas gotas en pantalla
    private final float BASE_MIN_SEPARATION_PX = 80f;

    // Estado ajustable por dificultad
    private long minSpawnMs = BASE_MIN_SPAWN_MS;
    private long maxSpawnMs = BASE_MAX_SPAWN_MS;
    private int maxDrops = BASE_MAX_DROPS;
    private float minSeparation = BASE_MIN_SEPARATION_PX;

	public Lluvia(Texture gotaBuena, Texture gotaMala, Sound ss, Music mm) {
		rainMusic = mm;
		dropSound = ss;
		this.gotaBuena = gotaBuena;
		this.gotaMala = gotaMala;
	}
	
	public void crear() {
		rainDropsPos = new Array<Rectangle>();
		rainDropsType = new Array<Integer>();
		// programar la primera aparición
		scheduleNextDrop();
	    rainMusic.setLooping(true);
	    rainMusic.play();
	}
	
	private void scheduleNextDrop() {
	    long intervalMs = MathUtils.random((int)minSpawnMs, (int)maxSpawnMs);
	    lastDropTime = TimeUtils.nanoTime() + intervalMs * 1_000_000L;
	}
	
	private boolean isTooClose(float x) {
	    for (int i = 0; i < rainDropsPos.size; i++) {
	        Rectangle r = rainDropsPos.get(i);
	        if (Math.abs(r.x - x) < minSeparation) return true;
	    }
	    return false;
	}

	private void crearGotaDeLluvia() {
	      // limita el total de gotas en pantalla
	      if (rainDropsPos.size >= maxDrops) {
	          scheduleNextDrop();
	          return;
	      }

	      Rectangle raindrop = new Rectangle();
	      float x;
	      int attempts = 0;
	      // intenta encontrar una posición no muy cercana a otras gotas (hasta 10 intentos)
	      do {
	          x = MathUtils.random(0, 800-64);
	          attempts++;
	      } while(attempts < 10 && isTooClose(x));
	      raindrop.x = x;
	      raindrop.y = 480;
	      raindrop.width = 64;
	      raindrop.height = 64;
	      rainDropsPos.add(raindrop);
	      // ver el tipo de gota
	      if (MathUtils.random(1,10)<5)	    	  
	         rainDropsType.add(1);
	      else 
	    	 rainDropsType.add(2);
	      // programar la siguiente aparición con intervalo aleatorio
	      scheduleNextDrop();
	   }
	
   public boolean actualizarMovimiento(Tarro tarro) { 
	   // generar gotas de lluvia cuando haya pasado el tiempo programado
	   if(TimeUtils.nanoTime() > lastDropTime) crearGotaDeLluvia();
	  
	   // ajustar dificultad en función de gotas recogidas
	   adjustDifficulty(tarro.getGotasRecolectadas());
	   
	   // revisar si las gotas cayeron al suelo o chocaron con el tarro
	   // iteramos hacia atrás para evitar saltarnos elementos al eliminar
	   for (int i = rainDropsPos.size - 1; i >= 0; i--) {
		  Rectangle raindrop = rainDropsPos.get(i);
	      raindrop.y -= 300 * Gdx.graphics.getDeltaTime();
	      // cae al suelo y se elimina
	      if(raindrop.y + 64 < 0) {
	    	  rainDropsPos.removeIndex(i); 
	    	  rainDropsType.removeIndex(i);
	    	  continue;
	      }
	      if(raindrop.overlaps(tarro.getArea())) { // la gota choca con el tarro
	    	if(rainDropsType.get(i)==1) { // gota dañina
	    	  tarro.dañar();
	    	  if (tarro.getVidas()<=0)
	    		 return false; // si se queda sin vidas retorna falso /game over
	    	  rainDropsPos.removeIndex(i);
	          rainDropsType.removeIndex(i);
	      	}else { // gota a recolectar
	    	  tarro.sumarPuntos(10);
	          tarro.incrementarGotasRecolectadas(); // contamos la gota recogida
	          dropSound.play();
	          rainDropsPos.removeIndex(i);
	          rainDropsType.removeIndex(i);
	      	}
	      }
	   } 
	  return true; 
   }
   
   private void adjustDifficulty(int gotasRecogidas) {
       int newLevel = gotasRecogidas / 50; // sube cada 50 gotas
       if (newLevel == difficultyLevel) return;
       difficultyLevel = newLevel;
       // por nivel aumentamos la dificultad: reducimos intervalos, aumentamos max drops, reducimos separación
       // límites para no quedar absurdos
       minSpawnMs = Math.max(120, BASE_MIN_SPAWN_MS - difficultyLevel * 60);
       maxSpawnMs = Math.max(250, BASE_MAX_SPAWN_MS - difficultyLevel * 120);
       maxDrops = Math.min(40, BASE_MAX_DROPS + difficultyLevel * 4);
       minSeparation = Math.max(20f, BASE_MIN_SEPARATION_PX - difficultyLevel * 10f);
   }

   // elimina gotas que estén dentro del área (usado para el poder)
   public void eliminarGotasEnArea(Rectangle area) {
       for (int i = rainDropsPos.size - 1; i >= 0; i--) {
           Rectangle r = rainDropsPos.get(i);
           if (r.overlaps(area)) {
               rainDropsPos.removeIndex(i);
               rainDropsType.removeIndex(i);
           }
       }
   }
   
   public void actualizarDibujoLluvia(SpriteBatch batch) { 
	  for (int i=0; i < rainDropsPos.size; i++ ) {
		  Rectangle raindrop = rainDropsPos.get(i);
		  if(rainDropsType.get(i)==1) // gota dañina
	         batch.draw(gotaMala, raindrop.x, raindrop.y); 
		  else
			 batch.draw(gotaBuena, raindrop.x, raindrop.y); 
	   }
   }
   public void destruir() {
      // liberar recursos usados por la lluvia
      if (gotaBuena != null) gotaBuena.dispose();
      if (gotaMala != null) gotaMala.dispose();
      if (dropSound != null) dropSound.dispose();
      if (rainMusic != null) rainMusic.dispose();
   }
   public void pausar() {
	  if (rainMusic != null) rainMusic.stop();
   }
   public void continuar() {
	  if (rainMusic != null) rainMusic.play();
   }
   
}