package dg.world.radar;

import arc.*;
import mindustry.gen.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.meta.*;

public class SamTurret extends ItemTurret{
    public float networkRange = 640f;

    public SamTurret(String name){
        super(name);
        targetAir = true;
        targetGround = false;
    }

    @Override
    public void load(){
        super.load();
        if(!Core.atlas.has(name)) region = Core.atlas.find("block-" + size);
    }

    @Override
    public void setStats(){
        super.setStats();
        stats.add(Stat.abilities, Core.bundle.format("dg-sam-network", (int)(networkRange / 8f)));
    }

    public class SamBuild extends ItemTurretBuild{
        @Override
        protected void findTarget(){
            super.findTarget();
            if(target == null) target = RadarNet.closest(team, x, y, networkRange);
        }

        @Override
        protected boolean validateTarget(){
            if(!super.validateTarget()) return false;
            if(isControlled() || logicControlled()) return true;
            if(!(target instanceof Unit)) return target != null && within(target, range());
            Unit u = (Unit)target;
            return within(u, range() + u.hitSize / 2f) || RadarNet.detected(team, u) && within(u, networkRange);
        }
    }
}
