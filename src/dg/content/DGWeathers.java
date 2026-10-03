package dg.content;

import dg.world.weather.StormWeather;
import mindustry.type.Weather;

public class DGWeathers{
    public static Weather storm;

    public static void load(){
        storm = new StormWeather("storm");
    }
}
