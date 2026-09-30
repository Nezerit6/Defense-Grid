package dg.entities.bullet;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import dg.content.DGFx;
import dg.graphics.DGDraw3D;
import mindustry.entities.bullet.ArtilleryBulletType;
import mindustry.gen.*;
import mindustry.graphics.*;

import static mindustry.Vars.*;

public class LobBulletType extends ArtilleryBulletType{
    public float arcHeight = 40f;
    public float smokeInterval = 2.5f;
    public Color smokeColor = Color.valueOf("a5a6ad");

    public LobBulletType(float speed, float damage){
        super(speed, damage);
        trailEffect = mindustry.content.Fx.none;
    }

    public float z(Bullet b){
        float f = b.fin();
        return arcHeight * Math.min(b.lifetime / lifetime, 1f) * 4f * f * (1f - f);
    }

    @Override
    public void update(Bullet b){
        super.update(b);
        if(!headless && smokeInterval > 0f && b.timer(1, smokeInterval)){
            DGFx.shellSmoke.at(b.x, b.y, z(b), smokeColor);
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

        float z = z(b), s = DGDraw3D.scale(z);
        float fall = Mathf.clamp(1f - z / (arcHeight * 1.5f));
        float off = DGDraw3D.shadowOffset(z), rot = b.rotation() - 90f;

        Draw.z(Layer.groundUnit - 1f);
        Draw.color(Pal.shadow, Pal.shadow.a * fall);
        Draw.rect(backRegion, b.x - off, b.y - off, width * (1f + z / 80f), height * (1f + z / 80f), rot);

        float px = DGDraw3D.x(b.x, z), py = DGDraw3D.y(b.y, z);
        float pitch = Mathf.cos(b.fin() * Mathf.pi) * 0.25f;
        Draw.z(Layer.bullet + z / 1000f);
        Draw.color(backColor);
        Draw.rect(backRegion, px, py, width * s, height * s * (1f - Math.abs(pitch)), rot);
        Draw.color(frontColor);
        Draw.rect(frontRegion, px, py, width * s, height * s * (1f - Math.abs(pitch)), rot);
        Draw.reset();

        Drawf.light(px, py, 16f * s, backColor, 0.4f);
    }
}
