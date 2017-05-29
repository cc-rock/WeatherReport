package net.example.weatherreport.common.api;

import net.example.weatherreport.common.entities.CityForecast;
import net.example.weatherreport.common.http.HttpWorker;
import net.example.weatherreport.common.http.RequestHandler;

/**
 * Created by Carlo on 29/05/2017.
 */

public class WeatherApiCaller {

    private final static String API_URL_TEMPLATE = "http://api.openweathermap.org/data/2.5/forecast?q={CITY}&units=metric&appid=6c3bb11dc3752b1165c10441b8204cd7";

    private HttpWorker<CityForecast> httpWorker;

    public WeatherApiCaller(HttpWorker<CityForecast> httpWorker) {
        this.httpWorker = httpWorker;
    }

    public void searchForecasts(String city, RequestHandler<CityForecast> handler) {
        httpWorker.doRequest(API_URL_TEMPLATE.replace("{CITY}", city), handler);
    }

    public void cancelAllRequests() {
        httpWorker.cancelAllPendingTasks();
    }

}
