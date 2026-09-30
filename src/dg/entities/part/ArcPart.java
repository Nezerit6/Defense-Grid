package dg.entities.part;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import dg.graphics.DGDraw3D;
import mindustry.entities.part.DrawPart;
import mindustry.graphics.*;

/** Crackling electric arc between two points of the turret, which may be at different heights. */
public class ArcPart extends DrawPart{
    public float x1, y1, z1, x2, y2, z2;
    public boolean mirror = false;
    public int segments = 4;
    /** Max sideways offset of each joint. */
    public float jitter = 1f;
    /** Ticks between re-rolling the shape. */
    public float interval = 3f;
    /** Chance of the arc being visible at full progress. */
    public float chance = 0.6f;
    public float stroke = 0.9f;
    public Color color = Pal.lancerLaser.cpy(), coreColor = Color.white.cpy();
    public PartProgress progress = PartProgress.warmup;
    public float layer = Layer.effect;

    private static final float[] points = new float[64];

    @Override
    public void draw(PartParams params){
        float prog = progress.getClamp(params);
        if(prog <= 0.001f) return;

        float pz = Draw.z();
        Draw.z(layer);

        int len = mirror && params.sideOverride == -1 ? 2 : 1;
        int tick = (int)(Time.time / interval);
        int segs = Math.min(segments, points.length / 2 - 1);

        for(int s = 0; s < len; s++){
            int i = params.sideOverride == -1 ? s : params.sideOverride;
            float sign = (i == 0 ? 1 : -1) * params.sideMultiplier;
            long seed = tick * 131L + i * 17L + (long)Mathf.round(params.x) * 7919L + (long)Mathf.round(params.y) * 104729L + (long)(x1 * 13f + y2 * 29f);

            if(Mathf.randomSeed(seed) > chance * prog) continue;

            Tmp.v1.set(x1 * sign, y1).rotate(params.rotation - 90).add(params.x, params.y);
            Tmp.v2.set(x2 * sign, y2).rotate(params.rotation - 90).add(params.x, params.y);
            float nx = -(Tmp.v2.y - Tmp.v1.y), ny = Tmp.v2.x - Tmp.v1.x, nl = Mathf.len(nx, ny);
            if(nl > 0.0001f){
                nx /= nl;
                ny /= nl;
            }

            for(int j = 0; j <= segs; j++){
                float t = j / (float)segs, off = j == 0 || j == segs ? 0f : Mathf.randomSeedRange(seed + j * 3L, jitter);
                float wx = Mathf.lerp(Tmp.v1.x, Tmp.v2.x, t) + nx * off, wy = Mathf.lerp(Tmp.v1.y, Tmp.v2.y, t) + ny * off, wz = Mathf.lerp(z1, z2, t);
                points[j * 2] = DGDraw3D.x(wx, wz);
                points[j * 2 + 1] = DGDraw3D.y(wy, wz);
            }

            Draw.color(color, prog);
            Lines.stroke(stroke * 1.8f);
            for(int j = 0; j < segs; j++){
                Lines.line(points[j * 2], points[j * 2 + 1], points[j * 2 + 2], points[j * 2 + 3]);
            }
            Draw.color(coreColor, prog);
            Lines.stroke(stroke * 0.7f);
            for(int j = 0; j < segs; j++){
                Lines.line(points[j * 2], points[j * 2 + 1], points[j * 2 + 2], points[j * 2 + 3]);
            }

            Drawf.light(points[0], points[1], points[segs * 2], points[segs * 2 + 1], stroke * 12f, color, 0.5f * prog);
        }

        Lines.stroke(1f);
        Draw.color();
        Draw.z(pz);
    }

    @Override
    public void load(String name){
    }
}
