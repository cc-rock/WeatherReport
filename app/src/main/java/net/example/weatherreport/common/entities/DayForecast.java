package net.example.weatherreport.common.entities;

import java.util.Date;
import java.util.List;

/**
 * Created by carlo.conserva on 24/05/2017.
 */

public class DayForecast {

    private Date forecastDay;
    private List<ThreeHourForecast> forecasts;

    public DayForecast(Date forecastDay, List<ThreeHourForecast> forecasts) {
        this.forecastDay = forecastDay;
        this.forecasts = forecasts;
    }

    public Date getForecastDay() {
        return forecastDay;
    }

    public List<ThreeHourForecast> getForecasts() {
        return forecasts;
    }
}
