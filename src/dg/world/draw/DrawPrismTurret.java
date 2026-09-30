package dg.world.draw;

import arc.graphics.g2d.*;
import mindustry.entities.part.DrawPart;
import mindustry.gen.Building;
import mindustry.graphics.*;
import mindustry.world.Block;
import mindustry.world.blocks.defense.turrets.Turret;
import mindustry.world.blocks.defense.turrets.Turret.TurretBuild;
import mindustry.world.draw.DrawTurret;

public class DrawPrismTurret extends DrawTurret{
    @Override
    public void draw(Building build){
        Turret turret = (Turret)build.block;
        TurretBuild tb = (TurretBuild)build;

        Draw.rect(base, build.x, build.y);
        Draw.color();
        Draw.z(Layer.turret);

        float progress = tb.progress();
        DrawPart.PartParams params = DrawPart.params.set(build.warmup(), 1f - progress, 1f - progress, tb.heat, tb.curRecoil, tb.charge, tb.x + tb.recoilOffset.x, tb.y + tb.recoilOffset.y, tb.rotation);
        for(DrawPart part : parts){
            params.setRecoil(part.recoilIndex >= 0 && tb.curRecoils != null ? tb.curRecoils[part.recoilIndex] : tb.curRecoil);
            part.draw(params);
        }
    }

    @Override
    public TextureRegion[] icons(Block block){
        return new TextureRegion[]{base};
    }
}
