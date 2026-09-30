package dg.entities.part;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.util.*;
import dg.graphics.DGDraw3D;
import mindustry.entities.part.DrawPart;

/** A small cylinder (mast, rod, emitter) standing up from the turret. */
public class TubePart extends DrawPart{
    public float x, y;
    public float z1 = 0f, z2 = 4f, radius = 1f;
    public Color lightColor = Color.valueOf("7b7b7b"), darkColor = Color.valueOf("4d4e58"), capColor = Color.valueOf("989aa4");
    public boolean mirror = false;
    public float layer = -1f, layerOffset = 0.001f;

    @Override
    public void draw(PartParams params){
        float pz = Draw.z();
        if(layer > 0) Draw.z(layer);
        Draw.z(Draw.z() + layerOffset);

        int len = mirror && params.sideOverride == -1 ? 2 : 1;
        for(int s = 0; s < len; s++){
            int i = params.sideOverride == -1 ? s : params.sideOverride;
            float sign = (i == 0 ? 1 : -1) * params.sideMultiplier;
            Tmp.v1.set(x * sign, y).rotate(params.rotation - 90).add(params.x, params.y);
            float wx = Tmp.v1.x, wy = Tmp.v1.y;

            DGDraw3D.tube(wx, wy, radius, z1, z2, lightColor, darkColor);
            Draw.color(capColor);
            Fill.circle(DGDraw3D.x(wx, z2), DGDraw3D.y(wy, z2), radius * DGDraw3D.scale(z2));
            Draw.color();
        }

        Draw.z(pz);
    }

    @Override
    public void load(String name){
    }
}
