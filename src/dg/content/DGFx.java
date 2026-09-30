package dg.content;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.Tmp;
import dg.graphics.*;
import dg.world.blocks.SmokeTestBlock;
import mindustry.entities.*;
import mindustry.entities.Effect.EffectContainer;
import mindustry.entities.effect.*;
import mindustry.graphics.*;

import static arc.graphics.g2d.Draw.*;
import static arc.graphics.g2d.Lines.*;
import static arc.math.Angles.*;

public class DGFx{
    private static final Rand rand = new Rand();
    private static final float gravity = 0.12f;
    private static float flyDst, flyZ;

    public static final Color
        smokeColor = Color.valueOf("6e7080"),
        dirt = Color.valueOf("5d5e68");

    public static final Effect

    customSmoke = new Effect(SmokeTestBlock.maxLifetime, 200f, e -> {
        if(e.data instanceof SmokeStyle) ((SmokeStyle)e.data).draw(e.x, e.y, e.rotation, e.time, e.id);
    }).layer(Layer.bullet - 1f),

    oreDepleted = new MultiEffect(new Effect(70f, 80f, e -> {
        e.scaled(20f, s -> {
            color(e.color, dirt, s.fin());
            stroke(1.6f * s.fout());
            Lines.square(e.x, e.y, 4f + 5f * s.finpow(), 45f * s.fin());
        });
        shards(e, 7, 0.3f, 0.6f, 1f, 0.9f, 1f, 4, Tmp.c3.set(e.color).lerp(Color.white, 0.3f), e.color, Tmp.c4.set(e.color).mul(0.5f), 360f, 0.3f);
    }), risingSmoke(6, 7f, 9f, 2.6f, Color.valueOf("8b8c95"), dirt, 70f, 180f, 0.4f));

    public static Effect risingSmoke(int count, float spread, float height, float size, Color from, Color to, float lifetime){
        return risingSmoke(count, spread, height, size, from, to, lifetime, 70f, 0f);
    }

    public static Effect risingSmoke(int count, float spread, float height, float size, Color from, Color to, float lifetime, float cone, float lifeRand){
        SmokeStyle style = new SmokeStyle(count, spread, height, size, lifetime, from, to);
        style.cone = cone;
        style.lifeRand = lifeRand;
        return new Effect(lifetime, 150f, e -> style.draw(e.x, e.y, e.rotation, e.time, e.id)).layer(Layer.bullet - 1f);
    }

    static void shards(EffectContainer e, int count, float hsMin, float hsRand, float vzMin, float vzRand, float size, int sides,
                       Color light, Color base, Color dark, float cone, float bounce){
        rand.setSeed(e.id + 101);
        for(int i = 0; i < count; i++){
            float ang = e.rotation + rand.range(cone / 2f), hs = hsMin + rand.random(hsRand), vz = vzMin + rand.random(vzRand);
            float s = size * rand.random(0.6f, 1.3f), spin = rand.range(14f), rot0 = rand.random(360f);
            float stretch = sides == 3 ? rand.random(1.2f, 1.9f) : 1f;

            float airTime = fly(e.time, hs, vz, bounce);
            float x = e.x + trnsx(ang, flyDst), y = e.y + trnsy(ang, flyDst), z = flyZ;
            float r = rot0 + spin * airTime, a = Mathf.clamp(e.fout() * 3f), sc = DGDraw3D.scale(z);

            z(Layer.groundUnit - 1f);
            color(Pal.shadow, Pal.shadow.a * a);
            Fill.poly(x - DGDraw3D.shadowOffset(z), y - DGDraw3D.shadowOffset(z), sides, s, r);

            z(Layer.flyingUnitLow - 1f);
            float px = DGDraw3D.x(x, z), py = DGDraw3D.y(y, z), rad = s * sc;
            color(dark, a);
            Fill.poly(px, py, sides, rad, r);
            if(stretch > 1f) Drawf.tri(px, py, rad * 1.2f, rad * stretch * 1.6f, r);
            color(Tmp.c1.set(base).lerp(light, 0.3f + 0.5f * Mathf.absin(r, 12f, 1f)), a);
            Fill.poly(px + rad * 0.2f, py + rad * 0.2f, sides, rad * 0.65f, r);
        }
        color();
    }

    static float fly(float t, float hs, float vz, float bounce){
        float t1 = 2f * vz / gravity;
        if(t < t1){
            flyDst = hs * t;
            flyZ = vz * t - gravity * t * t / 2f;
            return t;
        }

        float vz2 = vz * bounce, hs2 = hs * 0.5f, t2 = 2f * vz2 / gravity, tb = t - t1;
        if(tb < t2){
            flyDst = hs * t1 + hs2 * tb;
            flyZ = vz2 * tb - gravity * tb * tb / 2f;
            return t;
        }

        float ts = Math.min(tb - t2, 12f);
        flyDst = hs * t1 + hs2 * t2 + hs2 * (ts - ts * ts / 24f);
        flyZ = 0f;
        return t1 + t2;
    }
}
