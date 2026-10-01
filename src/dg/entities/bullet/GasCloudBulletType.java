package dg.entities.bullet;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import dg.graphics.SmokeStyle;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;

public class GasCloudBulletType extends BulletType{
    private static final Rand rand = new Rand();
    public float radius = 18f, growth = 1.4f, dps = 6f;
    public Color color = Color.valueOf("9ad15b"), dark = Color.valueOf("4f6b38");
    public StatusEffect gas = StatusEffects.none;
    public float gasDuration = 90f;
    public boolean ignites = false;

    public GasCloudBulletType(float speed){
        super(speed, 0f);
        drag = 0.035f;
        lifetime = 360f;
        collides = false;
        collidesTiles = false;
        collidesAir = true;
        collidesGround = true;
        hittable = false;
        reflectable = false;
        absorbable = false;
        keepVelocity = false;
        despawnEffect = Fx.none;
        hitEffect = Fx.none;
        shootEffect = Fx.none;
        smokeEffect = Fx.none;
        layer = Layer.flyingUnit + 0.6f;
        status = StatusEffects.none;
    }

    public float radius(Bullet b){
        return radius * (0.35f + growth * Interp.pow2Out.apply(Mathf.clamp(b.time / (lifetime * 0.6f))));
    }

    @Override
    public void update(Bullet b){
        super.update(b);
        if(b.timer(1, 10f)){
            float r = radius(b) * 0.85f;
            Units.nearbyEnemies(b.team, b.x, b.y, r, u -> {
                if(u.dead || !u.within(b, r + u.hitSize / 2f)) return;
                if(gas != StatusEffects.none) u.apply(gas, gasDuration);
                if(dps > 0f) u.damagePierce(dps / 6f);
            });
            if(ignites && Mathf.chance(0.25f)){
                Tmp.v1.rnd(Mathf.random(r));
                Fires.create(mindustry.Vars.world.tileWorld(b.x + Tmp.v1.x, b.y + Tmp.v1.y));
            }
        }
    }

    @Override
    public void draw(Bullet b){
        float r = radius(b), a = Mathf.clamp(b.time / 15f) * Mathf.clamp((b.lifetime - b.time) / 90f);
        TextureRegion puff = SmokeStyle.puff();
        rand.setSeed(b.id);
        Draw.z(layer);
        for(int i = 0; i < 9; i++){
            float ang = rand.random(360f) + b.time * rand.range(0.4f), d = r * rand.random(0.15f, 0.7f);
            float s = r * rand.random(0.55f, 0.9f), rot = rand.random(360f) + b.time * rand.range(0.6f);
            Draw.color(Tmp.c1.set(color).lerp(dark, rand.random(0.6f)).a(0.12f * a));
            float px = b.x + Angles.trnsx(ang, d), py = b.y + Angles.trnsy(ang, d);
            if(puff != null) Draw.rect(puff, px, py, s * 2f, s * 2f, rot);
            else Fill.circle(px, py, s);
        }
        Draw.reset();
    }

    @Override
    public void drawLight(Bullet b){
    }
}
