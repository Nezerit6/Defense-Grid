package dg.entities.part;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import dg.graphics.DGDraw3D;
import mindustry.entities.part.DrawPart;
import mindustry.graphics.*;

/** Particles (mist, smoke, sparks) that float up from the turret and drift in perspective as they rise. */
public class RisePart extends DrawPart{
    public float x, y;
    /** Radius of the area particles appear in. */
    public float spread = 2f;
    public boolean mirror = false;
    public int particles = 6;
    public float lifetime = 60f;
    /** Height particles reach at the end of their life. */
    public float rise = 8f;
    /** Sideways travel over the lifetime. */
    public float drift = 2f;
    public float size = 1.2f, sizeTo = 0f;
    public Color color = Color.white.cpy(), colorTo = Color.white.cpy();
    public float alpha = 0.7f;
    /** Controls how many particles are visible. */
    public PartProgress progress = PartProgress.warmup;
    public boolean additive = false;
    public float layer = Layer.effect - 1f;

    @Override
    public void draw(PartParams params){
        float prog = progress.getClamp(params);
        if(prog <= 0.001f) return;

        float pz = Draw.z();
        Draw.z(layer);
        if(additive) Draw.blend(Blending.additive);

        int len = mirror && params.sideOverride == -1 ? 2 : 1;
        long base = (long)Mathf.round(params.x) * 7919L + (long)Mathf.round(params.y) * 104729L;

        for(int s = 0; s < len; s++){
            int side = params.sideOverride == -1 ? s : params.sideOverride;
            float sign = (side == 0 ? 1 : -1) * params.sideMultiplier;
            Tmp.v3.set(x * sign, y).rotate(params.rotation - 90).add(params.x, params.y);

            for(int i = 0; i < particles; i++){
                long seed = base + i * 31L + side * 997L;
                float time = Time.time + Mathf.randomSeed(seed, lifetime);
                int cycle = (int)(time / lifetime);
                float fin = (time % lifetime) / lifetime;
                long cseed = seed + cycle * 6151L;

                //fewer particles at low progress
                if(Mathf.randomSeed(cseed + 1) > prog) continue;

                Tmp.v1.trns(Mathf.randomSeed(cseed + 2, 360f), Mathf.randomSeed(cseed + 3, spread));
                Tmp.v2.trns(Mathf.randomSeed(cseed + 4, 360f), drift * fin);

                float wz = rise * Interp.pow2Out.apply(fin);
                float wx = Tmp.v3.x + Tmp.v1.x + Tmp.v2.x, wy = Tmp.v3.y + Tmp.v1.y + Tmp.v2.y;
                float rad = Mathf.lerp(size, sizeTo, fin) * DGDraw3D.scale(wz);

                float a = alpha * Mathf.slope(Mathf.clamp(fin * 1.5f)) * prog;
                Tmp.c1.set(color).lerp(colorTo, fin).mulA(a);
                Tmp.c2.set(Tmp.c1).a(0f);
                Fill.light(DGDraw3D.x(wx, wz), DGDraw3D.y(wy, wz), 12, rad, Tmp.c1, Tmp.c2);
            }
        }

        Draw.blend();
        Draw.color();
        Draw.z(pz);
    }

    @Override
    public void load(String name){
    }
}
