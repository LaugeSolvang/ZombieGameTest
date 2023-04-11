package mapsystem;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import common.data.Entity;
import common.data.GameData;
import common.data.World;
import common.data.entities.obstruction.Obstruction;
import common.data.entities.weapon.WeaponSPI;
import common.data.entities.zombie.ZombieSPI;
import common.data.entityparts.PositionPart;
import common.services.IGamePluginService;

import java.util.Collection;
import java.util.ServiceLoader;

import static java.util.stream.Collectors.toList;

public class MapPlugin implements IGamePluginService {
    String[][] map;
    private Entity obstruction;
    private WeaponSPI weapon;
    @Override
    public void start(GameData gameData, World world) {
        //Create a map with strings per 32x32 pixels of the whole display
        map = new String[gameData.getDisplayHeight()/32][gameData.getDisplayWidth()/32];

        //Spawn a cube of 5x5 obstructions in the corner of the game
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                obstruction = createObstruction(i*32,j*32);
                world.addEntity(obstruction);
                map[i][j] = "obstruction";
            }
        }
        //Spawn an obstruction closer to the centre
        world.addEntity(createObstruction(gameData.getDisplayWidth()/2-64, gameData.getDisplayHeight()/2-64));

        for (WeaponSPI weapon : getWeaponSPI()) {
            world.addEntity(weapon.createWeapon(gameData.getDisplayWidth()/2+64, gameData.getDisplayHeight()/2+64));
        }

        for (ZombieSPI zombie : getZombieSPI()) {
            world.addEntity(zombie.createZombie(gameData.getDisplayWidth()/2+128, gameData.getDisplayHeight()/2+128));
        }


    }

    @Override
    public void stop(GameData gameData, World world) {

    }

    private Entity createObstruction(int x, int y) {
        Entity obstruction = new Obstruction();

        obstruction.setSprite(new Sprite(new Texture(Gdx.files.internal("Map/src/main/resources/obstruction.png"))));

        float width = obstruction.getSprite().getWidth();
        float height = obstruction.getSprite().getHeight();

        obstruction.add(new PositionPart(x, y, width, height, 0));
        obstruction.getSprite().setPosition(x,y);

        return obstruction;

    }

    protected Collection<? extends WeaponSPI> getWeaponSPI() {
        return ServiceLoader.load(WeaponSPI.class).stream().map(ServiceLoader.Provider::get).collect(toList());
    }

    protected Collection<? extends ZombieSPI> getZombieSPI() {
        return ServiceLoader.load(ZombieSPI.class).stream().map(ServiceLoader.Provider::get).collect(toList());
    }

}