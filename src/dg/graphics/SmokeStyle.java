package dg.graphics;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.Tmp;
import arc.util.io.*;

public class SmokeStyle{
    private static final Rand rand = new Rand();
    private static TextureRegion puff;

    public int count = 7;
    public float spread = 9f;
    public float height = 14f;
    public float size = 3.2f;
    public float lifetime = 70f;
    public float cone = 70f;
    public float alpha = 0.6f;
    public float lifeRand = 0f;
    public Color from = Color.valueOf("8b8c95"), to = Color.valueOf("6e7080");

    public static TextureRegion puff(){
        if(puff == null) puff = Core.atlas.find("dg-smoke-puff");
        return puff;
    }

    public SmokeStyle(){
    }

    public SmokeStyle(int count, float spread, float height, float size, float lifetime, Color from, Color to){
        this.count = count;
        this.spread = spread;
        this.height = height;
        this.size = size;
        this.lifetime = lifetime;
        this.from = from;
        this.to = to;
    }

    public void draw(float x, float y, float rotation, float time, long seed){
        if(time >= lifetime) return;
        float fin = time / lifetime;

        rand.setSeed(seed);
        for(int i = 0; i < count; i++){
            float life = 1f - rand.random(lifeRand), ang = rotation + rand.range(cone), len = rand.random(1f);
            float pin = fin / life;
            if(pin >= 1f || alpha * (1f - pin) < 0.01f) continue;

            float pinpow = Interp.pow3Out.apply(pin), pout = 1f - pin;
            float z = height * pinpow, dst = (1f + spread * pinpow) * len;
            float rad = size * (0.5f + 0.9f * Mathf.clamp(pin * 3f)) * (0.7f + 0.3f * pout) * DGDraw3D.scale(z);
            float px = DGDraw3D.x(x + Angles.trnsx(ang, dst), z), py = DGDraw3D.y(y + Angles.trnsy(ang, dst), z);
            if(puff().found()){
                Draw.color(Tmp.c1.set(from).lerp(to, pin), alpha * pout);
                Draw.rect(puff, px, py, rad * 2f, rad * 2f);
            }else{
                Color center = Tmp.c1.set(from).lerp(to, pin).mulA(alpha * pout);
                Fill.light(px, py, 12, rad, center, Tmp.c2.set(center).a(0f));
            }
        }
        Draw.color();
    }

    public SmokeStyle set(SmokeStyle other){
        count = other.count;
        spread = other.spread;
        height = other.height;
        size = other.size;
        lifetime = other.lifetime;
        cone = other.cone;
        alpha = other.alpha;
        lifeRand = other.lifeRand;
        from = other.from.cpy();
        to = other.to.cpy();
        return this;
    }

    public void write(Writes write){
        write.i(count);
        write.f(spread);
        write.f(height);
        write.f(size);
        write.f(lifetime);
        write.f(cone);
        write.f(alpha);
        write.f(lifeRand);
        write.i(from.rgba());
        write.i(to.rgba());
    }

    public void read(Reads read){
        count = read.i();
        spread = read.f();
        height = read.f();
        size = read.f();
        lifetime = read.f();
        cone = read.f();
        alpha = read.f();
        lifeRand = read.f();
        from = new Color(read.i());
        to = new Color(read.i());
    }
}
