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
import mindustry.world.blocks.defense.turrets.Turret.TurretBuild;

public class GlaiveBulletType extends BasicBulletType{
    public float returnAt = 0.45f, catchRange = 7f, spin = 24f, blades = 3, bladeLength = 7f, returnTurn = 14f;
    public float catchReload = 0.6f;
    public Color bladeColor = Color.valueOf("c0c4cf"), edgeColor = Color.white;

    public GlaiveBulletType(float speed, float damage){
        super(speed, damage);
        pierce = true;
        pierceCap = -1;
        pierceBuilding = false;
        collidesTiles = false;
        hittable = false;
        reflectable = false;
        keepVelocity = false;
        despawnEffect = mindustry.content.Fx.none;
    }

    @Override
    public void update(Bullet b){
        super.update(b);

        float out = lifetime * returnAt;
        if(b.fdata == 0f){
            b.vel.setLength(speed * Mathf.lerp(1f, 0.25f, Interp.pow2In.apply(Mathf.clamp(b.time / out))));
            if(b.time >= out){
                b.fdata = 1f;
                b.collided.clear();
            }
        }else if(b.owner instanceof Building && ((Building)b.owner).isValid()){
            Building owner = (Building)b.owner;
            b.vel.setAngle(Angles.moveToward(b.vel.angle(), b.angleTo(owner), returnTurn * Time.delta));
            b.vel.setLength(Math.min(b.vel.len() + 0.12f * Time.delta, speed * 1.25f));
            b.time = Math.min(b.time, b.lifetime - 2f);

            if(b.within(owner, catchRange)){
                DGFx.glaiveCatch.at(b.x, b.y, 0f, hitColor);
                if(owner instanceof TurretBuild){
                    TurretBuild turret = (TurretBuild)owner;
                    turret.reloadCounter = Math.max(turret.reloadCounter, ((mindustry.world.blocks.defense.turrets.Turret)turret.block).reload * catchReload);
                }
                b.remove();
            }
        }
    }

    public float z(Bullet b){
        return 2.5f + Mathf.sin(b.time, 8f, 0.6f);
    }

    @Override
    public void draw(Bullet b){
        drawTrail(b);

        float z = z(b), s = DGDraw3D.scale(z), px = DGDraw3D.x(b.x, z), py = DGDraw3D.y(b.y, z), off = DGDraw3D.shadowOffset(z);
        float rot = b.time * spin + b.id * 40f;

        Draw.z(Layer.groundUnit - 1f);
        Draw.color(Pal.shadow);
        for(int i = 0; i < blades; i++){
            Drawf.tri(b.x - off, b.y - off, bladeLength * 0.45f, bladeLength, rot + i * 360f / blades);
        }

        Draw.z(Layer.bullet);
        for(int i = 0; i < blades; i++){
            float a = rot + i * 360f / blades;
            Draw.color(bladeColor);
            Drawf.tri(px, py, bladeLength * 0.45f * s, bladeLength * s, a);
            Draw.color(edgeColor);
            Drawf.tri(px + Angles.trnsx(a + 90f, 0.8f * s), py + Angles.trnsy(a + 90f, 0.8f * s), bladeLength * 0.15f * s, bladeLength * 0.9f * s, a);
        }
        Draw.color(backColor);
        Fill.circle(px, py, 1.8f * s);
        Draw.color(frontColor);
        Fill.circle(px, py, 1f * s);
        Draw.reset();

        Drawf.light(px, py, 18f, backColor, 0.5f);
    }
}
