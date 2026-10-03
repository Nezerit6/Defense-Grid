package dg.world.weather;

import arc.*;
import arc.graphics.*;
import arc.graphics.Texture.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import dg.graphics.DGDraw3D;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.game.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.type.weather.*;
import mindustry.world.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

public class StormWeather extends RainWeather{
    public float blowInterval = 15f, blowChance = 0.012f, force = 0.22f, strikeMin = 150f, strikeMax = 480f, strikeDamage = 70f;
    public Color noiseColor = Color.valueOf("4c566b"), dim = Color.valueOf("1a2233"), boltColor = Color.valueOf("cfe0ff");
    public Texture noise;

    static final Seq<Flyer> flyers = new Seq<>();
    static final Seq<Building> shaken = new Seq<>();
    static final FloatSeq shakeOff = new FloatSeq();
    static float blowTimer, strikeTimer = 200f, rainX, rainY, rainVX, rainVY, noiseX, noiseY;
    public static float flash;
    static boolean hooked;

    static class Flyer{
        Item item;
        Team team;
        float x, y, z, vx, vy, vz, time, spin;
    }

    public StormWeather(String name){
        super(name);
        color = Color.valueOf("a9bfe8");
        density = 1000f;
        stroke = 1f;
        sizeMin = 10f;
        sizeMax = 38f;
        status = StatusEffects.wet;
        statusDuration = 120f;
        attrs.set(Attribute.light, -0.35f);
        attrs.set(Attribute.water, 0.25f);
        sound = Sounds.rain;
        soundVol = 0.35f;
        duration = 5f * Time.toMinutes;

        if(!hooked){
            hooked = true;
            Events.run(Trigger.preDraw, StormWeather::shakeBlocks);
            Events.run(Trigger.postDraw, StormWeather::unshakeBlocks);
        }
    }

    static WeatherState active(){
        return Groups.weather.find(w -> w.weather instanceof StormWeather && w.opacity > 0.05f);
    }

    static void shakeBlocks(){
        shaken.clear();
        shakeOff.clear();
        if(headless || !state.isGame()) return;
        WeatherState w = active();
        if(w == null) return;
        float amp = 0.45f * w.intensity * w.opacity, wx = w.windVector.x, wy = w.windVector.y;
        arc.math.geom.Rect r = Core.camera.bounds(Tmp.r1).grow(32f);
        int x1 = Math.max(0, (int)(r.x / tilesize)), y1 = Math.max(0, (int)(r.y / tilesize));
        int x2 = Math.min(world.width() - 1, (int)((r.x + r.width) / tilesize)), y2 = Math.min(world.height() - 1, (int)((r.y + r.height) / tilesize));
        for(int x = x1; x <= x2; x++){
            for(int y = y1; y <= y2; y++){
                Tile t = world.tile(x, y);
                if(t == null || t.build == null || !t.isCenter() || t.block().size > 2) continue;
                Building b = t.build;
                float s = Mathf.sin(Time.time * 0.35f + b.id * 1.7f) * 0.6f + Mathf.sin(Time.time * 0.9f + b.id) * 0.4f;
                float dx = (wx * 0.6f + Mathf.range(0.25f)) * s * amp / b.block.size, dy = (wy * 0.6f + Mathf.range(0.25f)) * s * amp / b.block.size;
                b.x += dx;
                b.y += dy;
                shaken.add(b);
                shakeOff.add(dx, dy);
            }
        }
    }

    static void unshakeBlocks(){
        for(int i = 0; i < shaken.size; i++){
            Building b = shaken.get(i);
            b.x -= shakeOff.get(i * 2);
            b.y -= shakeOff.get(i * 2 + 1);
        }
        shaken.clear();
        shakeOff.clear();
    }

