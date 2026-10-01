package dg.entities.bullet;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import dg.content.DGTurretFx;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;

public class MineBulletType extends BasicBulletType{
    public float armTime = 40f, size = 4f;
    public Color light = Pal.remove;

    public MineBulletType(float splashDamage, float radius){
        super(2.6f, 0f);
        this.splashDamage = splashDamage;
        splashDamageRadius = radius;
        drag = 0.07f;
        lifetime = 60f * 40f;
        collidesAir = false;
        collidesTiles = false;
        collideFloor = false;
        collideTerrain = false;
        hittable = false;
        reflectable = false;
        absorbable = false;
        hitSize = 9f;
        keepVelocity = false;
        layer = Layer.floor + 0.2f;
        hitEffect = DGTurretFx.mineBoom;
        despawnEffect = DGTurretFx.bulletPop;
        hitSound = mindustry.gen.Sounds.explosion;
        hitShake = 3f;
        status = mindustry.content.StatusEffects.blasted;
    }

    @Override
    public void update(Bullet b){
        super.update(b);
        if(b.fdata == 0f && b.time > armTime){
            b.fdata = 1f;
            DGTurretFx.mineArm.at(b.x, b.y);
        }
    }

    @Override
    public void draw(Bullet b){
        float fade = Mathf.clamp((b.lifetime - b.time) / 60f);
        Draw.z(Layer.floor + 0.2f);
        Draw.color(Pal.shadow, 0.4f * fade);
        Fill.circle(b.x - 1f, b.y - 1f, size + 0.5f);
        Draw.color(Tmp.c1.set(Color.valueOf("5a5c66")).a(fade));
        Fill.poly(b.x, b.y, 6, size, b.id * 31f);
        Draw.color(Tmp.c1.set(Color.valueOf("8a8c96")).a(fade));
        Fill.poly(b.x, b.y, 6, size * 0.6f, b.id * 31f);
        if(b.fdata > 0f){
            float blink = Mathf.absin(Time.time + b.id * 13f, 7f, 1f);
            Draw.color(Tmp.c1.set(light).a(fade * (0.4f + 0.6f * blink)));
            Fill.circle(b.x, b.y, 1.1f);
            Drawf.light(b.x, b.y, 10f, light, 0.5f * blink * fade);
        }
        Draw.reset();
    }
}
