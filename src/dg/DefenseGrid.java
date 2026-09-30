package dg;

import arc.*;
import dg.content.DGTechTree;
import dg.content.turrets.DGTurrets;
import dg.world.FiniteOres;
import mindustry.game.EventType.*;
import mindustry.gen.Icon;
import mindustry.mod.*;

import static mindustry.Vars.*;

public class DefenseGrid extends Mod{

    public DefenseGrid(){
        Events.on(ClientLoadEvent.class, e -> ui.settings.addCategory(Core.bundle.get("setting.dg-category"), Icon.production, t -> {
            t.checkPref("dg-finite-ores", true);
            t.sliderPref("dg-ore-richness", 4, 1, 16, i -> (i * 25) + "%");
        }));
    }

    @Override
    public void init(){
        FiniteOres.init();
    }

    @Override
    public void loadContent(){
        DGTurrets.load();
        DGTechTree.load();
    }
}
