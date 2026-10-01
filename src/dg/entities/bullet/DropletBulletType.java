package dg.entities.bullet;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import dg.content.DGTurretFx;
import dg.graphics.DGDraw3D;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.entities.bullet.*;

public class DropletBulletType extends LiquidBulletType{
    public float arcHeight = 10f;

    public DropletBulletType(Liquid liquid){
        super(liquid);
        orbSize = 3.2f;
        puddleSize = 14f;
        hitEffect = DGTurretFx.splash;
        despawnEffect = DGTurretFx.splash;
        despawnHit = false;
        trailLength = 6;
        trailWidth = 1.6f;
        if(liquid != null) trailColor = liquid.color;
    }

    @Override
    public void despawned(Bullet b){
        super.despawned(b);
        hit(b);
    }

    @Override
    public void draw(Bullet b){
        float z = arcHeight * 4f * b.fin() * b.fout(), sc = DGDraw3D.scale(z), off = DGDraw3D.shadowOffset(z);
        float px = DGDraw3D.x(b.x, z), py = DGDraw3D.y(b.y, z), rot = b.rotation();

        Draw.z(Layer.bullet - 1f);
        Draw.color(Pal.shadow, 0.35f);
        Fill.circle(b.x - off, b.y - off, orbSize * 0.8f);

        Draw.z(Layer.bullet);
        drawTrail(b);
        Draw.color(liquid.color);
        Fill.circle(px, py, orbSize * sc);
        Drawf.tri(px, py, orbSize * 2f * sc, orbSize * 3.4f * sc, rot + 180f);
        Draw.color(Tmp.c1.set(liquid.color).lerp(Color.white, 0.55f));
        Fill.circle(px + Angles.trnsx(rot + 60f, orbSize * 0.4f * sc), py + Angles.trnsy(rot + 60f, orbSize * 0.4f * sc), orbSize * 0.35f * sc);
        Draw.reset();
    }
}
