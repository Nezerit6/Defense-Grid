package dg.content;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.Tmp;
import dg.graphics.*;
import dg.world.blocks.SmokeTestBlock;
import mindustry.content.Fx;
import mindustry.entities.*;
import mindustry.entities.Effect.EffectContainer;
import mindustry.entities.effect.*;
import mindustry.graphics.*;

import static arc.graphics.g2d.Draw.*;
import static arc.graphics.g2d.Lines.*;
import static arc.math.Angles.*;

public class DGFx{
    private static final Rand rand = new Rand();
    private static final float[] points = new float[512];
    private static final float gravity = 0.12f;
    private static float flyDst, flyZ;

    public static final Color
        cryo = Color.valueOf("afeeee"),
        cryoDark = Color.valueOf("6974c4"),
        pulseColor = Color.valueOf("f9a3c7"),
        pulseDark = Color.valueOf("cb8ebf"),
        voidColor = Color.valueOf("a86bff"),
        voidDark = Color.valueOf("3a1d5c"),
        strikeColor = Color.valueOf("ff5845"),
        gold = Color.valueOf("ffd37f"),
        goldDark = Color.valueOf("e8a04c"),
        smokeColor = Color.valueOf("6e7080"),
        dirt = Color.valueOf("5d5e68"),
        metal = Color.valueOf("989aa4"),
        brass = Color.valueOf("e8c07a");

    public static final Effect

    customSmoke = new Effect(SmokeTestBlock.maxLifetime, 200f, e -> {
        if(e.data instanceof SmokeStyle) ((SmokeStyle)e.data).draw(e.x, e.y, e.rotation, e.time, e.id);
    }).layer(Layer.bullet - 1f),

    pulseShoot = new Effect(12f, 40f, e -> {
        color(Color.white, pulseColor, e.fin());
        for(int i : Mathf.signs){
            Drawf.tri(e.x, e.y, 2.4f * e.fout(), 7f, e.rotation + 55f * i);
        }
        Drawf.tri(e.x, e.y, 3f * e.fout(), 11f, e.rotation);
        stroke(1.2f * e.fout());
        Lines.circle(e.x, e.y, 1f + 5f * e.finpow());
        Drawf.light(e.x, e.y, 16f * e.fout(), pulseColor, 0.7f);
    }),

    ricochet = new Effect(22f, 60f, e -> {
        int bounce = e.data instanceof Integer ? (Integer)e.data : 1;
        float power = 1f + bounce * 0.18f;
        e.scaled(10f, s -> {
            color(Color.white, pulseColor, s.fin());
            stroke(1.6f * s.fout() * power);
            Lines.circle(e.x, e.y, (2f + 7f * s.finpow()) * power);
            Drawf.tri(e.x, e.y, 3f * s.fout() * power, 9f * power, e.rotation);
            Drawf.tri(e.x, e.y, 3f * s.fout() * power, 4f * power, e.rotation + 180f);
        });
        rand.setSeed(e.id);
        for(int i = 0; i < 3 + bounce; i++){
            float ang = rand.random(360f), hs = 0.6f + rand.random(1f), vz = 0.6f + rand.random(1.1f);
            fly(e.time, hs, vz, 0f);
            float x1 = DGDraw3D.x(e.x + trnsx(ang, flyDst), flyZ), y1 = DGDraw3D.y(e.y + trnsy(ang, flyDst), flyZ);
            fly(Math.max(e.time - 2.5f, 0f), hs, vz, 0f);
            float x2 = DGDraw3D.x(e.x + trnsx(ang, flyDst), flyZ), y2 = DGDraw3D.y(e.y + trnsy(ang, flyDst), flyZ);
            color(Color.white, pulseColor, e.fin());
            stroke(1f * e.fout());
            line(x2, y2, x1, y1);
        }
        Drawf.light(e.x, e.y, 20f * power * e.fout(), pulseColor, 0.7f);
    }),

