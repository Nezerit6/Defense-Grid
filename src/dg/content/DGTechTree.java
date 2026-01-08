package dg.content;

import arc.struct.Seq;
import mindustry.content.*;
import mindustry.ctype.UnlockableContent;
import mindustry.game.Objectives;

import static dg.content.turrets.DGTurrets.*;

public class DGTechTree {

    public static void load() {
        addToNode(Blocks.arc, () -> {
            node(arclet, () -> {});
        });

        /*addToNode(Blocks.scatter, () -> {
            node(needler, () -> {});
        });*/

        addToNode(Blocks.salvo, () -> {
            node(shatter, () -> {});
        });

        addToNode(Blocks.lancer, () -> {
            node(cryolance, () -> {});
        });

        addToNode(Blocks.salvo, () -> {
            node(arcflash, () -> {});
        });
    }

    private static void addToNode(UnlockableContent parent, Runnable children) {
        TechTree.TechNode parentNode = parent.techNode;
        if (parentNode == null) {
            for (TechTree.TechNode node : TechTree.all) {
                if (node.content == parent) {
                    parentNode = node;
                    break;
                }
            }
        }

        if (parentNode != null) {
            TechTree.TechNode prev = TechTree.context();
            try {
                java.lang.reflect.Field contextField = TechTree.class.getDeclaredField("context");
                contextField.setAccessible(true);
                contextField.set(null, parentNode);

                children.run();

                contextField.set(null, prev);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static TechTree.TechNode node(UnlockableContent content, Runnable children) {
        return TechTree.node(content, content.researchRequirements(), children);
    }

    private static TechTree.TechNode node(UnlockableContent content, mindustry.type.ItemStack[] requirements, Runnable children) {
        return TechTree.node(content, requirements, (Seq<Objectives.Objective>) null, children);
    }
}