package dg.world.blocks.turrets;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.blocks.defense.turrets.*;

import static mindustry.Vars.*;

public class FixedLaserTurret extends PowerTurret{
    public int steps = 8;
    public float aimSpeed = 2.5f, beamWidth = 10f;
    public Color aimColor = Color.valueOf("ff6a6a");

    public FixedLaserTurret(String name){
        super(name);
        rotate = true;
        quickRotate = false;
        rotateDraw = false;
        drawArrow = true;
        configurable = true;
        config(Integer.class, (FixedLaserBuild b, Integer v) -> b.aim = Mathf.mod(v, steps));
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        super.drawPlace(x, y, rotation, valid);
        float wx = x * tilesize + offset, wy = y * tilesize + offset, ang = rotation * 90f;
        Drawf.dashLine(aimColor, wx, wy, wx + Angles.trnsx(ang, range), wy + Angles.trnsy(ang, range));
    }

    public class FixedLaserBuild extends PowerTurretBuild{
        public int aim = -1;

        public float facing(){
            return aim * 360f / steps;
        }

        boolean inLine(Posc p, float size){
            Tmp.v1.set(p.getX() - x, p.getY() - y).rotate(-facing());
            return Tmp.v1.x > 0f && Math.abs(Tmp.v1.y) < beamWidth + size / 2f;
        }

        @Override
        public void created(){
            super.created();
            if(aim < 0) aim = Mathf.mod(Math.round(rotdeg() / (360f / steps)), steps);
            rotation = facing();
        }

        @Override
        public void updateTile(){
            if(aim < 0) aim = Mathf.mod(Math.round(rotdeg() / (360f / steps)), steps);
            super.updateTile();
            if(!isControlled()) rotation = Angles.moveToward(rotation, facing(), aimSpeed * delta());
        }

        @Override
        protected void turnToTarget(float targetRot){
            if(isControlled()) super.turnToTarget(targetRot);
        }

        @Override
        protected void findTarget(){
            target = Units.bestTarget(team, x, y, range(),
                e -> !e.dead() && e.checkTarget(targetAir, targetGround) && inLine(e, e.hitSize),
                b -> targetGround && inLine(b, b.block.size * tilesize), unitSort);
        }

        @Override
        public boolean configTapped(){
            configure(aim + 1);
            return false;
        }

        @Override
        public Object config(){
            return aim;
        }

        @Override
        public void drawSelect(){
            super.drawSelect();
            float f = facing();
            Drawf.dashLine(aimColor, x, y, x + Angles.trnsx(f, range()), y + Angles.trnsy(f, range()));
        }

        @Override
        public void draw(){
            super.draw();
            Draw.z(Layer.bullet - 1f);
            Draw.blend(Blending.additive);
            float len = range() * (0.25f + 0.1f * Mathf.absin(Time.time, 6f, 1f));
            Lines.stroke(1f, Tmp.c1.set(aimColor).a(0.35f * efficiency));
            Lines.lineAngle(x, y, rotation, len);
            Draw.blend();
            Draw.reset();
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.b(aim);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            aim = read.b();
        }
    }
}
