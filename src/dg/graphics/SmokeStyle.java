package dg.graphics;

import arc.graphics.Color;
import arc.graphics.g2d.Fill;
import arc.math.*;
import arc.util.Tmp;
import arc.util.io.*;

/** Parameters of smoke puffs that billow up from the ground and rise in perspective. */
public class SmokeStyle{
    private static final Rand rand = new Rand();

    public int count = 7;
    /** How far puffs spread from the source. */
    public float spread = 9f;
    /** Height puffs rise to. */
    public float height = 14f;
    public float size = 3.2f;
    public float lifetime = 70f;
    /** Half-angle of the direction puffs drift in, around the effect rotation. 180 spreads them all around. */
    public float cone = 70f;
    public float alpha = 0.6f;
    /** Random shortening of each puff's life, 0 for all puffs to fade together. */
    public float lifeRand = 0f;
    public Color from = Color.valueOf("8b8c95"), to = Color.valueOf("6e7080");

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

    /** Draws the smoke {@code time} ticks after it was emitted. */
    public void draw(float x, float y, float rotation, float time, long seed){
        if(time >= lifetime) return;
        float fin = time / lifetime;

        rand.setSeed(seed);
        for(int i = 0; i < count; i++){
            float life = 1f - rand.random(lifeRand), ang = rotation + rand.range(cone), len = rand.random(1f);
            float pin = fin / life;
            if(pin >= 1f) continue;

            float pinpow = Interp.pow3Out.apply(pin), pout = 1f - pin;
            float z = height * pinpow, dst = (1f + spread * pinpow) * len;
            float rad = size * (0.5f + 0.9f * Mathf.clamp(pin * 3f)) * (0.7f + 0.3f * pout) * DGDraw3D.scale(z);
            Color center = Tmp.c1.set(from).lerp(to, pin).mulA(alpha * pout), edge = Tmp.c2.set(center).a(0f);

            Fill.light(DGDraw3D.x(x + Angles.trnsx(ang, dst), z), DGDraw3D.y(y + Angles.trnsy(ang, dst), z), 12, rad, center, edge);
        }
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
