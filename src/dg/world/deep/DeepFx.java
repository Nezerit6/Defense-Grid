package dg.world.deep;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.entities.*;
import mindustry.graphics.*;

public class DeepFx{
    private static final Rand rand = new Rand();

    public static final Effect

    revealed = new Effect(70f, 200f, e -> {
        Draw.color(Color.white, e.color, e.fin());
        Lines.stroke(2.5f * e.fout());
        Lines.circle(e.x, e.y, e.rotation * (0.4f + 1.2f * e.finpow()));
        Draw.blend(Blending.additive);
        Fill.light(e.x, e.y, 20, e.rotation * 1.2f, Tmp.c1.set(e.color).a(0.5f * e.fout()), Tmp.c2.set(e.color).a(0f));
        Draw.blend();
    }).layer(Layer.floor + 0.5f),

    seismic = new Effect(90f, 500f, e -> {
        for(int i = 0; i < 3; i++){
            float f = Mathf.clamp(e.fin() * 1.3f - i * 0.15f);
            if(f <= 0f || f >= 1f) continue;
            Draw.color(e.color, (1f - f) * 0.7f);
            Lines.stroke(2f * (1f - f));
            rand.setSeed(e.id + i);
            int segs = 60;
            float r = e.rotation * f;
            for(int k = 0; k < segs; k++){
                float a1 = k * 360f / segs, a2 = (k + 1) * 360f / segs;
                float j1 = rand.range(2f) * (1f - f), j2 = rand.range(2f) * (1f - f);
                Lines.line(e.x + Angles.trnsx(a1, r + j1), e.y + Angles.trnsy(a1, r + j1), e.x + Angles.trnsx(a2, r + j2), e.y + Angles.trnsy(a2, r + j2), true);
            }
        }
    }).layer(Layer.floor + 0.5f),

    boreDust = new Effect(60f, e -> {
        rand.setSeed(e.id);
        Draw.color(Color.valueOf("9a8a74"), Color.valueOf("5d5e68"), e.fin());
        Draw.alpha(0.7f * e.fout());
        float d = 4f + 10f * e.finpow();
        Fill.circle(e.x + Angles.trnsx(e.rotation, d), e.y + Angles.trnsy(e.rotation, d), 1.5f + 3f * e.fin());
    }),

    boreChip = new Effect(30f, e -> {
        Draw.color(e.color, Color.valueOf("5d5e68"), e.fin());
        float d = 6f * e.finpow() + 2f;
        Fill.square(e.x + Angles.trnsx(e.rotation, d), e.y + Angles.trnsy(e.rotation, d), 1.1f * e.fout(), e.rotation + e.time * 8f);
    }),

    steam = new Effect(80f, e -> {
        Draw.color(Color.white, Color.valueOf("9a9ba3"), e.fin());
        Draw.alpha(0.45f * e.fout());
        Fill.circle(e.x + e.fin() * 4f, e.y + e.finpow() * 10f, 1.5f + 3f * e.fin());
    }),

    shutter = new Effect(40f, e -> {
        Draw.color(e.color);
        Lines.stroke(1.6f * e.fout());
        Lines.square(e.x, e.y, e.rotation + 4f * e.fin(), 0f);
    });
}
