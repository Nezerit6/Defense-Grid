package dg.entities.bullet;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import dg.content.DGFx;
import dg.graphics.DGDraw3D;
import mindustry.entities.Units;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.gen.*;
import mindustry.graphics.*;

public class GravityWellBulletType extends BasicBulletType{
    public float pullRadius = 72f, pullForce = 0.55f, tickDamage = 4f, pushForce = 5f, hover = 6f;
    public Color color = DGFx.voidColor, darkColor = DGFx.voidDark;

    private static final Rand rand = new Rand();

    public GravityWellBulletType(float speed, float damage){
        super(speed, damage);
        collides = false;
        collidesTiles = false;
        hittable = false;
        absorbable = false;
        reflectable = false;
        despawnHit = true;
        drag = 0.035f;
        keepVelocity = false;
        hitEffect = DGFx.singularityCollapse;
        despawnEffect = mindustry.content.Fx.none;
    }

    public float strength(Bullet b){
        return Interp.pow2Out.apply(Mathf.clamp(b.time / 30f)) * Mathf.clamp((b.lifetime - b.time) / 10f);
    }

    @Override
    public void update(Bullet b){
        super.update(b);

        float st = strength(b);
        boolean tick = b.timer(1, 10f);
        Units.nearbyEnemies(b.team, b.x, b.y, pullRadius, u -> {
            if(!u.checkTarget(collidesAir, collidesGround)) return;
            float dst = u.dst(b), fall = 1f - dst / pullRadius;
            if(dst > 2f){
                Tmp.v3.set(b.x - u.x, b.y - u.y).setLength(pullForce * fall * st * Time.delta / Math.max(1f, u.hitSize() / 10f));
                u.vel.add(Tmp.v3);
            }
            if(tick) u.damage(tickDamage * st);
        });
    }

    @Override
    public void hit(Bullet b, float x, float y){
        super.hit(b, x, y);
        Units.nearbyEnemies(b.team, x, y, pullRadius * 0.8f, u -> {
            if(!u.checkTarget(collidesAir, collidesGround)) return;
            Tmp.v3.set(u.x - x, u.y - y).setLength(pushForce * (1f - u.dst(x, y) / pullRadius));
            u.impulse(Tmp.v3.scl(u.mass()));
        });
    }

    @Override
    public void draw(Bullet b){
        float st = strength(b), z = hover * Interp.pow2Out.apply(Mathf.clamp(b.time / 20f));
        float px = DGDraw3D.x(b.x, z), py = DGDraw3D.y(b.y, z), s = DGDraw3D.scale(z);

        Draw.z(Layer.groundUnit - 1f);
        Draw.color(0f, 0f, 0f, 0.35f * st);
        Fill.circle(b.x - DGDraw3D.shadowOffset(z), b.y - DGDraw3D.shadowOffset(z), 6f * st);
        Draw.color(color, 0.15f * st);
        Lines.stroke(1f);
        Lines.circle(b.x, b.y, pullRadius * (0.96f + Mathf.absin(4f, 0.04f)));

        rand.setSeed(b.id);
        float tilt = 58f, sinT = Mathf.sinDeg(tilt), cosT = Mathf.cosDeg(tilt);
        for(int pass = 0; pass < 2; pass++){
            for(int i = 0; i < 30; i++){
                float phase = (rand.random(1f) + b.time * (0.008f + rand.random(0.006f))) % 1f;
                float r = (pullRadius * 0.55f) * (1f - Interp.pow2In.apply(phase)) + 3f;
                float a = rand.random(360f) + phase * 540f;
                float sa = Mathf.sinDeg(a), depth = sa * sinT;
                if((depth > 0f) != (pass == 1)) continue;
                float wz = z + depth * r * 0.35f;
                float wx = b.x + Mathf.cosDeg(a) * r, wy = b.y + sa * r * cosT;
                float ps = DGDraw3D.scale(wz);
                Draw.z(Layer.bullet + (pass == 1 ? 0.02f : -0.02f));
                Draw.color(Tmp.c1.set(darkColor).lerp(color, phase).lerp(Color.white, phase * phase * 0.6f), st * Mathf.slope(phase) * 1.4f);
                Fill.circle(DGDraw3D.x(wx, wz), DGDraw3D.y(wy, wz), (0.5f + phase * 1.1f) * ps);
            }
            if(pass == 0){
                Draw.z(Layer.bullet);
                Draw.color(color, 0.8f * st);
                Lines.stroke(1.4f * s * st);
                Lines.circle(px, py, (5.5f + Mathf.absin(3f, 0.8f)) * s * st);
                Draw.color(Color.black);
                Fill.circle(px, py, 4.2f * s * st);
                Draw.color(darkColor);
                Fill.circle(px, py, 2.4f * s * st);
            }
        }

        Draw.reset();
        Drawf.light(px, py, 50f * st, color, 0.8f);
    }
}
