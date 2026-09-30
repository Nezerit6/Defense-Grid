package dg.entities.bullet;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import dg.content.DGFx;
import dg.graphics.DGDraw3D;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.gen.*;
import mindustry.graphics.*;

public class StrikeBulletType extends BasicBulletType{
    public float fallHeight = 320f, fallStart = 0.35f, trackUntil = 0.8f, reticle = 22f;
    public Color color = DGFx.strikeColor, shellColor = Color.valueOf("ffe0b0");

    public StrikeBulletType(float damage, float radius){
        super(0f, 0f);
        splashDamage = damage;
        splashDamageRadius = radius;
        speed = 0f;
        lifetime = 130f;
        collides = false;
        collidesTiles = false;
        hittable = false;
        absorbable = false;
        reflectable = false;
        keepVelocity = false;
        despawnHit = true;
        hitShake = 6f;
        hitSound = mindustry.gen.Sounds.largeExplosion;
        hitEffect = DGFx.strikeBoom;
        despawnEffect = mindustry.content.Fx.none;
    }

    @Override
    public void update(Bullet b){
        super.update(b);
        if(b.data instanceof Unit && b.fin() < trackUntil){
            Unit u = (Unit)b.data;
            if(u.isValid()) b.set(u.x, u.y);
        }
        if(b.timer(1, Mathf.lerp(30f, 6f, b.fin()))){
            DGFx.markerPing.at(b.x, b.y);
        }
    }

    public float z(Bullet b){
        float t = Mathf.clamp((b.fin() - fallStart) / (1f - fallStart));
        return fallHeight * (1f - t * t);
    }

    @Override
    public void draw(Bullet b){
        float f = b.fin(), close = Mathf.clamp((f - fallStart) / (1f - fallStart));
        float rad = Mathf.lerp(reticle, reticle * 0.45f, Interp.pow2Out.apply(f));
        float rot = Time.time * 1.5f + b.id * 13f;

        Draw.z(Layer.groundUnit - 0.5f);
        Draw.color(color, 0.8f);
        Lines.stroke(1.2f);
        Lines.circle(b.x, b.y, rad);
        for(int i = 0; i < 3; i++){
            float a = rot + i * 120f;
            Drawf.tri(b.x + Angles.trnsx(a, rad + 3f), b.y + Angles.trnsy(a, rad + 3f), 3f, 4f, a + 180f);
        }
        Draw.color(color, 0.25f + 0.25f * Mathf.absin(4f - close * 3f, 1f));
        Fill.circle(b.x, b.y, rad * 0.35f);

        if(close > 0f){
            float z = z(b), px = DGDraw3D.x(b.x, z), py = DGDraw3D.y(b.y, z), s = DGDraw3D.scale(z);

            Draw.color(0f, 0f, 0f, 0.5f * close);
            Fill.circle(b.x - DGDraw3D.shadowOffset(z) * 0.3f, b.y - DGDraw3D.shadowOffset(z) * 0.3f, 3f + 10f * (1f - close));

            float z2 = z + 45f;
            Draw.z(Layer.effect);
            Draw.color(color, 0.25f * close);
            Lines.stroke(3f * s);
            Lines.line(DGDraw3D.x(b.x, z2), DGDraw3D.y(b.y, z2), px, py);
            Draw.color(Color.white, 0.6f * close);
            Lines.stroke(1.2f * s);
            Lines.line(DGDraw3D.x(b.x, z + 20f), DGDraw3D.y(b.y, z + 20f), px, py);

            Draw.color(shellColor);
            Fill.circle(px, py, 2.6f * s);
            Draw.color(Color.white);
            Fill.circle(px, py, 1.4f * s);
            Drawf.light(px, py, 30f * s, color, 0.8f);
        }

        Draw.reset();
    }
}
