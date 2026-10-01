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
        shatterColor = Color.valueOf("ea8878"),
        smokeColor = Color.valueOf("6e7080"),
        dirt = Color.valueOf("5d5e68"),
        metal = Color.valueOf("989aa4"),
        brass = Color.valueOf("e8c07a");

    public static final Effect

    shatterSmoke = risingSmoke(7, 9f, 14f, 3.2f, Color.valueOf("8b8c95"), smokeColor, 70f),

    arcflashSmoke = risingSmoke(9, 12f, 18f, 3.4f, Color.valueOf("a5a6ad"), smokeColor, 90f),

    frostPuff = risingSmoke(6, 7f, 10f, 2.4f, Color.white, cryo, 45f),

    customSmoke = new Effect(SmokeTestBlock.maxLifetime, 200f, e -> {
        if(e.data instanceof SmokeStyle) ((SmokeStyle)e.data).draw(e.x, e.y, e.rotation, e.time, e.id);
    }).layer(Layer.bullet - 1f),

    arcletShoot = new Effect(14f, 40f, e -> {
        rand.setSeed(e.id);
        for(int i = 0; i < 4; i++){
            float ang = e.rotation + rand.range(40f), len = 3f + rand.random(5f) * e.finpow() + 2f;
            float x1 = e.x, y1 = e.y;
            for(int j = 1; j <= 3; j++){
                float l = len * j / 3f;
                float x2 = e.x + trnsx(ang, l) + rand.range(1f), y2 = e.y + trnsy(ang, l) + rand.range(1f);
                color(Pal.lancerLaser, 0.4f * e.fout());
                stroke(1.8f * e.fout());
                line(x1, y1, x2, y2);
                color(Color.white, e.fout());
                stroke(0.7f * e.fout());
                line(x1, y1, x2, y2);
                x1 = x2;
                y1 = y2;
            }
        }
        Draw.blend(Blending.additive);
        Fill.light(e.x, e.y, 12, 5f * e.fout(), Tmp.c1.set(Pal.lancerLaser).a(0.7f * e.fout()), Tmp.c2.set(Pal.lancerLaser).a(0f));
        Draw.blend();
        Drawf.light(e.x, e.y, 18f * e.fout(), Pal.lancerLaser, 0.7f);
    }),

    chainArc = new Effect(20f, 400f, e -> {
        if(!(e.data instanceof Position)) return;
        Position p = (Position)e.data;
        float tx = p.getX(), ty = p.getY(), dst = Mathf.dst(e.x, e.y, tx, ty);
        int links = Mathf.clamp(Mathf.ceil(dst / 5f), 2, points.length / 2 - 1);
        float bulge = Math.min(dst * 0.12f, 9f);
        float nx = -(ty - e.y) / Math.max(dst, 0.01f), ny = (tx - e.x) / Math.max(dst, 0.01f);

        rand.setSeed(e.id * 31L + (long)(e.time / 3f));
        for(int i = 0; i <= links; i++){
            float t = i / (float)links, off = i == 0 || i == links ? 0f : rand.range(2.4f), z = bulge * 4f * t * (1f - t);
            float wx = Mathf.lerp(e.x, tx, t) + nx * off, wy = Mathf.lerp(e.y, ty, t) + ny * off;
            points[i * 2] = DGDraw3D.x(wx, z);
            points[i * 2 + 1] = DGDraw3D.y(wy, z);
        }

        float fout = e.fout(Interp.pow2In);
        color(e.color, 0.3f * fout);
        polyline(links, 4.5f * fout);
        color(Color.white, e.color, e.fin());
        polyline(links, 1.8f * fout + 0.2f);

        for(int i = 1; i < links; i++){
            if(!rand.chance(0.3f)) continue;
            float px = points[i * 2], py = points[i * 2 + 1], ang = rand.random(360f), len = 2f + rand.random(4f);
            float mx = px + trnsx(ang, len * 0.5f) + rand.range(1f), my = py + trnsy(ang, len * 0.5f) + rand.range(1f);
            stroke(0.8f * fout);
            line(px, py, mx, my);
            line(mx, my, px + trnsx(ang, len), py + trnsy(ang, len));
        }

        Drawf.light(e.x, e.y, tx, ty, 18f, e.color, 0.55f * fout);
    }).layer(Layer.bullet + 0.01f),

    arcHit = new Effect(18f, 40f, e -> {
        rand.setSeed(e.id);
        for(int i = 0; i < 6; i++){
            float ang = rand.random(360f), hs = 0.5f + rand.random(0.9f), vz = 0.6f + rand.random(1f);
            float t = e.time, d = hs * t, z = Math.max(vz * t - gravity * t * t / 2f, 0f);
            float t2 = Math.max(t - 2f, 0f), d2 = hs * t2, z2 = Math.max(vz * t2 - gravity * t2 * t2 / 2f, 0f);
            color(Color.white, Pal.lancerLaser, e.fin());
            stroke(0.9f * e.fout());
            line(DGDraw3D.x(e.x + trnsx(ang, d2), z2), DGDraw3D.y(e.y + trnsy(ang, d2), z2), DGDraw3D.x(e.x + trnsx(ang, d), z), DGDraw3D.y(e.y + trnsy(ang, d), z));
        }
        e.scaled(8f, s -> {
            color(Color.white, Pal.lancerLaser, s.fin());
            stroke(1.2f * s.fout());
            Lines.circle(e.x, e.y, 1f + 4f * s.finpow());
        });
        Drawf.light(e.x, e.y, 14f * e.fout(), Pal.lancerLaser, 0.6f);
    }),

    needleShoot = new Effect(9f, e -> {
        color(Color.white, Pal.lightOrange, e.fin());
        for(int i : Mathf.signs){
            Drawf.tri(e.x, e.y, 2.2f * e.fout(), 6f, e.rotation + 25f * i);
        }
        Drawf.tri(e.x, e.y, 2.6f * e.fout(), 9f, e.rotation);
        Drawf.light(e.x, e.y, 12f * e.fout(), Pal.lightOrange, 0.5f);
    }),

    needleCasing = casing(1f, 1.9f, 0.8f),

    shatterShoot = new Effect(18f, 80f, e -> {
        Color c = e.color.equals(Color.white) ? Pal.lightOrange : e.color;

        e.scaled(10f, s -> {
            color(Color.white, c, s.fin());
            Drawf.tri(e.x, e.y, 6f * s.fout(), 22f * s.fout() + 4f, e.rotation);
            Drawf.tri(e.x, e.y, 6f * s.fout(), 3f, e.rotation + 180f);
            for(int i : Mathf.signs){
                Drawf.tri(e.x, e.y, 3.5f * s.fout(), 10f * s.fout() + 2f, e.rotation + 78f * i);
            }
            Draw.blend(Blending.additive);
            Fill.light(e.x, e.y, 16, 7f * s.fout(), Tmp.c1.set(c).a(0.8f * s.fout()), Tmp.c2.set(c).a(0f));
            Draw.blend();
        });

        e.scaled(14f, s -> {
            color(Color.white, c, s.fin());
            stroke(2f * s.fout());
            Lines.circle(e.x, e.y, 3f + 11f * s.finpow());
        });

        rand.setSeed(e.id);
        for(int i = 0; i < 7; i++){
            float ang = e.rotation + rand.range(22f), hs = 1.2f + rand.random(1.6f), vz = 0.4f + rand.random(1.2f);
            float t = e.time, d = hs * t, z = Math.max(vz * t - gravity * t * t / 2f, 0f);
            color(Color.white, c, e.fin());
            Fill.circle(DGDraw3D.x(e.x + trnsx(ang, d), z), DGDraw3D.y(e.y + trnsy(ang, d), z), 0.9f * e.fout() * DGDraw3D.scale(z));
        }

        Drawf.light(e.x, e.y, 38f * e.fout(), c, 0.8f);
    }).layer(Layer.effect),

    shatterCasing = casing(1.7f, 3.4f, 1f),

    shatterBurst = new MultiEffect(
        burst(shatterColor, dirt, 26f, 10, 5, 1f),
        risingSmoke(8, 10f, 12f, 3f, Color.valueOf("8b8c95"), dirt, 80f, 180f, 0.4f)
    ),

    shatterBurstFire = new MultiEffect(
        burst(Pal.lightOrange, Color.valueOf("4d4e58"), 30f, 9, 9, 1.2f),
        fireTongues(Pal.lightishOrange, Pal.lightOrange, 30f),
        risingSmoke(9, 11f, 16f, 3.2f, Color.valueOf("6e7080"), Color.valueOf("3d3e46"), 95f, 180f, 0.4f)
    ),

    shrapnelHit = new Effect(14f, e -> {
        Color c = e.color.equals(Color.white) ? shatterColor : e.color;
        color(Color.white, c, e.fin());
        stroke(0.9f * e.fout());
        randLenVectors(e.id, 4, 1f + 7f * e.finpow(), e.rotation + 180f, 60f, (x, y) -> lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), 1f + 2.5f * e.fout()));
        color(Color.lightGray, 0.5f * e.fout());
        Fill.circle(e.x, e.y, 1.5f * e.fout());
    }),

    cryoShoot = new MultiEffect(new WaveEffect(){{
        colorFrom = Color.white;
        colorTo = cryo;
        sizeTo = 10f;
        lifetime = 16f;
        strokeFrom = 2f;
    }}, new Effect(22f, e -> {
        color(Color.white, cryo, e.fin());
        randLenVectors(e.id, 7, 3f + 20f * e.finpow(), e.rotation, 35f, (x, y) -> {
            float ang = Mathf.angle(x, y);
            Drawf.tri(e.x + x, e.y + y, 2.2f * e.fout(), 5f * e.fout() + 1f, ang);
            Drawf.tri(e.x + x, e.y + y, 2.2f * e.fout(), 2f * e.fout(), ang + 180f);
        });
        Drawf.light(e.x, e.y, 30f * e.fout(), cryo, 0.7f);
    }), Fx.lancerLaserShootSmoke),

    cryoHit = new MultiEffect(new Effect(60f, 80f, e -> {
        e.scaled(20f, s -> {
            color(Color.white, cryo, s.fin());
            stroke(1.6f * s.fout());
            Lines.circle(e.x, e.y, 2f + 11f * s.finpow());
            rand.setSeed(e.id + 7);
            for(int i = 0; i < 6; i++){
                float ang = rand.random(360f), len = (4f + rand.random(6f)) * s.finpow();
                lineAngle(e.x, e.y, ang, len);
            }
        });

        shards(e, 8, 0.5f, 0.9f, 1.3f, 1.1f, 1.2f, 3, Color.white, cryo, cryoDark, 360f, 0.4f);

        rand.setSeed(e.id + 3);
        for(int i = 0; i < 4; i++){
            float gx = e.x + rand.range(8f), gy = e.y + rand.range(8f), g = Mathf.slope(Mathf.clamp(e.fin() * 1.6f - rand.random(0.5f)));
            color(Color.white, g);
            for(int j = 0; j < 4; j++) Drawf.tri(gx, gy, 0.8f * g, 2.6f * g, j * 90f + 45f);
        }

        Drawf.light(e.x, e.y, 24f * e.fout(), cryo, 0.6f);
    }), risingSmoke(6, 7f, 10f, 2.4f, Color.white, cryo, 50f, 180f, 0.3f)),

    arcflashLaunch = new Effect(26f, 100f, e -> {
        float back = e.rotation + 180f;
        e.scaled(14f, s -> {
            color(Pal.missileYellow, Pal.missileYellowBack, s.fin());
            Drawf.tri(e.x, e.y, 7f * s.fout(), 26f * s.fout() + 3f, back);
            for(int i : Mathf.signs){
                Drawf.tri(e.x, e.y, 4f * s.fout(), 14f * s.fout() + 2f, back + 28f * i);
            }
            color(Color.white, Pal.missileYellow, s.fin());
            Drawf.tri(e.x, e.y, 4f * s.fout(), 8f * s.fout() + 2f, e.rotation);

            Draw.blend(Blending.additive);
            Fill.light(e.x, e.y, 16, 9f * s.fout(), Tmp.c1.set(Pal.missileYellow).a(0.8f * s.fout()), Tmp.c2.set(Pal.missileYellowBack).a(0f));
            Draw.blend();
        });

        e.scaled(18f, s -> {
            color(Pal.missileYellow, Pal.missileYellowBack, s.fin());
            stroke(2.4f * s.fout());
            Lines.circle(e.x, e.y, 4f + 12f * s.finpow());
        });

        rand.setSeed(e.id);
        for(int i = 0; i < 9; i++){
            float ang = back + rand.range(35f), hs = 1f + rand.random(1.8f), vz = 0.3f + rand.random(1f);
            float t = e.time, d = hs * t, z = Math.max(vz * t - gravity * t * t / 2f, 0f);
            color(Pal.missileYellow, Pal.missileYellowBack, e.fin());
            Fill.circle(DGDraw3D.x(e.x + trnsx(ang, d), z), DGDraw3D.y(e.y + trnsy(ang, d), z), 1f * e.fout() * DGDraw3D.scale(z));
        }

        Drawf.light(e.x, e.y, 45f * e.fout(), Pal.missileYellowBack, 0.9f);
    }),

    arcflashBoom = new MultiEffect(
        burst(Pal.missileYellowBack, dirt, 34f, 14, 10, 1.5f),
        fireTongues(Pal.missileYellow, Pal.missileYellowBack, 34f),
        risingSmoke(14, 16f, 30f, 5f, Color.valueOf("8b8c95"), Color.valueOf("4d4e58"), 150f, 180f, 0.45f)
    ),

    powerSpark = new Effect(40f, 30f, e -> {
        float load = Mathf.clamp(e.rotation), z = 2f + 9f * e.finpow();
        rand.setSeed(e.id);
        float dx = rand.range(4f) * e.fin(), dy = rand.range(4f) * e.fin();
        float s = (0.6f + 0.8f * load) * e.fout() * DGDraw3D.scale(z);
        Draw.blend(Blending.additive);
        color(e.color, (0.35f + 0.65f * load) * e.fout());
        Fill.square(DGDraw3D.x(e.x + dx, z), DGDraw3D.y(e.y + dy, z), s, 45f);
        color(Color.white, (0.3f + 0.5f * load) * e.fout());
        Fill.square(DGDraw3D.x(e.x + dx, z), DGDraw3D.y(e.y + dy, z), s * 0.45f, 45f);
        Draw.blend();
    }).layer(Layer.effect),

    nodeBlast = new Effect(50f, 120f, e -> {
        float r = 12f + e.rotation * 10f;
        e.scaled(10f, s -> {
            color(Color.white, s.fout());
            Fill.circle(e.x, e.y, r * 0.7f * s.fout());
        });
        e.scaled(24f, s -> {
            color(Color.white, e.color, s.fin());
            stroke(3f * s.fout());
            Lines.circle(e.x, e.y, 4f + r * 1.6f * s.finpow());
        });
        rand.setSeed(e.id);
        for(int i = 0; i < 10 + (int)e.rotation * 4; i++){
            float ang = rand.random(360f), hs = 0.8f + rand.random(1.6f), vz = 0.8f + rand.random(1.6f);
            fly(e.time, hs, vz, 0f);
            float x1 = DGDraw3D.x(e.x + trnsx(ang, flyDst), flyZ), y1 = DGDraw3D.y(e.y + trnsy(ang, flyDst), flyZ);
            fly(Math.max(e.time - 3f, 0f), hs, vz, 0f);
            float x2 = DGDraw3D.x(e.x + trnsx(ang, flyDst), flyZ), y2 = DGDraw3D.y(e.y + trnsy(ang, flyDst), flyZ);
            color(Color.white, e.color, e.fin());
            stroke(1.2f * e.fout());
            line(x2, y2, x1, y1);
        }
        Drawf.light(e.x, e.y, r * 5f * e.fout(), e.color, 0.9f);
    }),

    liftDust = risingSmoke(5, 14f, 10f, 4f, Color.valueOf("b0b1b8"), dirt, 90f, 25f, 0.4f),

    wallChips = new Effect(45f, 50f, e -> {
        shards(e, 2, 0.4f, 0.6f, 0.6f, 0.9f, 0.7f, 4, Tmp.c3.set(e.color).lerp(Color.white, 0.25f), e.color, Tmp.c4.set(e.color).mul(0.55f), 100f, 0.3f);
    }),

    wallDust = risingSmoke(2, 5f, 7f, 2f, Color.valueOf("9a9ba3"), dirt, 55f, 50f, 0.4f),

    wallCrumble = new MultiEffect(new Effect(80f, 90f, e -> {
        e.scaled(18f, s -> {
            color(e.color, dirt, s.fin());
            stroke(2f * s.fout());
            Lines.square(e.x, e.y, 4f + 6f * s.finpow(), 45f * s.fin());
        });
        shards(e, 12, 0.3f, 0.8f, 1.1f, 1.2f, 1.3f, 4, Tmp.c3.set(e.color).lerp(Color.white, 0.25f), e.color, Tmp.c4.set(e.color).mul(0.55f), 360f, 0.35f);
    }), risingSmoke(10, 9f, 12f, 3.2f, Color.valueOf("9a9ba3"), dirt, 100f, 180f, 0.45f)),

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
