package dg.world.radar;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.*;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

public class RadarBlock extends Block{
    public float range = 240f, sweepSpeed = 3f, beamWidth = 14f;
    public Color color = Color.valueOf("7dff9a");

    public RadarBlock(String name){
        super(name);
        update = true;
        solid = true;
        group = BlockGroup.turrets;
        envEnabled |= Env.space;
    }

    @Override
    public void init(){
        clipSize = Math.max(clipSize, range * 2f + 8f);
        super.init();
    }

    @Override
    public void load(){
        super.load();
        if(!Core.atlas.has(name)) region = Core.atlas.find("block-" + size);
    }

    @Override
    public void setStats(){
        super.setStats();
        stats.add(Stat.range, range / tilesize, StatUnit.blocks);
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        super.drawPlace(x, y, rotation, valid);
        Drawf.dashCircle(x * tilesize + offset, y * tilesize + offset, range, Pal.remove);
    }

    public class RadarBuild extends Building{
        public float sweep, warm;

        public float range(){
            return range;
        }

        @Override
        public void updateTile(){
            RadarNet.radars.add(this);
            warm = Mathf.lerpDelta(warm, efficiency, 0.04f);
            float prev = sweep;
            sweep = (sweep + sweepSpeed * edelta()) % 360f;
            if(efficiency <= 0f) return;
            float step = Angles.angleDist(prev, sweep);
            Units.nearbyEnemies(team, x, y, range, u -> {
                if(u.dead || !within(u, range)) return;
                float a = angleTo(u);
                float half = beamWidth / 2f + step + Mathf.radDeg * Mathf.atan2(dst(u), u.hitSize);
                if(Angles.angleDist(a, sweep - step / 2f) <= half) RadarNet.mark(team, u);
            });
        }

        @Override
        public void onRemoved(){
            RadarNet.radars.remove(this);
            super.onRemoved();
        }

        @Override
        public void draw(){
            super.draw();
            if(warm < 0.01f) return;

            Draw.z(Layer.floor + 0.3f);
            Draw.blend(Blending.additive);
            int trail = 14;
            for(int i = 0; i < trail; i++){
                float a1 = sweep - i * 4f, a2 = a1 - 4f, f = 1f - i / (float)trail;
                Draw.color(color, 0.16f * f * f * warm);
                Fill.tri(x, y, x + Angles.trnsx(a1, range), y + Angles.trnsy(a1, range), x + Angles.trnsx(a2, range), y + Angles.trnsy(a2, range));
            }
            Draw.blend();
            Draw.z(Layer.bullet - 1f);
            Lines.stroke(1.2f, Tmp.c1.set(color).a(0.7f * warm));
            Lines.lineAngle(x, y, sweep, range);
            Lines.stroke(0.8f, Tmp.c1.set(color).a(0.2f * warm));
            Lines.circle(x, y, range);

            Draw.z(Layer.turret);
            float dishR = size * tilesize * 0.32f;
            Draw.color(Color.valueOf("4b4e58"));
            Fill.circle(x, y, dishR * 0.55f);
            Draw.color(Color.valueOf("989aa4"));
            Lines.stroke(2f);
            Lines.arc(x, y, dishR, 0.45f, sweep - 81f);
            Lines.lineAngle(x, y, sweep, dishR + 1f);
            Draw.color(color, 0.6f + Mathf.absin(Time.time, 6f, 0.4f));
            Fill.circle(x + Angles.trnsx(sweep, dishR + 1f), y + Angles.trnsy(sweep, dishR + 1f), 1.1f);
            Drawf.light(x, y, 30f, color, 0.4f * warm);
            Draw.reset();
        }

        @Override
        public void drawSelect(){
            Drawf.dashCircle(x, y, range, Pal.remove);
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.f(sweep);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            sweep = read.f();
        }
    }
}
