package dg.content;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.Tmp;
import dg.graphics.DGDraw3D;
import mindustry.content.Fx;
import mindustry.entities.*;
import mindustry.entities.effect.*;
import mindustry.graphics.*;

import static arc.graphics.g2d.Draw.*;
import static arc.graphics.g2d.Lines.*;
import static arc.math.Angles.*;

public class DGFx{
    public static final Color
        cryo = Color.valueOf("afeeee"),
        shatterColor = Color.valueOf("ea8878"),
        smokeColor = Color.valueOf("6e7080");

    public static final Effect

    /** Smoke puffs that rise off the ground in perspective. */
    shatterSmoke = risingSmoke(7, 9f, 14f, 3.2f, Color.valueOf("8b8c95"), smokeColor, 70f),

    arcflashSmoke = risingSmoke(9, 12f, 18f, 3.4f, Color.valueOf("a5a6ad"), smokeColor, 90f),

    frostPuff = risingSmoke(6, 7f, 10f, 2.4f, Color.white, cryo, 45f),

    shatterShoot = new MultiEffect(Fx.shootBigColor, Fx.colorSparkBig, new WaveEffect(){{
        colorFrom = Color.white;
        colorTo = shatterColor;
        sizeTo = 12f;
        lifetime = 12f;
        strokeFrom = 2.5f;
    }}),

    cryoShoot = new MultiEffect(new WaveEffect(){{
        colorFrom = Color.white;
        colorTo = cryo;
        sizeTo = 10f;
        lifetime = 16f;
        strokeFrom = 2f;
    }}, new Effect(22f, e -> {
        //ice shards bursting forward
        color(Color.white, cryo, e.fin());
        randLenVectors(e.id, 7, 3f + 20f * e.finpow(), e.rotation, 35f, (x, y) -> {
            float ang = Mathf.angle(x, y);
            Drawf.tri(e.x + x, e.y + y, 2.2f * e.fout(), 5f * e.fout() + 1f, ang);
            Drawf.tri(e.x + x, e.y + y, 2.2f * e.fout(), 2f * e.fout(), ang + 180f);
        });
        Drawf.light(e.x, e.y, 30f * e.fout(), cryo, 0.7f);
    }), Fx.lancerLaserShootSmoke),

    arcflashLaunch = new MultiEffect(Fx.shootBig, new WaveEffect(){{
        colorFrom = Pal.missileYellow;
        colorTo = Pal.missileYellowBack;
        sizeTo = 13f;
        lifetime = 14f;
        strokeFrom = 2.5f;
    }}),

    arcletDischarge = new Effect(12f, e -> {
        color(Color.white, Pal.lancerLaser, e.fin());
        stroke(1.1f * e.fout());
        Lines.circle(e.x, e.y, 1f + 4.5f * e.finpow());
        randLenVectors(e.id, 4, 2f + 6f * e.finpow(), (x, y) -> lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), 1f + e.fout() * 2f));
        Drawf.light(e.x, e.y, 20f * e.fout(), Pal.lancerLaser, 0.6f);
    });

    /** Smoke that billows up from the ground; the effect rotation is the direction it drifts in. */
    public static Effect risingSmoke(int count, float spread, float height, float size, Color from, Color to, float lifetime){
        return new Effect(lifetime, 120f, e -> {
            float z = height * e.finpow();
            float scl = DGDraw3D.scale(z);
            Color center = Tmp.c1.set(from).lerp(to, e.fin()).mulA(0.6f * e.fout()), edge = Tmp.c2.set(center).a(0f);
            randLenVectors(e.id, count, 1f + spread * e.finpow(), e.rotation, 70f, (x, y) -> {
                float rad = size * (0.5f + 0.9f * Mathf.clamp(e.fin() * 3f)) * (0.7f + 0.3f * e.fout()) * scl;
                Fill.light(DGDraw3D.x(e.x + x, z), DGDraw3D.y(e.y + y, z), 12, rad, center, edge);
            });
        }).layer(Layer.bullet - 1f);
    }
}
