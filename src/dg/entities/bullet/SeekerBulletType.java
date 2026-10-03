package dg.entities.bullet;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import dg.content.DGTurretFx;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.blocks.defense.turrets.Turret.*;

public class SeekerBulletType extends BasicBulletType{
    public float seekDelay = 60f, seekSpeed = 7f, seekRange = 260f, hoverDrag = 0.06f;
    public Color color = Color.valueOf("ffb3e6");

    public SeekerBulletType(float speed, float damage){
        super(speed, damage);
        lifetime = 130f;
        keepVelocity = false;
        trailLength = 9;
        trailWidth = 1.8f;
        width = 7f;
        height = 10f;
        shrinkY = 0f;
        hitEffect = DGTurretFx.bulletPop;
        despawnEffect = DGTurretFx.bulletPop;
    }

    @Override
    public void init(){
        trailColor = backColor = hitColor = lightColor = color;
        frontColor = Color.white;
        super.init();
    }

    @Override
    public void update(Bullet b){
        super.update(b);
        if(b.fdata == 0f){
            b.vel.scl(Math.max(1f - hoverDrag * Time.delta, 0f));
            if(b.time >= seekDelay){
                b.fdata = 1f;
                Posc target = b.owner instanceof TurretBuild && ((TurretBuild)b.owner).target != null && ((TurretBuild)b.owner).target.within(b, seekRange) ? ((TurretBuild)b.owner).target : null;
                if(target == null) target = Units.closestTarget(b.team, b.x, b.y, seekRange, u -> u.checkTarget(collidesAir, collidesGround));
                float ang = target == null ? b.rotation() : b.angleTo(target);
                b.vel.trns(ang, seekSpeed);
                b.time = 0f;
                b.lifetime = seekRange / seekSpeed + 10f;
                DGTurretFx.seekerLock.at(b.x, b.y, ang, color);
            }
        }
    }

    @Override
    public void draw(Bullet b){
        if(b.fdata == 0f){
            float f = Mathf.clamp(b.time / seekDelay);
            Draw.z(Layer.bullet);
            Draw.blend(Blending.additive);
            Fill.light(b.x, b.y, 12, 4f + 3f * f, Tmp.c1.set(Color.white).a(0.5f + 0.4f * f), Tmp.c2.set(color).a(0f));
            Draw.blend();
            Lines.stroke(0.8f, Tmp.c1.set(color).a(f));
            Lines.circle(b.x, b.y, 7f * (1f - f) + 3f);
            Draw.reset();
        }
        super.draw(b);
    }
}
