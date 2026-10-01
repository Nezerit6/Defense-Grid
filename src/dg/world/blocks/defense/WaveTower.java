package dg.world.blocks.defense;

import arc.*;
import arc.audio.*;
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

public class WaveTower extends Block{
    public float range = 120f, reload = 90f, waveSpeed = 2.6f;
    public Color color = Color.white;
    public boolean targetAir = true, targetGround = true;
    public float force = 0f, damage = 0f;
    public StatusEffect status = StatusEffects.none;
    public float statusDuration = 0f;
    public boolean toggleGround = false, bullets = false;
    public int chevrons = 0;
    public Sound waveSound = Sounds.none;
    public Effect hitEffect = Fx.none, waveEffect = Fx.none;

    public WaveTower(String name){
        super(name);
        update = true;
        solid = true;
        group = BlockGroup.turrets;
        flags = EnumSet.of(BlockFlag.turret);
        priority = TargetPriority.turret;
        envEnabled |= Env.space;
        config(Boolean.class, (WaveBuild b, Boolean v) -> b.groundOnly = v);
    }

    @Override
    public void init(){
        configurable = toggleGround;
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
        stats.add(Stat.reload, 60f / reload, StatUnit.perSecond);
        if(!bullets){
            stats.add(Stat.targetsAir, targetAir);
            stats.add(Stat.targetsGround, targetGround);
        }
        if(damage > 0f) stats.add(Stat.damage, damage);
        if(status != StatusEffects.none) stats.add(Stat.abilities, status.emoji() + " " + status.localizedName);
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        super.drawPlace(x, y, rotation, valid);
        Drawf.dashCircle(x * tilesize + offset, y * tilesize + offset, range, color);
    }

    public class WaveBuild extends Building{
        public final FloatSeq waves = new FloatSeq(), modes = new FloatSeq();
        public float charge, glow;
        public boolean groundOnly;
        boolean found;

        public boolean affects(Unit u){
            return !u.dead && u.checkTarget(targetAir && !groundOnly, targetGround);
        }

        boolean hasTargets(){
            found = false;
            if(bullets){
                Groups.bullet.intersect(x - range, y - range, range * 2f, range * 2f, b -> {
                    if(!found && b.team != team && b.type.hittable && within(b, range)) found = true;
                });
            }else{
                found = Units.closestEnemy(team, x, y, range, this::affects) != null;
            }
            return found;
        }

        @Override
        public void updateTile(){
            glow = Mathf.lerpDelta(glow, efficiency, 0.05f);
            charge = Math.min(charge + edelta(), reload);

            if(charge >= reload && efficiency > 0f && hasTargets()){
                charge = 0f;
                boolean boosted = bullets && optionalEfficiency > 0f;
                waves.add(0f);
                modes.add(boosted ? 1f : 0f);
                if(boosted) consume();
                waveSound.at(x, y, Mathf.random(0.9f, 1.1f));
                waveEffect.at(x, y, range, color);
            }

            for(int i = waves.size - 1; i >= 0; i--){
                float r0 = waves.get(i), r1 = r0 + waveSpeed * Time.delta;
                pass(r0, r1, modes.get(i) > 0f);
                if(r1 > range){
                    waves.removeIndex(i);
                    modes.removeIndex(i);
                }else{
                    waves.set(i, r1);
                }
            }
        }

        void pass(float r0, float r1, boolean reflect){
            if(bullets){
                Groups.bullet.intersect(x - r1, y - r1, r1 * 2f, r1 * 2f, b -> {
                    if(b.team == team || !b.type.hittable) return;
                    float d = dst(b);
                    if(d < r0 || d >= r1) return;
                    if(reflect){
                        b.team(team);
                        b.owner = this;
                        b.vel.scl(-1f);
                        b.time = 0f;
                        b.collided.clear();
                        hitEffect.at(b.x, b.y, b.rotation(), color);
                    }else{
                        hitEffect.at(b.x, b.y, b.rotation(), Tmp.c1.set(color).lerp(Color.white, 0.4f));
                        b.hit = true;
                        b.remove();
                    }
                });
                return;
            }

            Units.nearbyEnemies(team, x, y, r1 + 16f, u -> {
                if(!affects(u)) return;
                float d = dst(u);
                if(d < r0 || d >= r1) return;
                hit(u, d);
            });
        }

        void hit(Unit u, float d){
            if(damage > 0f) u.damage(damage);
            if(status != StatusEffects.none) u.apply(status, statusDuration);
            if(force != 0f){
                float falloff = force > 0f ? Mathf.clamp(d / (range * 0.35f)) : 1f - 0.4f * d / range;
                Tmp.v1.set(x - u.x, y - u.y).setLength(Math.abs(force) * falloff * 14f * Mathf.sqrt(u.mass())).scl(Mathf.sign(force));
                u.impulseNet(Tmp.v1);
            }
            hitEffect.at(u.x, u.y, angleTo(u), color);
        }

        @Override
        public void draw(){
            super.draw();

            float f = charge / reload;
            Draw.z(Layer.bullet - 0.5f);
            Draw.blend(Blending.additive);
            Fill.light(x, y, 18, (2f + size * 3f * f) * glow, Tmp.c1.set(Color.white).a(0.7f * f * glow), Tmp.c2.set(color).a(0f));
            Draw.blend();
            Drawf.light(x, y, (size * 12f + 20f * f) * glow, color, 0.7f * glow);

            for(int i = 0; i < waves.size; i++){
                float r = waves.get(i), fin = r / range, a = 1f - Interp.pow2In.apply(fin);
                Color c = modes.get(i) > 0f ? Pal.accent : color;

                Draw.z(Layer.bullet - 0.4f);
                Draw.blend(Blending.additive);
                Lines.stroke(7f * a, Tmp.c1.set(c).a(0.18f * a));
                Lines.circle(x, y, r - 3f);
                Lines.stroke(2.2f * a, Tmp.c1.set(c).lerp(Color.white, 0.5f).a(0.9f * a));
                Lines.circle(x, y, r);
                Draw.blend();

                if(chevrons > 0){
                    float dir = force >= 0f ? 1f : -1f, len = 5f * a + 1f;
                    Lines.stroke(1.5f * a, Tmp.c1.set(c).a(0.85f * a));
                    for(int j = 0; j < chevrons; j++){
                        float ang = j * 360f / chevrons + r * 0.6f * dir;
                        float cx = x + Angles.trnsx(ang, r), cy = y + Angles.trnsy(ang, r);
                        float back = ang + (dir > 0f ? 0f : 180f);
                        Lines.lineAngle(cx, cy, back + 145f, len);
                        Lines.lineAngle(cx, cy, back - 145f, len);
                    }
                }
            }

            if(groundOnly){
                Draw.z(Layer.bullet - 0.4f);
                Lines.stroke(1f, Tmp.c1.set(color).a(0.6f));
                Lines.square(x, y, size * tilesize / 2f + 2f, 45f);
            }
            Draw.reset();
        }

        @Override
        public void drawSelect(){
            Drawf.dashCircle(x, y, range, color);
        }

        @Override
        public boolean configTapped(){
            boolean next = !groundOnly;
            configure(next);
            ui.showInfoToast(Core.bundle.get(next ? "dg-wave-ground" : "dg-wave-all"), 2f);
            return false;
        }

        @Override
        public Object config(){
            return groundOnly;
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.f(charge);
            write.bool(groundOnly);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            charge = read.f();
            groundOnly = read.bool();
        }
    }
}
