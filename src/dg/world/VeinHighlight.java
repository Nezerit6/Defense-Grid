package dg.world;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.*;
import arc.scene.ui.layout.Scl;
import arc.struct.*;
import arc.util.*;
import arc.util.pooling.*;
import mindustry.game.EventType.*;
import mindustry.graphics.*;
import mindustry.type.Item;
import mindustry.ui.*;
import mindustry.ui.fragments.MinimapFragment;
import mindustry.world.*;

import java.lang.reflect.Field;

import static mindustry.Vars.*;

public class VeinHighlight{
    public static final int maxTiles = 6000;

    static final IntSeq vein = new IntSeq();
    static final IntSet veinSet = new IntSet();
    static Block veinOre;
    static int total, veinX1, veinY1, veinX2, veinY2;
    static float lastScan = -999f;

    static final Rect rect = new Rect();
    static Minimap minimap;
    static Field panx, pany, zoom, baseSize;

    public static void init(){
        try{
            panx = field("panx");
            pany = field("pany");
            zoom = field("zoom");
            baseSize = field("baseSize");
        }catch(Exception e){
            Log.err("[dg] minimap fields not found", e);
        }

        Events.run(Trigger.draw, VeinHighlight::drawWorld);
        Events.run(Trigger.uiDrawEnd, VeinHighlight::drawUI);
        Events.on(WorldLoadEvent.class, e -> clear());
    }

    static Field field(String name) throws Exception{
        Field f = MinimapFragment.class.getDeclaredField(name);
        f.setAccessible(true);
        return f;
    }

    static void clear(){
        vein.clear();
        veinSet.clear();
        veinOre = null;
        total = 0;
    }

    static boolean active(){
        return FiniteOres.enabled() && state.isGame() && world.width() > 0;
    }

    static void scan(Tile start){
        if(start == null || !FiniteOres.finite(start)){
            clear();
            return;
        }

        if(veinOre == start.overlay() && veinSet.contains(start.pos()) && Time.time - lastScan < 20f) return;

        clear();
        lastScan = Time.time;
        veinOre = start.overlay();
        veinX1 = veinX2 = start.x;
        veinY1 = veinY2 = start.y;

        IntSeq queue = vein;
        queue.add(start.pos());
        veinSet.add(start.pos());

        for(int i = 0; i < queue.size && queue.size < maxTiles; i++){
            Tile tile = world.tile(queue.get(i));
            total += FiniteOres.amount(tile);
            veinX1 = Math.min(veinX1, tile.x);
            veinX2 = Math.max(veinX2, tile.x);
            veinY1 = Math.min(veinY1, tile.y);
            veinY2 = Math.max(veinY2, tile.y);

            for(int dx = -1; dx <= 1; dx++){
                for(int dy = -1; dy <= 1; dy++){
                    Tile other = world.tile(tile.x + dx, tile.y + dy);
                    if(other != null && other.overlay() == veinOre && FiniteOres.finite(other) && veinSet.add(other.pos())){
                        queue.add(other.pos());
                    }
                }
            }
        }
    }

    static boolean in(int x, int y){
        return x >= 0 && y >= 0 && x < world.width() && y < world.height() && veinSet.contains(Point2.pack(x, y));
    }

    static String label(){
        Item item = veinOre.itemDrop;
        return item.emoji() + " " + item.localizedName + "  " + total + "  [lightgray](" + vein.size + ")";
    }

    static void drawWorld(){
        if(!active() || mobile) return;

        Tile hover = Core.scene.hasMouse() ? null : world.tileWorld(Core.input.mouseWorldX(), Core.input.mouseWorldY());
        if(hover == null || !FiniteOres.finite(hover)){
            if(!overMinimap()) clear();
            return;
        }
        scan(hover);
        if(veinOre == null) return;

        Color color = veinOre.itemDrop.color;
        float pulse = 0.6f + Mathf.absin(5f, 0.4f), h = tilesize / 2f;

        Draw.z(Layer.overlayUI - 1f);
        Draw.color(color, 0.18f * pulse);
        for(int i = 0; i < vein.size; i++){
            Tile t = world.tile(vein.get(i));
            Fill.square(t.worldx(), t.worldy(), h);
        }

        Lines.stroke(1.2f, Tmp.c1.set(color).lerp(Color.white, 0.3f).a(pulse));
        for(int i = 0; i < vein.size; i++){
            Tile t = world.tile(vein.get(i));
            float x = t.worldx(), y = t.worldy();
            if(!in(t.x + 1, t.y)) Lines.line(x + h, y - h, x + h, y + h);
            if(!in(t.x - 1, t.y)) Lines.line(x - h, y - h, x - h, y + h);
            if(!in(t.x, t.y + 1)) Lines.line(x - h, y + h, x + h, y + h);
            if(!in(t.x, t.y - 1)) Lines.line(x - h, y - h, x + h, y - h);
        }
        Draw.reset();

        text(label(), Core.input.mouseWorldX(), Core.input.mouseWorldY() + tilesize * 1.5f, 0.25f / Scl.scl(1f), color);
    }

