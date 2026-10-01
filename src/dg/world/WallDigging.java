package dg.world;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import dg.content.DGFx;
import mindustry.content.Blocks;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.UnitType;
import mindustry.world.*;
import mindustry.world.blocks.storage.CoreBlock;

import static mindustry.Vars.*;

public class WallDigging{
    public static float digTime = 900f;

    static final ObjectSet<UnitType> coreUnits = new ObjectSet<>();
    static final IntIntMap digs = new IntIntMap();
    static final IntFloatMap progress = new IntFloatMap();
    static final IntSeq remove = new IntSeq();

    public static void init(){
        for(Block block : content.blocks()){
            if(block instanceof CoreBlock && ((CoreBlock)block).unitType != null) coreUnits.add(((CoreBlock)block).unitType);
        }

        Events.on(TapEvent.class, e -> tap(e.player, e.tile));
        Events.on(WorldLoadBeginEvent.class, e -> {
            digs.clear();
            progress.clear();
        });
        Events.run(Trigger.update, WallDigging::update);
        if(!headless) Events.run(Trigger.draw, WallDigging::draw);
    }

    public static boolean diggable(Tile tile){
        return tile != null && tile.block().isStatic() && tile.build == null;
    }

    static boolean canDig(Unit unit){
        return unit != null && unit.isValid() && coreUnits.contains(unit.type) && unit.type.mineSpeed > 0f;
    }

    static void tap(Player player, Tile tile){
        if(player == null || !canDig(player.unit())) return;
        Unit unit = player.unit();

        if(diggable(tile) && unit.within(tile, unit.type.mineRange) && !unit.activelyBuilding()){
            if(digs.get(unit.id, -1) == tile.pos()){
                digs.remove(unit.id);
            }else{
                digs.put(unit.id, tile.pos());
                unit.mineTile = null;
            }
        }else{
            digs.remove(unit.id);
        }
    }

    static void update(){
        if(!state.isPlaying() || digs.size == 0) return;

        remove.clear();
        for(IntIntMap.Entry e : digs.entries()){
            Unit unit = Groups.unit.getByID(e.key);
            Tile tile = world.tile(e.value);

            if(!canDig(unit) || !unit.isPlayer() || !diggable(tile) || !unit.within(tile, unit.type.mineRange) || unit.activelyBuilding() || unit.mineTile != null){
                remove.add(e.key);
                continue;
            }

            float p = progress.get(e.value, 0f) + Time.delta * unit.type.mineSpeed / digTime;
            Color color = tile.block().mapColor;

            if(Mathf.chanceDelta(0.25f)){
                DGFx.wallChips.at(tile.worldx() + Mathf.range(3f), tile.worldy() + Mathf.range(3f), unit.angleTo(tile) + 180f, color);
            }
            if(Mathf.chanceDelta(0.06f)){
                DGFx.wallDust.at(tile.worldx() + Mathf.range(3f), tile.worldy() + Mathf.range(3f), unit.angleTo(tile) + 180f);
            }
            if(!headless) control.sound.loop(unit.type.mineSound, unit, unit.type.mineSoundVolume);

            if(p >= 1f){
                progress.remove(e.value, 0f);
                remove.add(e.key);
                DGFx.wallCrumble.at(tile.worldx(), tile.worldy(), 0f, color);
                if(!net.client()) tile.setNet(Blocks.air);
            }else{
                progress.put(e.value, p);
            }
        }

        for(int i = 0; i < remove.size; i++) digs.remove(remove.get(i));
    }

    static void draw(){
        if(state.isMenu()) return;

        Draw.z(Layer.blockCracks);
        for(IntFloatMap.Entry e : progress.entries()){
            Tile tile = world.tile(e.key);
            if(tile == null || !diggable(tile)) continue;
            int stage = Mathf.clamp((int)(e.value * BlockRenderer.crackRegions), 0, BlockRenderer.crackRegions - 1);
            Draw.color(0f, 0f, 0f, 0.35f + 0.4f * e.value);
            Draw.rect(renderer.blocks.cracks[0][stage], tile.worldx(), tile.worldy());
        }
        Draw.color();

        for(IntIntMap.Entry e : digs.entries()){
            Unit unit = Groups.unit.getByID(e.key);
            Tile tile = world.tile(e.value);
            if(unit == null || tile == null) continue;

            float angle = unit.angleTo(tile);
            float focus = unit.hitSize / 2f + Mathf.absin(Time.time, 1.1f, 0.5f);
            float px = unit.x + Angles.trnsx(angle, focus), py = unit.y + Angles.trnsy(angle, focus);
            float ex = tile.worldx() + Mathf.sin(Time.time + 48, 12f, 1f), ey = tile.worldy() + Mathf.sin(Time.time + 48, 14f, 1f);

            Draw.z(Layer.flyingUnit + 0.1f);
            Draw.color(Color.lightGray, Color.white, 0.7f + Mathf.absin(Time.time, 0.5f, 0.3f));
            Drawf.laser(Core.atlas.find("minelaser"), Core.atlas.find("minelaser-end"), px, py, ex, ey, 0.75f);

            if(unit.isLocal()){
                Lines.stroke(1f, Pal.accent);
                Lines.poly(tile.worldx(), tile.worldy(), 4, tilesize / 2f * Mathf.sqrt2, Time.time);
            }
            Draw.reset();
        }
    }
}
