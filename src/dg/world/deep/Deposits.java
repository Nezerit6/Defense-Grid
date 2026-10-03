package dg.world.deep;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import dg.world.VeinHighlight;
import mindustry.content.*;
import mindustry.game.EventType.*;
import mindustry.graphics.*;
import mindustry.io.*;
import mindustry.io.SaveFileReader.*;
import mindustry.type.*;
import mindustry.world.*;

import java.io.*;

import static mindustry.Vars.*;

public class Deposits{
    public static float signalLife = 60f * 75f;

    public static class Deposit{
        public int x, y, radius, tiles, amount, max;
        public Item item;
        public float signal, quality, err;

        public int pos(){
            return Point2.pack(x, y);
        }

        public boolean contains(int tx, int ty){
            return Mathf.dst2(tx, ty, x, y) <= radius * radius;
        }

        public boolean visible(){
            return signal > 0f && amount > 0;
        }

        public int estimate(){
            float guess = amount * (1f + err * (1f - quality));
            float step = quality > 0.85f ? 10f : quality > 0.5f ? 50f : 250f;
            return Math.max((int)(Math.round(guess / step) * step), (int)step);
        }

        public float richness(){
            return Math.min(tiles, 30);
        }
    }

    public static final Seq<Deposit> all = new Seq<>();
    static final IntMap<float[]> saved = new IntMap<>();
    static Deposit hovered;

    public static void init(){
        SaveVersion.addCustomChunk("dg-deposits", new CustomChunk(){
            @Override
            public void write(DataOutput stream) throws IOException{
                stream.writeInt(all.size);
                for(Deposit d : all){
                    stream.writeInt(d.pos());
                    stream.writeInt(d.amount);
                    stream.writeFloat(d.signal);
                    stream.writeFloat(d.quality);
                }
            }

            @Override
            public void read(DataInput stream) throws IOException{
                saved.clear();
                int n = stream.readInt();
                for(int i = 0; i < n; i++){
                    saved.put(stream.readInt(), new float[]{stream.readInt(), stream.readFloat(), stream.readFloat()});
                }
                apply();
            }
        });

        Events.on(WorldLoadBeginEvent.class, e -> saved.clear());
        Events.on(WorldLoadEvent.class, e -> {
            generate();
            apply();
        });
        Events.run(Trigger.update, Deposits::update);
        if(!headless) Events.run(Trigger.draw, Deposits::draw);
    }

    static void apply(){
        if(saved.size == 0) return;
        for(Deposit d : all){
            float[] s = saved.get(d.pos());
            if(s == null) continue;
            d.amount = (int)s[0];
            d.signal = s[1];
            d.quality = s[2];
        }
    }

    static void generate(){
        all.clear();
        int w = world.width(), h = world.height();
        long seed = w * 73856093L ^ h * 19349663L;
        for(int i = 0; i < w * h; i += 97){
            seed = seed * 31 + world.tiles.geti(i).floorID();
        }
        Rand rand = new Rand(seed);
        Item[] pool = {Items.copper, Items.copper, Items.lead, Items.lead, Items.coal, Items.titanium, Items.titanium, Items.scrap, Items.sand, Items.thorium};
        int count = Math.max(3, w * h / 700);
        for(int i = 0; i < count * 4 && all.size < count; i++){
            int x = rand.random(4, w - 5), y = rand.random(4, h - 5);
            Tile t = world.tile(x, y);
            if(t == null || t.floor().isDeep() || t.floor().isLiquid || t.block().isStatic()) continue;
            boolean close = false;
            for(Deposit o : all) if(Mathf.dst(o.x, o.y, x, y) < 12f) close = true;
            if(close) continue;
            Deposit d = new Deposit();
            d.x = x;
            d.y = y;
            d.radius = rand.random(2, 4);
            for(int dx = -d.radius; dx <= d.radius; dx++) for(int dy = -d.radius; dy <= d.radius; dy++) if(dx * dx + dy * dy <= d.radius * d.radius) d.tiles++;
            d.item = pool[rand.random(pool.length - 1)];
            d.max = d.amount = (int)(d.tiles * rand.random(40f, 80f) / Math.max(d.item.hardness, 1) * 2f);
            d.err = rand.range(0.6f);
            all.add(d);
        }
    }

    static void update(){
        if(!state.isGame() || state.isPaused()) return;
        for(Deposit d : all){
            if(d.signal > 0f){
                d.signal = Math.max(d.signal - Time.delta / signalLife, 0f);
                if(d.signal <= 0f) d.quality *= 0.5f;
            }
        }
    }

    public static void ping(Deposit d, float strength){
        boolean was = d.visible();
        d.signal = Math.max(d.signal, strength);
        d.quality = Math.min(d.quality + 0.22f * strength, 1f);
        if(!was && d.visible() && !headless) DeepFx.revealed.at(d.x * tilesize, d.y * tilesize, d.radius * tilesize, d.item.color);
    }