    static void drawUI(){
        if(!active() || mobile) return;

        Vec2 mouse = Tmp.v1.set(Core.input.mouse());
        float mx = mouse.x, my = mouse.y;

        if(ui.minimapfrag.shown()){
            if(!fullMapRect()) return;
        }else if(!smallMapRect()){
            return;
        }

        if(!rect.contains(mx, my)) return;

        Rect region = tileRegion();
        int tx = (int)(region.x + (mx - rect.x) / rect.width * region.width);
        int ty = (int)(region.y + (my - rect.y) / rect.height * region.height);
        Tile hover = world.tile(tx, ty);
        if(hover == null || !FiniteOres.finite(hover)){
            clear();
            return;
        }
        scan(hover);
        if(veinOre == null) return;

        Color color = veinOre.itemDrop.color;
        float pulse = 0.6f + Mathf.absin(5f, 0.4f);
        float sx = rect.width / region.width, sy = rect.height / region.height;

        Draw.color(Tmp.c2.set(color).lerp(Color.white, 0.35f), 0.55f * pulse);
        for(int i = 0; i < vein.size; i++){
            Tile t = world.tile(vein.get(i));
            float x = rect.x + (t.x - region.x) * sx, y = rect.y + (t.y - region.y) * sy;
            if(!rect.contains(x, y)) continue;
            Fill.crect(x, y, sx, sy);
        }

        Lines.stroke(Math.max(1.5f, Math.min(sx, sy) * 0.35f), Tmp.c1.set(Color.white).a(0.7f + 0.3f * pulse));
        for(int i = 0; i < vein.size; i++){
            Tile t = world.tile(vein.get(i));
            float x = rect.x + (t.x - region.x) * sx, y = rect.y + (t.y - region.y) * sy;
            if(!rect.contains(x, y)) continue;
            if(!in(t.x + 1, t.y)) Lines.line(x + sx, y, x + sx, y + sy);
            if(!in(t.x - 1, t.y)) Lines.line(x, y, x, y + sy);
            if(!in(t.x, t.y + 1)) Lines.line(x, y + sy, x + sx, y + sy);
            if(!in(t.x, t.y - 1)) Lines.line(x, y, x + sx, y);
        }
        Draw.reset();

        text(label(), mx, my + Scl.scl(22f), 1f, color);
        Draw.flush();
    }

    static boolean overMinimap(){
        if(mobile || !active()) return false;
        Vec2 mouse = Tmp.v1.set(Core.input.mouse());
        boolean found = ui.minimapfrag.shown() ? fullMapRect() : smallMapRect();
        return found && rect.contains(mouse.x, mouse.y);
    }

    static Rect tileRegion(){
        if(ui.minimapfrag.shown()){
            return Tmp.r2.set(0f, 0f, world.width(), world.height());
        }
        TextureRegion reg = renderer.minimap.getRegion();
        float w = world.width(), h = world.height();
        return Tmp.r2.set(reg.u * w, h - reg.v2 * h, (reg.u2 - reg.u) * w, (reg.v2 - reg.v) * h);
    }

    static boolean smallMapRect(){
        if(renderer.minimap.getRegion() == null) return false;
        if(minimap == null || !minimap.hasParent()) minimap = find(Core.scene.root);
        if(minimap == null || !visible(minimap) || minimap.getChildren().isEmpty()) return false;

        Element view = minimap.getChildren().first();
        Vec2 pos = view.localToStageCoordinates(Tmp.v2.set(0f, 0f));
        rect.set(pos.x, pos.y, view.getWidth(), view.getHeight());
        return true;
    }

    static boolean fullMapRect(){
        if(zoom == null) return false;
        try{
            float z = zoom.getFloat(ui.minimapfrag), base = baseSize.getFloat(ui.minimapfrag);
            float px = panx.getFloat(ui.minimapfrag), py = pany.getFloat(ui.minimapfrag);
            float w = Core.scene.getWidth(), h = Core.scene.getHeight();
            float ratio = (float)world.height() / world.width();
            float size = base * z * world.width();
            rect.set(w / 2f + px * z - size / 2f, h / 2f + py * z - size / 2f * ratio, size, size * ratio);
            return true;
        }catch(Exception e){
            return false;
        }
    }

    static boolean visible(Element e){
        while(e != null){
            if(!e.visible) return false;
            e = e.parent;
        }
        return true;
    }

    static Minimap find(Group group){
        for(Element child : group.getChildren()){
            if(child instanceof Minimap) return (Minimap)child;
            if(child instanceof Group){
                Minimap found = find((Group)child);
                if(found != null) return found;
            }
        }
        return null;
    }

    public static void text(String text, float x, float y, float scale, Color color){
        Draw.z(Layer.overlayUI + 1f);
        Font font = Fonts.outline;
        GlyphLayout layout = Pools.obtain(GlyphLayout.class, GlyphLayout::new);
        boolean ints = font.usesIntegerPositions();
        font.setUseIntegerPositions(false);
        font.getData().setScale(scale);
        layout.setText(font, text);
        if(scale >= 1f) x = Mathf.clamp(x, layout.width / 2f + 6f, Core.scene.getWidth() - layout.width / 2f - 6f);

        Draw.color(0f, 0f, 0f, 0.45f);
        float pad = 2f * scale / 0.25f * 0.25f + 1f;
        Fill.rect(x, y, layout.width + pad * 4f, layout.height + pad * 3f);
        font.setColor(Tmp.c1.set(color).lerp(Color.white, 0.6f));
        font.draw(text, x, y + layout.height / 2f, Align.center);

        font.setColor(Color.white);
        font.getData().setScale(1f);
        font.setUseIntegerPositions(ints);
        Pools.free(layout);
        Draw.reset();
    }
}

