package bulletsystem;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import common.data.Entity;
import common.data.GameData;
import common.data.World;
import common.data.entities.bullet.Bullet;
import common.data.entities.bullet.BulletSPI;
import common.data.entityparts.LifePart;
import common.data.entityparts.MovingPart;
import common.data.entityparts.PositionPart;
import common.data.entityparts.TimerPart;
import common.services.IEntityProcessingService;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class BulletProcessor implements IEntityProcessingService, BulletSPI {
    @Override
    public void process(GameData gameData, World world) {
        for (Entity bullet : world.getEntities(Bullet.class)) {
            PositionPart positionPart = bullet.getPart(PositionPart.class);
            MovingPart movingPart = bullet.getPart(MovingPart.class);
            TimerPart timerPart = bullet.getPart(TimerPart.class);

            if (timerPart.getExpiration() < 0) {
                world.removeEntity(bullet);
            }

            movingPart.process(gameData, bullet);
            positionPart.process(gameData, bullet);
            timerPart.process(gameData, bullet);
        }
    }

    @Override
    public Entity createBullet(Entity weapon, GameData gameData) {
        Entity bullet = new Bullet();
        bullet.setSprite(new Sprite(new Texture(Gdx.files.internal("Bullet/src/main/resources/bullet.png"))));

        PositionPart weaponPos = weapon.getPart(PositionPart.class);

        float x = weaponPos.getX();
        float y = weaponPos.getY();
        float radians = weaponPos.getRadians();
        float speed = 350;
        float width = bullet.getSprite().getWidth();
        float height = bullet.getSprite().getHeight();

        bullet.setRadius(2);

        float bx = (float) cos(radians) * weapon.getRadius() * bullet.getRadius();
        float by = (float) sin(radians) * weapon.getRadius() * bullet.getRadius();

        bullet.add(new PositionPart(bx + x, by + y, width, height, radians));
        bullet.add(new LifePart(1));
        bullet.add(new MovingPart(0, 5000000, speed, 5));
        bullet.add(new TimerPart(1));


        MovingPart movingPart = bullet.getPart(MovingPart.class);
        if (radians == 0) {
            movingPart.setRight(true);
        } else if (radians == 3.14f) {
            movingPart.setLeft(true);
        } else if (radians == 3.14f/2) {
            movingPart.setUp(true);
        } else if (radians == 3*3.14f/2) {
            movingPart.setDown(true);
        }

        return bullet;
    }
}
