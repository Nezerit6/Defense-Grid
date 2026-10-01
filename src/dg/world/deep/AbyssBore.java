package dg.world.deep;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.entities.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

public class AbyssBore extends Block{
    public float drillTime = 600f, stopTime = 120f;
    public int capacity = 50;
    public TextureRegion rotator;

    public AbyssBore(String name){
        super(name);
        update = true;
        solid = true;
        hasItems = true;
        itemCapacity = 50;
        group = BlockGroup.drills;
        envEnabled |= Env.space;
    }

    @Override
    public void load(){
        super.load();
        if(!Core.atlas.has(name)) region = Core.atlas.find("block-" + size);
        rotator = Core.atlas.find("laser-drill-rotator");
    }

    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation){
        return Deposits.foundAt(tile.x, tile.y) != null;
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        super.drawPlace(x, y, rotation, valid);
        Deposits.Deposit d = Deposits.foundAt(x, y);
        if(d == null){
            drawPlaceText(Core.bundle.get("dg-bore-nodeposit"), x, y, false);
        }else{
            drawPlaceText(Core.bundle.format("dg-bore-yield", d.item.emoji(), d.yield), x, y, true);
        }
    }

    @Override
    public void setStats(){
        super.setStats();
        stats.add(Stat.drillSpeed, Core.bundle.format("dg-bore-cycle", (int)(drillTime / 60f)));
    }

    @Override
    public void setBars(){
        super.setBars();
        addBar("dg-bore", (BoreBuild e) -> new Bar(() -> Core.bundle.get(e.phase == 1 ? "dg-bore-drilling" : e.phase == 2 ? "dg-bore-stopping" : "dg-bore-open"),
            () -> e.phase == 1 ? Pal.accent : Pal.lightOrange, () -> e.phase == 1 ? e.progress / drillTime : e.phase == 2 ? 1f - e.progress / stopTime : 1f));
    }

    public class BoreBuild extends Building{
        public int phase;
        public float progress, spin, spinSpeed, shake;
        public Deposits.Deposit deposit;

        Deposits.Deposit deposit(){
            if(deposit == null || !deposit.contains(tile.x, tile.y)) deposit = Deposits.at(tile.x, tile.y);
            return deposit;
        }

        @Override
        public void updateTile(){
            Deposits.Deposit d = deposit();
            if(d == null) return;

            if(phase == 0){
                if(items.total() + d.yield <= capacity && potentialEfficiency > 0f){
                    phase = 1;
                    progress = 0f;
                }
            }else if(phase == 1){
                progress += edelta();
                spinSpeed = Mathf.lerpDelta(spinSpeed, 14f * efficiency, 0.03f);
                shake = Mathf.lerpDelta(shake, efficiency, 0.05f);
                if(Mathf.chanceDelta(0.25f * efficiency)){
                    float a = Mathf.random(360f);
                    DeepFx.boreDust.at(x + Angles.trnsx(a, size * 4f), y + Angles.trnsy(a, size * 4f), a);
                }
                if(Mathf.chanceDelta(0.3f * efficiency)) DeepFx.boreChip.at(x, y, Mathf.random(360f), d.item.color);
                if(timer(timerDump, 6f) && !headless) Effect.shake(0.6f * efficiency, 6f, this);
                if(progress >= drillTime){
                    phase = 2;
                    progress = stopTime;
                    Sounds.drillImpact.at(x, y, 0.8f, 0.7f);
                }
            }else if(phase == 2){
                progress -= edelta();
                spinSpeed = Mathf.lerpDelta(spinSpeed, 0f, 0.06f);
                shake = Mathf.lerpDelta(shake, 0.25f, 0.04f);
                if(progress <= 0f){
                    phase = 3;
                    spinSpeed = 0f;
                    items.add(d.item, Math.min(d.yield, capacity - items.total()));
                    DeepFx.shutter.at(x, y, size * tilesize / 2f, d.item.color);
                    Sounds.door.at(x, y);
                }
            }else{
                shake = Mathf.lerpDelta(shake, 0f, 0.05f);
                if(items.total() + d.yield <= capacity) phase = 0;
            }

            spin += spinSpeed * Time.delta;
            if(phase == 3 || items.total() > 0) dumpAccumulate();
        }

        @Override
        public boolean acceptItem(Building source, Item item){
            return false;
        }

        @Override
        public boolean shouldConsume(){
            return phase == 1 || phase == 2;
        }

        @Override
        public void draw(){
            float amp = shake * 1.1f;
            float jx = Mathf.range(amp), jy = Mathf.range(amp);
            Draw.rect(region, x + jx, y + jy);
            Draw.z(Layer.blockOver);
            if(rotator.found()){
                Draw.rect(rotator, x + jx * 1.5f, y + jy * 1.5f, spin);
            }else{
                Lines.stroke(2f, Color.valueOf("989aa4"));
                Lines.poly(x + jx, y + jy, 6, 6f, spin);
            }
            Deposits.Deposit d = deposit();
            if(d != null){
                float glow = phase == 1 ? Mathf.absin(Time.time, 3f, 0.4f) + 0.3f : phase == 3 ? 0.8f : 0.15f;
                Draw.color(d.item.color, glow);
                Lines.stroke(1.2f);
                Lines.square(x + jx, y + jy, size * tilesize / 2f - 1.5f);
                if(phase == 3){
                    Draw.color();
                    Draw.rect(d.item.fullIcon, x + size * tilesize / 2f - 4f, y + size * tilesize / 2f - 4f, 6f, 6f);
                }
                Drawf.light(x, y, 30f + 20f * shake, d.item.color, 0.4f * glow);
            }
            Draw.reset();
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.b(phase);
            write.f(progress);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            phase = read.b();
            progress = read.f();
        }
    }
}
