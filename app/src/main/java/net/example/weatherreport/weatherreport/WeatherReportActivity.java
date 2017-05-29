package net.example.weatherreport.weatherreport;

import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import net.example.weatherreport.WeatherReportApplication;
import net.example.weatherreport.common.ui.GenericMessageDialogFragment;
import net.example.weatherreport.R;
import net.example.weatherreport.common.api.WeatherApiCaller;
import net.example.weatherreport.common.entities.CityForecast;
import net.example.weatherreport.common.http.HttpWorker;
import net.example.weatherreport.common.http.URLConnectionFactory;
import net.example.weatherreport.common.json.ForecastJsonParser;

import java.util.concurrent.Executors;

public class WeatherReportActivity extends AppCompatActivity implements WeatherReportView {

    private static final String CURRENT_QUERY_BUNDLE_KEY = "current_query";

    private SearchView searchView;
    private RecyclerView searchResults;
    private TextView city;

    private WeatherReportAdapter adapter;

    private DialogFragment loadingFragment;

    private WeatherReportPresenter presenter;

    private String currentQueryTemp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_report);

        searchResults = (RecyclerView)findViewById(R.id.search_results);
        city = (TextView)findViewById(R.id.city);

        presenter = new WeatherReportPresenter(((WeatherReportApplication)getApplication()).getWeatherApiCaller());

        searchResults.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL);
        searchResults.addItemDecoration(dividerItemDecoration);
        adapter = new WeatherReportAdapter(((WeatherReportApplication)getApplication()).getImageLoader());
        searchResults.setAdapter(adapter);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (searchView != null && searchView.getQuery() != null) {
            outState.putString(CURRENT_QUERY_BUNDLE_KEY, searchView.getQuery().toString());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // this method is called only if this activity is being restored after being destroyed by the system
        // (for example during screen rotation or low system resources)
        // so if there was a query in the text field, redo the search to show the results again
        String currentQuery = savedInstanceState.getString(CURRENT_QUERY_BUNDLE_KEY);
        if (!TextUtils.isEmpty(currentQuery)) {
            presenter.searchForecasts(currentQuery);
            currentQueryTemp = currentQuery;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        presenter.bindView(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        presenter.unbindView();
    }

    @Override
    public void showLoading() {
        loadingFragment = GenericMessageDialogFragment.newInstance(
                R.string.loading, 0, false);
        loadingFragment.show(getSupportFragmentManager(), "dialog");
    }

    @Override
    public void showSearchError() {
        DialogFragment newFragment = GenericMessageDialogFragment.newInstance(
                R.string.error, R.string.search_error, true);
        newFragment.show(getSupportFragmentManager(), "dialog");
    }

    @Override
    public void showSearchResults(CityForecast cityForecast) {
        city.setText(cityForecast.getCity());
        adapter.setItems(cityForecast.getDayForecasts());
    }

    @Override
    public void hideLoading() {
        if (loadingFragment != null) {
            loadingFragment.dismiss();
            loadingFragment = null;
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.weather_search_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.search);

        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
        }
        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    searchView.clearFocus();
                    presenter.searchForecasts(query);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    return false;
                }
            });
            if (currentQueryTemp != null) {
                searchView.setQuery(currentQueryTemp, false);
                searchView.setIconified(false);
                searchView.clearFocus();
                currentQueryTemp = null;
            }
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.dispose();
        adapter.dispose();
    }
}
