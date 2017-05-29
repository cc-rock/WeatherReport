package net.example.weatherreport.common.json;

import android.util.JsonReader;

import net.example.weatherreport.common.entities.CityForecast;
import net.example.weatherreport.common.entities.DayForecast;
import net.example.weatherreport.common.entities.ThreeHourForecast;
import net.example.weatherreport.common.http.StreamDecoder;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by carlo.conserva on 24/05/2017.
 */

public class ForecastJsonParser implements StreamDecoder<CityForecast> {

    public CityForecast decodeStream(InputStream in) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        List<DayForecast> dayForecasts = null;
        String city = null;
        try {
            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                if (name.equals("cod")) {
                    String responseCode = reader.nextString();
                    if (!"200".equals(responseCode)) {
                        throw new IOException("Error received from server");
                    }
                } else if (name.equals("list")) {
                    dayForecasts = readForecasts(reader);
                } else if (name.equals("city")) {
                    city = readCity(reader);
                } else {
                    reader.skipValue();
                }
            }
            reader.endObject();
        } finally {
            reader.close();
        }
        return new CityForecast(city, dayForecasts);
    }

    private String readCity(JsonReader reader) throws IOException {
        reader.beginObject();
        String cityName = "";
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("name")) {
                cityName = reader.nextString();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return cityName;
    }

    private List<DayForecast> readForecasts(JsonReader reader) throws IOException {
        List<ThreeHourForecast> forecasts = new ArrayList<>();
        List<DayForecast> dayForecasts = new ArrayList<>();
        reader.beginArray();
        Calendar calendar = Calendar.getInstance();
        int currentDay = -1;
        while (reader.hasNext()) {
            reader.beginObject();
            Date forecastTime = null;
            float temperature = 0f;
            String weatherDescription = "";
            String icon = "";
            while (reader.hasNext()) {
                String name = reader.nextName();
                if (name.equals("dt")) {
                    forecastTime = new Date(reader.nextLong() * 1000);
                } else if (name.equals("main")) {
                    reader.beginObject();
                    while (reader.hasNext()) {
                        String mainName = reader.nextName();
                        if (mainName.equals("temp")) {
                            temperature = (float)reader.nextDouble();
                        } else {
                            reader.skipValue();
                        }
                    }
                    reader.endObject();
                } else if (name.equals("weather")) {
                    reader.beginArray();
                    boolean skip = false;
                    while (reader.hasNext()) {
                        reader.beginObject();
                        while (reader.hasNext()) {
                            String weatherName = reader.nextName();
                            if (!skip && weatherName.equals("weather")) {
                                weatherDescription = reader.nextString();
                            } else if (!skip && weatherName.equals("icon")) {
                                icon = reader.nextString();
                            } else {
                                reader.skipValue();
                            }
                        }
                        reader.endObject();
                        skip = true;
                    }
                    reader.endArray();
                } else {
                    reader.skipValue();
                }
            }
            reader.endObject();
            calendar.setTime(forecastTime);
            int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
            if (dayOfYear > currentDay) {
                currentDay = dayOfYear;
                addDayForecast(dayForecasts, forecasts);
                forecasts = new ArrayList<>();
            }
            forecasts.add(new ThreeHourForecast(forecastTime, temperature, weatherDescription, icon));
        }
        reader.endArray();
        addDayForecast(dayForecasts, forecasts);
        return dayForecasts;
    }

    private void addDayForecast(List<DayForecast> dayForecasts, List<ThreeHourForecast> forecasts) {
        if (forecasts.size() > 0) {
            DayForecast dayForecast = new DayForecast(forecasts.get(0).getForecastTime(), forecasts);
            dayForecasts.add(dayForecast);
        }
    }

}
