package dg;

import dg.content.DGTechTree;
import dg.content.turrets.DGTurrets;
import mindustry.mod.*;

public class DefenseGrid extends Mod{

    @Override
    public void loadContent(){
        DGTurrets.load();
        DGTechTree.load();
    }
}