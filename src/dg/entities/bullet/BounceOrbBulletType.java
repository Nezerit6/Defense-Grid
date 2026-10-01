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

public class BounceOrbBulletType extends BasicBulletType{
    public float bounceRange = 110f, bounceSlow = 0.72f;
    public int bounces = 5;
    public Color color = Color.valueOf("bfe6ff");

    public BounceOrbBulletType(float speed, float damage){
        super(speed, damage);
        pierce = true;
        pierceBuilding = false;
        collidesTiles = false;
        collideFloor = false;
        collideTerrain = false;
        pierceCap = bounces + 1;
        hitSize = 7f;
        lifetime = 240f;
        keepVelocity = false;
        hittable = false;
        trailLength = 10;
        trailWidth = 2.4f;
        trailColor = color;
        hitColor = color;
        lightColor = color;
        lightRadius = 30f;
        lightOpacity = 0.8f;
        hitEffect = DGTurretFx.bulletTurn;
        despawnEffect = DGTurretFx.orbVanish;
        status = mindustry.content.StatusEffects.shocked;
        statusDuration = 30f;
        hitSound = mindustry.gen.Sounds.spark;
    }

    @Override
    public void init(){
        pierceCap = bounces + 1;
        trailColor = hitColor = lightColor = color;
        super.init();
    }

    @Override
    public void hitEntity(Bullet b, Hitboxc entity, float health){
        super.hitEntity(b, entity, health);
        if(b.collided.size > bounces){
            b.remove();
            return;
        }

        Unit next = Units.closestEnemy(b.team, b.x, b.y, bounceRange, u -> !u.dead && u.checkTarget(collidesAir, collidesGround) && !b.collided.contains(u.id));
        if(next == null){
            b.remove();
            return;
        }

        float speed = b.vel.len() * bounceSlow;
        b.vel.set(next.x - b.x, next.y - b.y).setLength(Math.max(speed, 0.6f));
        b.time = 0f;
        b.lifetime = Math.min(lifetime, b.dst(next) / b.vel.len() * 1.6f + 20f);
        DGTurretFx.bounceArc.at(b.x, b.y, 0f, color, new float[]{next.x, next.y});
    }

    @Override
    public void draw(Bullet b){
        drawTrail(b);
        float pulse = 1f + Mathf.absin(Time.time + b.id, 3f, 0.25f), r = hitSize * 0.6f * pulse;
        Draw.z(Layer.bullet);
        Draw.blend(Blending.additive);
        Fill.light(b.x, b.y, 14, r * 2.2f, Tmp.c1.set(color).a(0.8f), Tmp.c2.set(color).a(0f));
        Draw.blend();
        Draw.color(Color.white);
        Fill.circle(b.x, b.y, r * 0.55f);
        Draw.color(color);
        Lines.stroke(1f);
        for(int i = 0; i < 3; i++){
            float a = Time.time * 6f + b.id * 40f + i * 120f;
            Lines.lineAngle(b.x + Angles.trnsx(a, r * 0.6f), b.y + Angles.trnsy(a, r * 0.6f), a + Mathf.range(30f), r * 0.9f);
        }
        Draw.reset();
    }
}
