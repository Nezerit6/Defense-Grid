package dg.world.weather;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import dg.graphics.DGDraw3D;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.type.weather.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

public class StormWeather extends ParticleWeather{
    public float blowInterval = 15f, blowChance = 0.012f, rainDensity = 420f;
    public Color rainColor = Color.valueOf("dbe8ff"), dim = Color.valueOf("1a2233");
    final Color rainDraw = new Color();

    static final Seq<Flyer> flyers = new Seq<>();
    static float blowTimer, flash, nextFlash = 300f;

    static class Flyer{
        Item item;
        Team team;
        float x, y, z, vx, vy, vz, time, spin;
    }

    public StormWeather(String name){
        super(name);
        color = Color.valueOf("9aa6b8");
        noiseColor = Color.valueOf("4c566b");
        particleRegion = "particle";
        drawNoise = true;
        useWindVector = true;
        noiseLayers = 2;
        sizeMin = 2f;
        sizeMax = 4.5f;
        minAlpha = 0.4f;
        maxAlpha = 0.9f;
        density = 4000f;
        baseSpeed = 9f;
        opacityMultiplier = 0.5f;
        force = 0.22f;
        status = StatusEffects.wet;
        statusDuration = 120f;
        attrs.set(Attribute.light, -0.35f);
        attrs.set(Attribute.water, 0.25f);
        sound = Sounds.rain;
        soundVol = 0.35f;
        duration = 5f * Time.toMinutes;
    }

    @Override
    public void update(WeatherState state){
        super.update(state);
        if(mindustry.Vars.state.isPaused()) return;

        if(!net.client()){
            blowTimer += Time.delta;
            if(blowTimer >= blowInterval){
                blowTimer = 0f;
                float windAngle = state.windVector.angle();
                Groups.build.each(b -> {
                    if(!(b instanceof Conveyor.ConveyorBuild) || b.block instanceof ArmoredConveyor) return;
                    Conveyor.ConveyorBuild c = (Conveyor.ConveyorBuild)b;
                    if(c.len <= 0) return;
                    float cross = Math.abs(Mathf.sinDeg(windAngle - c.rotation * 90f));
                    if(!Mathf.chance(blowChance * state.intensity * (0.25f + 0.75f * cross))) return;
                    int i = Mathf.random(c.len - 1);
                    Item item = c.ids[i];
                    if(item == null) return;
                    float along = (c.ys[i] - 0.5f) * tilesize, side = c.xs[i] * tilesize / 2f;
                    float ix = c.x + Angles.trnsx(c.rotation * 90f, along, side), iy = c.y + Angles.trnsy(c.rotation * 90f, along, side);
                    if(c.removeStack(item, 1) > 0) launch(item, c.team, ix, iy, state);
                });
            }
        }

        for(int i = flyers.size - 1; i >= 0; i--){
            Flyer f = flyers.get(i);
            f.time += Time.delta;
            f.vx += state.windVector.x * 0.02f * state.intensity * Time.delta;
            f.vy += state.windVector.y * 0.02f * state.intensity * Time.delta;
            f.x += f.vx * Time.delta;
            f.y += f.vy * Time.delta;
            f.z += f.vz * Time.delta;
            f.vz -= 0.08f * Time.delta;
            if(f.z <= 0f){
                land(f);
                flyers.remove(i);
            }
        }

        if(!headless){
            flash = Math.max(flash - Time.delta / 10f, 0f);
            nextFlash -= Time.delta * state.intensity;
            if(nextFlash <= 0f){
                nextFlash = Mathf.random(240f, 900f);
                flash = 1f;
                Effect.shake(1.5f, 20f, Core.camera.position);
                Sounds.thruster.play(0.6f, 0.4f, 0f);
            }
        }
    }

    void launch(Item item, Team team, float x, float y, WeatherState state){
        Flyer f = new Flyer();
        f.item = item;
        f.team = team;
        f.x = x;
        f.y = y;
        f.z = 2f;
        float speed = Mathf.random(1.2f, 2.4f) * state.intensity;
        f.vx = state.windVector.x * speed + Mathf.range(0.4f);
        f.vy = state.windVector.y * speed + Mathf.range(0.4f);
        f.vz = Mathf.random(1.2f, 2.2f);
        f.spin = Mathf.range(15f);
        flyers.add(f);
        if(!headless) StormFx.lift.at(x, y, 0f, item.color);
    }

    void land(Flyer f){
        mindustry.world.Tile t = world.tileWorld(f.x, f.y);
        if(!net.client() && t != null && t.build != null && t.build.team == f.team && t.build.acceptItem(t.build, f.item)){
            t.build.handleItem(t.build, f.item);
        }else if(!headless){
            StormFx.drop.at(f.x, f.y, Mathf.random(360f), f.item.color, f.item);
        }
    }

    void rain(float xspeed, float yspeed, float intensity, float alpha){
        Rand r = Weather.rand;
        r.setSeed(1);
        float pad = 40f;
        Tmp.r1.setCentered(Core.camera.position.x, Core.camera.position.y, Core.camera.width + pad * 2f, Core.camera.height + pad * 2f);
        int total = (int)(Tmp.r1.area() / rainDensity * intensity);
        float ang = Angles.angle(xspeed, -yspeed);
        for(int i = 0; i < total; i++){
            float scl = r.random(0.7f, 1.3f), len = r.random(10f, 22f) * scl;
            float x = r.random(0f, world.unitWidth()) + Time.time * xspeed * scl - Tmp.r1.x;
            float y = r.random(0f, world.unitHeight()) - Time.time * yspeed * scl - Tmp.r1.y;
            x = Mathf.mod(x, Tmp.r1.width) + Tmp.r1.x;
            y = Mathf.mod(y, Tmp.r1.height) + Tmp.r1.y;
            Lines.stroke(r.random(0.6f, 1.1f));
            Draw.color(rainColor, alpha * r.random(0.35f, 0.85f));
            Lines.lineAngle(x, y, ang, len);
        }
    }

    @Override
    public void drawOver(WeatherState state){
        float a = state.opacity * state.intensity;
        Draw.z(Layer.weather - 1f);
        Draw.color(dim, 0.35f * a);
        Fill.rect(Core.camera.position.x, Core.camera.position.y, Core.camera.width + 4f, Core.camera.height + 4f);

        super.drawOver(state);

        float wx = state.windVector.x, wy = state.windVector.y;
        Draw.z(Layer.weather);
        rain(wx * 6f, 11f - wy * 3f, state.intensity, a);

        Draw.z(Layer.weather - 0.5f);
        for(Flyer f : flyers){
            float sc = DGDraw3D.scale(f.z), off = DGDraw3D.shadowOffset(f.z);
            Draw.color(Pal.shadow, 0.35f);
            Draw.rect(f.item.fullIcon, f.x - off, f.y - off, 5f, 5f, f.time * f.spin);
            Draw.color();
            Draw.rect(f.item.fullIcon, DGDraw3D.x(f.x, f.z), DGDraw3D.y(f.y, f.z), 6f * sc, 6f * sc, f.time * f.spin);
        }

        if(flash > 0f){
            Draw.z(Layer.weather + 1f);
            Draw.blend(Blending.additive);
            Draw.color(Color.valueOf("d8e4ff"), 0.45f * flash * flash * state.opacity);
            Fill.rect(Core.camera.position.x, Core.camera.position.y, Core.camera.width + 4f, Core.camera.height + 4f);
            Draw.blend();
        }
        Draw.reset();
    }
}
