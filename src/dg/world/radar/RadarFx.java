package dg.world.radar;

import arc.graphics.g2d.*;
import mindustry.entities.*;
import mindustry.graphics.*;

public class RadarFx{
    public static final Effect contact = new Effect(50f, e -> {
        Draw.color(Pal.remove);
        Lines.stroke(2f * e.fout());
        Lines.circle(e.x, e.y, e.rotation + 4f + 30f * e.finpow());
        Lines.stroke(1f * e.fout());
        Lines.circle(e.x, e.y, e.rotation + 2f + 18f * e.finpow());
    }).layer(Layer.overlayUI - 1f);
}
