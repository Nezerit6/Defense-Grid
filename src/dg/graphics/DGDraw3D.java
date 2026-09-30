package dg.graphics;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.Tmp;
import mindustry.graphics.Pal;

/**
 * Pseudo-3D ("2.5D") helpers. Things with height are projected with a pinhole camera hovering above the
 * screen center, so tall parts lean away from the center of the screen and grow slightly, the same way
 * MEEPofFaith's Draw3D/Perspective does it.
 */
public class DGDraw3D{
    /** Field of view of the imaginary camera looking down at the map. Bigger values exaggerate the parallax. */
    public static float fov = 70f;
    /** Extra sprite growth per world unit of height, on top of the perspective scale. */
    public static float sizeScale = 1f / 90f;
    /** How far shadows are cast per world unit of height. Shadows fall towards the bottom left, like vanilla turret shadows. */
    public static float shadowLength = 0.8f;
    /** Light comes from this direction; used to shade cylinders. */
    public static float lightAngle = 0f;

    private static final Color tmpCol = new Color();

    public static float cameraZ(){
        return Math.max(Core.camera.width, Core.camera.height) / 2f / (float)Math.tan(fov / 2f * Mathf.degRad);
    }

    /** @return perspective multiplier of a point at height z. */
    public static float persp(float z){
        if(z <= 0f) return 1f;
        float cz = cameraZ();
        return cz / Math.max(cz - z, 1f);
    }

    public static float x(float x, float z){
        return z <= 0f ? x : Core.camera.position.x + (x - Core.camera.position.x) * persp(z);
    }

    public static float y(float y, float z){
        return z <= 0f ? y : Core.camera.position.y + (y - Core.camera.position.y) * persp(z);
    }

    /** @return how much a sprite at height z should be scaled. */
    public static float scale(float z){
        return z <= 0f ? 1f : persp(z) + z * sizeScale;
    }

    /** @return offset of a shadow cast by something at height z, applied to both axes (negative). */
    public static float shadowOffset(float z){
        return Math.max(z, 0f) * shadowLength;
    }

    /** Draws a region at height z. Does not touch the current layer. */
    public static void rect(TextureRegion region, float x, float y, float z, float rotation){
        float s = scale(z), px = Draw.xscl, py = Draw.yscl;
        Draw.xscl *= s;
        Draw.yscl *= s;
        Draw.rect(region, x(x, z), y(y, z), rotation);
        Draw.xscl = px;
        Draw.yscl = py;
    }

    /** Draws a line between two points with different heights. */
    public static void line(float x1, float y1, float z1, float x2, float y2, float z2){
        Lines.line(x(x1, z1), y(y1, z1), x(x2, z2), y(y2, z2), false);
    }

    /** Draws the flat shadow of a region lifted to height z. */
    public static void shadow(TextureRegion region, float x, float y, float z, float rotation, float alpha){
        float off = shadowOffset(z);
        Draw.color(Pal.shadow, Pal.shadow.a * alpha);
        Draw.rect(region, x - off, y - off, rotation);
        Draw.color();
    }

    /**
     * Draws the side of a vertical cylinder standing between heights z1 and z2. Faces towards {@link #lightAngle} are lit.
     * Adapted from Draw3D#tube of Progressed Materials.
     */
    public static void tube(float x, float y, float rad, float z1, float z2, Color light, Color dark){
        float bx = x(x, z1), by = y(y, z1), tx = x(x, z2), ty = y(y, z2);
        float brad = rad * scale(z1), trad = rad * scale(z2);
        int vert = Math.max(Lines.circleVertices(Math.max(brad, trad)), 12);
        float space = 360f / vert;
        float start = tubeStartAngle(bx, by, tx, ty, brad, trad);

        for(int i = 0; i < vert; i++){
            float a1 = start + space * i, a2 = a1 + space;
            float c1 = tmpCol.set(light).lerp(dark, Angles.angleDist(a1, lightAngle) / 180f).toFloatBits();
            float c2 = tmpCol.set(light).lerp(dark, Angles.angleDist(a2, lightAngle) / 180f).toFloatBits();

            Fill.quad(
            bx + Angles.trnsx(a1, brad), by + Angles.trnsy(a1, brad), c1,
            bx + Angles.trnsx(a2, brad), by + Angles.trnsy(a2, brad), c2,
            tx + Angles.trnsx(a2, trad), ty + Angles.trnsy(a2, trad), c2,
            tx + Angles.trnsx(a1, trad), ty + Angles.trnsy(a1, trad), c1
            );
        }
    }

    /** Angle of the outer tangent of two circles, used to only draw the visible half of a cylinder. By Xelo, via Progressed Materials. */
    public static float tubeStartAngle(float x1, float y1, float x2, float y2, float rad1, float rad2){
        if(x1 == x2 && y1 == y2) return 0f;

        float d = Mathf.dst(x2 - x1, y2 - y1);
        float f = Mathf.sqrt(Math.max(d * d - Mathf.sqr(rad2 - rad1), 0.0001f));
        float a = rad1 > rad2 ? Mathf.atan2(rad1 - rad2, f) : (rad1 < rad2 ? Mathf.pi - Mathf.atan2(rad2 - rad1, f) : Mathf.halfPi);
        Tmp.v1.set(x2 - x1, y2 - y1).scl(1f / d);
        Tmp.v2.set(Tmp.v1).rotateRad(Mathf.pi - a).scl(-rad2).add(x2, y2);

        return Angles.angle(x2, y2, Tmp.v2.x, Tmp.v2.y);
    }
}
