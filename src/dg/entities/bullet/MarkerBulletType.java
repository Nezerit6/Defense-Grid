package dg.entities.bullet;

import dg.content.DGFx;
import mindustry.entities.bullet.*;
import mindustry.gen.*;

public class MarkerBulletType extends BasicBulletType{
    public BulletType strike;

    public MarkerBulletType(float speed, float damage, BulletType strike){
        super(speed, damage);
        this.strike = strike;
        scaleLife = true;
        collidesTiles = false;
        despawnEffect = mindustry.content.Fx.none;
    }

    @Override
    public void hitEntity(Bullet b, Hitboxc entity, float health){
        super.hitEntity(b, entity, health);
        call(b, b.x, b.y, entity instanceof Unit ? entity : null);
    }

    @Override
    public void despawned(Bullet b){
        super.despawned(b);
        call(b, b.x, b.y, null);
    }

    void call(Bullet b, float x, float y, Object target){
        DGFx.markerPing.at(x, y);
        strike.create(b.owner, b.team, x, y, 0f, -1f, 1f, 1f, target);
    }
}
