package dg.world.deep;

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

public class SeismicScanner extends Block{
    public float range = 200f, reload = 360f;
    public Color color = Color.valueOf("ffb35a");

    public SeismicScanner(String name){
        super(name);
        update = true;
        solid = true;
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
        Drawf.dashCircle(x * tilesize + offset, y * tilesize + offset, range, color);
    }

    public class ScannerBuild extends Building{
        public float charge, hammer;

        @Override
        public void updateTile(){
            charge += edelta();
            hammer = Mathf.lerpDelta(hammer, 0f, 0.08f);
            if(charge >= reload){
                charge = 0f;
                hammer = 1f;
                DeepFx.seismic.at(x, y, range, color);
                Effect.shake(1.5f, 12f, this);
                Sounds.drillImpact.at(x, y, 0.6f, 0.6f);
                for(Deposits.Deposit d : Deposits.all){
                    float dst = Mathf.dst(d.x * tilesize, d.y * tilesize, x, y);
                    if(d.amount > 0 && dst <= range + d.radius * tilesize){
                        float strength = 1f - 0.6f * Mathf.clamp(dst / range);
                        Time.run(dst / range * 70f, () -> Deposits.ping(d, strength));
                    }
                }
            }
        }

        @Override
        public void draw(){
            float jx = Mathf.range(1.2f) * hammer, jy = Mathf.range(1.2f) * hammer;
            Draw.rect(region, x + jx, y + jy);
            float f = charge / reload;
            Draw.z(Layer.blockOver);
            Draw.color(Color.valueOf("4b4e58"));
            Fill.square(x + jx, y + jy, 4f, 45f);
            Draw.color(color, 0.3f + 0.7f * f);
            Fill.square(x + jx, y + jy, 2.5f * f + 0.5f, 45f);
            Lines.stroke(1f, Tmp.c1.set(color).a(0.6f));
            Lines.arc(x, y, size * tilesize / 2f - 2f, f, 90f);
            Drawf.light(x, y, 20f + 30f * hammer, color, 0.5f);
            Draw.reset();
        }

        @Override
        public void drawSelect(){
            Drawf.dashCircle(x, y, range, color);
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.f(charge);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            charge = read.f();
        }
    }
}
