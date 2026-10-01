package dg.content;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import dg.graphics.*;
import mindustry.entities.*;
import mindustry.entities.effect.*;
import mindustry.graphics.*;

import static arc.graphics.g2d.Draw.*;
import static arc.graphics.g2d.Lines.*;
import static arc.math.Angles.*;

public class DGTurretFx{
    private static final Rand rand = new Rand();

    public static final Color
        frost = Color.valueOf("cfefff"),
        pull = Color.valueOf("b18cff"),
        push = Color.valueOf("ff9c5a"),
        shield = Color.valueOf("7fd3ff"),
        toxic = Color.valueOf("9ad15b"),
        spark = Color.valueOf("bfe6ff"),
        orb = Color.valueOf("ffd27a");

    public static final Effect

    frostMote = new Effect(70f, e -> {
        rand.setSeed(e.id);
        float z = 1f + 6f * e.fin(), drift = rand.range(6f) * e.fin();
        float x = DGDraw3D.x(e.x + drift, z), y = DGDraw3D.y(e.y + Mathf.sin(e.time + rand.random(20f), 8f, 1.5f), z);
        color(frost, e.color, e.fin());
        alpha(Mathf.slope(e.fin()) * 0.9f);
        float s = 1.4f * DGDraw3D.scale(z);
        for(int i = 0; i < 3; i++){
            Lines.stroke(0.5f);
            Lines.lineAngleCenter(x, y, i * 60f + e.time * 2f, s * 2.2f);
        }
        Fill.circle(x, y, s * 0.4f);
    }).layer(Layer.flyingUnit + 1f),

    iceShatter = new Effect(60f, 80f, e -> {
        float size = Math.max(e.rotation, 6f);
        e.scaled(14f, s -> {
            color(Color.white, frost, s.fin());
            stroke(1.6f * s.fout());
            Lines.poly(e.x, e.y, 6, size * 0.7f + size * 0.6f * s.finpow(), e.id * 37f);
        });
        DGFx.shards(e, 6 + (int)(size / 4f), 0.4f, 0.8f, 0.6f, 0.8f, 1.2f + size / 20f, 3, Color.white, frost, DGFx.cryoDark, 360f, 0.3f);
    }),

    toxicBubble = new Effect(50f, e -> {
        color(e.color, Color.valueOf("4f7a2a"), e.fin());
        float z = 2f + 8f * e.finpow();
        stroke(0.7f * e.fout());
        Lines.circle(DGDraw3D.x(e.x, z), DGDraw3D.y(e.y, z), (0.6f + 1.6f * e.fin()) * DGDraw3D.scale(z));
    }).layer(Layer.flyingUnit + 1f),

    bulletPop = new Effect(22f, e -> {
        color(Color.white, e.color, e.fin());
        stroke(1.4f * e.fout());
        Lines.circle(e.x, e.y, 2f + 6f * e.finpow());
        rand.setSeed(e.id);
        for(int i = 0; i < 5; i++){
            float a = rand.random(360f), d = 2f + 9f * e.finpow() * rand.random(0.6f, 1f);
            Lines.lineAngle(e.x + trnsx(a, d), e.y + trnsy(a, d), a, 3f * e.fout());
        }
    }),

    bulletTurn = new Effect(26f, e -> {
        color(Color.white, e.color, e.fin());
        stroke(1.6f * e.fout());
        Lines.arc(e.x, e.y, 3f + 7f * e.finpow(), 0.5f, e.rotation - 90f);
        Draw.blend(Blending.additive);
        Fill.light(e.x, e.y, 10, 7f * e.fout(), Tmp.c1.set(e.color).a(0.7f * e.fout()), Tmp.c2.set(e.color).a(0f));
        Draw.blend();
    }),

    splash = new Effect(36f, 60f, e -> {
        rand.setSeed(e.id);
        e.scaled(16f, s -> {
            color(e.color);
            alpha(0.7f * s.fout());
            stroke(1.4f * s.fout());
            Lines.ellipse(e.x, e.y, 3f + 8f * s.finpow(), 1f, 0.55f, 0f);
        });
        for(int i = 0; i < 9; i++){
            float ang = e.rotation + 180f + rand.range(110f), hs = 0.4f + rand.random(1.1f), vz = 0.6f + rand.random(1.2f);
            DGFx.fly(e.time, hs, vz, 0f);
            float z = DGFx.flyZ();
            if(z <= 0f && e.time > 4f) continue;
            float sc = DGDraw3D.scale(z);
            color(Tmp.c1.set(e.color).lerp(Color.white, 0.3f), 0.9f * e.fout());
            Fill.circle(DGDraw3D.x(e.x + trnsx(ang, DGFx.flyDst()), z), DGDraw3D.y(e.y + trnsy(ang, DGFx.flyDst()), z), (0.6f + rand.random(0.8f)) * sc);
        }
    }).layer(Layer.bullet - 0.1f),

    seekerLock = new Effect(18f, e -> {
        color(Color.white, e.color, e.fin());
        stroke(1.2f * e.fout());
        Lines.square(e.x, e.y, 2f + 4f * e.fout(), 45f + e.fin() * 90f);
    }),

