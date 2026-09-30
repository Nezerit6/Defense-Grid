package dg.world.blocks.turrets;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import arc.util.io.*;
import arc.math.geom.Vec2;
import dg.content.DGFx;
import dg.graphics.DGDraw3D;
import dg.world.draw.DrawEmptyTurret;
import mindustry.entities.bullet.BulletType;
import mindustry.graphics.*;
import mindustry.world.blocks.defense.turrets.PowerTurret;
import mindustry.world.blocks.defense.turrets.Turret.TurretBuild;

public class CapacitorTurret extends PowerTurret{
    public int maxStacks = 12;
    public float stackTime = 40f, releaseInterval = 4f, orbitRadius = 9f, orbitHeight = 4f, orbitTilt = 55f;
    public Color orbColor = DGFx.gold, orbCore = Color.white;

    public CapacitorTurret(String name){
        super(name);
        drawer = new DrawEmptyTurret(){
            @Override
            public void drawExtra(TurretBuild build){
                ((CapacitorBuild)build).drawOrbs();
            }
        };
    }

    public class CapacitorBuild extends PowerTurretBuild{
        public int stacks;
        public float stackTimer, releaseTimer, spin, flash;

        @Override
        public void updateTile(){
            super.updateTile();

            spin += Time.delta * (isShooting() ? 6f : 1.2f);
            flash = Math.max(flash - Time.delta / 12f, 0f);

            if(!isShooting()){
                releaseTimer = 0f;
                if(stacks < maxStacks && efficiency > 0f){
                    stackTimer += Time.delta * efficiency;
                    if(stackTimer >= stackTime){
                        stackTimer = 0f;
                        stacks++;
                        Tmp.v1.set(orbPos(stacks - 1, stacks));
                        DGFx.capacitorStack.at(Tmp.v1.x, Tmp.v1.y);
                    }
                }
            }else if(stacks > 0 && target != null && efficiency > 0f){
                releaseTimer += Time.delta;
                if(releaseTimer >= releaseInterval){
                    releaseTimer = 0f;
                    release();
                }
            }
        }

        void release(){
            stacks--;
            Vec2 pos = orbPos(stacks, stacks + 1);
            float ox = pos.x, oy = pos.y;
            BulletType type = peekAmmo();
            float angle = Angles.angle(ox, oy, targetPos.x, targetPos.y) + Mathf.range(12f);
            type.create(this, team, ox, oy, angle, -1f, 1f, 1f, null, null, targetPos.x, targetPos.y);
            DGFx.capacitorShoot.at(ox, oy, angle);
            shootSound.at(ox, oy, 1f + (maxStacks - stacks) * 0.04f);
            heat = 1f;
            flash = 1f;
        }

        Vec2 orbPos(int index, int count){
            float a = spin + index * 360f / Math.max(count, 1);
            float sa = Mathf.sinDeg(a);
            float wz = orbitHeight + sa * Mathf.sinDeg(orbitTilt) * orbitRadius * 0.3f;
            return Tmp.v2.set(DGDraw3D.x(x + Mathf.cosDeg(a) * orbitRadius, wz), DGDraw3D.y(y + sa * Mathf.cosDeg(orbitTilt) * orbitRadius, wz));
        }

        void drawOrbs(){
            float full = stacks / (float)maxStacks;

            Draw.z(Layer.turret + 0.5f);
            Draw.color(orbColor, 0.25f + 0.4f * full);
            Lines.stroke(1f);
            Lines.circle(x, y, 3f + 2f * full);
            Draw.color(orbColor, 0.6f);
            Fill.circle(x, y, 1.5f + 1.5f * full + (flash * 1.5f));

            for(int pass = 0; pass < 2; pass++){
                for(int i = 0; i < stacks; i++){
                    float a = spin + i * 360f / stacks;
                    float sa = Mathf.sinDeg(a), depth = sa * Mathf.sinDeg(orbitTilt);
                    if((depth > 0f) != (pass == 1)) continue;
                    float wz = orbitHeight + depth * orbitRadius * 0.3f;
                    float wx = x + Mathf.cosDeg(a) * orbitRadius, wy = y + sa * Mathf.cosDeg(orbitTilt) * orbitRadius;
                    float px = DGDraw3D.x(wx, wz), py = DGDraw3D.y(wy, wz), s = DGDraw3D.scale(wz);

                    Draw.z(Layer.groundUnit - 1f);
                    Draw.color(Pal.shadow);
                    Fill.circle(wx - DGDraw3D.shadowOffset(wz), wy - DGDraw3D.shadowOffset(wz), 1.3f);

                    Draw.z(Layer.effect + (pass == 1 ? 0.01f : -0.01f));
                    Draw.color(Tmp.c1.set(orbColor).mul(0.7f + 0.3f * (depth + 1f) / 2f));
                    Fill.circle(px, py, 1.7f * s);
                    Draw.color(orbCore);
                    Fill.circle(px, py, 0.9f * s);
                    Drawf.light(px, py, 10f * s, orbColor, 0.5f);
                }
            }
            Draw.reset();
        }

        @Override
        public byte version(){
            return 2;
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.i(stacks);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            if(revision >= 2) stacks = read.i();
        }
    }
}
