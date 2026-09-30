package dg.entities.part;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import dg.graphics.DGDraw3D;
import mindustry.entities.part.*;
import mindustry.graphics.*;

/** A {@link RegionPart} that can be lifted off the turret. Lifted parts are drawn in perspective and cast a shadow below them. */
public class LiftPart extends RegionPart{
    /** Height of the part when {@link #liftProgress} is 0. */
    public float height = 0f;
    /** Height added when {@link #liftProgress} is 1. */
    public float lift = 0f;
    public PartProgress liftProgress = PartProgress.warmup;
    /** Opacity of the shadow cast by this part. */
    public float shadowAlpha = 1f;
    /** If positive, the part moves to this layer once lifted past {@link #liftLayerThreshold}, e.g. to rise above the rest of the turret. */
    public float liftLayer = -1f, liftLayerThreshold = 0.05f;

    public LiftPart(String region){
        super(region);
    }

    public LiftPart(){
    }

    @Override
    public void draw(PartParams params){
        float z = Draw.z();
        if(layer > 0) Draw.z(layer);
        if(under && turretShading) Draw.z(z - 0.0001f);
        Draw.z(Draw.z() + layerOffset);

        float liftProg = liftProgress.getClamp(params);
        if(liftLayer > 0 && liftProg > liftLayerThreshold) Draw.z(liftLayer);

        float prevZ = Draw.z();
        float prog = progress.getClamp(params), sclProg = growProgress.getClamp(params);
        float mx = moveX * prog, my = moveY * prog, mr = moveRot * prog + rotation,
            gx = growX * sclProg, gy = growY * sclProg;

        for(int i = 0; i < moves.size; i++){
            PartMove move = moves.get(i);
            float p = move.progress.getClamp(params);
            mx += move.x * p;
            my += move.y * p;
            mr += move.rot * p;
            gx += move.gx * p;
            gy += move.gy * p;
        }

        float h = height + lift * liftProg, hs = DGDraw3D.scale(h);

        int len = mirror && params.sideOverride == -1 ? 2 : 1;
        float preXscl = Draw.xscl, preYscl = Draw.yscl;
        Draw.xscl *= xScl + gx;
        Draw.yscl *= yScl + gy;

        for(int s = 0; s < len; s++){
            int i = params.sideOverride == -1 ? s : params.sideOverride;

            TextureRegion region = drawRegion ? regions[Math.min(i, regions.length - 1)] : null;
            float sign = (i == 0 ? 1 : -1) * params.sideMultiplier;
            Tmp.v1.set((x + mx) * sign, y + my).rotateRadExact((params.rotation - 90) * Mathf.degRad);

            float
                rx = params.x + Tmp.v1.x,
                ry = params.y + Tmp.v1.y,
                px = DGDraw3D.x(rx, h),
                py = DGDraw3D.y(ry, h),
                rot = mr * sign + params.rotation - 90;

            Draw.xscl *= sign;

            //shadow on whatever is below; drawn first at the same layer so it lands on top of the turret body
            if(drawRegion && shadowAlpha > 0f && h > 0.01f){
                TextureRegion shadowRegion = outlines[Math.min(i, outlines.length - 1)].found() ? outlines[Math.min(i, outlines.length - 1)] : region;
                Draw.z(prevZ);
                DGDraw3D.shadow(shadowRegion, rx, ry, h, rot, shadowAlpha * Mathf.clamp(h));
            }

            Draw.xscl *= hs;
            Draw.yscl *= hs;

            if(outline && drawRegion){
                Draw.z(prevZ + outlineLayerOffset);
                Draw.rect(outlines[Math.min(i, regions.length - 1)], px, py, rot);
                Draw.z(prevZ);
            }

            if(drawRegion && region.found()){
                Draw.z(prevZ);
                if(color != null && colorTo != null){
                    Draw.color(color, colorTo, prog);
                }else if(color != null){
                    Draw.color(color);
                }

                if(mixColor != null && mixColorTo != null){
                    Draw.mixcol(mixColor, mixColorTo, prog);
                }else if(mixColor != null){
                    Draw.mixcol(mixColor, mixColor.a);
                }

                Draw.blend(blending);
                Draw.rect(region, px, py, rot);
                Draw.blend();
                if(color != null) Draw.color();
                Draw.mixcol();
            }

            if(heat.found()){
                float hprog = heatProgress.getClamp(params);
                heatColor.write(Tmp.c1).a(hprog * heatColor.a);
                Drawf.additive(heat, Tmp.c1, px, py, rot, turretShading ? turretHeatLayer : Draw.z() + heatLayerOffset);
                if(heatLight) Drawf.light(px, py, heat, rot, Tmp.c1, heatLightOpacity * hprog);
            }

            Draw.xscl /= hs;
            Draw.yscl /= hs;
            Draw.xscl *= sign;
        }

        Draw.color();
        Draw.mixcol();
        Draw.z(z);

        if(children.size > 0){
            for(int s = 0; s < len; s++){
                int i = (params.sideOverride == -1 ? s : params.sideOverride);
                float sign = (i == 1 ? -1 : 1) * params.sideMultiplier;
                Tmp.v1.set((x + mx) * sign, y + my).rotateRadExact((params.rotation - 90) * Mathf.degRad);

                childParam.set(params.warmup, params.reload, params.smoothReload, params.heat, params.recoil, params.charge, params.x + Tmp.v1.x, params.y + Tmp.v1.y, i * sign + mr * sign + params.rotation);
                childParam.sideMultiplier = params.sideMultiplier;
                childParam.life = params.life;
                childParam.sideOverride = i;
                for(DrawPart child : children){
                    child.draw(childParam);
                }
            }
        }

        Draw.scl(preXscl, preYscl);
    }
}
