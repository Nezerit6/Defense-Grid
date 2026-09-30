package dg.entities.part;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.util.*;
import dg.graphics.DGDraw3D;
import mindustry.entities.part.DrawPart;

public class TubePart extends DrawPart{
    public float x, y;
    public float z1 = 0f, z2 = 4f, radius = 1f;
    public Color lightColor = Color.valueOf("7b7b7b"), darkColor = Color.valueOf("4d4e58"), capColor = Color.valueOf("989aa4");
    public Color outlineColor = Color.valueOf("404049");
    public float outlineStroke = 0.5f;
    public boolean mirror = false;
    public float layer = -1f, layerOffset = 0f;

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
            float tx = DGDraw3D.x(wx, z2), ty = DGDraw3D.y(wy, z2), tr = radius * DGDraw3D.scale(z2);

            if(outlineStroke > 0f){
                Draw.color(outlineColor);
                DGDraw3D.tube(wx, wy, radius + outlineStroke, z1, z2, outlineColor, outlineColor);
                Fill.circle(tx, ty, tr + outlineStroke);
            }
            DGDraw3D.tube(wx, wy, radius, z1, z2, lightColor, darkColor);
            Draw.color(capColor);
            Fill.circle(tx, ty, tr);
            Draw.color();
        }

        Draw.z(pz);
    }

    @Override
    public void load(String name){
    }
}
