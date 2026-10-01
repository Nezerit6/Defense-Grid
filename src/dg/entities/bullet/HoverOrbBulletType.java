package dg.entities.bullet;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import dg.content.DGTurretFx;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;

public class HoverOrbBulletType extends BasicBulletType{
    public float minSpeed = 0.35f, hoverTime = 180f, fadeTime = 45f, size = 3f;
    public Color color = Color.valueOf("ffd27a");

    public HoverOrbBulletType(float speed, float damage){
        super(speed, damage);
        drag = 0.035f;
        lifetime = 60f * 12f;
        pierce = true;
        pierceCap = 3;
        pierceBuilding = true;
        hitSize = 6f;
        keepVelocity = false;
        trailLength = 7;
        trailWidth = 1.3f;
        hitEffect = DGTurretFx.bulletPop;
        despawnEffect = DGTurretFx.orbVanish;
    }

    @Override
    public void init(){
        trailColor = hitColor = lightColor = color;
        super.init();
    }

    @Override
    public void update(Bullet b){
        super.update(b);
        if(b.fdata == 0f && b.vel.len() < minSpeed){
            b.fdata = b.time;
            b.vel.setZero();
        }
        if(b.fdata > 0f){
            b.vel.setZero();
            if(b.time - b.fdata > hoverTime + fadeTime) b.remove();
        }
    }

    @Override
    public void draw(Bullet b){
        drawTrail(b);
        float hover = b.fdata > 0f ? b.time - b.fdata : 0f;
        float fade = 1f - Mathf.clamp((hover - hoverTime) / fadeTime);
        float bob = b.fdata > 0f ? Mathf.sin(b.time + b.id * 9f, 14f, 1.2f) : 0f;
        float pulse = 1f + Mathf.absin(b.time + b.id * 5f, 5f, 0.2f);
        float r = size * pulse * (0.4f + 0.6f * Interp.pow2Out.apply(fade)), x = b.x, y = b.y + bob;

        Draw.z(Layer.bullet);
        Draw.color(Pal.shadow, 0.25f * fade);
        Fill.circle(b.x - 2f, b.y - 3f, r * 0.8f);
        Draw.blend(Blending.additive);
        Fill.light(x, y, 14, r * 3f, Tmp.c1.set(color).a(0.65f * fade), Tmp.c2.set(color).a(0f));
        Draw.blend();
        Draw.color(Tmp.c1.set(color).a(fade));
        Fill.circle(x, y, r);
        Draw.color(Tmp.c1.set(Color.white).a(fade));
        Fill.circle(x - r * 0.25f, y + r * 0.25f, r * 0.45f);
        if(b.fdata > 0f){
            Lines.stroke(0.8f, Tmp.c1.set(color).a(0.6f * fade));
            float a = b.time * 3f + b.id * 20f;
            Lines.arc(x, y, r * 2.2f, 0.25f, a);
            Lines.arc(x, y, r * 2.2f, 0.25f, a + 180f);
        }
        Drawf.light(x, y, r * 8f, color, 0.6f * fade);
        Draw.reset();
    }
}
