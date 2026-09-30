package dg.graphics;

import arc.Core;
import arc.graphics.g2d.*;
import arc.math.*;
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
}
