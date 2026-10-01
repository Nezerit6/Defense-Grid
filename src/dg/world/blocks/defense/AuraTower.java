package dg.world.blocks.defense;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

public class AuraTower extends Block{
    public float range = 90f, interval = 12f, statusDuration = 45f, damage = 0f;
    public StatusEffect status = StatusEffects.slow;
    public Color color = Color.white;
    public boolean targetAir = true, targetGround = true;
    public Effect moteEffect = Fx.none;
    public float moteChance = 0.25f;

    public AuraTower(String name){
        super(name);
        update = true;
        solid = true;
        group = BlockGroup.turrets;
        flags = EnumSet.of(BlockFlag.turret);
        priority = TargetPriority.turret;
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
        stats.add(Stat.targetsAir, targetAir);
        stats.add(Stat.targetsGround, targetGround);
        stats.add(Stat.abilities, status.emoji() + " " + status.localizedName);
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        super.drawPlace(x, y, rotation, valid);
        Drawf.dashCircle(x * tilesize + offset, y * tilesize + offset, range, color);
    }

    public class AuraBuild extends Building{
        public float timer, warm, spin;

        @Override
        public void updateTile(){
            warm = Mathf.lerpDelta(warm, efficiency, 0.03f);
            spin += Time.delta * warm;
            timer += Time.delta;

            if(timer >= interval && efficiency > 0f){
                timer = 0f;
                Units.nearbyEnemies(team, x, y, range, u -> {
                    if(u.dead || !u.checkTarget(targetAir, targetGround) || !within(u, range)) return;
                    u.apply(status, statusDuration);
                    if(damage > 0f) u.damage(damage * interval / 60f);
                });
            }

            if(!headless && warm > 0.05f && Mathf.chanceDelta(moteChance * warm)){
                Tmp.v1.rnd(Mathf.sqrt(Mathf.random()) * range);
                moteEffect.at(x + Tmp.v1.x, y + Tmp.v1.y, 0f, color);
            }
        }

        @Override
        public void draw(){
            super.draw();
            if(warm < 0.01f) return;

            Draw.z(Layer.floor + 0.1f);
            Draw.blend(Blending.additive);
            Fill.light(x, y, 40, range, Tmp.c1.set(color).a(0.03f * warm), Tmp.c2.set(color).a(0.13f * warm));
            Draw.blend();

            Draw.z(Layer.bullet - 0.5f);
            Lines.stroke(1.2f, Tmp.c1.set(color).a(0.5f * warm));
            int segs = 24;
            for(int i = 0; i < segs; i++){
                float a = spin * 0.4f + i * 360f / segs;
                Lines.arc(x, y, range, 0.5f / segs, a);
            }
            Lines.stroke(1f, Tmp.c1.set(Color.white).a(0.35f * warm));
            for(int i = 0; i < 6; i++){
                float a = -spin * 0.8f + i * 60f, r = range * (0.35f + 0.08f * Mathf.sin(spin * 0.05f + i));
                Lines.lineAngleCenter(x + Angles.trnsx(a, r), y + Angles.trnsy(a, r), a, 6f);
            }
            Drawf.light(x, y, range * 0.9f, color, 0.35f * warm);
            Draw.reset();
        }

        @Override
        public void drawSelect(){
            Drawf.dashCircle(x, y, range, color);
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.f(warm);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            warm = read.f();
        }
    }
}
