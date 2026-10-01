package dg.world.deep;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
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
    public static class Deposit{
        public int x, y, radius, yield;
        public Item item;
        public boolean found;
        public float reveal;

        public int pos(){
            return Point2.pack(x, y);
        }

        public boolean contains(int tx, int ty){
            return Mathf.dst2(tx, ty, x, y) <= radius * radius;
        }
    }

    public static final Seq<Deposit> all = new Seq<>();
    static final IntSet found = new IntSet();

    public static void init(){
        SaveVersion.addCustomChunk("dg-deposits", new CustomChunk(){
            @Override
            public void write(DataOutput stream) throws IOException{
                stream.writeInt(all.count(d -> d.found));
                for(Deposit d : all) if(d.found) stream.writeInt(d.pos());
            }

            @Override
            public void read(DataInput stream) throws IOException{
                found.clear();
                int n = stream.readInt();
                for(int i = 0; i < n; i++) found.add(stream.readInt());
                for(Deposit d : all){
                    d.found = found.contains(d.pos());
                    d.reveal = d.found ? 1f : 0f;
                }
            }
        });

        Events.on(WorldLoadBeginEvent.class, e -> found.clear());
        Events.on(WorldLoadEvent.class, e -> generate());
        if(!headless) Events.run(Trigger.draw, Deposits::draw);
    }

    static void generate(){
        all.clear();
        int w = world.width(), h = world.height();
        long seed = w * 73856093L ^ h * 19349663L;
        for(int i = 0; i < w * h; i += 97){
            Tile t = world.tiles.geti(i);
            seed = seed * 31 + t.floorID();
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
            d.item = pool[rand.random(pool.length - 1)];
            d.yield = 8 + d.radius * 4 + (d.item.hardness <= 1 ? 4 : 0);
            d.found = found.contains(d.pos());
            d.reveal = d.found ? 1f : 0f;
            all.add(d);
        }
    }

    public static Deposit at(int x, int y){
        for(Deposit d : all) if(d.contains(x, y)) return d;
        return null;
    }

    public static Deposit foundAt(int x, int y){
        Deposit d = at(x, y);
        return d != null && d.found ? d : null;
    }

    public static void reveal(Deposit d){
        if(d.found) return;
        d.found = true;
        found.add(d.pos());
        if(!headless) DeepFx.revealed.at(d.x * tilesize, d.y * tilesize, d.radius * tilesize, d.item.color);
    }

    static boolean focused(){
        return control.input.block instanceof SeismicScanner || control.input.block instanceof AbyssBore;
    }

    static void draw(){
        if(!state.isGame() || all.isEmpty()) return;
        boolean focus = focused();
        Rect view = Core.camera.bounds(Tmp.r1).grow(64f);
        Draw.z(Layer.floor + 0.4f);
        for(Deposit d : all){
            if(!d.found) continue;
            d.reveal = Mathf.approachDelta(d.reveal, 1f, 0.02f);
            float wx = d.x * tilesize, wy = d.y * tilesize, R = (d.radius + 0.5f) * tilesize;
            if(!view.overlaps(wx - R, wy - R, R * 2f, R * 2f)) continue;
            float base = (focus ? 0.55f : 0.22f) * d.reveal, pulse = Mathf.absin(Time.time + d.x * 7f, 10f, 0.25f);

            Draw.color(d.item.color, base * 0.25f);
            for(int dx = -d.radius; dx <= d.radius; dx++){
                for(int dy = -d.radius; dy <= d.radius; dy++){
                    if(dx * dx + dy * dy > d.radius * d.radius) continue;
                    float f = 1f - Mathf.len(dx, dy) / (d.radius + 1f);
                    Draw.color(d.item.color, base * (0.15f + 0.35f * f + pulse * f));
                    Fill.square(wx + dx * tilesize, wy + dy * tilesize, tilesize / 2f * (0.35f + 0.4f * f), 45f);
                }
            }
            Lines.stroke(1f, Tmp.c1.set(d.item.color).a(base * 1.4f));
            int segs = 28;
            for(int i = 0; i < segs; i += 2){
                Lines.arc(wx, wy, R, 1f / segs, i * 360f / segs + Time.time * 0.3f);
            }
            Draw.color(1f, 1f, 1f, Math.min(base * 1.8f, 1f));
            Draw.rect(d.item.fullIcon, wx, wy, 8f * d.reveal, 8f * d.reveal);
        }
        Draw.reset();
    }
}
