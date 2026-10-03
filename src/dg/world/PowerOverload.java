package dg.world;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import dg.content.DGFx;
import mindustry.content.*;
import mindustry.core.Renderer;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.Bar;
import mindustry.world.Block;
import mindustry.world.blocks.power.PowerNode;
import mindustry.world.blocks.power.PowerNode.PowerNodeBuild;

import static mindustry.Vars.*;

public class PowerOverload{
    public static final Color sparkColor = Color.valueOf("fff36b"), hotColor = Color.valueOf("ff6a3d");
    public static float overloadTime = 300f;

    static final ObjectFloatMap<Block> limits = new ObjectFloatMap<>();
    static final Seq<Teamc> targets = new Seq<>();

    public static void init(){
        limits.put(Blocks.powerNode, 1500f);
        limits.put(Blocks.powerNodeLarge, 6000f);
        limits.put(Blocks.surgeTower, 20000f);

        for(Block block : content.blocks()){
            if(!(block instanceof PowerNode) || block.buildType.get().getClass() != PowerNodeBuild.class) continue;
            PowerNode node = (PowerNode)block;
            if(!limits.containsKey(node)) limits.put(node, 1500f * node.size * node.size);
            node.update = true;

            node.buildType = () -> node.new PowerNodeBuild(){
                float overheat, flicker;

                @Override
                public void updateTile(){
                    super.updateTile();
                    if(!enabled()) return;

                    float load = load(this);
                    if(load > 1f){
                        overheat += Time.delta * Math.min(load, 3f) / overloadTime;
                    }else{
                        overheat = Math.max(overheat - Time.delta / overloadTime, 0f);
                    }

                    if(!headless && load > 0.02f){
                        float chance = Mathf.clamp(load) * 0.25f * node.size + overheat * 0.6f;
                        if(Mathf.chanceDelta(chance)){
                            DGFx.powerSpark.at(x + Mathf.range(node.size * 3f), y + Mathf.range(node.size * 3f), Mathf.clamp(load), overheat > 0f ? Tmp.c1.set(sparkColor).lerp(hotColor, overheat) : sparkColor);
                        }
                    }

                    if(overheat >= 1f) discharge(this, load);
                }

                @Override
                public void draw(){
                    super.draw();
                    if(!enabled() || Mathf.zero(Renderer.laserOpacity) || isPayload()) return;

                    float load = Mathf.clamp(load(this), 0f, 1.5f);
                    if(load <= 0.01f && overheat <= 0f) return;

                    flicker = overheat > 0f ? Mathf.absin(Time.time, 1.5f - overheat, overheat) : 0f;
                    Color color = Tmp.c2.set(sparkColor).lerp(hotColor, overheat);

                    Draw.z(Layer.power + 0.1f);
                    Draw.blend(Blending.additive);
                    for(int i = 0; i < power.links.size; i++){
                        Building link = world.build(power.links.get(i));
                        if(!node.linkValid(this, link)) continue;
                        if(link.block instanceof PowerNode && link.id >= id) continue;

                        Draw.color(color, Renderer.laserOpacity * Mathf.clamp(0.12f + 0.55f * load + flicker * 0.4f));
                        float angle = Angles.angle(x, y, link.x, link.y), vx = Mathf.cosDeg(angle), vy = Mathf.sinDeg(angle);
                        float len1 = node.size * tilesize / 2f - 1.5f, len2 = link.block.size * tilesize / 2f - 1.5f;
                        float x1 = x + vx * len1, y1 = y + vy * len1, x2 = link.x - vx * len2, y2 = link.y - vy * len2;
                        Drawf.laser(node.laser, node.laserEnd, x1, y1, x2, y2, node.laserScale * (0.5f + 0.7f * load));

                        if(!headless && Mathf.chanceDelta(0.03f * load + overheat * 0.1f)){
                            float t = Mathf.random();
                            DGFx.powerSpark.at(Mathf.lerp(x1, x2, t), Mathf.lerp(y1, y2, t), Mathf.clamp(load), color);
                        }
                    }

                    Draw.color(color, Mathf.clamp(0.2f + 0.5f * load + flicker));
                    Draw.rect("circle-shadow", x, y, node.size * tilesize * (1.2f + load * 0.8f), node.size * tilesize * (1.2f + load * 0.8f));
                    Draw.blend();
                    Draw.reset();

                    Drawf.light(x, y, node.size * 20f * (0.5f + load), color, 0.4f + 0.4f * Mathf.clamp(load) + flicker * 0.3f);
                }

                @Override
                public void drawSelect(){
                    super.drawSelect();
                    if(overheat > 0f) Drawf.dashCircle(x, y, radius(node), Tmp.c1.set(hotColor).a(0.5f + overheat * 0.5f));
                }
            };

            node.addBar("dg-power-load", (PowerNodeBuild b) -> new Bar(
                () -> Core.bundle.format("bar.dg-power-load", (int)(Mathf.clamp(load(b), 0f, 9.99f) * 100)),
                () -> Tmp.c3.set(sparkColor).lerp(hotColor, Mathf.clamp(load(b) - 0.5f)),
                () -> Mathf.clamp(load(b))
            ));
        }
    }

    public static boolean enabled(){
        return Core.settings.getBool("dg-power-overload", true);
    }

    public static float limit(Block block){
        return limits.get(block, 1500f) * Core.settings.getInt("dg-power-limit", 4) / 4f;
    }

    public static float flow(Building build){
        if(build.power == null || build.power.graph == null) return 0f;
        return build.power.graph.getLastPowerProduced() * 60f / Math.max(Time.delta, 0.001f);
    }

    public static float load(Building build){
        return flow(build) / limit(build.block);
    }

    static float radius(Block block){
        return block.size * tilesize * 4f + 24f;
    }

    static void discharge(Building build, float load){
        float range = radius(build.block);
        int bolts = 3 + build.block.size * 2;
        float damage = 40f * build.block.size * Math.min(load, 3f);

        targets.clear();
        Units.nearby(null, build.x, build.y, range, u -> {
            if(!u.isFlying() && u.isValid()) targets.add(u);
        });
        indexer.eachBlock(null, build.x, build.y, range, other -> other != build, targets::add);
        targets.sort(t -> t.dst2(build));
        if(targets.size > bolts) targets.truncate(bolts);

        DGFx.nodeBlast.at(build.x, build.y, build.block.size, sparkColor);
        Sounds.spark.at(build.x, build.y, 0.8f, 1.5f);
        Effect.shake(3f, 20f, build);

        for(Teamc target : targets){
            DGFx.chainArc.at(build.x, build.y, 4f, sparkColor, target);
            if(net.client()) continue;
            if(target instanceof Unit){
                ((Unit)target).damage(damage);
                ((Unit)target).apply(StatusEffects.shocked, 120f);
            }else if(target instanceof Building){
                ((Building)target).damage(damage);
            }
        }

        if(!net.client()) build.kill();
    }
}