    @Override
    public void update(WeatherState state){
        if(mindustry.Vars.state.isPaused()) return;
        float in = state.intensity;

        if(!net.client()){
            state.windVector.rotate((Mathf.sin(Time.time + state.id * 997f, 260f, 0.18f) + Mathf.sin(Time.time, 47f, 0.08f)) * Time.delta).nor();
        }

        float speed = force * in * Time.delta;
        for(Unit unit : Groups.unit) unit.impulse(state.windVector.x * speed, state.windVector.y * speed);

        rainVX = state.windVector.x * 5f * in;
        rainVY = 6f - state.windVector.y * 2f;
        rainX += rainVX * Time.delta;
        rainY += rainVY * Time.delta;
        noiseX += state.windVector.x * 9f * in * Time.delta / 2000f;
        noiseY += state.windVector.y * 9f * in * Time.delta / 2000f;

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
                    if(!Mathf.chance(blowChance * in * (0.25f + 0.75f * cross))) return;
                    int i = Mathf.random(c.len - 1);
                    Item item = c.ids[i];
                    if(item == null) return;
                    float along = (c.ys[i] - 0.5f) * tilesize, side = c.xs[i] * tilesize / 2f;
                    float ix = c.x + Angles.trnsx(c.rotation * 90f, along, side), iy = c.y + Angles.trnsy(c.rotation * 90f, along, side);
                    if(c.removeStack(item, 1) > 0) launch(item, c.team, ix, iy, state);
                });
            }

            strikeTimer -= Time.delta * in;
            if(strikeTimer <= 0f){
                strikeTimer = Mathf.random(strikeMin, strikeMax);
                strike();
            }
        }

        for(int i = flyers.size - 1; i >= 0; i--){
            Flyer f = flyers.get(i);
            f.time += Time.delta;
            f.vx += state.windVector.x * 0.02f * in * Time.delta;
            f.vy += state.windVector.y * 0.02f * in * Time.delta;
            f.x += f.vx * Time.delta;
            f.y += f.vy * Time.delta;
            f.z += f.vz * Time.delta;
            f.vz -= 0.08f * Time.delta;
            if(f.z <= 0f){
                land(f);
                flyers.remove(i);
            }
        }

        flash = Math.max(flash - Time.delta / 12f, 0f);
    }

    void strike(){
        float x, y;
        Building target = Groups.build.size() > 0 && Mathf.chance(0.5f) ? Groups.build.index(Mathf.random(Groups.build.size() - 1)) : null;
        if(target != null){
            x = target.x + Mathf.range(12f);
            y = target.y + Mathf.range(12f);
        }else{
            x = Mathf.random(world.unitWidth());
            y = Mathf.random(world.unitHeight());
        }
        float rot = Mathf.random(360f);
        if(net.server()) Call.effect(StormFx.bolt, x, y, rot, boltColor);
        else StormFx.bolt.at(x, y, rot, boltColor);
        Damage.damage(Team.derelict, x, y, 18f, strikeDamage);
        for(int i = 0; i < 3; i++) Lightning.create(Team.derelict, boltColor, strikeDamage * 0.2f, x, y, Mathf.random(360f), Mathf.random(5, 10));
        Tile t = world.tileWorld(x, y);
        if(t != null && Mathf.chance(0.4f)) Fires.create(t);
        Sounds.explosionbig.at(x, y, Mathf.random(0.5f, 0.7f), 0.8f);
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

    void rain(float intensity){
        Rand rand = Weather.rand;
        rand.setSeed(0);
        float padding = sizeMax * 0.9f;
        Tmp.r1.setCentered(Core.camera.position.x, Core.camera.position.y, Core.graphics.getWidth() / renderer.minScale(), Core.graphics.getHeight() / renderer.minScale());
        Tmp.r1.grow(padding);
        Core.camera.bounds(Tmp.r2);
        int total = (int)(Tmp.r1.area() / density * intensity);
        Lines.stroke(stroke);
        float alpha = Draw.getColor().a;
        Draw.color(color);

        for(int i = 0; i < total; i++){
            float scl = rand.random(0.5f, 1f);
            float scl2 = rand.random(0.5f, 1f);
            float size = rand.random(sizeMin, sizeMax);
            float x = rand.random(0f, world.unitWidth()) + rainX * scl2;
            float y = rand.random(0f, world.unitHeight()) - rainY * scl;
            float tint = rand.random(1f) * alpha;

            x -= Tmp.r1.x;
            y -= Tmp.r1.y;
            x = Mathf.mod(x, Tmp.r1.width);
            y = Mathf.mod(y, Tmp.r1.height);
            x += Tmp.r1.x;
            y += Tmp.r1.y;

            if(Tmp.r3.setCentered(x, y, size).overlaps(Tmp.r2)){
                Draw.alpha(tint);
                Lines.lineAngle(x, y, Angles.angle(rainVX * scl2, -rainVY * scl), size / 2f);
            }
        }
    }

    void clouds(float opacity){
        if(noise == null){
            noise = Core.assets.get("sprites/noiseAlpha.png", Texture.class);
            noise.setWrap(TextureWrap.repeat);
            noise.setFilter(TextureFilter.linear);
        }
        for(int i = 0; i < 2; i++){
            float scale = 1f / (2000f * (1f - i * 0.01f)), off = i * 0.29f;
            Draw.alpha(opacity * (1f - i * 0.2f));
            Draw.tint(noiseColor);
            Tmp.tr1.texture = noise;
            Core.camera.bounds(Tmp.r1);
            Tmp.tr1.set(Tmp.r1.x * scale, Tmp.r1.y * scale, (Tmp.r1.x + Tmp.r1.width) * scale, (Tmp.r1.y + Tmp.r1.height) * scale);
            Tmp.tr1.scroll(-noiseX * (1f + i * 0.1f) - off, -noiseY * (1f + i * 0.1f) - off);
            Draw.rect(Tmp.tr1, Core.camera.position.x, Core.camera.position.y, Core.camera.width, -Core.camera.height);
        }
    }

    @Override
    public void drawOver(WeatherState state){
        float base = Draw.getColor().a, a = state.opacity * state.intensity;
        Draw.color(dim, 0.35f * a);
        Fill.rect(Core.camera.position.x, Core.camera.position.y, Core.camera.width + 4f, Core.camera.height + 4f);

        clouds(state.opacity * 0.5f);

        Draw.color(1f, 1f, 1f, Math.min(base * 1.3f, 1f));
        rain(state.intensity);

        for(Flyer f : flyers){
            float sc = DGDraw3D.scale(f.z), off = DGDraw3D.shadowOffset(f.z);
            Draw.color(Pal.shadow, 0.35f);
            Draw.rect(f.item.fullIcon, f.x - off, f.y - off, 5f, 5f, f.time * f.spin);
            Draw.color();
            Draw.rect(f.item.fullIcon, DGDraw3D.x(f.x, f.z), DGDraw3D.y(f.y, f.z), 6f * sc, 6f * sc, f.time * f.spin);
        }

        if(flash > 0f){
            Draw.blend(Blending.additive);
            Draw.color(Color.valueOf("d8e4ff"), 0.45f * flash * flash * state.opacity);
            Fill.rect(Core.camera.position.x, Core.camera.position.y, Core.camera.width + 4f, Core.camera.height + 4f);
            Draw.blend();
        }
        Draw.reset();
    }
}
