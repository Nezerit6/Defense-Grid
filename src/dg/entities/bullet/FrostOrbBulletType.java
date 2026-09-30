package dg.entities.bullet;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import dg.content.DGFx;
import dg.graphics.DGDraw3D;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.gen.*;
import mindustry.graphics.*;

import static mindustry.Vars.*;

public class FrostOrbBulletType extends BasicBulletType{
    public float hover = 5f, orbRadius = 3.2f;
    public int shards = 5;
    public Color color = Color.valueOf("afeeee"), coreColor = Color.valueOf("e8ffff"), darkColor = Color.valueOf("6974c4");

    public FrostOrbBulletType(float speed, float damage){
        super(speed, damage);
        hittable = false;
        reflectable = false;
    }

    public float z(Bullet b){
        return hover * Interp.pow2Out.apply(Mathf.clamp(b.time / 12f)) + Mathf.sin(b.time, 6f, 0.6f);
    }

    @Override
    public void update(Bullet b){
        super.update(b);
        if(!headless && b.timer(1, 3f)){
            DGFx.frostMote.at(b.x, b.y, z(b), color);
        }
    }

    @Override
    public void updateTrail(Bullet b){
        if(!headless && trailLength > 0){
            if(b.trail == null) b.trail = new Trail(trailLength);
            b.trail.length = trailLength;
            float z = z(b);
            b.trail.update(DGDraw3D.x(b.x, z), DGDraw3D.y(b.y, z), DGDraw3D.scale(z));
        }
    }

    @Override
    public void draw(Bullet b){
        drawTrail(b);

        float z = z(b), s = DGDraw3D.scale(z), r = orbRadius * s;
        float px = DGDraw3D.x(b.x, z), py = DGDraw3D.y(b.y, z), off = DGDraw3D.shadowOffset(z);

        Draw.z(Layer.groundUnit - 1f);
        Draw.color(Pal.shadow);
        Fill.circle(b.x - off, b.y - off, orbRadius * 0.9f);

        float spin = b.time * 5f + b.id * 37f;
        for(int pass = 0; pass < 2; pass++){
            for(int i = 0; i < shards; i++){
                float a = spin + i * 360f / shards, sa = Mathf.sinDeg(a), depth = sa * Mathf.sinDeg(60f);
                if((depth > 0f) != (pass == 1)) continue;
                float lx = Mathf.cosDeg(a) * orbRadius * 2.1f, ly = sa * orbRadius * 2.1f * Mathf.cosDeg(60f);
                Tmp.v1.set(lx, ly).rotate(b.rotation());
                float wz = z + depth * orbRadius * 2.1f;
                float sx = DGDraw3D.x(b.x + Tmp.v1.x, wz), sy = DGDraw3D.y(b.y + Tmp.v1.y, wz), ss = DGDraw3D.scale(wz);
                float ang = Tmp.v1.angle() + 90f;
                Draw.z(Layer.bullet + (pass == 1 ? 0.02f : -0.02f));
                Draw.color(Tmp.c1.set(darkColor).lerp(Color.white, (depth + 1f) / 2f));
                Drawf.tri(sx, sy, 1.3f * ss, 3f * ss, ang);
                Drawf.tri(sx, sy, 1.3f * ss, 1.6f * ss, ang + 180f);
            }
            if(pass == 0){
                Draw.z(Layer.bullet);
                Draw.color(color);
                Fill.circle(px, py, r);
                Draw.color(coreColor);
                Fill.circle(px, py, r * 0.55f);
                Draw.color(Color.white);
                Fill.circle(px - r * 0.25f, py + r * 0.25f, r * 0.2f);
            }
        }

        Draw.reset();
        Drawf.light(px, py, 30f * s, color, 0.8f);
    }
}
