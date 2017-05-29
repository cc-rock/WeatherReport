package net.example.weatherreport.weatherreport;

import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.example.weatherreport.R;
import net.example.weatherreport.common.entities.DayForecast;
import net.example.weatherreport.common.entities.ThreeHourForecast;
import net.example.weatherreport.common.ui.ImageLoader;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by Carlo on 29/05/2017.
 */

public class WeatherReportAdapter extends RecyclerView.Adapter<WeatherReportAdapter.DayForecastViewHolder> {

    private List<DayForecast> items = new ArrayList<>();
    private Calendar calendar = Calendar.getInstance();
    private DateFormat dayDateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.UK);

    private ImageLoader imageLoader;

    public WeatherReportAdapter(ImageLoader imageLoader) {
        this.imageLoader = imageLoader;
    }

    public void setItems(List<DayForecast> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    public void dispose() {
        imageLoader.cancelAllRequests();
    }

    @Override
    public DayForecastViewHolder onCreateViewHolder(ViewGroup parent, int position) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.day_forecast, parent, false);
        return new DayForecastViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DayForecastViewHolder holder, int position) {
        DayForecast dayForecast = items.get(position);
        holder.day.setText(dayDateFormat.format(dayForecast.getForecastDay()));
        resetCell(holder, 0);
        resetCell(holder, 3);
        resetCell(holder, 6);
        resetCell(holder, 9);
        resetCell(holder, 12);
        resetCell(holder, 15);
        resetCell(holder, 18);
        resetCell(holder, 21);
        for(ThreeHourForecast forecast : dayForecast.getForecasts()) {
            bindCell(holder, forecast);
        }
    }

    private void resetCell(DayForecastViewHolder holder, int startHour) {
        ThreeDayForecastViewHolder cell = holder.cells.get(startHour);
        if (cell != null) {
            cell.time.setText(cell.time.getResources().getString(R.string.cell_time, startHour, startHour + 3));
            cell.temperature.setText(R.string.not_available);
        }
    }

    private void bindCell(DayForecastViewHolder holder, ThreeHourForecast forecast) {
        calendar.setTime(forecast.getForecastTime());
        int startHour = calendar.get(Calendar.HOUR_OF_DAY);
        ThreeDayForecastViewHolder cell = holder.cells.get(startHour);
        if (cell != null) {
            cell.temperature.setText(cell.time.getResources().getString(R.string.cell_temp, forecast.getTemperature()));
            imageLoader.load("http://openweathermap.org/img/w/" + forecast.getIcon() + ".png", cell.icon);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class DayForecastViewHolder extends RecyclerView.ViewHolder {

        TextView day;
        SparseArray<ThreeDayForecastViewHolder> cells = new SparseArray<>();

        DayForecastViewHolder(View itemView) {
            super(itemView);
            day = (TextView)itemView.findViewById(R.id.day);
            cells.append(0, new ThreeDayForecastViewHolder(itemView.findViewById(R.id.from_0)));
            cells.append(3, new ThreeDayForecastViewHolder(itemView.findViewById(R.id.from_3)));
            cells.append(6, new ThreeDayForecastViewHolder(itemView.findViewById(R.id.from_6)));
            cells.append(9, new ThreeDayForecastViewHolder(itemView.findViewById(R.id.from_9)));
            cells.append(12, new ThreeDayForecastViewHolder(itemView.findViewById(R.id.from_12)));
            cells.append(15, new ThreeDayForecastViewHolder(itemView.findViewById(R.id.from_15)));
            cells.append(18, new ThreeDayForecastViewHolder(itemView.findViewById(R.id.from_18)));
            cells.append(21, new ThreeDayForecastViewHolder(itemView.findViewById(R.id.from_21)));
        }
    }

    private class ThreeDayForecastViewHolder {

        TextView time;
        TextView temperature;
        ImageView icon;

        ThreeDayForecastViewHolder(View itemView) {
            time = (TextView)itemView.findViewById(R.id.time);
            temperature = (TextView)itemView.findViewById(R.id.temperature);
            icon = (ImageView) itemView.findViewById(R.id.icon);
        }

    }
}
