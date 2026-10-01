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
    public float drillTime = 600f;
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

    public float spinUpTime = 180f, windDownTime = 150f, coolTime = 420f, baseTime = 400f, hardnessTime = 120f;

    public float perCycle(Deposits.Deposit d){
        return drillTime * d.richness() / (baseTime + hardnessTime * d.item.hardness);
    }

    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation){
        return Deposits.visibleAt(tile.x, tile.y) != null;
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        super.drawPlace(x, y, rotation, valid);
        Deposits.Deposit d = Deposits.visibleAt(x, y);
        if(d == null){
            drawPlaceText(Core.bundle.get("dg-bore-nodeposit"), x, y, false);
        }else{
            drawPlaceText(Core.bundle.format("dg-bore-yield", d.item.emoji(), (int)perCycle(d)), x, y, true);
        }
    }

    @Override
    public void setStats(){
        super.setStats();
        stats.add(Stat.drillSpeed, Core.bundle.format("dg-bore-cycle", (int)((spinUpTime + drillTime + windDownTime + coolTime) / 60f)));
    }

    @Override
    public void setBars(){
        super.setBars();
        addBar("dg-bore", (BoreBuild e) -> new Bar(() -> Core.bundle.get(phaseNames[e.phase]),
            () -> e.phase == 2 ? Pal.accent : e.phase == 4 ? Color.valueOf("8ab4ff") : Pal.lightOrange, () -> e.phaseFraction()));
    }

    static final String[] phaseNames = {"dg-bore-idle", "dg-bore-spinup", "dg-bore-drilling", "dg-bore-stopping", "dg-bore-cooling", "dg-bore-open"};

    public class BoreBuild extends Building{
        public int phase;
        public float time, spin, spinSpeed, shake, heat, mined;
        public Deposits.Deposit deposit;

        Deposits.Deposit deposit(){
            if(deposit == null || deposit.amount <= 0 || !deposit.contains(tile.x, tile.y)) deposit = Deposits.at(tile.x, tile.y);
            return deposit;
        }

        public float phaseFraction(){
            switch(phase){
                case 1: return time / spinUpTime;
                case 2: return time / drillTime;
                case 3: return 1f - time / windDownTime;
                case 4: return 1f - time / coolTime;
                case 5: return items.total() / (float)capacity;
                default: return 0f;
            }
        }

        void next(int p){
            phase = p;
            time = 0f;
        }

        @Override
        public void updateTile(){
            Deposits.Deposit d = deposit();
            if(d != null && phase >= 1 && phase <= 3) d.signal = Math.max(d.signal, 0.35f);

            switch(phase){
                case 0:
                    if(d != null && items.total() == 0 && potentialEfficiency > 0f) next(1);
                    break;
                case 1:
                    time += edelta();
                    spinSpeed = Mathf.lerp(0f, 9f, Interp.pow2In.apply(Mathf.clamp(time / spinUpTime)));
                    shake = Mathf.clamp(time / spinUpTime) * 0.6f;
                    heat = Mathf.approachDelta(heat, 0.2f, 0.002f);
                    if(time >= spinUpTime) next(2);
                    break;
                case 2:
                    time += edelta();
                    spinSpeed = Mathf.lerpDelta(spinSpeed, 9f * efficiency, 0.05f);
                    shake = Mathf.lerpDelta(shake, 0.6f + Mathf.absin(time, 7f, 0.4f), 0.1f) * Math.max(efficiency, 0.2f);
                    heat = Mathf.approachDelta(heat, 1f, 0.0025f * efficiency);
                    if(d != null){
                        mined += edelta() * d.richness() / (baseTime + hardnessTime * d.item.hardness);
                        while(mined >= 1f && items.total() < capacity){
                            mined -= 1f;
                            if(Deposits.take(d, 1) > 0) items.add(d.item, 1);
                        }
                        if(Mathf.chanceDelta(0.2f * efficiency)) DeepFx.boreChip.at(x, y, Mathf.random(360f), d.item.color);
                    }
                    if(Mathf.chanceDelta(0.12f * efficiency)){
                        float a = Mathf.random(360f);
                        DeepFx.boreDust.at(x + Angles.trnsx(a, size * 4f), y + Angles.trnsy(a, size * 4f), a);
                    }
                    if(timer(timerDump, 10f) && !headless) Effect.shake(0.25f * efficiency, 10f, this);
                    if(time >= drillTime || d == null || items.total() >= capacity){
                        next(3);
                        Sounds.drillImpact.at(x, y, 0.7f, 0.6f);
                    }
                    break;
                case 3:
                    time += Time.delta;
                    spinSpeed = 9f * Interp.pow2Out.apply(1f - Mathf.clamp(time / windDownTime));
                    shake = 0.35f * (1f - Mathf.clamp(time / windDownTime));
                    if(time >= windDownTime){
                        next(4);
                        spinSpeed = 0f;
                        shake = 0f;
                    }
                    break;
                case 4:
                    time += Time.delta;
                    heat = Mathf.approachDelta(heat, 0f, 1f / coolTime);
                    if(Mathf.chanceDelta(0.08f * heat)) DeepFx.steam.at(x + Mathf.range(size * 3f), y + Mathf.range(size * 3f));
                    if(time >= coolTime){
                        next(5);
                        DeepFx.shutter.at(x, y, size * tilesize / 2f, d == null ? Color.white : d.item.color);
                        Sounds.door.at(x, y);
                    }
                    break;
                default:
                    dumpAccumulate();
                    if(items.total() == 0) next(0);
                    break;
            }

            spin += spinSpeed * Time.delta;
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
            float amp = shake * 0.35f;
            float jx = Mathf.range(amp), jy = Mathf.range(amp);
            Draw.rect(region, x + jx, y + jy);
            if(heat > 0.01f){
                Draw.blend(Blending.additive);
                Draw.color(Color.valueOf("ff6a3a"), heat * 0.3f);
                Draw.rect(region, x + jx, y + jy);
                Draw.blend();
                Draw.color();
            }
            Draw.z(Layer.blockOver);
            if(rotator.found()){
                Draw.rect(rotator, x + jx * 1.5f, y + jy * 1.5f, spin);
            }else{
                Lines.stroke(2f, Color.valueOf("989aa4"));
                Lines.poly(x + jx, y + jy, 6, 6f, spin);
            }
            Deposits.Deposit d = deposit();
            Color c = d == null ? Color.gray : d.item.color;
            float glow = phase == 2 ? Mathf.absin(Time.time, 3f, 0.3f) + 0.25f : phase == 5 ? 0.8f : 0.12f;
            Draw.color(c, glow);
            Lines.stroke(1.2f);
            Lines.square(x + jx, y + jy, size * tilesize / 2f - 1.5f);
            if(phase == 5 && items.total() > 0){
                Draw.color();
                Item top = items.first();
                if(top != null) Draw.rect(top.fullIcon, x + size * tilesize / 2f - 4f, y + size * tilesize / 2f - 4f, 6f, 6f);
            }
            Drawf.light(x, y, 30f + 30f * heat, Tmp.c1.set(c).lerp(Color.valueOf("ff6a3a"), heat), 0.3f + 0.4f * heat);
            Draw.reset();
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.b(phase);
            write.f(time);
            write.f(heat);
            write.f(mined);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            phase = read.b();
            time = read.f();
            heat = read.f();
            mined = read.f();
        }
    }
}
