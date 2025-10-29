package puppy.code;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

public class Tarro {
   private Rectangle bucket;
   private Texture bucketImage;
   private Sound sonidoHerido;
   private int vidas = 3;
   private int puntos = 0;
   private int velx = 400;
   private boolean herido = false;
   private int tiempoHeridoMax=50;
   private int tiempoHerido;

   // contador de gotas recogidas (cada gota buena suma 1 aquí)
   private int gotasRecolectadas = 0;
   
   public Tarro(Texture tex, Sound ss) {
	   bucketImage = tex;
	   sonidoHerido = ss;
   }
   
	public int getVidas() {
		return vidas;
	}

	public int getPuntos() {
		return puntos;
	}
	public Rectangle getArea() {
		return bucket;
	}
	public void sumarPuntos(int pp) {
		puntos+=pp;
	}
	
	public int getGotasRecolectadas() {
	    return gotasRecolectadas;
	}
	public void incrementarGotasRecolectadas() {
	    gotasRecolectadas++;
	}
	// comprobar si tiene suficiente para lanzar poder (20 gotas)
	public boolean puedeLanzarPoder() {
	    return gotasRecolectadas >= 20;
	}
	// consumir 20 gotas al lanzar el poder
	public void consumirPoder() {
	    if (puedeLanzarPoder()) gotasRecolectadas -= 20;
	    if (gotasRecolectadas < 0) gotasRecolectadas = 0;
	}
	

   public void crear() {
	      bucket = new Rectangle();
	      bucket.x = 800 / 2 - 64 / 2;
	      bucket.y = 20;
	      bucket.width = 64;
	      bucket.height = 64;
   }
   public void dañar() {
	  vidas--;
	  herido = true;
	  tiempoHerido=tiempoHeridoMax;
	  sonidoHerido.play();
   }
   public void dibujar(SpriteBatch batch) {
	 if (!herido)  
	   batch.draw(bucketImage, bucket.x, bucket.y);
	 else {
	
	   batch.draw(bucketImage, bucket.x, bucket.y+ MathUtils.random(-5,5));
	   tiempoHerido--;
	   if (tiempoHerido<=0) herido = false;
	 }
   } 
   
   
   public void actualizarMovimiento() { 
	   // movimiento desde mouse/touch
	   /*if(Gdx.input.isTouched()) {
		      Vector3 touchPos = new Vector3();
		      touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
		      camera.unproject(touchPos);
		      bucket.x = touchPos.x - 64 / 2;
		}*/
	   //movimiento desde teclado
	   if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) bucket.x -= velx * Gdx.graphics.getDeltaTime();
	   if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) bucket.x += velx * Gdx.graphics.getDeltaTime();
	   // que no se salga de los bordes izq y der
	   if(bucket.x < 0) bucket.x = 0;
	   if(bucket.x > 800 - 64) bucket.x = 800 - 64;
   }
    

public void destruir() {
	    bucketImage.dispose();
   }

public boolean estaHerido() {
   return herido;
}
   
}