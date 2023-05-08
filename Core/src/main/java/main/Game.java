package main;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import common.data.Entity;
import common.data.GameData;
import common.data.World;
import common.data.entityparts.PositionPart;
import common.services.IEntityProcessingService;
import common.services.IGamePluginService;
import common.services.IPostEntityProcessingService;
import managers.GameInputProcessor;
import managers.SpriteCache;

import java.util.Collection;
import java.util.ServiceLoader;

import static common.data.GameKeys.ENTER;
import static common.data.GameKeys.ESCAPE;
import static java.util.stream.Collectors.toList;

public class Game implements ApplicationListener {
    private SpriteBatch sb;
    private final GameData gameData = new GameData();
    private final World world = new World();

    @Override
    public void create() {
        gameData.setDisplayWidth(Gdx.graphics.getWidth());
        gameData.setDisplayHeight(Gdx.graphics.getHeight());

        OrthographicCamera cam = new OrthographicCamera(gameData.getDisplayWidth(), gameData.getDisplayHeight());
        cam.translate((float) gameData.getDisplayWidth() / 2, (float) gameData.getDisplayHeight() / 2);
        cam.update();

        sb = new SpriteBatch();

        Gdx.input.setInputProcessor(new GameInputProcessor(gameData));

        for (IGamePluginService iGamePlugin : getPluginServices()) {
            iGamePlugin.start(gameData, world);
            System.out.println(iGamePlugin.getClass());
        }
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        gameData.setDelta(Gdx.graphics.getDeltaTime());

        deleteEntity();

        update();

        draw();

        gameData.getKeys().update();
    }

    private void update() {
        for (IEntityProcessingService entityProcessorService : getEntityProcessingServices()) {
            entityProcessorService.process(gameData, world);
        }
        for (IPostEntityProcessingService postEntityProcessorService : getPostEntityProcessingServices()) {
            postEntityProcessorService.process(gameData, world);
        }


        gameData.setGameTime(Math.max(gameData.getGameTime() + gameData.getDelta(), 0.18F));
    }

    private void deleteEntity() {
        for (IGamePluginService iGamePlugin : getPluginServices()) {
            if (gameData.getKeys().isDown(ESCAPE) && (iGamePlugin.getClass().getName().equals("playersystem.PlayerPlugin"))) {
                iGamePlugin.stop(gameData, world);
            }
            if (gameData.getKeys().isDown(ENTER) && (iGamePlugin.getClass().getName().equals("mapsystem.MapPlugin"))) {
                iGamePlugin.stop(gameData, world);
                System.out.println("Map");
            }
        }
    }

    private void draw() {
        sb.begin();
        //Draw all sprites, update the sprites position beforehand
        for (Entity entity : world.getEntities()) {
            Sprite sprite = SpriteCache.getSprite(entity.getPath());
            PositionPart positionPart = entity.getPart(PositionPart.class);
            positionPart.setDimension(sprite.getWidth(), sprite.getHeight());
            sprite.setPosition(positionPart.getX(), positionPart.getY());

            sprite.draw(sb);
        }
        sb.end();
    }

    @Override
    public void resize(int i, int i1) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {

    }

    private Collection<? extends IGamePluginService> getPluginServices() {
        return ServiceLoader.load(IGamePluginService.class).stream().map(ServiceLoader.Provider::get).collect(toList());
    }

    private Collection<? extends IEntityProcessingService> getEntityProcessingServices() {
        return ServiceLoader.load(IEntityProcessingService.class).stream().map(ServiceLoader.Provider::get).collect(toList());
    }

    private Collection<? extends IPostEntityProcessingService> getPostEntityProcessingServices() {
        return ServiceLoader.load(IPostEntityProcessingService.class).stream().map(ServiceLoader.Provider::get).collect(toList());
    }
}
