package dg;

import arc.*;
import dg.content.DGStatus;
import dg.content.DGTechTree;
import dg.content.turrets.DGArsenal;
import dg.content.turrets.DGTurrets;
import dg.world.FiniteOres;
import mindustry.game.EventType.*;
import mindustry.gen.Icon;
import mindustry.mod.*;

import static mindustry.Vars.*;

public class DefenseGrid extends Mod{

    public DefenseGrid(){
        Events.on(ClientLoadEvent.class, e -> dg.ui.EffectLab.init());
        Events.on(ClientLoadEvent.class, e -> ui.settings.addCategory(Core.bundle.get("setting.dg-category"), Icon.production, t -> {
            t.checkPref("dg-finite-ores", true);
            t.sliderPref("dg-ore-richness", 4, 1, 16, i -> (i * 25) + "%");
            t.checkPref("dg-power-overload", true);
            t.checkPref("dg-fancy-destroy", true);
            t.sliderPref("dg-power-limit", 4, 1, 16, i -> (i * 25) + "%");
        }));
    }

    @Override
    public void init(){
        FiniteOres.init();
        dg.content.DGDestroyFx.init();
    }

    @Override
    public void loadContent(){
        DGStatus.load();
        DGTurrets.load();
        DGArsenal.load();
        DGTechTree.load();
    }
}
