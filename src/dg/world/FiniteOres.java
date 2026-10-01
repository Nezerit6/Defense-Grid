package dg.world;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.Rect;
import arc.scene.ui.layout.Scl;
import arc.struct.*;
import arc.util.*;
import arc.util.pooling.*;
import dg.content.DGFx;
import mindustry.ai.BlockIndexer;
import mindustry.ai.types.MinerAI;
import mindustry.content.Blocks;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.io.SaveFileReader.CustomChunk;
import mindustry.io.SaveVersion;
import mindustry.type.Item;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import mindustry.world.blocks.production.Drill;
import mindustry.world.blocks.production.Drill.DrillBuild;

import java.io.*;

import static mindustry.Vars.*;

public class FiniteOres{
    public static final int veinRadius = 3;
    public static final IntIntMap amounts = new IntIntMap(), maxes = new IntIntMap();
    public static final IntSet depleted = new IntSet();

    private static final Seq<Tile> tiles = new Seq<>();
    private static java.lang.reflect.Field oresField, allOresField, quadSize;
    private static final IntFloatMap unitTimers = new IntFloatMap(), lastTimers = new IntFloatMap();
    private static final IntIntMap unitTiles = new IntIntMap(), lastTiles = new IntIntMap();
    private static final Rect view = new Rect();

    public static void init(){
        SaveVersion.addCustomChunk("dg-finite-ores", new CustomChunk(){
            @Override
            public void write(DataOutput stream) throws IOException{
                stream.writeInt(amounts.size);
                for(IntIntMap.Entry e : amounts.entries()){
                    stream.writeInt(e.key);
                    stream.writeInt(e.value);
                    stream.writeInt(maxes.get(e.key, e.value));
                }
            }

            @Override
            public void read(DataInput stream) throws IOException{
                amounts.clear();
                maxes.clear();
                int size = stream.readInt();
                for(int i = 0; i < size; i++){
                    int pos = stream.readInt();
                    amounts.put(pos, stream.readInt());
                    maxes.put(pos, stream.readInt());
                }
            }
        });

        Events.on(WorldLoadBeginEvent.class, e -> {
            amounts.clear();
            maxes.clear();
            depleted.clear();
        });

        for(Block block : content.blocks()){
            if(!(block instanceof Drill) || block.buildType.get().getClass() != DrillBuild.class) continue;
            Drill drill = (Drill)block;

            drill.buildType = () -> drill.new DrillBuild(){
                @Override
                public void offload(Item item){
                    super.offload(item);
                    mined(this, item);
                }
            };

            drill.addBar("dg-ore", (DrillBuild b) -> new Bar(
                () -> Core.bundle.format("bar.dg-ore", b.dominantItem == null ? 0 : left(b, b.dominantItem)),
                () -> b.dominantItem == null ? Pal.darkishGray : b.dominantItem.color,
                () -> b.dominantItem == null ? 0f : fraction(b, b.dominantItem)
            ));
        }

        Events.run(Trigger.update, FiniteOres::updateUnits);
        WallDigging.init();
        PowerOverload.init();

        if(!headless){
            Events.run(Trigger.draw, FiniteOres::draw);
            VeinHighlight.init();
        }
    }

    public static boolean enabled(){
        return Core.settings.getBool("dg-finite-ores", true);
    }

    public static float richness(){
        return Core.settings.getInt("dg-ore-richness", 4) / 4f;
    }

    public static boolean finite(Tile tile){
        return tile != null && tile.overlay() instanceof OreBlock && !((OreBlock)tile.overlay()).wallOre && tile.overlay().itemDrop != null && !tile.block().isStatic();
    }

    public static int initial(Tile tile){
        Item item = tile.overlay().itemDrop;
        int same = 0, total = 0;
        for(int dx = -veinRadius; dx <= veinRadius; dx++){
            for(int dy = -veinRadius; dy <= veinRadius; dy++){
                if(dx * dx + dy * dy > veinRadius * veinRadius + 1) continue;
                total++;
                Tile other = world.tile(tile.x + dx, tile.y + dy);
                if(other != null && other.overlay() == tile.overlay()) same++;
            }
        }
        float density = same / (float)total;
        float base = 1000f / (1f + item.hardness * 0.5f);
        return Math.max(20, (int)(base * richness() * (0.4f + 1.6f * density * density)));
    }

    public static int amount(Tile tile){
        int stored = amounts.get(tile.pos(), -1);
        return stored >= 0 ? stored : initial(tile);
    }

    public static int max(Tile tile){
        int stored = maxes.get(tile.pos(), -1);
        return stored >= 0 ? stored : initial(tile);
    }

    static void touch(Tile tile){
        if(!amounts.containsKey(tile.pos())){
            int value = initial(tile);
            amounts.put(tile.pos(), value);
            maxes.put(tile.pos(), value);
        }
    }

    static void mined(DrillBuild build, Item item){
        if(!enabled() || net.client()) return;

        build.tile.getLinkedTilesAs(build.block, tiles);
        tiles.removeAll(t -> !finite(t) || t.drop() != item);
        if(tiles.isEmpty()) return;

        if(use(tiles.random())) build.onProximityUpdate();
    }

