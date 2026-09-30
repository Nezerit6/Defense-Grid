package dg.entities.part;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import dg.graphics.DGDraw3D;
import mindustry.entities.part.DrawPart;
import mindustry.graphics.*;

public class PrismPart extends DrawPart{
    public float[] verts = {};
    public float x, y, rotation, z0 = 0f, z1 = 2f;
    public float moveX, moveY, moveRot, lift;
    public PartProgress progress = PartProgress.warmup, recoilProgress = PartProgress.recoil;
    public float recoilY = 0f;
    public boolean mirror = false;
    public Color top = Color.valueOf("7b7b7b"), light = Color.valueOf("989aa4"), dark = Color.valueOf("3a3b44"), outlineColor = Color.valueOf("404049");
    public float outlineStroke = 1.1f;
    public float inset = 0.18f;
    public Color insetColor;
    public @Nullable Color glow;
    public PartProgress glowProgress = PartProgress.heat;
    public float shadowAlpha = 1f;
    public float layer = -1f, layerOffset = 0f;

    private static final float[] base = new float[64], tops = new float[64], world = new float[64], tmp = new float[64];

    public static float[] regular(int sides, float radius, float angle){
        float[] out = new float[sides * 2];
        for(int i = 0; i < sides; i++){
            out[i * 2] = Angles.trnsx(angle + i * 360f / sides, radius);
            out[i * 2 + 1] = Angles.trnsy(angle + i * 360f / sides, radius);
        }
        return out;
    }

    public static float[] rect(float x, float y, float w, float h){
        return new float[]{x - w / 2f, y - h / 2f, x + w / 2f, y - h / 2f, x + w / 2f, y + h / 2f, x - w / 2f, y + h / 2f};
    }

    @Override
    public void draw(PartParams params){
        int n = Math.min(verts.length / 2, 32);
        if(n < 3) return;

        float pz = Draw.z();
        if(layer > 0) Draw.z(layer);
        if(under && turretShading) Draw.z(Draw.z() - 0.0001f);
        Draw.z(Draw.z() + layerOffset);
        float z = Draw.z();

        float prog = progress.getClamp(params), rec = recoilProgress.getClamp(params);
        float mx = moveX * prog, my = moveY * prog - recoilY * rec, mr = moveRot * prog + rotation;
        float h0 = z0 + lift * prog, h1 = z1 + lift * prog;

        int len = mirror && params.sideOverride == -1 ? 2 : 1;
        for(int s = 0; s < len; s++){
            int side = params.sideOverride == -1 ? s : params.sideOverride;
            float sign = (side == 0 ? 1 : -1) * params.sideMultiplier;
            float rot = params.rotation - 90f;

            for(int i = 0; i < n; i++){
                int j = sign > 0 ? i : n - 1 - i;
                Tmp.v1.set(verts[j * 2], verts[j * 2 + 1]).rotate(mr).add(x + mx, y + my);
                Tmp.v1.x *= sign;
                Tmp.v1.rotate(rot).add(params.x, params.y);
                world[i * 2] = Tmp.v1.x;
                world[i * 2 + 1] = Tmp.v1.y;
                base[i * 2] = DGDraw3D.x(Tmp.v1.x, h0);
                base[i * 2 + 1] = DGDraw3D.y(Tmp.v1.y, h0);
                tops[i * 2] = DGDraw3D.x(Tmp.v1.x, h1);
                tops[i * 2 + 1] = DGDraw3D.y(Tmp.v1.y, h1);
            }

            float area = 0f, bcx = 0f, bcy = 0f, tcx = 0f, tcy = 0f;
            for(int i = 0; i < n; i++){
                int k = (i + 1) % n;
                area += world[i * 2] * world[k * 2 + 1] - world[k * 2] * world[i * 2 + 1];
                bcx += base[i * 2];
                bcy += base[i * 2 + 1];
                tcx += tops[i * 2];
                tcy += tops[i * 2 + 1];
            }
            float orient = Math.signum(area), dx = (tcx - bcx) / n, dy = (tcy - bcy) / n;

            if(shadowAlpha > 0f){
                float off = DGDraw3D.shadowOffset(h1) + 0.6f;
                for(int i = 0; i < n; i++){
                    tmp[i * 2] = world[i * 2] - off;
                    tmp[i * 2 + 1] = world[i * 2 + 1] - off;
                }
                Draw.z(Layer.turret - 0.4f);
                Draw.color(Pal.shadow, Pal.shadow.a * shadowAlpha);
                Fill.poly(tmp, n * 2);
            }

            Draw.z(z - 0.0005f);
            Draw.color(outlineColor);
            Lines.stroke(outlineStroke);
            for(int i = 0; i < n; i++){
                int k = (i + 1) % n;
                Lines.line(base[i * 2], base[i * 2 + 1], base[k * 2], base[k * 2 + 1], false);
                Lines.line(tops[i * 2], tops[i * 2 + 1], tops[k * 2], tops[k * 2 + 1], false);
                Lines.line(base[i * 2], base[i * 2 + 1], tops[i * 2], tops[i * 2 + 1], false);
                Fill.circle(tops[i * 2], tops[i * 2 + 1], outlineStroke / 2f);
                Fill.circle(base[i * 2], base[i * 2 + 1], outlineStroke / 2f);
            }
            Fill.poly(base, n * 2);

            Draw.z(z);
            for(int pass = 0; pass < 2; pass++){
                for(int i = 0; i < n; i++){
                    int k = (i + 1) % n;
                    float ex = world[k * 2] - world[i * 2], ey = world[k * 2 + 1] - world[i * 2 + 1];
                    float nx = ey * orient, ny = -ex * orient;
                    boolean front = nx * dx + ny * dy < 0f;
                    if(front != (pass == 1)) continue;

                    float shade = (Mathf.cosDeg(Angles.angle(nx, ny) - DGDraw3D.lightAngle) + 1f) / 2f;
                    float c = Tmp.c1.set(dark).lerp(light, shade).toFloatBits();
                    Fill.quad(base[i * 2], base[i * 2 + 1], c, base[k * 2], base[k * 2 + 1], c, tops[k * 2], tops[k * 2 + 1], c, tops[i * 2], tops[i * 2 + 1], c);
                }
            }

            Draw.color(top);
            Fill.poly(tops, n * 2);

            if(inset > 0f){
                for(int i = 0; i < n; i++){
                    tmp[i * 2] = Mathf.lerp(tops[i * 2], tcx / n, inset);
                    tmp[i * 2 + 1] = Mathf.lerp(tops[i * 2 + 1], tcy / n, inset);
                }
                Draw.color(insetColor != null ? insetColor : Tmp.c1.set(top).lerp(light, 0.35f));
                Fill.poly(tmp, n * 2);
            }

            if(glow != null){
                float g = glowProgress.getClamp(params);
                if(g > 0.001f){
                    Draw.z(Layer.turretHeat);
                    Draw.blend(Blending.additive);
                    Draw.color(glow, g * glow.a);
                    Fill.poly(tops, n * 2);
                    Draw.blend();
                    Drawf.light(tcx / n, tcy / n, 12f, glow, 0.5f * g);
                    Draw.z(z);
                }
            }
        }

        Draw.color();
        Draw.z(pz);
    }

    @Override
    public void load(String name){
    }
}