    public static Deposit at(int x, int y){
        for(Deposit d : all) if(d.amount > 0 && d.contains(x, y)) return d;
        return null;
    }

    public static Deposit visibleAt(int x, int y){
        Deposit d = at(x, y);
        return d != null && d.visible() ? d : null;
    }

    public static int take(Deposit d, int wanted){
        int got = Math.min(wanted, d.amount);
        d.amount -= got;
        if(d.amount <= 0 && !headless) DeepFx.revealed.at(d.x * tilesize, d.y * tilesize, d.radius * tilesize, Color.gray);
        return got;
    }

    static boolean focused(){
        return control.input.block instanceof SeismicScanner || control.input.block instanceof AbyssBore;
    }

    static void draw(){
        if(!state.isGame() || all.isEmpty()) return;
        boolean focus = focused();
        Rect view = Core.camera.bounds(Tmp.r1).grow(64f);
        int mx = mindustry.core.World.toTile(Core.input.mouseWorldX()), my = mindustry.core.World.toTile(Core.input.mouseWorldY());
        hovered = Core.scene.hasMouse() || mobile ? null : visibleAt(mx, my);
        float h = tilesize / 2f;

        for(Deposit d : all){
            if(!d.visible()) continue;
            float wx = d.x * tilesize, wy = d.y * tilesize, R = (d.radius + 0.5f) * tilesize;
            if(!view.overlaps(wx - R, wy - R, R * 2f, R * 2f)) continue;
            float sig = Mathf.clamp(d.signal * 3f), base = (focus || d == hovered ? 0.6f : 0.25f) * sig;
            float pulse = Mathf.absin(Time.time + d.x * 7f, 10f, 0.25f);

            Draw.z(Layer.floor + 0.4f);
            for(int dx = -d.radius; dx <= d.radius; dx++){
                for(int dy = -d.radius; dy <= d.radius; dy++){
                    if(dx * dx + dy * dy > d.radius * d.radius) continue;
                    float f = 1f - Mathf.len(dx, dy) / (d.radius + 1f), full = (float)d.amount / d.max;
                    Draw.color(d.item.color, base * (0.15f + 0.35f * f + pulse * f) * (0.4f + 0.6f * full));
                    Fill.square(wx + dx * tilesize, wy + dy * tilesize, h * (0.35f + 0.4f * f), 45f);
                }
            }

            if(d == hovered){
                Draw.z(Layer.overlayUI - 1f);
                Draw.color(d.item.color, 0.15f * (0.6f + pulse));
                for(int dx = -d.radius; dx <= d.radius; dx++){
                    for(int dy = -d.radius; dy <= d.radius; dy++){
                        if(dx * dx + dy * dy <= d.radius * d.radius) Fill.square(wx + dx * tilesize, wy + dy * tilesize, h);
                    }
                }
                Lines.stroke(1.2f, Tmp.c1.set(d.item.color).lerp(Color.white, 0.3f).a(0.7f + pulse));
                for(int dx = -d.radius; dx <= d.radius; dx++){
                    for(int dy = -d.radius; dy <= d.radius; dy++){
                        if(dx * dx + dy * dy > d.radius * d.radius) continue;
                        float x = wx + dx * tilesize, y = wy + dy * tilesize;
                        if(!in(d, dx + 1, dy)) Lines.line(x + h, y - h, x + h, y + h);
                        if(!in(d, dx - 1, dy)) Lines.line(x - h, y - h, x - h, y + h);
                        if(!in(d, dx, dy + 1)) Lines.line(x - h, y + h, x + h, y + h);
                        if(!in(d, dx, dy - 1)) Lines.line(x - h, y - h, x + h, y - h);
                    }
                }
                String label = d.item.emoji() + " " + d.item.localizedName + "  ~" + d.estimate() + "  [lightgray](" + Core.bundle.get("dg-deposit-deep") + ", " + (int)(d.quality * 100) + "%)";
                VeinHighlight.text(label, wx, wy + R + 6f, 0.25f / Scl.scl(1f), d.item.color);
            }else{
                Draw.z(Layer.floor + 0.4f);
                Lines.stroke(1f, Tmp.c1.set(d.item.color).a(base * 1.4f));
                int segs = 28;
                for(int i = 0; i < segs; i += 2){
                    Lines.arc(wx, wy, R, 1f / segs, i * 360f / segs + Time.time * 0.3f);
                }
                Draw.color(1f, 1f, 1f, Math.min(base * 1.8f, 1f));
                Draw.rect(d.item.fullIcon, wx, wy, 8f, 8f);
            }
        }
        Draw.reset();
    }

    static boolean in(Deposit d, int dx, int dy){
        return dx * dx + dy * dy <= d.radius * d.radius;
    }
}