    static void updateUnits(){
        if(!enabled() || net.client() || !state.isPlaying()){
            unitTimers.clear();
            return;
        }

        lastTimers.clear();
        lastTimers.putAll(unitTimers);
        lastTiles.clear();
        lastTiles.putAll(unitTiles);
        unitTimers.clear();
        unitTiles.clear();

        Groups.unit.each(u -> {
            if(u.controller() instanceof MinerAI) redirect(u, (MinerAI)u.controller());

            Tile tile = u.mineTile;
            if(tile == null || !finite(tile)) return;

            int pos = tile.pos();
            if(lastTiles.get(u.id, -1) == pos && u.mineTimer < lastTimers.get(u.id, 0f) && u.getMineResult(tile) == tile.drop()){
                if(use(tile)) u.mineTile = null;
            }

            if(u.mineTile != null){
                unitTimers.put(u.id, u.mineTimer);
                unitTiles.put(u.id, pos);
            }
        });
    }

    static void redirect(Unit unit, MinerAI ai){
        boolean stale = ai.ore != null && (depleted.contains(ai.ore.pos()) || ai.targetItem != null && ai.ore.drop() != ai.targetItem);
        if(unit.mineTile != null && depleted.contains(unit.mineTile.pos())) stale = true;
        if(!stale) return;

        unit.mineTile = null;
        ai.ore = null;
        if(ai.targetItem != null && !indexer.hasOre(ai.targetItem)) ai.targetItem = null;
        if(ai.targetItem != null) ai.ore = indexer.findClosestOre(unit, ai.targetItem);
    }

    static boolean use(Tile tile){
        Item item = tile.drop();
        touch(tile);
        int left = amounts.get(tile.pos()) - 1;

        if(left > 0){
            amounts.put(tile.pos(), left);
            return false;
        }

        for(int dx = -veinRadius; dx <= veinRadius; dx++){
            for(int dy = -veinRadius; dy <= veinRadius; dy++){
                Tile other = world.tile(tile.x + dx, tile.y + dy);
                if(other != null && other != tile && other.overlay() == tile.overlay() && finite(other)) touch(other);
            }
        }

        amounts.remove(tile.pos());
        maxes.remove(tile.pos());
        DGFx.oreDepleted.at(tile.worldx(), tile.worldy(), 0f, item.color);
        unindex(tile, item);
        depleted.add(tile.pos());
        tile.setOverlayNet(Blocks.air);
        return true;
    }

    static void unindex(Tile tile, Item item){
        try{
            if(oresField == null){
                oresField = BlockIndexer.class.getDeclaredField("ores");
                oresField.setAccessible(true);
                allOresField = BlockIndexer.class.getDeclaredField("allOres");
                allOresField.setAccessible(true);
                quadSize = BlockIndexer.class.getDeclaredField("quadrantSize");
                quadSize.setAccessible(true);
            }
            IntSeq[][][] ores = (IntSeq[][][])oresField.get(indexer);
            if(ores == null || ores[item.id] == null) return;
            int q = quadSize.getInt(indexer);
            IntSeq seq = ores[item.id][tile.x / q][tile.y / q];
            if(seq != null && seq.removeValue(tile.pos())){
                ((ObjectIntMap<Item>)allOresField.get(indexer)).increment(item, -1);
            }
        }catch(Exception e){
            Log.err("[dg] could not update ore index", e);
        }
    }

    static int left(DrillBuild build, Item item){
        build.tile.getLinkedTilesAs(build.block, tiles);
        int sum = 0;
        for(Tile t : tiles){
            if(finite(t) && t.drop() == item) sum += amount(t);
        }
        return sum;
    }

    static float fraction(DrillBuild build, Item item){
        build.tile.getLinkedTilesAs(build.block, tiles);
        int sum = 0, total = 0;
        for(Tile t : tiles){
            if(finite(t) && t.drop() == item){
                sum += amount(t);
                total += max(t);
            }
        }
        return total == 0 ? 0f : sum / (float)total;
    }

    static void draw(){
        if(!enabled() || state.isMenu()) return;

        Core.camera.bounds(view).grow(tilesize * 2f);

        Draw.z(Layer.floor + 0.1f);
        for(IntIntMap.Entry e : amounts.entries()){
            Tile tile = world.tile(e.key);
            if(tile == null || !view.contains(tile.worldx(), tile.worldy()) || !finite(tile)) continue;

            float spent = 1f - e.value / (float)Math.max(maxes.get(e.key, e.value), 1);
            if(spent <= 0.01f) continue;

            Floor floor = tile.floor();
            if(floor.variantRegions == null || floor.variantRegions.length == 0) continue;
            Draw.alpha(spent * 0.65f);
            Draw.rect(floor.variantRegions[Mathf.randomSeed(tile.pos(), 0, Math.max(0, floor.variantRegions.length - 1))], tile.worldx(), tile.worldy());
        }
        Draw.color();
    }
}
