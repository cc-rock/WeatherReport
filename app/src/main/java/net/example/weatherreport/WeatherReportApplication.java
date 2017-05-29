package net.example.weatherreport;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;

import net.example.weatherreport.common.api.WeatherApiCaller;
import net.example.weatherreport.common.entities.CityForecast;
import net.example.weatherreport.common.http.HttpWorker;
import net.example.weatherreport.common.http.StreamDecoder;
import net.example.weatherreport.common.http.URLConnectionFactory;
import net.example.weatherreport.common.json.ForecastJsonParser;
import net.example.weatherreport.common.ui.ImageLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executors;

/**
 * Created by Carlo on 29/05/2017.
 */

public class WeatherReportApplication extends Application {

    private WeatherApiCaller weatherApiCaller;
    private ImageLoader imageLoader;

    @Override
    public void onCreate() {
        super.onCreate();

        HttpWorker<CityForecast> apiHttpWorker = new HttpWorker<>(
                Executors.newCachedThreadPool(),
                new Handler(Looper.getMainLooper()),
                new URLConnectionFactory(),
                new ForecastJsonParser(),
                5
        );
        weatherApiCaller = new WeatherApiCaller(apiHttpWorker);

        HttpWorker<Bitmap> imageLoaderHttpWorker = new HttpWorker<>(
                Executors.newCachedThreadPool(),
                new Handler(Looper.getMainLooper()),
                new URLConnectionFactory(),
                new StreamDecoder<Bitmap>() {
                    @Override
                    public Bitmap decodeStream(InputStream is) throws IOException {
                        return BitmapFactory.decodeStream(is);
                    }
                },
                50
        );
        imageLoader = new ImageLoader(imageLoaderHttpWorker);

    }

    public WeatherApiCaller getWeatherApiCaller() {
        return weatherApiCaller;
    }

    public ImageLoader getImageLoader() {
        return imageLoader;
    }
}
