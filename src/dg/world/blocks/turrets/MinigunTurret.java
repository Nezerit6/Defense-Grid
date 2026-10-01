package dg.world.blocks.turrets;

import arc.*;
import arc.math.*;
import arc.util.io.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.world.blocks.defense.turrets.*;

public class MinigunTurret extends ItemTurret{
    public float spinUp = 1f / 200f, spinDown = 1f / 70f, minSpeed = 0.1f;

    public MinigunTurret(String name){
        super(name);
    }

    @Override
    public void setBars(){
        super.setBars();
        addBar("dg-spin", (MinigunBuild e) -> new Bar(() -> Core.bundle.format("bar.dg-spin", (int)(e.spin * 100)), () -> Pal.accent, () -> e.spin));
    }

    public class MinigunBuild extends ItemTurretBuild{
        public float spin;

        @Override
        public void updateTile(){
            super.updateTile();
            boolean firing = wasShooting && hasAmmo() && efficiency > 0f;
            spin = Mathf.approachDelta(spin, firing ? 1f : 0f, firing ? spinUp : spinDown);
        }

        @Override
        protected float baseReloadSpeed(){
            return super.baseReloadSpeed() * Mathf.lerp(minSpeed, 1f, spin * spin);
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.f(spin);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            spin = read.f();
        }
    }
}
