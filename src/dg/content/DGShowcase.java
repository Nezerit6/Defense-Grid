package dg.content;

import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import dg.graphics.*;
import mindustry.entities.*;
import mindustry.entities.effect.*;
import mindustry.graphics.*;

import static arc.graphics.g2d.Draw.*;
import static arc.graphics.g2d.Lines.*;
import static arc.math.Angles.*;

public class DGShowcase{
    private static final Rand rand = new Rand(), rand2 = new Rand();
    private static float px, py, ps;
    private static final float g = 0.12f;

    public static final OrderedMap<String, Effect> all = new OrderedMap<>();

    static void p(float x, float y, float z){
        px = DGDraw3D.x(x, z);
        py = DGDraw3D.y(y, z);
        ps = DGDraw3D.scale(z);
    }

    static void glow(float x, float y, float r, Color c, float a){
        Draw.blend(Blending.additive);
        Fill.light(x, y, 20, r, Tmp.c1.set(c).a(a), Tmp.c2.set(c).a(0f));
        Draw.blend();
    }

    static void shadow(float x, float y, float r, float a){
        float z0 = Draw.z();
        z(Layer.floor + 0.5f);
        color(Pal.shadow, a);
        Fill.circle(x, y, r);
        z(z0);
    }

    static void bolt(float x1, float y1, float x2, float y2, float jag, int links, float width){
        float lx = x1, ly = y1;
        stroke(width);
        for(int i = 1; i <= links; i++){
            float f = i / (float)links, nx = Mathf.lerp(x1, x2, f), ny = Mathf.lerp(y1, y2, f);
            if(i < links){
                nx += rand2.range(jag);
                ny += rand2.range(jag);
            }
            line(lx, ly, nx, ny);
            lx = nx;
            ly = ny;
        }
    }

    static Effect add(String name, float life, float clip, Cons<Effect.EffectContainer> r){
        Effect e = new Effect(life, clip, r);
        all.put(name, e);
        return e;
    }

    static void add(String name, Effect e){
        all.put(name, e);
    }

