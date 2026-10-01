package dg.world;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.input.KeyCode;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.ui.layout.Scl;
import arc.struct.*;
import arc.util.*;
import dg.content.DGFx;
import dg.graphics.DGDraw3D;
import mindustry.entities.Effect;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.*;
import mindustry.world.blocks.storage.CoreBlock;

import static mindustry.Vars.*;

public class LiftOff{
    public static KeyCode key = KeyCode.l;
    public static int maxSize = 48;
    public static float ignition = 90f;

    static final Seq<Structure> structures = new Seq<>();
    static final Seq<Building> selected = new Seq<>();
    static final ObjectSet<Building> seen = new ObjectSet<>();
    static int startX = -1, startY = -1;
    static boolean selecting;

    static class Part{
        Block block;
        float dx, dy, rotation;
    }

    static class Structure{
        final Seq<Part> parts = new Seq<>();
        final FloatSeq thrusters = new FloatSeq();
        float x, y, w, h, time, seed;

        float z(){
            if(time < ignition) return 1.5f * Interp.pow2In.apply(time / ignition);
            float t = time - ignition;
            return 1.5f + 0.03f * t + 0.0014f * t * t;
        }
    }

    public static void init(){
        Events.run(Trigger.update, LiftOff::update);
        Events.on(WorldLoadBeginEvent.class, e -> {
            structures.clear();
            selecting = false;
        });
        if(!headless) Events.run(Trigger.draw, LiftOff::draw);
    }

    static void update(){
        if(!state.isGame()){
            selecting = false;
            return;
        }

        if(!headless && !mobile) updateInput();

        for(int i = structures.size - 1; i >= 0; i--){
            Structure s = structures.get(i);
            s.time += Time.delta;
            float z = s.z();

            if(s.time < ignition + 120f && !headless){
                if(Mathf.chanceDelta(0.35f + s.time / ignition * 0.4f)){
                    float ex = s.x + Mathf.range(s.w / 2f + 4f), ey = s.y + Mathf.range(s.h / 2f + 4f);
                    DGFx.liftDust.at(ex, ey, Angles.angle(s.x, s.y, ex, ey));
                }
                Effect.shake(Mathf.clamp(s.time / ignition) * 2.5f, 4f, s.x, s.y);
            }

            if(s.time > 2000f || z > DGDraw3D.cameraZ() * 0.9f){
                structures.remove(i);
            }
        }
    }

    static void updateInput(){
        if(Core.scene.hasKeyboard() || player.dead()){
            selecting = false;
            return;
        }

        Tile tile = world.tileWorld(Core.input.mouseWorldX(), Core.input.mouseWorldY());

        if(Core.input.keyTap(key) && tile != null){
            selecting = true;
            startX = tile.x;
            startY = tile.y;
        }

        if(selecting) collect(tile);

        if(selecting && Core.input.keyRelease(key)){
            selecting = false;
            if(selected.any()){
                if(net.client()){
                    ui.showInfoToast(Core.bundle.get("dg-liftoff-host"), 3f);
                }else{
                    launch(selected);
                }
            }
            selected.clear();
        }
    }

    static Rect area(Tile end, Rect out){
        int ex = end == null ? startX : end.x, ey = end == null ? startY : end.y;
        ex = Mathf.clamp(ex, startX - maxSize, startX + maxSize);
        ey = Mathf.clamp(ey, startY - maxSize, startY + maxSize);
        int x1 = Math.min(startX, ex), y1 = Math.min(startY, ey), x2 = Math.max(startX, ex), y2 = Math.max(startY, ey);
        return out.set(x1, y1, x2 - x1 + 1, y2 - y1 + 1);
    }

    static void collect(Tile end){
        selected.clear();
        seen.clear();
        Rect r = area(end, Tmp.r3);
        for(int x = (int)r.x; x < r.x + r.width; x++){
            for(int y = (int)r.y; y < r.y + r.height; y++){
                Tile t = world.tile(x, y);
                if(t == null || t.build == null) continue;
                Building b = t.build;
                if(b.team != player.team() || b.block instanceof CoreBlock) continue;
                if(!r.contains(b.tile.x, b.tile.y) || !seen.add(b)) continue;
                selected.add(b);
            }
        }
    }

