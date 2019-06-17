package com.example.android.whattowear;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

/**
 * A simple {@link Fragment} subclass.
 */
public class WeatherFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<List<WeatherHour>> {

    public static final String LOG_TAG = MainActivity.class.getName();

    /**
     * Constant value for the weather loader ID. We can choose any integer.
     * This really only comes into play if you're using multiple loaders.
     */
    private static final int WEATHER_LOADER_ID = 1;

    /** Adapter for the list of hourly forecasts */
    private WeatherHourAdapter mAdapter;

    /** TextView that is displayed when the list is empty */
    private TextView mEmptyStateTextView;

    // example
    // https://api.weatherbit.io/v2.0/forecast/hourly?city=Boulder,CO&units=F&key=f094684ee2f4472eb7a5c2d423fa484d&hours=48


    public WeatherFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_weather, container, false);

        // Find a reference to the {@link ListView} in the layout
        ListView hourlyForecastListView = (ListView) rootView.findViewById(R.id.list);

        // Set empty view for the case there is no data to display
        mEmptyStateTextView = (TextView) rootView.findViewById(R.id.empty_view);
        hourlyForecastListView.setEmptyView(mEmptyStateTextView);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String tempUnits = sharedPrefs.getString(
                getString(R.string.settings_units_key),
                getString(R.string.settings_units_default));

        // Create a new adapter that takes an empty list of hourly weathers as input
        mAdapter = new WeatherHourAdapter(getActivity(), new ArrayList<WeatherHour>(), tempUnits);

        // Set the adapter on the {@link ListView}
        // so the list can be populated in the user interface
        hourlyForecastListView.setAdapter(mAdapter);

        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {
            // Get a reference to the LoaderManager, in order to interact with loaders.
            LoaderManager loaderManager = getLoaderManager();

            // Initialize the loader. Pass in the int ID constant defined above and pass in null for
            // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
            // because this activity implements the LoaderCallbacks interface).
            loaderManager.initLoader(WEATHER_LOADER_ID, null, this);
        } else {
            // Otherwise, display error
            // First, hide loading indicator so error message will be visible
            View loadingIndicator = rootView.findViewById(R.id.loading_indicator);
            loadingIndicator.setVisibility(View.GONE);

            // Update empty state with no connection error message
            mEmptyStateTextView.setText(R.string.no_internet_connection);
        }

        // populate options menu
        setHasOptionsMenu(true);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Settings" menu option
            case R.id.action_edit_settings:
                Intent settingsIntent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<List<WeatherHour>> onCreateLoader(int i, Bundle bundle) {

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String tempUnits = sharedPrefs.getString(
                getString(R.string.settings_units_key),
                getString(R.string.settings_units_default));

        String cityName = sharedPrefs.getString(
                getString(R.string.settings_city_key),
                getString(R.string.settings_city_default)
        );

        Uri baseUri = Uri.parse(QueryUtils.WEATHER_BIT_REQUEST_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();

        uriBuilder.appendPath("hourly");
        uriBuilder.appendQueryParameter("city", cityName);
        uriBuilder.appendQueryParameter("units", tempUnits);
        uriBuilder.appendQueryParameter("key", QueryUtils.API_KEY);
        uriBuilder.appendQueryParameter("hours", "12");

        return new WeatherHourLoader(getActivity(), uriBuilder.toString());
    }

    /**
     * This method runs on the main UI thread after the background work has been
     * completed. This method receives as input, the return value from the doInBackground()
     * method. First we clear out the adapter, to get rid of weather data from a previous
     * query to USGS. Then we update the adapter with the new list of hourly weathers,
     * which will trigger the ListView to re-populate its list items.
     */
    @Override
    public void onLoadFinished(@NonNull Loader<List<WeatherHour>> loader, List<WeatherHour> weathers) {
        // Hide loading indicator because the data has been loaded
        View loadingIndicator = getView().findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.GONE);

        // Set empty state text to display "No weather data found."
        mEmptyStateTextView.setText(R.string.no_weather_data);

        // Clear the adapter of previous weather data
        mAdapter.clear();

        // If there is a valid list of {@link WeatherHour}s, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (weathers != null && !weathers.isEmpty()) {
            mAdapter.addAll(weathers);
        }

        Log.v(LOG_TAG, "onLoadFinished");
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<WeatherHour>> loader) {
        // Clear the adapter of previous hourly weather data
        mAdapter.clear();

        Log.v(LOG_TAG, "onLoaderReset");
    }

}
