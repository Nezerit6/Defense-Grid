package dg.ui;

import arc.*;
import arc.input.KeyCode;
import arc.scene.ui.layout.*;
import arc.math.*;
import dg.content.DGShowcase;
import mindustry.entities.*;
import mindustry.game.EventType.*;
import mindustry.ui.dialogs.*;

import static mindustry.Vars.*;

public class EffectLab{
    public static KeyCode openKey = KeyCode.k, spawnKey = KeyCode.j;
    static Effect current;
    static String currentName = "";
    static BaseDialog dialog;

    public static void init(){
        DGShowcase.load();

        dialog = new BaseDialog("Effect Lab");
        dialog.addCloseButton();
        Table list = new Table();
        int i = 0;
        for(String name : DGShowcase.all.keys()){
            Effect fx = DGShowcase.all.get(name);
            list.button(name, () -> {
                current = fx;
                currentName = name;
                dialog.hide();
                spawn(Core.camera.position.x, Core.camera.position.y);
            }).size(210f, 52f).pad(4f);
            if(++i % 4 == 0) list.row();
        }
        dialog.cont.add("[lightgray]K: open this menu   J: play the selected effect at the cursor").padBottom(10f).row();
        dialog.cont.pane(list).grow();

        Events.run(Trigger.update, () -> {
            if(!state.isGame() || Core.scene.hasKeyboard() || Core.scene.hasDialog() && !dialog.isShown()) return;
            if(Core.input.keyTap(openKey)){
                if(dialog.isShown()) dialog.hide();
                else dialog.show();
            }
            if(current != null && !dialog.isShown() && Core.input.keyTap(spawnKey)){
                spawn(Core.input.mouseWorldX(), Core.input.mouseWorldY());
            }
        });
    }

    static void spawn(float x, float y){
        current.at(x, y, Mathf.random(360f));
    }
}