    public static void launch(Seq<Building> builds){
        if(builds.isEmpty()) return;

        Structure s = new Structure();
        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
        for(Building b : builds){
            float half = b.block.size * tilesize / 2f;
            minX = Math.min(minX, b.x - half);
            minY = Math.min(minY, b.y - half);
            maxX = Math.max(maxX, b.x + half);
            maxY = Math.max(maxY, b.y + half);
        }
        s.x = (minX + maxX) / 2f;
        s.y = (minY + maxY) / 2f;
        s.w = maxX - minX;
        s.h = maxY - minY;
        s.seed = Mathf.random(1000f);

        for(Building b : builds){
            Part p = new Part();
            p.block = b.block;
            p.dx = b.x - s.x;
            p.dy = b.y - s.y;
            p.rotation = b.block.rotate ? b.rotdeg() : 0f;
            s.parts.add(p);
        }

        int nx = Math.max(1, Math.round(s.w / 24f)), ny = Math.max(1, Math.round(s.h / 24f));
        for(int ix = 0; ix < nx; ix++){
            for(int iy = 0; iy < ny; iy++){
                s.thrusters.add(minX + (ix + 0.5f) * s.w / nx, minY + (iy + 0.5f) * s.h / ny);
            }
        }

        for(Building b : builds){
            if(b.isValid()) b.tile.removeNet();
        }

        structures.add(s);
        Sounds.missileLaunch.at(s.x, s.y, 0.6f, 1.5f);
    }

    static void draw(){
        if(state.isMenu()) return;

        if(selecting){
            Tile end = world.tileWorld(Core.input.mouseWorldX(), Core.input.mouseWorldY());
            Rect r = area(end, Tmp.r3);
            Draw.z(Layer.overlayUI);
            for(Building b : selected){
                Lines.stroke(1f, Tmp.c1.set(Pal.accent).a(0.6f + Mathf.absin(4f, 0.4f)));
                Lines.square(b.x, b.y, b.block.size * tilesize / 2f + 1f);
            }
            Drawf.dashRect(Pal.accent, r.x * tilesize - tilesize / 2f, r.y * tilesize - tilesize / 2f, r.width * tilesize, r.height * tilesize);
            VeinHighlight.text(Core.bundle.format("dg-liftoff", selected.size), Core.input.mouseWorldX(), Core.input.mouseWorldY() + tilesize * 2f, 0.25f / Scl.scl(1f), Pal.accent);
        }

        float cz = DGDraw3D.cameraZ();
        for(Structure s : structures){
            float z = s.z(), sc = DGDraw3D.scale(z), off = DGDraw3D.shadowOffset(z);
            float fade = 1f - Mathf.curve(z, cz * 0.55f, cz * 0.88f);
            float wobble = s.time < ignition ? Mathf.sin(Time.time, 1.5f, 0.4f * s.time / ignition) : 0f;
            float lift = Mathf.clamp(s.time / ignition);

            Draw.z(Layer.block + 0.5f);
            Draw.color(0f, 0f, 0f, 0.35f * Mathf.clamp(1f - z / 300f));
            float shs = 1f + z / 250f;
            for(Part p : s.parts){
                Draw.rect(p.block.fullIcon, s.x + p.dx * shs - off, s.y + p.dy * shs - off, p.block.fullIcon.width * Draw.scl * shs, p.block.fullIcon.height * Draw.scl * shs, p.rotation);
            }

            for(int i = 0; i < s.thrusters.size; i += 2){
                float tx = s.thrusters.get(i), ty = s.thrusters.get(i + 1);
                float flick = 0.8f + Mathf.absin(Time.time + i * 7f, 1.2f, 0.4f);
                float rad = (4f + 6f * lift) * flick * sc;
                float px = DGDraw3D.x(tx, z), py = DGDraw3D.y(ty, z);

                Draw.z(Layer.flyingUnit + 0.5f);
                Draw.blend(Blending.additive);
                if(z > 3f){
                    Lines.stroke(rad * 0.9f, Tmp.c1.set(Pal.missileYellowBack).a(0.35f * fade * lift));
                    Lines.line(tx, ty, px, py);
                    Lines.stroke(rad * 0.35f, Tmp.c1.set(Color.white).a(0.5f * fade * lift));
                    Lines.line(tx, ty, px, py);
                }
                Fill.light(px, py, 16, rad * 1.6f, Tmp.c1.set(Color.white).a(0.9f * fade * lift), Tmp.c2.set(Pal.missileYellowBack).a(0f));
                Draw.blend();
                Drawf.light(px, py, rad * 6f, Pal.missileYellowBack, 0.8f * fade * lift);
            }

            Draw.z(Layer.flyingUnit + 1f + z / 1000f);
            float preX = Draw.xscl, preY = Draw.yscl;
            Draw.xscl = Draw.yscl = sc;
            Draw.color(1f, 1f, 1f, fade);
            for(Part p : s.parts){
                float wx = s.x + p.dx + wobble, wy = s.y + p.dy;
                Draw.rect(p.block.fullIcon, DGDraw3D.x(wx, z), DGDraw3D.y(wy, z), p.rotation);
            }
            Draw.xscl = preX;
            Draw.yscl = preY;
            Draw.reset();
        }
    }
}
