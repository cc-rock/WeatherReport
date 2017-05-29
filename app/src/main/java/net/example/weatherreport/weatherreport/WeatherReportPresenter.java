package net.example.weatherreport.weatherreport;

import net.example.weatherreport.common.api.WeatherApiCaller;
import net.example.weatherreport.common.entities.CityForecast;
import net.example.weatherreport.common.http.RequestHandler;

/**
 * Created by Carlo on 29/05/2017.
 */

public class WeatherReportPresenter {

    private WeatherReportView view;

    private WeatherApiCaller apiCaller;

    public WeatherReportPresenter(WeatherApiCaller apiCaller) {
        this.apiCaller = apiCaller;
    }

    public void searchForecasts(String city) {
        if (isViewBound()) {
            view.showLoading();
        }
        apiCaller.searchForecasts(city, new RequestHandler<CityForecast>() {
            @Override
            public void requestCompleted(CityForecast data) {
                if (isViewBound()) {
                    view.hideLoading();
                    view.showSearchResults(data);
                }
            }

            @Override
            public void requestFailed(Throwable error) {
                if (isViewBound()) {
                    view.hideLoading();
                    view.showSearchError();
                }
            }
        });
    }

    public void bindView(WeatherReportView view) {
        this.view = view;
    }

    public void unbindView() {
        this.view = null;
    }

    private boolean isViewBound() {
        return view != null;
    }

    public void dispose() {
        apiCaller.cancelAllRequests();
    }
}