    bounceArc = new Effect(14f, 200f, e -> {
        if(!(e.data instanceof float[])) return;
        float[] to = (float[])e.data;
        color(Color.white, e.color, e.fin());
        rand.setSeed(e.id);
        float px = e.x, py = e.y;
        int links = 6;
        for(int i = 1; i <= links; i++){
            float f = i / (float)links, nx = Mathf.lerp(e.x, to[0], f), ny = Mathf.lerp(e.y, to[1], f);
            if(i < links){
                nx += rand.range(5f);
                ny += rand.range(5f);
            }
            stroke(1.6f * e.fout());
            line(px, py, nx, ny);
            px = nx;
            py = ny;
        }
    }),

    orbVanish = new Effect(50f, 60f, e -> {
        rand.setSeed(e.id);
        e.scaled(20f, s -> {
            Draw.blend(Blending.additive);
            Fill.light(e.x, e.y, 14, 8f * s.fout(), Tmp.c1.set(Color.white).a(0.8f * s.fout()), Tmp.c2.set(e.color).a(0f));
            Draw.blend();
            color(e.color, Color.white, s.fout());
            stroke(1.2f * s.fout());
            Lines.circle(e.x, e.y, 2f + 9f * s.finpow());
        });
        for(int i = 0; i < 7; i++){
            float ang = rand.random(360f), d = 2f + 12f * e.finpow() * rand.random(0.4f, 1f), z = 10f * e.fin() * rand.random(0.5f, 1f);
            float x = DGDraw3D.x(e.x + trnsx(ang, d), z), y = DGDraw3D.y(e.y + trnsy(ang, d), z);
            color(Color.white, e.color, e.fin());
            Fill.square(x, y, 1.3f * e.fout() * DGDraw3D.scale(z), 45f + e.time * 4f);
        }
    }).layer(Layer.bullet + 0.1f),

    mineArm = new Effect(20f, e -> {
        color(Pal.remove);
        stroke(1f * e.fout());
        Lines.circle(e.x, e.y, 2f + 10f * e.finpow());
    }),

    mineBoom = new MultiEffect(
        DGFx.burst(Pal.remove, DGFx.dirt, 26f, 10, 6, 1.2f),
        DGFx.risingSmoke(8, 10f, 16f, 3.6f, Color.valueOf("8b8c95"), DGFx.smokeColor, 100f, 180f, 0.4f)
    ),

    clusterBoom = new MultiEffect(
        DGFx.burst(Pal.missileYellowBack, DGFx.dirt, 30f, 12, 10, 1.4f),
        DGFx.fireTongues(Pal.missileYellow, Pal.missileYellowBack, 26f),
        DGFx.risingSmoke(10, 12f, 20f, 4f, Color.valueOf("8b8c95"), DGFx.smokeColor, 120f, 180f, 0.4f)
    ),

    railShoot = new Effect(10f, e -> {
        color(e.color);
        float w = 1.2f + 6f * e.fout();
        Drawf.tri(e.x, e.y, w, 24f * e.fout(), e.rotation);
        for(int i : Mathf.signs){
            Drawf.tri(e.x, e.y, w * 0.9f, 14f * e.fout(), e.rotation + i * 90f);
        }
        Drawf.tri(e.x, e.y, w, 4f * e.fout(), e.rotation + 180f);
    }),

    railTrail = new Effect(18f, 400f, e -> {
        if(!(e.data instanceof arc.math.geom.Vec2)) return;
        arc.math.geom.Vec2 v = (arc.math.geom.Vec2)e.data;
        color(e.color);
        rand.setSeed(e.id);
        stroke(e.fout() * 0.8f + 0.4f);
        for(int i = 0; i < 5; i++){
            float d = rand.random(8f, Math.max(v.dst(e.x, e.y) - 8f, 9f));
            Lines.lineAngleCenter(e.x + trnsx(e.rotation, d), e.y + trnsy(e.rotation, d), e.rotation + e.finpow(), e.foutpowdown() * 14f * rand.random(0.5f, 1f) + 0.3f);
        }
        e.scaled(12f, b -> {
            stroke(b.fout() * 1.2f);
            color(e.color);
            Lines.line(e.x, e.y, v.x, v.y);
        });
    }),

    railEnd = new Effect(14f, e -> {
        color(e.color);
        Drawf.tri(e.x, e.y, e.fout() * 1.5f, 5f, e.rotation);
    }),

    shellCasing = DGFx.casing(1.6f, 3.4f, 1.1f),

    laserCharge = new Effect(40f, 60f, e -> {
        rand.setSeed(e.id);
        for(int i = 0; i < 8; i++){
            float ang = rand.random(360f), d = 18f * e.fout() * rand.random(0.6f, 1f);
            color(e.color, Color.white, e.fin());
            stroke(1.2f * e.fin());
            Lines.lineAngle(e.x + trnsx(ang, d), e.y + trnsy(ang, d), ang + 180f, 3f * e.fin());
        }
        Draw.blend(Blending.additive);
        Fill.light(e.x, e.y, 14, 7f * e.fin(), Tmp.c1.set(Color.white).a(0.9f * e.fin()), Tmp.c2.set(e.color).a(0f));
        Draw.blend();
    });
}
