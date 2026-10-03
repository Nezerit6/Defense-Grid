package dg.world.weather;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import dg.graphics.DGDraw3D;
import mindustry.entities.*;
import mindustry.graphics.*;
import mindustry.type.*;

public class StormFx{
    public static final Effect

    lift = new Effect(22f, e -> {
        Draw.color(Color.white, e.color, e.fin());
        Lines.stroke(1f * e.fout());
        Lines.circle(e.x, e.y, 1f + 5f * e.finpow());
    }),

    bolt = new Effect(36f, 600f, e -> {
        if(e.time < 2f){
            StormWeather.flash = 1f;
            Effect.shake(3f, 18f, e.x, e.y);
        }
        Rand r = new Rand(e.id * 31L + (int)(e.time / 3f));
        float a = e.time < 8f ? 1f : e.fout(), top = 320f;
        int links = 16;
        for(int k = 0; k < 2; k++){
            float px = e.x, py = e.y, pz = 0f;
            Draw.blend(k == 0 ? Blending.additive : Blending.normal);
            for(int i = 1; i <= links; i++){
                float f = i / (float)links, z = top * f;
                float nx = e.x + r.range(10f) * f + Angles.trnsx(e.rotation, 40f * f * f), ny = e.y + r.range(10f) * f + Angles.trnsy(e.rotation, 40f * f * f);
                Draw.color(k == 0 ? e.color : Color.white, k == 0 ? 0.3f * a : a);
                Lines.stroke((k == 0 ? 9f : 3f) * (1f - f * 0.5f) * a);
                Lines.line(DGDraw3D.x(px, pz), DGDraw3D.y(py, pz), DGDraw3D.x(nx, z), DGDraw3D.y(ny, z));
                if(k == 1 && r.chance(0.3f)){
                    float bx = nx + r.range(35f), by = ny + r.range(35f), bz = z - r.random(40f);
                    Lines.stroke(1.2f * a);
                    Lines.line(DGDraw3D.x(nx, z), DGDraw3D.y(ny, z), DGDraw3D.x(bx, Math.max(bz, 0f)), DGDraw3D.y(by, Math.max(bz, 0f)));
                }
                px = nx;
                py = ny;
                pz = z;
            }
        }
        Draw.blend(Blending.additive);
        Fill.light(e.x, e.y, 20, 40f * a, Tmp.c1.set(Color.white).a(a), Tmp.c2.set(e.color).a(0f));
        Draw.blend();
        e.scaled(20f, s -> {
            Draw.color(Color.white, e.color, s.fin());
            Lines.stroke(2.5f * s.fout());
            Lines.circle(e.x, e.y, 4f + 36f * s.finpow());
        });
        Drawf.light(e.x, e.y, 220f * a, e.color, a);
    }).layer(Layer.weather - 0.5f),

    drop = new Effect(90f, e -> {
        if(!(e.data instanceof Item)) return;
        Item item = (Item)e.data;
        float slide = 4f * Interp.pow2Out.apply(Mathf.clamp(e.time / 20f));
        e.scaled(14f, s -> {
            Draw.color(Color.valueOf("a9c3df"), s.fout() * 0.7f);
            Lines.stroke(1f * s.fout());
            Lines.circle(e.x, e.y, 2f + 6f * s.finpow());
        });
        Draw.alpha(Mathf.clamp(e.fout() * 3f));
        Draw.rect(item.fullIcon, e.x + Angles.trnsx(e.rotation, slide), e.y + Angles.trnsy(e.rotation, slide), 5f, 5f, e.rotation);
    }).layer(Layer.debris + 0.3f);
}
