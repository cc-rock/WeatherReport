package net.example.weatherreport.common.entities;

import java.util.Date;

/**
 * Created by carlo.conserva on 24/05/2017.
 */

public class ThreeHourForecast {

    private Date forecastTime;
    private float temperature;
    private String weather;
    private String icon;

    public ThreeHourForecast(Date forecastTime, float temperature, String weather, String icon) {
        this.forecastTime = forecastTime;
        this.temperature = temperature;
        this.weather = weather;
        this.icon = icon;
    }

    public Date getForecastTime() {
        return forecastTime;
    }

    public float getTemperature() {
        return temperature;
    }

    public String getWeather() {
        return weather;
    }

    public String getIcon() {
        return icon;
    }
}
