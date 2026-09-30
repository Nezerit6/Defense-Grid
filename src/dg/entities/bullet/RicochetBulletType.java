package dg.entities.bullet;

import arc.struct.IntSeq;
import dg.content.DGFx;
import mindustry.entities.Units;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.gen.*;

public class RicochetBulletType extends BasicBulletType{
    public int bounces = 6;
    public float bounceRange = 90f, damageScale = 1.15f, speedScale = 1.08f;

    public RicochetBulletType(float speed, float damage){
        super(speed, damage);
        pierce = false;
    }

    @Override
    public void hitEntity(Bullet b, Hitboxc entity, float health){
        super.hitEntity(b, entity, health);

        IntSeq hits = b.data instanceof IntSeq ? (IntSeq)b.data : new IntSeq();
        hits.add(entity.id());
        if(hits.size > bounces) return;

        Unit next = Units.closestEnemy(b.team, b.x, b.y, bounceRange, u -> u.checkTarget(collidesAir, collidesGround) && !hits.contains(u.id));
        if(next == null) return;

        float angle = b.angleTo(next);
        DGFx.ricochet.at(b.x, b.y, angle, hitColor, hits.size);
        create(b.owner, b.team, b.x, b.y, angle, b.damage * damageScale, (float)Math.pow(speedScale, hits.size), 1f, hits);
    }
}
