package dg.entities.part;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import dg.graphics.DGDraw3D;
import mindustry.entities.part.DrawPart;
import mindustry.graphics.*;

/**
 * Shards orbiting a point on a tilted ring. The ring is really 3D: shards on the near half are higher,
 * bigger, brighter and drawn over whatever sits in the middle, shards on the far half are drawn under it.
 */
public class OrbitPart extends DrawPart{
    public float x, y, z;
    public int count = 6;
    public float radius = 8f, radiusTo = -1f;
    /** How much the ring is tilted towards the camera, in degrees. 0 is a flat ring. */
    public float tilt = 60f;
    /** Rotation of the tilt axis relative to the turret. */
    public float tiltRotation = 0f;
    /** Spin in degrees per tick. */
    public float spinSpeed = 2f;
    /** Additional spin applied over progress, in degrees. */
    public float spinProgress = 0f;
    public float shardWidth = 1.2f, shardLength = 2.2f;
    public Color color = Color.white.cpy(), backColor = Color.gray.cpy();
    public PartProgress progress = PartProgress.warmup;
    /** Progress that controls opacity and shard size. */
    public PartProgress alphaProgress = PartProgress.warmup;
    public float lightOpacity = 0.3f;
    /** Layer of the ring center. The near half is drawn slightly above it, the far half slightly below. */
    public float layer = Layer.effect;

    @Override
    public void draw(PartParams params){
        float prog = progress.getClamp(params), alpha = alphaProgress.getClamp(params);
        if(alpha <= 0.001f) return;

        float pz = Draw.z();
        float rad = radiusTo < 0 ? radius : Mathf.lerp(radius, radiusTo, prog);
        float spin = Time.time * spinSpeed + spinProgress * prog;
        float baseRot = params.rotation - 90f + tiltRotation;
        float cosT = Mathf.cosDeg(tilt), sinT = Mathf.sinDeg(tilt);

        Tmp.v3.set(x, y).rotate(params.rotation - 90).add(params.x, params.y);
        float cx = Tmp.v3.x, cy = Tmp.v3.y;

        for(int i = 0; i < count; i++){
            float a = spin + i * 360f / count;
            float ca = Mathf.cosDeg(a), sa = Mathf.sinDeg(a);
            //position on the ring in turret space; depth is how close the shard is to the camera
            float depth = sa * sinT;
            Tmp.v1.set(ca * rad, sa * rad * cosT).rotate(baseRot);
            //tangent, for shard orientation
            Tmp.v2.set(-sa, ca * cosT).rotate(baseRot);

            float wz = z + depth * rad;
            float px = DGDraw3D.x(cx + Tmp.v1.x, wz), py = DGDraw3D.y(cy + Tmp.v1.y, wz);
            float s = DGDraw3D.scale(wz) * alpha, near = (depth / Math.max(sinT, 0.001f) + 1f) / 2f;

            Draw.z(layer + depth * 0.01f);
            Draw.color(Tmp.c1.set(backColor).lerp(color, near), alpha);
            float angle = Tmp.v2.angle();
            Drawf.tri(px, py, shardWidth * s, shardLength * s, angle);
            Drawf.tri(px, py, shardWidth * s, shardLength * 0.6f * s, angle + 180f);
            Drawf.light(px, py, shardLength * 5f * s, color, lightOpacity * alpha);
        }

        Draw.color();
        Draw.z(pz);
    }

    @Override
    public void load(String name){
    }
}
