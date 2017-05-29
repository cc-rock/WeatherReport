package net.example.weatherreport.common.entities;

import java.util.List;

/**
 * Created by carlo on 29/05/2017.
 */

public class CityForecast {

    private String city;
    private List<DayForecast> dayForecasts;

    public CityForecast(String city, List<DayForecast> dayForecasts) {
        this.city = city;
        this.dayForecasts = dayForecasts;
    }

    public String getCity() {
        return city;
    }

    public List<DayForecast> getDayForecasts() {
        return dayForecasts;
    }
}
