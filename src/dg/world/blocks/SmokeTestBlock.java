package dg.world.blocks;

import arc.func.*;
import arc.graphics.Color;
import arc.scene.ui.Slider;
import arc.scene.ui.layout.Table;
import arc.util.*;
import arc.util.io.*;
import dg.content.DGFx;
import dg.graphics.SmokeStyle;
import mindustry.gen.Building;
import mindustry.ui.Styles;
import mindustry.world.Block;

public class SmokeTestBlock extends Block{
    public static final float maxLifetime = 240f;

    public float smokeX = 0f, smokeY = 0f;

    public SmokeTestBlock(String name){
        super(name);
        update = true;
        solid = true;
        configurable = true;
    }

    static class Preset{
        final String name;
        final SmokeStyle style;

        Preset(String name, SmokeStyle style){
            this.name = name;
            this.style = style;
        }
    }

    static final Preset[] presets = {
        new Preset("Quake", new SmokeStyle(7, 9f, 14f, 3.2f, 70f, Color.valueOf("8b8c95"), Color.valueOf("6e7080"))),
        new Preset("Hornet", new SmokeStyle(9, 12f, 18f, 3.4f, 90f, Color.valueOf("a5a6ad"), Color.valueOf("6e7080"))),
        new Preset("Frost", new SmokeStyle(6, 7f, 10f, 2.4f, 45f, Color.white, Color.valueOf("afeeee"))),
        new Preset("Plant", new SmokeStyle(10, 10f, 26f, 3.6f, 160f, Color.valueOf("c4c5cc"), Color.valueOf("5d5e68"))),
        new Preset("Soot", new SmokeStyle(8, 8f, 20f, 3f, 120f, Color.valueOf("4d4e58"), Color.valueOf("2c2d38"))),
        new Preset("Toxic", new SmokeStyle(8, 9f, 16f, 3f, 110f, Color.valueOf("d7ff8a"), Color.valueOf("6e8f3a")))
    };

    static{
        presets[3].style.cone = 180f;
        presets[3].style.lifeRand = 0.45f;
        presets[4].style.cone = 180f;
        presets[4].style.lifeRand = 0.3f;
        presets[5].style.lifeRand = 0.3f;
    }

    public class SmokeTestBuild extends Building{
        public SmokeStyle style = new SmokeStyle().set(presets[3].style);
        public float interval = 20f;
        public float wind = 90f;

        protected float counter;

        @Override
        public void updateTile(){
            counter += Time.delta;
            if(counter >= interval){
                counter %= interval;
                DGFx.customSmoke.at(x + smokeX, y + smokeY, wind, Color.white, style);
            }
        }

        @Override
        public void buildConfiguration(Table table){
            table.table(Styles.black6, t -> {
                t.defaults().pad(2f);

                t.table(p -> {
                    int i = 0;
                    for(Preset preset : presets){
                        p.button(preset.name, Styles.flatt, () -> {
                            style.set(preset.style);
                            rebuild(table);
                        }).size(104f, 36f);
                        if(++i % 3 == 0) p.row();
                    }
                }).colspan(2).row();

                slider(t, "Puffs", 1, 30, 1, style.count, v -> style.count = (int)v);
                slider(t, "Spread", 0, 40, 0.5f, style.spread, v -> style.spread = v);
                slider(t, "Height", 0, 60, 0.5f, style.height, v -> style.height = v);
                slider(t, "Size", 0.5f, 10, 0.1f, style.size, v -> style.size = v);
                slider(t, "Lifetime", 10, maxLifetime, 5, style.lifetime, v -> style.lifetime = v);
                slider(t, "Life random", 0, 0.9f, 0.05f, style.lifeRand, v -> style.lifeRand = v);
                slider(t, "Cone", 0, 180, 5, style.cone, v -> style.cone = v);
                slider(t, "Alpha", 0.05f, 1, 0.05f, style.alpha, v -> style.alpha = v);
                slider(t, "Interval", 1, 120, 1, interval, v -> interval = v);
                slider(t, "Wind", 0, 360, 5, wind, v -> wind = v);
            });
        }

        void rebuild(Table table){
            table.clearChildren();
            buildConfiguration(table);
        }

        void slider(Table t, String name, float min, float max, float step, float value, Floatc cons){
            float[] current = {value};
            t.label(() -> name + ": " + Strings.autoFixed(current[0], 2)).left().width(140f);
            Slider slider = new Slider(min, max, step, false);
            slider.setValue(value);
            slider.moved(v -> {
                current[0] = v;
                cons.get(v);
            });
            t.add(slider).width(200f).row();
        }

        @Override
        public byte version(){
            return 1;
        }

        @Override
        public void write(Writes write){
            super.write(write);
            style.write(write);
            write.f(interval);
            write.f(wind);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            if(revision >= 1){
                style.read(read);
                interval = read.f();
                wind = read.f();
            }
        }
    }
}
