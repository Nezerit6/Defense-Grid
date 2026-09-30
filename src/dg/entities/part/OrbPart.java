package dg.entities.part;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import dg.graphics.DGDraw3D;
import mindustry.entities.part.DrawPart;
import mindustry.graphics.*;

public class OrbPart extends DrawPart{
    public float x, y, z;
    public float lift = 0f;
    public PartProgress liftProgress = PartProgress.warmup;
    public float radius = 2f, radiusTo = -1f;
    public float coreScl = 0.5f;
    public float pulseScl = 0f, pulseMag = 0f;
    public Color color = Pal.lancerLaser.cpy(), coreColor = Color.white.cpy();
    public PartProgress progress = PartProgress.warmup;
    public PartProgress alphaProgress = PartProgress.constant(1f);
    public float spikes = 0f, spikeRotateSpeed = 1f;
    public float pool = 2.5f, poolAlpha = 0.3f;
    public float lightRadius = 3f, lightOpacity = 0.6f;
    public boolean mirror = false;
    public float layer = Layer.effect;

    @Override
    public void draw(PartParams params){
        float prog = progress.getClamp(params), alpha = alphaProgress.getClamp(params);
        float rad = (radiusTo < 0 ? radius : Mathf.lerp(radius, radiusTo, prog)) * (pulseScl > 0f ? 1f + Mathf.absin(pulseScl, pulseMag) : 1f);
        if(rad <= 0.01f || alpha <= 0.001f) return;

        float pz = Draw.z(), h = z + lift * liftProgress.getClamp(params);
        int len = mirror && params.sideOverride == -1 ? 2 : 1;

        for(int s = 0; s < len; s++){
            int i = params.sideOverride == -1 ? s : params.sideOverride;
            float sign = (i == 0 ? 1 : -1) * params.sideMultiplier;
            Tmp.v1.set(x * sign, y).rotate(params.rotation - 90);

            float
                rx = params.x + Tmp.v1.x,
                ry = params.y + Tmp.v1.y,
                px = DGDraw3D.x(rx, h),
                py = DGDraw3D.y(ry, h),
                r = rad * DGDraw3D.scale(h);

            if(pool > 0f){
                Draw.z(Layer.turretHeat);
                Draw.blend(Blending.additive);
                Fill.light(rx, ry, 16, rad * pool, Tmp.c1.set(color).mulA(poolAlpha * alpha), Tmp.c2.set(color).a(0f));
                Draw.blend();
            }

            Draw.z(layer);
            Draw.color(color, alpha);
            Fill.circle(px, py, r);
            if(spikes > 0f){
                for(int j = 0; j < 4; j++){
                    Drawf.tri(px, py, r * 0.7f, spikes * r, j * 90f + Time.time * spikeRotateSpeed * sign);
                }
            }
            Draw.color(coreColor, alpha);
            Fill.circle(px, py, r * coreScl);
            Draw.color();

            Drawf.light(px, py, r * lightRadius * 4f, color, lightOpacity * alpha);
        }

        Draw.z(pz);
    }

    @Override
    public void load(String name){
    }
}
