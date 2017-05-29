package net.example.weatherreport.weatherreport;

import net.example.weatherreport.common.entities.CityForecast;

/**
 * Created by Carlo on 29/05/2017.
 */

public interface WeatherReportView {

    void showLoading();

    void showSearchError();

    void showSearchResults(CityForecast cityForecast);

    void hideLoading();

}
