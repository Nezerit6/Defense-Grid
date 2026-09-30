package dg.content;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import dg.graphics.*;
import dg.world.blocks.SmokeTestBlock;
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

    /** Smoke drawn with the {@link SmokeStyle} passed as effect data; used by the smoke test block. */
    customSmoke = new Effect(SmokeTestBlock.maxLifetime, 200f, e -> {
        if(e.data instanceof SmokeStyle) ((SmokeStyle)e.data).draw(e.x, e.y, e.rotation, e.time, e.id);
    }).layer(Layer.bullet - 1f);

    /** Smoke that billows up from the ground; the effect rotation is the direction it drifts in. */
    public static Effect risingSmoke(int count, float spread, float height, float size, Color from, Color to, float lifetime){
        SmokeStyle style = new SmokeStyle(count, spread, height, size, lifetime, from, to);
        return new Effect(lifetime, 120f, e -> style.draw(e.x, e.y, e.rotation, e.time, e.id)).layer(Layer.bullet - 1f);
    }
}
