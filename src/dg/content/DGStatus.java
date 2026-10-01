package dg.content;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;

public class DGStatus{
    public static StatusEffect chilled, frozen, toxic;

    public static void load(){
        chilled = new StatusEffect("chilled"){{
            color = DGFx.cryo;
            speedMultiplier = 0.45f;
            reloadMultiplier = 0.8f;
            effect = DGTurretFx.frostMote;
            effectChance = 0.08f;
            init(() -> opposite(StatusEffects.burning, StatusEffects.melting));
        }};

        frozen = new StatusEffect("frozen"){{
            color = Color.valueOf("d8f6ff");
            speedMultiplier = 0.001f;
            reloadMultiplier = 0.001f;
            dragMultiplier = 6f;
            disarm = true;
            healthMultiplier = 0.85f;
            init(() -> opposite(StatusEffects.burning, StatusEffects.melting));
        }

            @Override
            public void draw(Unit unit, float time){
                float s = unit.hitSize * 0.75f + 3f, a = Mathf.clamp(time / 20f);
                Draw.z(Layer.flyingUnit + 0.5f);
                Draw.color(Tmp.c1.set(DGFx.cryo).a(0.42f * a));
                Fill.poly(unit.x, unit.y, 6, s, unit.id * 37f);
                Lines.stroke(1.4f, Tmp.c1.set(Color.white).a(0.75f * a));
                Lines.poly(unit.x, unit.y, 6, s, unit.id * 37f);
                Draw.color(Tmp.c1.set(Color.white).a(0.55f * a));
                for(int i = 0; i < 3; i++){
                    float ang = unit.id * 37f + i * 120f + 30f;
                    Lines.lineAngle(unit.x + Angles.trnsx(ang, s * 0.2f), unit.y + Angles.trnsy(ang, s * 0.2f), ang, s * 0.55f);
                }
                Draw.reset();
            }

            @Override
            public void update(Unit unit, float time){
                super.update(unit, time);
                if(time <= Time.delta + 0.001f) DGTurretFx.iceShatter.at(unit.x, unit.y, unit.hitSize);
            }
        };

        toxic = new StatusEffect("toxic"){{
            color = Color.valueOf("9ad15b");
            damage = 0.35f;
            healthMultiplier = 0.8f;
            speedMultiplier = 0.85f;
            effect = DGTurretFx.toxicBubble;
            effectChance = 0.1f;
        }};
    }
}
