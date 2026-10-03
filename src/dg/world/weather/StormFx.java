package dg.world.weather;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
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