    public static void load(){
        Color fire = Color.valueOf("ffb05a"), fireDark = Color.valueOf("d0482a"), smoke = Color.valueOf("6e7080");

        add("Meteor impact", 160f, 400f, e -> {
            float fall = 40f;
            if(e.time < fall){
                float f = e.time / fall, z = 260f * (1f - f) * (1f - f), ox = -120f * (1f - f);
                for(int i = 0; i < 14; i++){
                    float tf = Math.max(f - i * 0.018f, 0f), tz = 260f * (1f - tf) * (1f - tf), tox = -120f * (1f - tf);
                    p(e.x + tox, e.y + tox * 0.4f, tz);
                    color(Color.white, fireDark, i / 14f);
                    alpha(1f - i / 14f);
                    Fill.circle(px, py, (5f - i * 0.3f) * ps);
                }
                p(e.x + ox, e.y + ox * 0.4f, z);
                glow(px, py, 22f * ps, fire, 0.9f);
                shadow(e.x + ox - z * 0.3f, e.y + ox * 0.4f - z * 0.3f, 6f * f, 0.4f * f);
                return;
            }
            float t = e.time - fall, life = e.lifetime - fall, fin = t / life, fout = 1f - fin;
            if(t < 8f) glow(e.x, e.y, 90f * (1f - t / 8f), Color.white, 1f);
            color(Color.white, fire, Mathf.clamp(t / 20f));
            stroke(4f * fout);
            Lines.circle(e.x, e.y, 10f + 90f * Interp.pow3Out.apply(Mathf.clamp(t / 35f)));
            z(Layer.floor + 0.6f);
            color(Color.valueOf("2a2328"), 0.7f * Mathf.clamp(fout * 2f));
            Fill.circle(e.x, e.y, 18f);
            color(fireDark, 0.6f * fout);
            Fill.circle(e.x, e.y, 10f);
            z(Layer.effect);
            rand.setSeed(e.id);
            for(int i = 0; i < 30; i++){
                float ang = rand.random(360f), hs = 0.6f + rand.random(2.4f), vz = 1f + rand.random(2.5f);
                float tt = t * 0.8f, d = hs * tt, z = Math.max(vz * tt - g * tt * tt / 2f, 0f);
                p(e.x + trnsx(ang, d), e.y + trnsy(ang, d), z);
                color(fire, Color.valueOf("3a3438"), fin);
                Fill.square(px, py, (1f + rand.random(1.6f)) * ps * Mathf.clamp(fout * 2f), ang + t * 6f);
            }
            for(int i = 0; i < 16; i++){
                float ang = rand.random(360f), rise = 20f + rand.random(40f), lf = Mathf.clamp(t / (life * rand.random(0.6f, 1f)));
                p(e.x + trnsx(ang, 18f * lf), e.y + trnsy(ang, 18f * lf), rise * Interp.pow2Out.apply(lf));
                color(Color.valueOf("8b8c95"), smoke, lf);
                alpha(0.6f * (1f - lf));
                Fill.circle(px, py, (5f + 12f * lf) * ps);
            }
            Drawf.light(e.x, e.y, 160f * fout, fire, 0.9f);
        });

        add("Nuke", 300f, 600f, e -> {
            float fin = e.fin(), fout = e.fout();
            if(e.time < 12f){
                glow(e.x, e.y, 400f * (1f - e.time / 12f), Color.white, 1f);
            }
            e.scaled(70f, s -> {
                color(Color.white, fire, s.fin());
                stroke(6f * s.fout());
                Lines.circle(e.x, e.y, 20f + 220f * s.finpow());
                stroke(2f * s.fout());
                Lines.circle(e.x, e.y, 10f + 160f * s.finpow());
            });
            rand.setSeed(e.id);
            float stem = 140f * Interp.pow2Out.apply(Mathf.clamp(e.time / 120f));
            for(int i = 0; i < 40; i++){
                float h = rand.random(1f) * stem, wob = rand.range(8f) + Mathf.sin(e.time * 0.05f + i, 1f, 3f);
                p(e.x + wob, e.y + rand.range(6f), h);
                color(fire, smoke, Mathf.clamp(e.time / 160f + rand.random(0.3f)));
                alpha(0.75f * fout);
                Fill.circle(px, py, (6f + 4f * fin) * ps);
            }
            for(int i = 0; i < 70; i++){
                float ang = rand.random(360f), rr = rand.random(1f), cap = stem + 10f;
                float rad = (20f + 45f * Interp.pow2Out.apply(Mathf.clamp(e.time / 150f))) * Mathf.sqrt(rr);
                float z = cap + Mathf.sin(ang * 3f) * 6f + rand.range(10f) - rr * 14f;
                float rot = ang + e.time * 0.3f * (1f - rr);
                p(e.x + trnsx(rot, rad), e.y + trnsy(rot, rad), z);
                color(Color.white, fire, Mathf.clamp(e.time / 40f));
                Tmp.c3.set(fire).lerp(fireDark, rr).lerp(smoke, Mathf.clamp(e.time / 220f));
                color(Tmp.c3, 0.8f * fout);
                Fill.circle(px, py, (8f + 8f * fin) * ps);
            }
            for(int i = 0; i < 40; i++){
                float ang = rand.random(360f), d = 30f + 180f * Interp.pow3Out.apply(Mathf.clamp(e.time / 90f)) * rand.random(0.7f, 1f);
                color(Color.valueOf("8b8c95"), smoke, fin);
                alpha(0.45f * fout);
                Fill.circle(e.x + trnsx(ang, d), e.y + trnsy(ang, d), 10f + 10f * fin);
            }
            Drawf.light(e.x, e.y, 500f * fout, fire, 1f);
        });

        add("Sky lightning", 40f, 400f, e -> {
            rand2.setSeed(e.id + (int)(e.time / 3f));
            float a = e.time < 6f ? 1f : e.fout();
            for(int k = 0; k < 2; k++){
                float lx = e.x, ly = e.y;
                int links = 14;
                for(int i = 1; i <= links; i++){
                    float f = i / (float)links, z = 300f * f;
                    float nx = e.x + rand2.range(12f) * f, ny = e.y + rand2.range(12f) * f;
                    p(nx, ny, z);
                    color(Color.white, Color.valueOf("9fc8ff"), k);
                    alpha(a);
                    stroke((k == 0 ? 3.5f : 8f) * (1f - f * 0.5f) * a);
                    if(k == 1){
                        Draw.blend(Blending.additive);
                        alpha(0.25f * a);
                    }
                    p(lx, ly, 300f * (i - 1) / links);
                    float ox = px, oy = py;
                    p(nx, ny, z);
                    line(ox, oy, px, py);
                    Draw.blend();
                    lx = nx;
                    ly = ny;
                    if(k == 0 && rand2.chance(0.25f)){
                        float bx = nx + rand2.range(30f), by = ny + rand2.range(30f);
                        p(bx, by, z + rand2.random(-30f, 10f));
                        stroke(1.2f * a);
                        float bxp = px, byp = py;
                        p(nx, ny, z);
                        line(px, py, bxp, byp);
                    }
                }
            }
            glow(e.x, e.y, 60f * a, Color.valueOf("9fc8ff"), a);
            e.scaled(20f, s -> {
                color(Color.white, Color.valueOf("9fc8ff"), s.fin());
                stroke(2f * s.fout());
                Lines.circle(e.x, e.y, 6f + 40f * s.finpow());
            });
            Drawf.light(e.x, e.y, 200f * a, Color.valueOf("9fc8ff"), 1f);
        });

        add("Portal", 240f, 200f, e -> {
            float open = Interp.pow3Out.apply(Mathf.clamp(e.time / 40f)) * Interp.pow3Out.apply(Mathf.clamp((e.lifetime - e.time) / 40f));
            Color a = Color.valueOf("9b5cff"), b = Color.valueOf("4de1ff");
            for(int i = 0; i < 5; i++){
                float r = 34f * open * (1f - i * 0.15f);
                color(a, b, i / 5f);
                stroke(2.5f * open);
                Lines.arc(e.x, e.y, r, 0.6f, e.time * (3f + i) * (i % 2 == 0 ? 1f : -1f));
            }
            glow(e.x, e.y, 40f * open, a, 0.8f);
            color(Color.valueOf("0b0614"), open);
            Fill.circle(e.x, e.y, 14f * open);
            rand.setSeed(e.id);
            for(int i = 0; i < 40; i++){
                float off = rand.random(60f), f = ((e.time + off) % 60f) / 60f, ang = rand.random(360f) + f * 220f;
                float d = 80f * (1f - Interp.pow2In.apply(f)) * open;
                color(b, Color.white, f);
                alpha(open * f);
                Fill.square(e.x + trnsx(ang, d), e.y + trnsy(ang, d), 1.4f * (1f - f) + 0.4f, ang);
            }
            Drawf.light(e.x, e.y, 120f * open, a, 0.9f);
        });

        add("Black hole", 300f, 300f, e -> {
            float open = Mathf.clamp(e.time / 50f) * Mathf.clamp((e.lifetime - e.time) / 50f);
            rand.setSeed(e.id);
            for(int i = 0; i < 160; i++){
                float r = 20f + rand.random(80f), sp = 180f / Mathf.sqrt(r), ang = rand.random(360f) + e.time * sp * 0.05f;
                float x = trnsx(ang, r), y = trnsy(ang, r) * 0.35f;
                float z = -y * 0.5f;
                p(e.x + x, e.y + y, Math.max(z, 0f));
                color(Color.white, Color.valueOf("ff7a3a"), (r - 20f) / 80f);
                alpha(open * (1f - (r - 20f) / 100f));
                Fill.square(px, py, 1.2f * ps, ang);
            }
            color(Color.black, open);
            Fill.circle(e.x, e.y, 16f * open);
            color(Color.valueOf("ffd9a0"), open);
            stroke(1.5f);
            Lines.circle(e.x, e.y, 17f * open);
            glow(e.x, e.y, 30f * open, Color.valueOf("ff7a3a"), 0.5f * open);
        });

        add("Fireworks", 150f, 400f, e -> {
            rand.setSeed(e.id);
            Color[] cols = {Color.valueOf("ff5f7a"), Color.valueOf("ffd166"), Color.valueOf("6ee7ff"), Color.valueOf("a78bfa"), Color.valueOf("7cff8a")};
            for(int k = 0; k < 4; k++){
                float delay = k * 12f, t = e.time - delay;
                if(t < 0f) continue;
                float bx = e.x + rand.range(50f), by = e.y + rand.range(30f), bz = 120f + rand.random(60f), up = 35f;
                Color c = cols[rand.random(cols.length - 1)];
                if(t < up){
                    float z = bz * Interp.pow2Out.apply(t / up);
                    p(bx, by, z);
                    color(Color.white);
                    Fill.circle(px, py, 1.5f * ps);
                    color(fire, 0.5f);
                    for(int i = 1; i < 6; i++){
                        p(bx, by, bz * Interp.pow2Out.apply(Math.max(t - i * 2f, 0f) / up));
                        Fill.circle(px, py, (1.2f - i * 0.15f) * ps);
                    }
                    continue;
                }
                float tt = t - up, life = 80f;
                if(tt > life) continue;
                float f = tt / life;
                if(tt < 6f) glow(DGDraw3D.x(bx, bz), DGDraw3D.y(by, bz), 60f * DGDraw3D.scale(bz) * (1f - tt / 6f), c, 1f);
                for(int i = 0; i < 36; i++){
                    float ang = i * 10f + rand.range(4f), sp = 1.6f + rand.random(0.5f);
                    float d = sp * 30f * (1f - Mathf.pow(0.96f, tt)), z = bz - 0.02f * tt * tt;
                    for(int j = 0; j < 3; j++){
                        float tj = Math.max(tt - j * 3f, 0f), dj = sp * 30f * (1f - Mathf.pow(0.96f, tj)), zj = bz - 0.02f * tj * tj;
                        p(bx + trnsx(ang, dj), by + trnsy(ang, dj), Math.max(zj, 0f));
                        color(Color.white, c, f + j * 0.2f);
                        alpha((1f - f) * (1f - j * 0.3f) * (rand.chance(0.5f) ? 1f : Mathf.absin(tt, 2f, 1f)));
                        Fill.circle(px, py, 1.3f * ps * (1f - j * 0.25f));
                    }
                }
                Drawf.light(DGDraw3D.x(bx, bz), DGDraw3D.y(by, bz), 150f * (1f - f), c, 0.9f);
            }
        });

        add("Tornado", 360f, 300f, e -> {
            float a = Mathf.clamp(e.time / 40f) * Mathf.clamp((e.lifetime - e.time) / 50f);
            float wx = e.x + Mathf.sin(e.time, 40f, 20f), wy = e.y + Mathf.sin(e.time + 30f, 55f, 12f);
            rand.setSeed(e.id);
            for(int i = 0; i < 90; i++){
                float h = rand.random(1f), z = h * 160f, r = 6f + h * h * 45f;
                float ang = rand.random(360f) + e.time * (14f - h * 6f);
                float cx = Mathf.lerp(wx, e.x, h * 0.4f);
                p(cx + trnsx(ang, r), e.y + trnsy(ang, r) * 0.45f, z);
                color(Color.valueOf("a8a49a"), Color.valueOf("5b5850"), h);
                alpha(0.45f * a);
                Fill.circle(px, py, (3f + h * 6f) * ps);
            }
            for(int i = 0; i < 20; i++){
                float h = rand.random(0.1f, 0.8f), ang = rand.random(360f) + e.time * 9f, r = 10f + h * 40f;
                p(wx + trnsx(ang, r), wy + trnsy(ang, r) * 0.45f, h * 160f);
                color(Color.valueOf("4a3a2e"), a);
                Fill.square(px, py, 1.6f * ps, ang * 3f);
            }
            for(int i = 0; i < 12; i++){
                float ang = rand.random(360f) + e.time * 4f, d = 15f + rand.random(25f);
                color(Color.valueOf("8b7e6a"), 0.25f * a);
                Fill.circle(wx + trnsx(ang, d), wy + trnsy(ang, d) * 0.5f, 6f);
            }
        });

        add("Hex shield", 160f, 200f, e -> {
            float up = Interp.pow3Out.apply(Mathf.clamp(e.time / 30f)), a = Mathf.clamp((e.lifetime - e.time) / 40f);
            float R = 60f * up;
            Color c = Color.valueOf("7fd3ff");
            Draw.blend(Blending.additive);
            Fill.light(e.x, e.y, 40, R, Tmp.c1.set(c).a(0.04f * a), Tmp.c2.set(c).a(0.25f * a));
            Draw.blend();
            float hs = 9f;
            for(int ix = -8; ix <= 8; ix++){
                for(int iy = -8; iy <= 8; iy++){
                    float hx = ix * hs * 1.5f, hy = (iy + (ix % 2 == 0 ? 0f : 0.5f)) * hs * 1.732f;
                    float d = Mathf.len(hx, hy);
                    if(d > R - hs) continue;
                    float wave = Mathf.clamp(1f - Math.abs(d - (e.time * 2.2f % (R + 40f))) / 14f);
                    color(c, Color.white, wave);
                    alpha(a * (0.25f + 0.75f * wave) * (0.4f + 0.6f * d / R));
                    stroke(1f);
                    Lines.poly(e.x + hx, e.y + hy, 6, hs * 0.95f);
                }
            }
            color(Color.white, c, 0.5f);
            alpha(a);
            stroke(2f);
            Lines.circle(e.x, e.y, R);
            Drawf.light(e.x, e.y, R * 2f, c, 0.6f * a);
        });

        add("Teleport", 110f, 300f, e -> {
            Color c = Color.valueOf("8af2c0");
            float beam = Mathf.slope(Mathf.clamp(e.time / 60f));
            for(int i = 0; i < 3; i++){
                float w = (12f - i * 4f) * beam;
                p(e.x, e.y, 400f);
                Draw.blend(Blending.additive);
                color(i == 2 ? Color.white : c, i == 0 ? 0.25f : 0.6f);
                stroke(w);
                line(e.x, e.y, px, py);
                Draw.blend();
            }
            rand.setSeed(e.id);
            for(int i = 0; i < 5; i++){
                float f = ((e.time * 1.5f + i * 20f) % 100f) / 100f, z = f * 120f;
                p(e.x, e.y, z);
                color(c, Color.white, f);
                alpha(beam * (1f - f));
                stroke(1.5f * ps);
                Lines.circle(px, py, 14f * ps);
            }
            for(int i = 0; i < 30; i++){
                float ang = rand.random(360f), r = rand.random(4f, 14f), f = (e.time * rand.random(0.5f, 1.2f) / 100f + rand.random(1f)) % 1f;
                p(e.x + trnsx(ang + e.time * 4f, r), e.y + trnsy(ang + e.time * 4f, r), f * 160f);
                color(Color.white, c, f);
                alpha(beam);
                Fill.square(px, py, 1f * ps, 45f);
            }
            if(e.time > 55f && e.time < 70f) glow(e.x, e.y, 50f * (1f - (e.time - 55f) / 15f), Color.white, 1f);
            Drawf.light(e.x, e.y, 100f * beam, c, 0.9f);
        });

        add("Orbital strike", 140f, 400f, e -> {
            Color c = Color.valueOf("ff4f6a");
            if(e.time < 60f){
                float f = e.time / 60f;
                color(c, 0.4f + 0.6f * Mathf.absin(e.time, 2f, 1f));
                stroke(1.2f);
                Lines.circle(e.x, e.y, 40f * (1f - f) + 6f);
                for(int i = 0; i < 4; i++) Lines.lineAngle(e.x + trnsx(i * 90f + e.time * 3f, 46f * (1f - f) + 8f), e.y + trnsy(i * 90f + e.time * 3f, 46f * (1f - f) + 8f), i * 90f + e.time * 3f + 180f, 8f);
                p(e.x, e.y, 500f);
                Draw.blend(Blending.additive);
                color(c, 0.25f * f);
                stroke(2f * f);
                line(e.x, e.y, px, py);
                Draw.blend();
                return;
            }
            float t = e.time - 60f, a = 1f - Mathf.clamp((t - 40f) / 40f);
            p(e.x, e.y, 500f);
            Draw.blend(Blending.additive);
            for(int i = 0; i < 3; i++){
                color(i == 2 ? Color.white : c, i == 0 ? 0.3f : 0.8f);
                stroke((24f - i * 8f) * a * (1f + Mathf.absin(t, 1.5f, 0.15f)));
                line(e.x, e.y, px, py);
            }
            Draw.blend();
            glow(e.x, e.y, 60f * a, c, a);
            z(Layer.floor + 0.6f);
            color(Color.valueOf("1d1416"), 0.8f * Mathf.clamp(t / 10f) * Mathf.clamp((e.lifetime - e.time) / 30f + 0.3f));
            Fill.circle(e.x, e.y, 22f);
            z(Layer.effect);
            rand.setSeed(e.id);
            for(int i = 0; i < 24; i++){
                float ang = rand.random(360f), hs = 1f + rand.random(2f), vz = 1.5f + rand.random(2f), tt = (t + rand.random(40f)) % 40f;
                float d = hs * tt, z = Math.max(vz * tt - g * tt * tt / 2f, 0f);
                p(e.x + trnsx(ang, d), e.y + trnsy(ang, d), z);
                color(Color.white, c, tt / 40f);
                alpha(a);
                Fill.square(px, py, 1.4f * ps, ang);
            }
            Drawf.light(e.x, e.y, 220f * a, c, 1f);
        });

        add("Geyser", 200f, 300f, e -> {
            Color c = Color.valueOf("7fc8ff");
            rand.setSeed(e.id);
            float active = Mathf.clamp((e.lifetime - e.time - 60f) / 20f);
            for(int i = 0; i < 160; i++){
                float off = rand.random(80f), tt = (e.time + off) % 80f;
                if(e.time + off - tt > e.lifetime - 80f && tt > e.time) continue;
                float launched = e.time - tt;
                if(launched < 0f || launched > e.lifetime - 70f) continue;
                float ang = rand.random(360f), hs = rand.random(0.5f), vz = 3.5f + rand.random(1.5f);
                float d = hs * tt, z = vz * tt - g * tt * tt / 2f;
                if(z < 0f) continue;
                p(e.x + trnsx(ang, d), e.y + trnsy(ang, d), z);
                color(Color.white, c, z / 100f);
                alpha(0.85f);
                Fill.circle(px, py, (1.2f + rand.random(1f)) * ps);
            }
            for(int i = 0; i < 3; i++){
                float f = ((e.time + i * 15f) % 45f) / 45f;
                color(c, (1f - f) * 0.7f * active);
                stroke(1.5f);
                Lines.ellipse(e.x, e.y, 6f + 30f * f, 1f, 0.6f, 0f);
            }
        });

        add("EMP", 90f, 300f, e -> {
            Color c = Color.valueOf("6ad7ff");
            e.scaled(50f, s -> {
                Draw.blend(Blending.additive);
                Fill.light(e.x, e.y, 40, 140f * s.finpow(), Tmp.c1.set(c).a(0f), Tmp.c2.set(c).a(0.5f * s.fout()));
                Draw.blend();
                color(Color.white, c, s.fin());
                stroke(3f * s.fout());
                Lines.circle(e.x, e.y, 140f * s.finpow());
            });
            rand2.setSeed(e.id + (int)(e.time / 4f));
            color(Color.white, c, 0.4f);
            alpha(e.fout());
            for(int i = 0; i < 8; i++){
                float ang = rand2.random(360f), r1 = rand2.random(10f, 60f), r2 = r1 + rand2.random(20f, 60f);
                r1 *= e.finpow() * 2f;
                r2 *= e.finpow() * 2f;
                bolt(e.x + trnsx(ang, r1), e.y + trnsy(ang, r1), e.x + trnsx(ang + rand2.range(20f), r2), e.y + trnsy(ang + rand2.range(20f), r2), 6f, 5, 1.2f);
            }
            Drawf.light(e.x, e.y, 200f * e.fout(), c, 0.9f);
        });

        add("Sakura", 360f, 300f, e -> {
            rand.setSeed(e.id);
            for(int i = 0; i < 50; i++){
                float start = rand.random(200f), t = e.time - start;
                if(t < 0f || t > 160f) continue;
                float f = t / 160f, z = 140f * (1f - f), sway = Mathf.sin(t * 0.05f + i, 1f, 14f);
                p(e.x + rand.range(90f) + sway + f * 40f, e.y + rand.range(60f), z);
                float rot = t * 3f + i * 40f, flip = Math.abs(Mathf.cos(t * 0.08f + i));
                color(Color.valueOf("ffc2d6"), Color.valueOf("ff8fb3"), flip);
                alpha(Mathf.clamp((160f - t) / 20f));
                Fill.poly(px, py, 3, 2.4f * ps * (0.4f + 0.6f * flip), rot);
                Fill.poly(px, py, 3, 2.4f * ps * (0.4f + 0.6f * flip), rot + 60f);
                shadow(px - z * 0.4f, py - z * 0.4f, 1.6f, 0.15f);
            }
        });

        add("Confetti", 180f, 300f, e -> {
            rand.setSeed(e.id);
            Color[] cols = {Color.valueOf("ff5f7a"), Color.valueOf("ffd166"), Color.valueOf("6ee7ff"), Color.valueOf("a78bfa"), Color.valueOf("7cff8a")};
            for(int i = 0; i < 80; i++){
                float ang = 90f + rand.range(55f), sp = 2f + rand.random(3f), t = e.time;
                float drag = 1f - Mathf.pow(0.97f, t), d = sp / 0.03f * drag;
                float z = Math.max((sp * 1.6f) / 0.03f * drag - 0.4f * t, 0f) * 0.5f;
                float dx = trnsx(ang, d) * 0.4f + Mathf.sin(t * 0.1f + i, 1f, 6f), dy = rand.range(10f) + d * 0.1f;
                p(e.x + dx, e.y + dy, z);
                color(cols[i % cols.length]);
                alpha(Mathf.clamp((e.lifetime - t) / 30f));
                float spin = t * (6f + rand.random(10f));
                Fill.rect(px, py, 3f * ps, 1.6f * ps * Math.abs(Mathf.cosDeg(spin)), spin * 0.3f);
            }
        });

        add("Supernova", 220f, 500f, e -> {
            float fin = e.fin();
            Color[] cols = {Color.valueOf("ffffff"), Color.valueOf("ffe28a"), Color.valueOf("ff7a5a"), Color.valueOf("c35bff"), Color.valueOf("4d7dff")};
            if(e.time < 50f){
                float f = e.time / 50f;
                glow(e.x, e.y, 20f + 30f * Mathf.absin(e.time, 3f * (1f - f) + 0.5f, 1f), Color.valueOf("ffe28a"), 0.9f);
                return;
            }
            float t = e.time - 50f, life = e.lifetime - 50f, f = t / life;
            if(t < 10f) glow(e.x, e.y, 300f * (1f - t / 10f), Color.white, 1f);
            for(int i = 0; i < cols.length; i++){
                float r = (40f + i * 35f) * Interp.pow3Out.apply(Mathf.clamp(t / (40f + i * 20f)));
                Draw.blend(Blending.additive);
                Fill.light(e.x, e.y, 40, r, Tmp.c1.set(cols[i]).a(0f), Tmp.c2.set(cols[i]).a(0.35f * (1f - f)));
                Draw.blend();
            }
            rand.setSeed(e.id);
            for(int i = 0; i < 60; i++){
                float ang = rand.random(360f), d = 250f * Interp.pow2Out.apply(f) * rand.random(0.3f, 1f);
                color(Color.white, cols[i % cols.length], f);
                alpha(1f - f);
                stroke(1.4f);
                Lines.lineAngle(e.x + trnsx(ang, d), e.y + trnsy(ang, d), ang, 8f * (1f - f));
            }
            glow(e.x, e.y, 14f * (1f - f), Color.white, 1f);
            Drawf.light(e.x, e.y, 400f * (1f - fin), Color.valueOf("ffe28a"), 1f);
        });

        add("Scanner", 200f, 200f, e -> {
            Color c = Color.valueOf("4dffb0");
            float a = Mathf.clamp(e.time / 20f) * Mathf.clamp((e.lifetime - e.time) / 20f), S = 60f;
            stroke(1f);
            color(c, 0.2f * a);
            for(int i = -6; i <= 6; i++){
                line(e.x - S, e.y + i * 10f, e.x + S, e.y + i * 10f);
                line(e.x + i * 10f, e.y - S, e.x + i * 10f, e.y + S);
            }
            float sy = e.y - S + (e.time * 1.6f % (S * 2f));
            Draw.blend(Blending.additive);
            for(int i = 0; i < 10; i++){
                color(c, a * (1f - i / 10f) * 0.5f);
                stroke(2f);
                line(e.x - S, sy - i * 2f, e.x + S, sy - i * 2f);
            }
            Draw.blend();
            color(c, a);
            stroke(2f);
            for(int i = 0; i < 4; i++){
                float cx = e.x + (i % 2 == 0 ? -S : S), cy = e.y + (i < 2 ? -S : S), sx = i % 2 == 0 ? 1f : -1f, syy = i < 2 ? 1f : -1f;
                line(cx, cy, cx + 10f * sx, cy);
                line(cx, cy, cx, cy + 10f * syy);
            }
            rand.setSeed(e.id + (int)(e.time / 10f));
            for(int i = 0; i < 6; i++){
                float bx = e.x + rand.range(S - 6f), by = e.y + rand.range(S - 6f);
                if(Math.abs(by - sy) < 14f){
                    color(Color.white, c, 0.3f);
                    alpha(a);
                    Lines.square(bx, by, 3f + Mathf.absin(e.time, 2f, 2f), 45f);
                }
            }
        });

        add("Magic circle", 240f, 200f, e -> {
            Color c = Color.valueOf("ffcf5a");
            float a = Interp.pow2Out.apply(Mathf.clamp(e.time / 40f)) * Mathf.clamp((e.lifetime - e.time) / 30f);
            z(Layer.floor + 0.7f);
            color(c, a);
            stroke(1.5f);
            Lines.circle(e.x, e.y, 50f * a);
            Lines.circle(e.x, e.y, 44f * a);
            Lines.poly(e.x, e.y, 3, 44f * a, e.time);
            Lines.poly(e.x, e.y, 3, 44f * a, e.time + 60f);
            Lines.poly(e.x, e.y, 6, 22f * a, -e.time * 2f);
            Lines.circle(e.x, e.y, 16f * a);
            for(int i = 0; i < 12; i++){
                float ang = i * 30f - e.time * 0.6f;
                float rx = e.x + trnsx(ang, 47f * a), ry = e.y + trnsy(ang, 47f * a);
                Lines.square(rx, ry, 1.6f, ang + 45f);
                Lines.lineAngleCenter(rx, ry, ang, 2.5f);
            }
            z(Layer.effect);
            glow(e.x, e.y, 60f * a, c, 0.35f * a);
            rand.setSeed(e.id);
            for(int i = 0; i < 24; i++){
                float ang = rand.random(360f), r = rand.random(50f), f = (e.time / 90f + rand.random(1f)) % 1f;
                p(e.x + trnsx(ang, r), e.y + trnsy(ang, r), f * 70f);
                color(Color.white, c, f);
                alpha(a * (1f - f));
                Fill.square(px, py, 1.2f * ps, 45f);
            }
            Drawf.light(e.x, e.y, 130f * a, c, 0.8f);
        });

        add("Ice spikes", 200f, 200f, e -> {
            rand.setSeed(e.id);
            float a = Mathf.clamp((e.lifetime - e.time) / 30f);
            for(int i = 0; i < 18; i++){
                float ang = rand.random(360f), d = rand.random(8f, 60f), delay = d * 0.6f;
                float grow = Interp.swingOut.apply(Mathf.clamp((e.time - delay) / 18f));
                if(grow <= 0f) continue;
                float h = (16f + rand.random(30f)) * grow * a, bx = e.x + trnsx(ang, d), by = e.y + trnsy(ang, d), lean = rand.range(10f);
                p(bx + lean, by, h);
                float tx = px, ty = py;
                float w = (3f + rand.random(3f)) * grow;
                color(DGFx.cryoDark, a);
                Fill.tri(bx - w, by, bx + w, by, tx, ty);
                color(Color.valueOf("cfefff"), a);
                Fill.tri(bx - w * 0.2f, by, bx + w, by, tx, ty);
                color(Color.white, 0.8f * a);
                Fill.circle(tx, ty, 0.8f);
            }
            e.scaled(25f, s -> {
                color(Color.white, DGFx.cryo, s.fin());
                stroke(2f * s.fout());
                Lines.circle(e.x, e.y, 70f * s.finpow());
            });
        });

        add("Bubbles", 240f, 250f, e -> {
            rand.setSeed(e.id);
            for(int i = 0; i < 40; i++){
                float start = rand.random(140f), t = e.time - start;
                if(t < 0f || t > 100f) continue;
                float f = t / 100f, z = f * 160f, r = 2f + rand.random(4f);
                p(e.x + rand.range(40f) + Mathf.sin(t * 0.1f + i, 1f, 5f), e.y + rand.range(25f), z);
                if(f > 0.92f){
                    color(Color.white, (1f - f) * 10f);
                    stroke(1f);
                    for(int k = 0; k < 6; k++) Lines.lineAngle(px, py, k * 60f, r * ps * (1f + (f - 0.92f) * 20f));
                    continue;
                }
                color(Color.valueOf("bfe9ff"), 0.6f);
                stroke(1f * ps);
                Lines.circle(px, py, r * ps);
                color(Color.white, 0.8f);
                Fill.circle(px - r * 0.35f * ps, py + r * 0.35f * ps, r * 0.25f * ps);
            }
        });

        add("Glitch", 120f, 200f, e -> {
            rand2.setSeed(e.id + (int)(e.time / 4f));
            float a = e.fslope() * 2f;
            for(int i = 0; i < 18; i++){
                float w = rand2.random(4f, 40f), h = rand2.random(1f, 6f), x = e.x + rand2.range(50f), y = e.y + rand2.range(50f);
                color(i % 3 == 0 ? Color.valueOf("ff2a6a") : i % 3 == 1 ? Color.valueOf("2affd5") : Color.white);
                alpha(Mathf.clamp(a) * rand2.random(0.4f, 1f));
                Fill.rect(x, y, w, h);
            }
            color(Color.white, Mathf.clamp(a));
            stroke(1f);
            Lines.rect(e.x - 30f + rand2.range(4f), e.y - 20f, 60f, 40f);
        });

        add("Sonic boom", 60f, 300f, e -> {
            for(int i = 0; i < 6; i++){
                float f = Mathf.clamp((e.time - i * 4f) / 40f);
                if(f <= 0f || f >= 1f) continue;
                color(Color.white, (1f - f) * 0.8f);
                stroke(2.5f * (1f - f));
                float d = 10f + 140f * f;
                Lines.arc(e.x + trnsx(e.rotation, d * 0.3f), e.y + trnsy(e.rotation, d * 0.3f), d * 0.7f, 0.3f, e.rotation + 180f - 54f);
            }
            rand.setSeed(e.id);
            for(int i = 0; i < 20; i++){
                float ang = e.rotation + 180f + rand.range(40f), d = 120f * e.finpow() * rand.random(0.4f, 1f);
                color(Color.valueOf("d8dde6"), 0.4f * e.fout());
                Fill.circle(e.x + trnsx(ang, d), e.y + trnsy(ang, d), 3f + 6f * e.fin());
            }
        });

        add("Rain cloud", 300f, 300f, e -> {
            float a = Mathf.clamp(e.time / 40f) * Mathf.clamp((e.lifetime - e.time) / 40f);
            rand.setSeed(e.id);
            for(int i = 0; i < 26; i++){
                p(e.x + rand.range(55f) + Mathf.sin(e.time * 0.02f + i, 1f, 4f), e.y + rand.range(20f), 110f + rand.range(10f));
                color(Color.valueOf("7b7f8c"), Color.valueOf("4b4e58"), rand.random(1f));
                alpha(0.7f * a);
                Fill.circle(px, py, (10f + rand.random(8f)) * ps);
            }
            for(int i = 0; i < 70; i++){
                float off = rand.random(40f), tt = (e.time + off) % 40f, z = 110f * (1f - tt / 40f);
                float dx = rand.range(55f), dy = rand.range(20f);
                p(e.x + dx, e.y + dy, z);
                float tx = px, ty = py;
                p(e.x + dx, e.y + dy, z + 8f);
                color(Color.valueOf("a8d8ff"), 0.7f * a);
                stroke(0.8f);
                line(px, py, tx, ty);
                if(tt > 36f){
                    color(Color.valueOf("a8d8ff"), 0.6f * a);
                    Lines.ellipse(e.x + dx, e.y + dy, 2f + (tt - 36f), 1f, 0.6f, 0f);
                }
            }
            if(rand2.chance(0.01f * a)) glow(e.x, e.y, 80f, Color.white, 0.6f);
        });

        add("Hearts", 200f, 250f, e -> {
            rand.setSeed(e.id);
            for(int i = 0; i < 24; i++){
                float start = rand.random(100f), t = e.time - start;
                if(t < 0f || t > 100f) continue;
                float f = t / 100f, z = f * 120f, s = (2f + rand.random(2f)) * Interp.swingOut.apply(Mathf.clamp(t / 20f));
                p(e.x + rand.range(40f) + Mathf.sin(t * 0.08f + i, 1f, 8f), e.y + rand.range(20f), z);
                color(Color.valueOf("ff5f7a"), Color.valueOf("ffb3c6"), f);
                alpha(1f - Interp.pow3In.apply(f));
                float r = s * ps;
                Fill.circle(px - r * 0.5f, py + r * 0.3f, r * 0.6f);
                Fill.circle(px + r * 0.5f, py + r * 0.3f, r * 0.6f);
                Fill.tri(px - r * 1.08f, py + r * 0.15f, px + r * 1.08f, py + r * 0.15f, px, py - r * 1.2f);
            }
        });

        add("Galaxy", 400f, 300f, e -> {
            float a = Mathf.clamp(e.time / 60f) * Mathf.clamp((e.lifetime - e.time) / 60f);
            rand.setSeed(e.id);
            for(int i = 0; i < 300; i++){
                int arm = i % 3;
                float r = rand.random(1f), rr = r * 90f, ang = arm * 120f + r * 280f + rand.range(18f) * (1f - r * 0.5f) + e.time * (0.6f - r * 0.4f);
                color(Color.white, arm == 0 ? Color.valueOf("8ab4ff") : arm == 1 ? Color.valueOf("c79bff") : Color.valueOf("ffb38a"), r);
                alpha(a * (1f - r * 0.6f) * (0.5f + 0.5f * Mathf.absin(e.time + i * 7f, 4f, 1f)));
                Fill.square(e.x + trnsx(ang, rr), e.y + trnsy(ang, rr) * 0.6f, 0.9f + (1f - r), 45f);
            }
            glow(e.x, e.y, 30f * a, Color.valueOf("fff1c2"), a);
        });

        add("DNA helix", 260f, 250f, e -> {
            float a = Mathf.clamp(e.time / 30f) * Mathf.clamp((e.lifetime - e.time) / 30f);
            for(int i = 0; i < 30; i++){
                float h = i * 5f, ang = h * 6f + e.time * 4f;
                float x1 = trnsx(ang, 10f), x2 = trnsx(ang + 180f, 10f), d1 = trnsy(ang, 1f), d2 = -d1;
                p(e.x + x1, e.y + d1 * 6f, h);
                float ax = px, ay = py, as = ps;
                p(e.x + x2, e.y + d2 * 6f, h);
                color(Color.valueOf("8af2c0"), 0.35f * a);
                stroke(0.8f);
                line(ax, ay, px, py);
                color(Color.valueOf("5ab0ff"), Color.white, (d1 + 1f) / 2f);
                alpha(a);
                Fill.circle(ax, ay, 2f * as);
                color(Color.valueOf("ff6aa8"), Color.white, (d2 + 1f) / 2f);
                alpha(a);
                Fill.circle(px, py, 2f * ps);
            }
        });

        add("Lightning tree", 50f, 300f, e -> {
            rand2.setSeed(e.id);
            color(Color.white, Color.valueOf("c49bff"), e.fin());
            alpha(e.fout());
            branch(e.x, e.y, 90f + rand2.range(10f), 70f, 2.4f * e.fout() + 0.4f, 5);
            glow(e.x, e.y, 40f * e.fout(), Color.valueOf("c49bff"), e.fout());
        });

        add("Chain blasts", 140f, 400f, e -> {
            rand.setSeed(e.id);
            float ang0 = rand.random(360f);
            for(int i = 0; i < 8; i++){
                float t = e.time - i * 9f;
                if(t < 0f || t > 50f) continue;
                float bx = e.x + trnsx(ang0 + i * 25f, i * 18f), by = e.y + trnsy(ang0 + i * 25f, i * 18f), f = t / 50f;
                if(t < 5f) glow(bx, by, 40f * (1f - t / 5f), Color.white, 1f);
                color(fire, fireDark, f);
                stroke(3f * (1f - f));
                Lines.circle(bx, by, 4f + 26f * Interp.pow3Out.apply(f));
                for(int k = 0; k < 6; k++){
                    float a = rand.random(360f), d = 22f * Interp.pow2Out.apply(f) * rand.random(0.5f, 1f);
                    p(bx + trnsx(a, d), by + trnsy(a, d), 20f * f * rand.random(1f));
                    color(fire, smoke, f);
                    alpha(0.8f * (1f - f));
                    Fill.circle(px, py, (5f + 6f * f) * ps);
                }
                Drawf.light(bx, by, 80f * (1f - f), fire, 0.9f);
            }
        });

        add("Dragon breath", 120f, 300f, e -> {
            rand.setSeed(e.id);
            float emit = Mathf.clamp((90f - e.time) / 10f);
            for(int i = 0; i < 90; i++){
                float off = rand.random(30f), t = (e.time + off) % 30f, born = e.time - t;
                if(born < 0f || born > 90f) continue;
                float f = t / 30f, ang = e.rotation + rand.range(14f) + Mathf.sin(born * 0.2f, 1f, 6f), d = 140f * Interp.pow2Out.apply(f);
                p(e.x + trnsx(ang, d), e.y + trnsy(ang, d), 8f + 20f * f * f);
                color(Color.white, fire, Mathf.clamp(f * 3f));
                Tmp.c3.set(Draw.getColor()).lerp(smoke, Mathf.clamp((f - 0.6f) * 2.5f));
                color(Tmp.c3, (1f - f));
                Fill.circle(px, py, (2f + 12f * f) * ps);
            }
            glow(e.x, e.y, 20f * emit, fire, emit);
            Drawf.light(e.x + trnsx(e.rotation, 60f), e.y + trnsy(e.rotation, 60f), 160f * emit, fire, 0.8f);
        });

        add("Drone swarm", 300f, 300f, e -> {
            float a = Mathf.clamp(e.time / 30f) * Mathf.clamp((e.lifetime - e.time) / 30f);
            rand.setSeed(e.id);
            for(int i = 0; i < 24; i++){
                float r = 30f + rand.random(40f), sp = rand.random(1.5f, 3f) * (i % 2 == 0 ? 1f : -1f), ph = rand.random(360f);
                float ang = ph + e.time * sp, z = 30f + Mathf.sin(e.time * 0.05f + i, 1f, 15f);
                p(e.x + trnsx(ang, r), e.y + trnsy(ang, r), z);
                float hdg = ang + 90f * Mathf.sign(sp);
                shadow(e.x + trnsx(ang, r) - z * 0.5f, e.y + trnsy(ang, r) - z * 0.5f, 2f, 0.3f * a);
                color(Color.valueOf("3a3d48"), a);
                Fill.poly(px, py, 3, 3f * ps, hdg);
                color(Color.valueOf("ff4f6a"), a * Mathf.absin(e.time + i * 3f, 3f, 1f));
                Fill.circle(px, py, 0.8f * ps);
                if(rand2.chance(0.004f)){
                    color(Color.valueOf("ff4f6a"), a);
                    stroke(0.8f);
                    line(px, py, e.x, e.y);
                }
            }
        });

        add("Sun", 300f, 300f, e -> {
            float a = Mathf.clamp(e.time / 40f) * Mathf.clamp((e.lifetime - e.time) / 40f);
            Color c1 = Color.valueOf("ffe28a"), c2 = Color.valueOf("ff8a3a");
            glow(e.x, e.y, 90f * a, c2, 0.5f * a);
            rand.setSeed(e.id);
            for(int i = 0; i < 6; i++){
                float ang = rand.random(360f) + e.time * 0.2f, f = ((e.time + i * 25f) % 150f) / 150f;
                color(c2, c1, f);
                alpha(a * Mathf.slope(f));
                stroke(3f * Mathf.slope(f));
                float lx = e.x + trnsx(ang, 28f), ly = e.y + trnsy(ang, 28f);
                for(int k = 0; k < 12; k++){
                    float t1 = k / 12f, t2 = (k + 1) / 12f;
                    float h1 = Mathf.sin(t1 * Mathf.PI) * 30f * f, h2 = Mathf.sin(t2 * Mathf.PI) * 30f * f;
                    float a1 = ang + t1 * 40f, a2 = ang + t2 * 40f;
                    line(e.x + trnsx(a1, 28f + h1), e.y + trnsy(a1, 28f + h1), e.x + trnsx(a2, 28f + h2), e.y + trnsy(a2, 28f + h2));
                }
            }
            for(int i = 0; i < 40; i++){
                float ang = i * 9f + e.time * 0.4f, r = 26f + Mathf.absin(e.time + i * 13f, 4f, 3f);
                color(c2, a);
                Fill.circle(e.x + trnsx(ang, r), e.y + trnsy(ang, r), 3f);
            }
            color(c1, a);
            Fill.circle(e.x, e.y, 27f * a);
            color(Color.white, a);
            Fill.circle(e.x, e.y, 18f * a);
            Drawf.light(e.x, e.y, 260f * a, c1, 1f);
        });

        add("Ripples", 180f, 250f, e -> {
            z(Layer.floor + 0.6f);
            rand.setSeed(e.id);
            for(int k = 0; k < 6; k++){
                float bx = e.x + rand.range(40f), by = e.y + rand.range(30f), start = rand.random(60f);
                for(int i = 0; i < 3; i++){
                    float f = Mathf.clamp((e.time - start - i * 10f) / 90f);
                    if(f <= 0f || f >= 1f) continue;
                    color(Color.valueOf("bfe9ff"), (1f - f) * 0.7f);
                    stroke(1.4f * (1f - f));
                    Lines.ellipse(bx, by, 4f + 40f * f, 1f, 0.6f, 0f);
                }
            }
        });

        add("Crystal burst", 120f, 250f, e -> {
            DGFx.shards(e, 26, 0.6f, 1.6f, 1.2f, 1.8f, 2.2f, 3, Color.white, Color.valueOf("b98cff"), Color.valueOf("5a3a9a"), 360f, 0.35f);
            e.scaled(20f, s -> {
                color(Color.white, Color.valueOf("b98cff"), s.fin());
                stroke(3f * s.fout());
                Lines.poly(e.x, e.y, 6, 6f + 40f * s.finpow(), 30f);
                glow(e.x, e.y, 50f * s.fout(), Color.valueOf("b98cff"), s.fout());
            });
        });

        add("Fire pillar", 220f, 300f, e -> {
            float a = Mathf.clamp(e.time / 20f) * Mathf.clamp((e.lifetime - e.time) / 40f);
            rand.setSeed(e.id);
            for(int i = 0; i < 120; i++){
                float off = rand.random(40f), f = ((e.time + off) % 40f) / 40f, ang = rand.random(360f) + f * 400f;
                float r = 6f + 10f * f + rand.random(4f);
                p(e.x + trnsx(ang, r), e.y + trnsy(ang, r) * 0.5f, f * 150f);
                color(Color.white, fire, Mathf.clamp(f * 2.5f));
                Tmp.c3.set(Draw.getColor()).lerp(fireDark, Mathf.clamp(f * 1.5f - 0.3f)).lerp(smoke, Mathf.clamp(f * 2f - 1f));
                color(Tmp.c3, a * (1f - f));
                Fill.circle(px, py, (3f + 5f * f) * ps);
            }
            glow(e.x, e.y, 50f * a, fire, 0.8f * a);
            Drawf.light(e.x, e.y, 200f * a, fire, 1f);
        });

        add("Dust devil", 240f, 250f, DGFx.risingSmoke(24, 30f, 60f, 6f, Color.valueOf("c8b48c"), Color.valueOf("6b5a40"), 240f, 360f, 0.5f).renderer);

        add("Firework salvo", new MultiEffect(all.get("Fireworks"), all.get("Confetti")));

        add("Armageddon", new MultiEffect(all.get("Nuke"), all.get("Sky lightning"), all.get("Chain blasts")));
    }

    static void branch(float x, float y, float ang, float len, float w, int depth){
        if(depth <= 0 || len < 4f) return;
        float ex = x + trnsx(ang, len), ey = y + trnsy(ang, len);
        bolt(x, y, ex, ey, len * 0.12f, 4, w);
        int kids = rand2.random(1, 3);
        for(int i = 0; i < kids; i++){
            branch(ex, ey, ang + rand2.range(45f), len * rand2.random(0.5f, 0.75f), w * 0.7f, depth - 1);
        }
    }
}