    glaiveThrow = new Effect(16f, 60f, e -> {
        color(Color.white, metal, e.fin());
        stroke(1.4f * e.fout());
        for(int i : Mathf.signs){
            Lines.arc(e.x, e.y, 4f + 6f * e.finpow(), 0.3f, e.rotation - 54f + 90f * i * 0.3f);
        }
        randLenVectors(e.id, 5, 2f + 10f * e.finpow(), e.rotation, 30f, (x, y) -> lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), 1f + 3f * e.fout()));
    }),

    glaiveHit = new Effect(16f, 40f, e -> {
        rand.setSeed(e.id);
        for(int i = 0; i < 5; i++){
            float ang = e.rotation + rand.range(70f), hs = 0.8f + rand.random(1.4f), vz = 0.4f + rand.random(1f);
            fly(e.time, hs, vz, 0f);
            float x1 = DGDraw3D.x(e.x + trnsx(ang, flyDst), flyZ), y1 = DGDraw3D.y(e.y + trnsy(ang, flyDst), flyZ);
            fly(Math.max(e.time - 2f, 0f), hs, vz, 0f);
            float x2 = DGDraw3D.x(e.x + trnsx(ang, flyDst), flyZ), y2 = DGDraw3D.y(e.y + trnsy(ang, flyDst), flyZ);
            color(Color.white, Pal.lightOrange, e.fin());
            stroke(0.9f * e.fout());
            line(x2, y2, x1, y1);
        }
    }),

    glaiveCatch = new Effect(20f, 50f, e -> {
        color(Color.white, e.color, e.fin());
        stroke(2f * e.fout());
        Lines.circle(e.x, e.y, 3f + 9f * e.finpow());
        for(int i = 0; i < 4; i++){
            Drawf.tri(e.x, e.y, 2.2f * e.fout(), 8f * e.fout(), i * 90f + 45f + e.fin() * 60f);
        }
        Drawf.light(e.x, e.y, 22f * e.fout(), e.color, 0.6f);
    }),

    singularityCharge = new Effect(50f, 80f, e -> {
        rand.setSeed(e.id);
        for(int i = 0; i < 14; i++){
            float start = rand.random(0.4f), f = Mathf.clamp((e.fin() - start) / (1f - start));
            if(f <= 0f) continue;
            float ang = rand.random(360f) + f * 200f, len = (1f - Interp.pow2In.apply(f)) * rand.random(14f, 26f);
            float z = 3f + rand.range(4f) * (1f - f);
            color(voidColor, Color.white, f);
            Fill.circle(DGDraw3D.x(e.x + trnsx(ang, len), z), DGDraw3D.y(e.y + trnsy(ang, len), z), 1.1f * Mathf.slope(f) * DGDraw3D.scale(z));
        }
        color(voidDark, 0.8f * e.fin());
        Fill.circle(e.x, e.y, 3f * e.fin());
        Drawf.light(e.x, e.y, 30f * e.fin(), voidColor, 0.7f);
    }),

    singularityCollapse = new MultiEffect(new Effect(50f, 160f, e -> {
        e.scaled(12f, s -> {
            color(Color.white, voidColor, s.fin());
            stroke(3f * s.fin());
            Lines.circle(e.x, e.y, 40f * (1f - s.finpow()));
        });
        e.scaled(34f, s -> {
            float f = Mathf.clamp((s.fin() - 0.3f) / 0.7f);
            if(f <= 0f) return;
            color(voidColor, voidDark, f);
            stroke(4f * (1f - f));
            Lines.circle(e.x, e.y, 4f + 52f * Interp.pow3Out.apply(f));
            Draw.blend(Blending.additive);
            Fill.light(e.x, e.y, 20, 16f * (1f - f), Tmp.c1.set(Color.white).lerp(voidColor, f).a(0.9f * (1f - f)), Tmp.c2.set(voidColor).a(0f));
            Draw.blend();
        });
        shards(e, 12, 0.5f, 1.2f, 1.2f, 1.3f, 1.2f, 4, Color.valueOf("c9a8ff"), voidColor, voidDark, 360f, 0.3f);
        embers(e, 10, 0.8f, 1.2f, 1.2f, 1.5f, voidColor);
        Drawf.light(e.x, e.y, 80f * e.fout(), voidColor, 0.9f);
    }), risingSmoke(10, 14f, 18f, 3.6f, Color.valueOf("7a5aa8"), voidDark, 110f, 180f, 0.4f)),

    markerShoot = new Effect(10f, 40f, e -> {
        color(Color.white, strikeColor, e.fin());
        Drawf.tri(e.x, e.y, 2.4f * e.fout(), 10f, e.rotation);
        Drawf.tri(e.x, e.y, 2.4f * e.fout(), 3f, e.rotation + 180f);
        Drawf.light(e.x, e.y, 12f * e.fout(), strikeColor, 0.6f);
    }),

    markerPing = new Effect(30f, 60f, e -> {
        color(strikeColor);
        for(int i = 0; i < 2; i++){
            float f = Mathf.clamp(e.fin() * 1.5f - i * 0.4f);
            stroke(1.5f * (1f - f));
            Lines.circle(e.x, e.y, 2f + 14f * f);
        }
    }),

    strikeBoom = new MultiEffect(
        burst(Pal.missileYellowBack, dirt, 48f, 20, 16, 2f),
        fireTongues(Pal.missileYellow, strikeColor, 48f),
        risingSmoke(16, 18f, 38f, 5.5f, Color.valueOf("8b8c95"), Color.valueOf("3d3e46"), 170f, 180f, 0.45f)
    ),

    strikeBoomFire = new MultiEffect(
        burst(Pal.lightOrange, Color.valueOf("4d4e58"), 44f, 14, 22, 1.8f),
        fireTongues(Pal.lightishOrange, Pal.lightOrange, 52f),
        fireTongues(Pal.lightOrange, strikeColor, 36f),
        risingSmoke(16, 18f, 34f, 5f, Color.valueOf("6e7080"), Color.valueOf("2c2d38"), 170f, 180f, 0.45f)
    ),

    capacitorStack = new Effect(18f, 40f, e -> {
        color(Color.white, gold, e.fin());
        stroke(1.2f * e.fout());
        Lines.circle(e.x, e.y, 1f + 5f * e.finpow());
        Drawf.light(e.x, e.y, 12f * e.fout(), gold, 0.6f);
    }),

    capacitorShoot = new Effect(14f, 40f, e -> {
        color(Color.white, gold, e.fin());
        stroke(1.4f * e.fout());
        Lines.circle(e.x, e.y, 1f + 6f * e.finpow());
        randLenVectors(e.id, 4, 1f + 8f * e.finpow(), e.rotation, 40f, (x, y) -> lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), 1f + 2.5f * e.fout()));
        Drawf.light(e.x, e.y, 16f * e.fout(), gold, 0.7f);
    }),

    capacitorHit = new MultiEffect(
        burst(gold, dirt, 12f, 3, 6, 0.6f),
        risingSmoke(4, 5f, 7f, 1.8f, Color.valueOf("fff0c8"), goldDark, 40f, 180f, 0.3f)
    );

    public static Effect risingSmoke(int count, float spread, float height, float size, Color from, Color to, float lifetime){
        return risingSmoke(count, spread, height, size, from, to, lifetime, 70f, 0f);
    }

    public static Effect risingSmoke(int count, float spread, float height, float size, Color from, Color to, float lifetime, float cone, float lifeRand){
        SmokeStyle style = new SmokeStyle(count, spread, height, size, lifetime, from, to);
        style.cone = cone;
        style.lifeRand = lifeRand;
        return new Effect(lifetime, 150f, e -> style.draw(e.x, e.y, e.rotation, e.time, e.id)).layer(Layer.bullet - 1f);
    }

    public static Effect burst(Color glow, Color debris, float radius, int chunks, int embers, float power){
        return new Effect(70f, radius * 4f, e -> {
            e.scaled(7f, s -> {
                color(Color.white, s.fout());
                Fill.circle(e.x, e.y, radius * 0.45f * s.fout());
            });

            e.scaled(28f, s -> {
                float z = 5f * power * s.finpow(), sc = DGDraw3D.scale(z);
                Draw.blend(Blending.additive);
                Fill.light(DGDraw3D.x(e.x, z), DGDraw3D.y(e.y, z), 20, radius * (0.35f + 0.25f * s.finpow()) * sc,
                    Tmp.c1.set(Color.white).lerp(glow, s.fin()).a(0.9f * s.fout()), Tmp.c2.set(glow).a(0f));
                Draw.blend();
            });

            e.scaled(16f, s -> {
                color(Color.white, glow, s.fin());
                stroke(2.8f * s.fout());
                Lines.circle(e.x, e.y, 3f + radius * s.finpow());
            });
            e.scaled(36f, s -> {
                color(debris, 0.5f * s.fout());
                stroke(3.5f * s.fout());
                Lines.circle(e.x, e.y, 4f + radius * 1.25f * s.finpow());
            });

            shards(e, chunks, 0.4f * power, 0.8f * power, 1.4f * power, 1.3f * power, 1.3f, 4, metal, debris, Color.valueOf("3d3e46"), 360f, 0.3f);
            embers(e, embers, 0.6f * power, 1.1f * power, 1.6f * power, 1.5f * power, glow);

            Drawf.light(e.x, e.y, radius * 2.4f * e.fout(Interp.pow2In), glow, 0.9f);
        });
    }

    public static Effect fireTongues(Color from, Color to, float radius){
        return new Effect(34f, radius * 3f, e -> {
            rand.setSeed(e.id + 11);
            for(int i = 0; i < 7; i++){
                float ang = rand.random(360f), len = radius * (0.25f + rand.random(0.35f)), z = rand.random(3f, 9f) * e.finpow();
                float f = Mathf.clamp(e.fin() * 1.4f - rand.random(0.3f));
                float fx = e.x + trnsx(ang, len * Interp.pow2Out.apply(f)), fy = e.y + trnsy(ang, len * Interp.pow2Out.apply(f));
                float rad = radius * 0.16f * Mathf.slope(f) * DGDraw3D.scale(z);
                if(rad <= 0.01f) continue;
                Draw.blend(Blending.additive);
                Fill.light(DGDraw3D.x(fx, z), DGDraw3D.y(fy, z), 12, rad, Tmp.c1.set(from).lerp(to, f).a(0.9f), Tmp.c2.set(to).a(0f));
                Draw.blend();
            }
        }).layer(Layer.effect + 0.01f);
    }

    public static Effect casing(float width, float length, float power){
        return new Effect(110f, 60f, e -> {
            float side = -Mathf.sign(e.rotation), rot = Math.abs(e.rotation);
            rand.setSeed(e.id);
            float ang = rot + side * (95f + rand.range(15f)), hs = (0.45f + rand.random(0.25f)) * power, vz = (1.2f + rand.random(0.5f)) * power;
            float spin = (8f + rand.random(10f)) * side, rot0 = rot + rand.random(360f);

            float airTime = fly(e.time, hs, vz, 0.35f);
            float x = e.x + trnsx(ang, flyDst), y = e.y + trnsy(ang, flyDst), z = flyZ;
            float r = rot0 + spin * airTime, a = Mathf.clamp(e.fout() * 4f), sc = DGDraw3D.scale(z);

            z(Layer.groundUnit - 1f);
            color(Pal.shadow, Pal.shadow.a * a);
            Fill.rect(x - DGDraw3D.shadowOffset(z), y - DGDraw3D.shadowOffset(z), width, length, r);

            z(Layer.flyingUnitLow - 1f);
            float px = DGDraw3D.x(x, z), py = DGDraw3D.y(y, z);
            color(Tmp.c1.set(brass).lerp(Pal.lightishGray, e.fin()), a);
            Fill.rect(px, py, width * sc, length * sc, r);
            color(Tmp.c1.set(Color.white).lerp(brass, 0.4f + e.fin() * 0.6f), a);
            Fill.rect(px + trnsx(r, width * 0.2f * sc), py + trnsy(r, width * 0.2f * sc), width * 0.35f * sc, length * 0.8f * sc, r);
        });
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

    static void embers(EffectContainer e, int count, float hsMin, float hsRand, float vzMin, float vzRand, Color glow){
        rand.setSeed(e.id + 202);
        z(Layer.effect);
        for(int i = 0; i < count; i++){
            float ang = rand.random(360f), hs = hsMin + rand.random(hsRand), vz = vzMin + rand.random(vzRand);
            float life = rand.random(0.5f, 1f), f = Mathf.clamp(e.fin() / life);
            if(f >= 1f) continue;

            fly(e.time, hs, vz, 0f);
            float x1 = DGDraw3D.x(e.x + trnsx(ang, flyDst), flyZ), y1 = DGDraw3D.y(e.y + trnsy(ang, flyDst), flyZ);
            fly(Math.max(e.time - 3f, 0f), hs, vz, 0f);
            float x2 = DGDraw3D.x(e.x + trnsx(ang, flyDst), flyZ), y2 = DGDraw3D.y(e.y + trnsy(ang, flyDst), flyZ);

            color(Color.white, glow, f);
            stroke(1.1f * (1f - f));
            line(x2, y2, x1, y1);
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

    static void polyline(int links, float width){
        stroke(width);
        for(int i = 0; i < links; i++){
            line(points[i * 2], points[i * 2 + 1], points[i * 2 + 2], points[i * 2 + 3]);
        }
    }

}
