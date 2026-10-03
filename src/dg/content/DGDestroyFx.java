package dg.content;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import dg.graphics.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.*;
import mindustry.world.blocks.ConstructBlock.*;

import static arc.graphics.g2d.Draw.*;
import static arc.math.Angles.*;
import static mindustry.Vars.*;

public class DGDestroyFx{
    private static final Rand rand = new Rand();
    private static final TextureRegion piece = new TextureRegion();
    private static final Effect[] dust = new Effect[5];

    public static class Wreck{
        public Block block;
        public float rotation;
        public Color tint;

        Wreck(Block block, float rotation, Color tint){
            this.block = block;
            this.rotation = rotation;
            this.tint = tint;
        }
    }

    public static final Effect shatter = new Effect(200f, 260f, e -> {
        if(!(e.data instanceof Wreck)) return;
        Wreck w = (Wreck)e.data;
        Block block = w.block;
        TextureRegion region = block.fullIcon;
        int size = block.size;
        float S = size * tilesize, power = 1f + size * 0.15f;
        int n = Math.min(size + 2, 7);
        float cell = S / n;

        e.scaled(7f, s -> {
            Draw.blend(Blending.additive);
            Fill.light(e.x, e.y, 20, S * 0.8f * s.fout() + 3f, Tmp.c1.set(Color.white).a(0.7f * s.fout()), Tmp.c2.set(Pal.lightOrange).a(0f));
            Draw.blend();
        });
        e.scaled(22f, s -> {
            z(Layer.effect);
            color(Color.white, Pal.lightOrange, s.fin());
            Lines.stroke(1.4f * s.fout() * power);
            Lines.circle(e.x, e.y, S * 0.4f + S * 1.3f * s.finpow());
        });

        rand.setSeed(e.id);
        for(int ix = 0; ix < n; ix++){
            for(int iy = 0; iy < n; iy++){
                float lx = (ix + 0.5f) * cell - S / 2f, ly = (iy + 0.5f) * cell - S / 2f;
                float ox = trnsx(w.rotation, lx, ly), oy = trnsy(w.rotation, lx, ly);
                float out = Angles.angle(ox, oy) + rand.range(35f), centre = Mathf.len(lx, ly) / (S / 2f);
                float hs = (0.1f + rand.random(0.4f) * (0.3f + centre)) * power;
                float vz = (0.5f + rand.random(0.9f)) * power;
                float spin = rand.range(9f), life = rand.random(0.7f, 1f);

                float delay = rand.random(3f), t = Math.max(e.time - delay, 0f);
                float air = DGFx.fly(t, hs, vz, 0.25f);
                float dst = DGFx.flyDst(), z = DGFx.flyZ();
                float x = e.x + ox + trnsx(out, dst), y = e.y + oy + trnsy(out, dst);
                float rot = w.rotation + spin * air;
                float fade = 1f - Mathf.curve(e.fin(), 0.6f * life, life);
                if(fade <= 0f) continue;
                float sc = DGDraw3D.scale(z), heat = Mathf.clamp(1f - e.time / 50f);

                float u = region.u, v = region.v, u2 = region.u2, v2 = region.v2;
                piece.set(region.texture);
                piece.set(Mathf.lerp(u, u2, ix / (float)n), Mathf.lerp(v2, v, (iy + 1) / (float)n), Mathf.lerp(u, u2, (ix + 1) / (float)n), Mathf.lerp(v2, v, iy / (float)n));

                z(z > 0.5f ? Layer.flyingUnitLow - 1f : Layer.debris + 0.1f);
                float off = DGDraw3D.shadowOffset(z);
                if(z > 0.5f){
                    color(Pal.shadow, Pal.shadow.a * fade * 0.8f);
                    Draw.rect(piece, x - off, y - off, cell, cell, rot);
                }
                Tmp.c1.set(Color.white).lerp(Tmp.c2.set(w.tint).mul(0.5f), Mathf.clamp(e.time / 120f) * 0.6f);
                color(Tmp.c1, fade);
                Draw.rect(piece, DGDraw3D.x(x, z), DGDraw3D.y(y, z), cell * sc, cell * sc, rot);
                if(heat > 0f){
                    Draw.blend(Blending.additive);
                    color(Pal.lightOrange, heat * 0.5f * fade);
                    Draw.rect(piece, DGDraw3D.x(x, z), DGDraw3D.y(y, z), cell * sc, cell * sc, rot);
                    Draw.blend();
                }
            }
        }

        e.scaled(70f, s -> DGFx.shards(s, 3 + size * 2, 0.3f, 0.7f * power, 0.7f, 1.1f * power, 1f + size * 0.15f, 4,
            Tmp.c3.set(w.tint).lerp(Color.white, 0.3f), w.tint, Tmp.c4.set(w.tint).mul(0.45f), 360f, 0.3f));
        e.scaled(45f, s -> DGFx.embers(s, 4 + size * 3, 0.6f, 1.2f * power, 0.8f, 1.4f * power, Pal.lightOrange));

        z(Layer.debris + 0.2f);
        rand.setSeed(e.id + 7);
        for(int i = 0; i < size * 3; i++){
            float ex = e.x + rand.range(S * 0.7f), ey = e.y + rand.range(S * 0.7f), start = rand.random(30f), t = e.time - start;
            if(t < 0f || t > 140f) continue;
            float glow = Mathf.slope(t / 140f) * Mathf.absin(e.time + i * 11f, 4f, 0.5f) + 0.3f;
            Draw.blend(Blending.additive);
            Fill.light(ex, ey, 10, 3f + size, Tmp.c1.set(Pal.lightOrange).a(glow * Mathf.slope(t / 140f)), Tmp.c2.set(Pal.lightOrange).a(0f));
            Draw.blend();
        }

        Drawf.light(e.x, e.y, S * 4f * Mathf.clamp(1f - e.time / 40f) + 10f, Pal.lightOrange, 0.8f * e.fout());
        Draw.reset();
    });

    public static void init(){
        for(int i = 0; i < dust.length; i++){
            int s = i + 1;
            dust[i] = DGFx.risingSmoke(5 + s * 4, 6f + s * 6f, 10f + s * 6f, 2.6f + s * 0.9f, Color.valueOf("9a9ba3"), DGFx.dirt, 110f + s * 20f, 360f, 0.45f);
        }

        Events.on(ClientLoadEvent.class, e -> {
            for(Block b : content.blocks()){
                if(b.destroyEffect == Fx.dynamicExplosion) b.destroyEffect = Fx.none;
            }
        });

        Events.on(BlockDestroyEvent.class, e -> {
            if(headless || !Core.settings.getBool("dg-fancy-destroy", true)) return;
            Building b = e.tile.build;
            if(b == null) return;
            Block block = b instanceof ConstructBuild ? ((ConstructBuild)b).current : b.block;
            if(block == null || block.fullIcon == null) return;
            if(b instanceof ConstructBuild && ((ConstructBuild)b).progress < 0.2f) block = b.block;
            Color mc = block.mapColor, tint = mc.r + mc.g + mc.b > 0.2f ? mc.cpy() : DGFx.metal;
            float rot = block.rotate && block.rotateDraw ? b.rotdeg() : 0f;
            shatter.at(b.x, b.y, 0f, tint, new Wreck(block, rot, tint));
            dust[Math.min(block.size, dust.length) - 1].at(b.x, b.y);
            Effect.shake(1f + block.size * 0.8f, 10f + block.size * 4f, b);
        });
    }
}
