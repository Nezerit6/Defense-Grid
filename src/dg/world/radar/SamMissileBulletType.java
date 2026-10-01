package dg.world.radar;

import arc.math.*;
import arc.util.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.world.blocks.defense.turrets.Turret.*;

public class SamMissileBulletType extends MissileBulletType{
    public float turnRate = 4.5f, boostTime = 14f, retarget = 300f;

    public SamMissileBulletType(float speed, float damage){
        super(speed, damage);
        collidesGround = false;
        collidesTiles = false;
        homingPower = 0f;
        keepVelocity = false;
        lifetime = 300f;
        despawnHit = true;
    }

    @Override
    public void init(Bullet b){
        super.init(b);
        if(b.owner instanceof TurretBuild && ((TurretBuild)b.owner).target instanceof Unit) b.data = ((TurretBuild)b.owner).target;
    }

    @Override
    public void update(Bullet b){
        super.update(b);
        if(b.time < boostTime) return;

        Unit target = b.data instanceof Unit ? (Unit)b.data : null;
        if(target == null || target.dead || !target.isValid()){
            target = RadarNet.closest(b.team, b.x, b.y, retarget);
            b.data = target;
            if(target == null) return;
        }

        float turn = turnRate * Mathf.clamp((b.time - boostTime) / 30f + 0.3f);
        b.vel.setAngle(Angles.moveToward(b.rotation(), b.angleTo(target), turn * Time.delta));
    }
}
