package dg.world.radar;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import mindustry.game.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.blocks.defense.turrets.*;

import static mindustry.Vars.*;

public class RadarNet{
    public static float memory = 240f, alarmCooldown = 360f;
    static final ObjectSet<RadarBlock.RadarBuild> radars = new ObjectSet<>();
    static final IntMap<IntFloatMap> seen = new IntMap<>();
    static final Seq<RadarBlock.RadarBuild> tmp = new Seq<>();
    static float lastAlarm = -9999f;
    static Unit best;
    static float bestDst;

    public static void init(){
        Events.on(WorldLoadEvent.class, e -> {
            radars.clear();
            seen.clear();
        });
        if(!headless) Events.run(Trigger.draw, RadarNet::draw);
    }

    static IntFloatMap map(Team team){
        IntFloatMap m = seen.get(team.id);
        if(m == null) seen.put(team.id, m = new IntFloatMap());
        return m;
    }

    public static Seq<RadarBlock.RadarBuild> radars(){
        tmp.clear();
        for(RadarBlock.RadarBuild r : radars){
            if(r.isValid()) tmp.add(r);
        }
        if(tmp.size != radars.size){
            radars.clear();
            radars.addAll(tmp);
        }
        return tmp;
    }

    public static boolean detected(Team team, Unit u){
        return map(team).get(u.id, 0f) > Time.time;
    }

    public static void mark(Team team, Unit u){
        boolean fresh = !detected(team, u);
        map(team).put(u.id, Time.time + memory);
        if(fresh && !headless && player != null && team == player.team() && !u.isGrounded() && Core.settings.getBool("dg-radar-alarm", true)){
            RadarFx.contact.at(u.x, u.y, u.hitSize);
            if(Time.time - lastAlarm > alarmCooldown){
                lastAlarm = Time.time;
                Sounds.message.play(1f, 0.8f, 0f);
                Time.run(14f, () -> Sounds.message.play(1f, 0.8f, 0f));
                ui.showInfoToast(Core.bundle.format("dg-radar-alarm", u.type.localizedName), 3f);
            }
        }
    }

    public static boolean covered(Team team, float x, float y){
        for(RadarBlock.RadarBuild r : radars()){
            if(r.team == team && r.efficiency > 0f && r.within(x, y, r.range())) return true;
        }
        return false;
    }

    public static Unit closest(Team team, float x, float y, float range){
        best = null;
        bestDst = range;
        IntFloatMap m = map(team);
        Groups.unit.each(u -> {
            if(u.team == team || u.dead || u.isGrounded() || m.get(u.id, 0f) <= Time.time) return;
            float d = u.dst(x, y);
            if(d < bestDst){
                bestDst = d;
                best = u;
            }
        });
        return best;
    }

    static boolean showNetwork(){
        if(control.input.block instanceof RadarBlock || control.input.block instanceof SamTurret) return true;
        if(control.input.config.getSelected() instanceof RadarBlock.RadarBuild || control.input.config.getSelected() instanceof SamTurret.SamBuild) return true;
        mindustry.world.Tile t = world.tileWorld(Core.input.mouseWorldX(), Core.input.mouseWorldY());
        return t != null && t.build != null && (t.build instanceof RadarBlock.RadarBuild || t.build instanceof SamTurret.SamBuild);
    }

    static void draw(){
        if(!state.isGame() || player == null) return;
        Team team = player.team();
        IntFloatMap m = map(team);

        Draw.z(Layer.overlayUI - 1f);
        Groups.unit.each(u -> {
            float exp = m.get(u.id, 0f);
            if(u.team == team || exp <= Time.time) return;
            float a = Mathf.clamp((exp - Time.time) / memory) * (0.6f + Mathf.absin(Time.time, 5f, 0.4f));
            float s = u.hitSize * 0.8f + 4f;
            Lines.stroke(1.2f, Tmp.c1.set(Pal.remove).a(a));
            Lines.square(u.x, u.y, s, 45f + Time.time * 2f);
            for(int i = 0; i < 4; i++){
                float ang = i * 90f;
                Lines.lineAngle(u.x + Angles.trnsx(ang, s + 2f), u.y + Angles.trnsy(ang, s + 2f), ang, 3f);
            }
        });

        if(!showNetwork()) return;
        Seq<RadarBlock.RadarBuild> list = radars();
        Draw.z(Layer.overlayUI - 1f);
        Lines.stroke(1.6f, Tmp.c1.set(Pal.remove).a(0.8f));
        int segs = 90;
        for(RadarBlock.RadarBuild r : list){
            if(r.team != team) continue;
            float R = r.range();
            for(int i = 0; i < segs; i++){
                float a1 = i * 360f / segs, a2 = (i + 1) * 360f / segs, am = (a1 + a2) / 2f;
                float mx = r.x + Angles.trnsx(am, R), my = r.y + Angles.trnsy(am, R);
                boolean inside = false;
                for(RadarBlock.RadarBuild o : list){
                    if(o != r && o.team == team && o.within(mx, my, o.range() - 0.5f)){
                        inside = true;
                        break;
                    }
                }
                if(!inside){
                    Lines.line(r.x + Angles.trnsx(a1, R), r.y + Angles.trnsy(a1, R), r.x + Angles.trnsx(a2, R), r.y + Angles.trnsy(a2, R));
                }
            }
        }
        Groups.build.each(b -> b instanceof SamTurret.SamBuild && b.team == team, b -> {
            Lines.stroke(1.2f, Tmp.c1.set(Pal.heal).a(0.8f));
            Lines.dashCircle(b.x, b.y, ((Turret.TurretBuild)b).range());
        });
        Draw.reset();
    }
}
