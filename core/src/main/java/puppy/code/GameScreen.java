package puppy.code;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.math.Rectangle;

public class GameScreen implements Screen {

	private final GameLluviaMenu game;
	private SpriteBatch batch;
	private OrthographicCamera camera;
	private Tarro tarro;
	private Lluvia lluvia;
	private final com.badlogic.gdx.graphics.g2d.BitmapFont font;
	
	public GameScreen(final GameLluviaMenu game) {
		this.game = game;
        this.batch = game.getBatch();
        this.font = game.getFont();
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 800, 480);

        // cargar recursos
		  Sound hurtSound = Gdx.audio.newSound(Gdx.files.internal("hurt.ogg"));
		  tarro = new Tarro(new Texture(Gdx.files.internal("bucket.png")),hurtSound);
         
	      Texture gota = new Texture(Gdx.files.internal("drop.png"));
         Texture gotaMala = new Texture(Gdx.files.internal("dropBad.png"));
         
         Sound dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
	     Music rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));
	     
	     lluvia = new Lluvia(gota, gotaMala, dropSound, rainMusic);
	     
	     // creacion del tarro
	     tarro.crear();
	     // creacion de la lluvia
	     lluvia.crear();
	}

	@Override
	public void render(float delta) {
		ScreenUtils.clear(0, 0, 0.2f, 1);
		camera.update();
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		//dibujar textos
		font.draw(batch, "Puntos: " + tarro.getPuntos(), 5, 475);
		font.draw(batch, "Vidas : " + tarro.getVidas(), 670, 475);
		font.draw(batch, "HighScore : " + game.getHigherScore(), camera.viewportWidth/2-50, 475);
		font.draw(batch, "Gotas recogidas: " + tarro.getGotasRecolectadas(), 5, 455);
		font.draw(batch, "Presiona SPACE para lanzar poder (20 gotas)", 200, 455);

		if (!tarro.estaHerido()) {
			// movimiento del tarro desde teclado
	        tarro.actualizarMovimiento();        
			// caida de la lluvia 
	        if (!lluvia.actualizarMovimiento(tarro)) {
	    	  //actualizar HigherScore
	    	  if (game.getHigherScore()<tarro.getPuntos())
	    		  game.setHigherScore(tarro.getPuntos());  
	    	  //ir a la ventana de finde juego y destruir la actual
	    	  game.setScreen(new GameOverScreen(game));
	    	  dispose();
	        }
		}
		
		// manejar uso de poder: al presionar SPACE si tiene >=20 gotas
		if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && tarro.puedeLanzarPoder()) {
		    // area frontal sobre el tarro (ajusta altura si quieres)
		    Rectangle poderArea = new Rectangle(tarro.getArea().x, tarro.getArea().y + tarro.getArea().height, tarro.getArea().width, 220);
		    lluvia.eliminarGotasEnArea(poderArea);
		    tarro.consumirPoder();
		    // (opcional) reproducir sonido de poder si lo añades
		}
		
		tarro.dibujar(batch);
		lluvia.actualizarDibujoLluvia(batch);
		
		batch.end();
	}

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void show() {
	  lluvia.continuar();
	}

	@Override
	public void hide() {

	}

	@Override
	public void pause() {
		lluvia.pausar();
		game.setScreen(new PausaScreen(game, this)); 
	}

	@Override
	public void resume() {

	}

	@Override
	public void dispose() {
      tarro.destruir();
      lluvia.destruir();
	}
}