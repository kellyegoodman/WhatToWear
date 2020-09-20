package com.example.android.whattowear;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;

import com.example.android.whattowear.data.ClothesProvider;


/**
 * A simple {@link Fragment} subclass.
 */
public class OutfitFragment extends Fragment {

    public static final String LOG_TAG = MainActivity.class.getName();

    private ClothesProvider mClothesProvider =  new ClothesProvider();

    /**
     * Constant value for the weather loader ID. We can choose any integer.
     * This really only comes into play if you're using multiple loaders.
     */
    private static final int WEATHER_LOADER_ID = 2;

    /** TextView that is displayed when weather data is unavailable */
    private TextView mEmptyStateTextView;

    private View mEmptyOutfitView;

    private String mTemperatureUnits;

    private double mOptimalOutfitWarmth;

    private OutfitLogic m_casualOutfitLogic;
    private OutfitLogic m_formalOutfitLogic;

    /** Adapter for the list of outfits */
    private OutfitAdapter mAdapter;

    private double FarenheitToCelsius(double temp_in_F) {
        return (temp_in_F - 32) * (5.0/9.0);
    }

    private double TempToDesiredWarmth(double temp_in_C) {
        return 4000 - (100 * temp_in_C);
    }

    // Weather Data callback
    private LoaderManager.LoaderCallbacks<WeatherDay> weatherLoaderListener
            = new LoaderManager.LoaderCallbacks<WeatherDay>() {
        @Override
        public Loader<WeatherDay> onCreateLoader(int i, Bundle bundle) {

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

            uriBuilder.appendPath("daily");
            uriBuilder.appendQueryParameter("city", cityName);
            uriBuilder.appendQueryParameter("units", tempUnits);
            uriBuilder.appendQueryParameter("key", QueryUtils.API_KEY);
            uriBuilder.appendQueryParameter("days", "1");

            return new WeatherDayLoader(getActivity(), uriBuilder.toString());
        }

        /**
         * This method runs on the main UI thread after the background work has been
         * completed. This method receives as input, the return value from the doInBackground()
         * method. First we clear out the adapter, to get rid of weather data from a previous
         * query. Then we update the adapter with the new daily weather.
         */
        @Override
        public void onLoadFinished(@NonNull Loader<WeatherDay> loader, WeatherDay weather) {
            // Hide loading indicator because the data has been loaded
            View loadingIndicator = getView().findViewById(R.id.loading_daily_weather_indicator);
            loadingIndicator.setVisibility(View.GONE);

            // Set empty state text to display "No weather data found."
            mEmptyStateTextView.setText(R.string.no_daily_weather_data);

            // Clear the adapter of previous weather data
            //clearWeather();

            // If there is a valid {@link WeatherDay}, then update the display.
            if (weather != null) {
                // set the desired warmth factor
                if (mTemperatureUnits.equals(getString(R.string.settings_units_Celsius_value))) {
                    mOptimalOutfitWarmth = TempToDesiredWarmth(weather.getHighTemperature());
                } else if (mTemperatureUnits.equals(getString(R.string.settings_units_Farenheit_value))) {
                    mOptimalOutfitWarmth = TempToDesiredWarmth(FarenheitToCelsius(
                            weather.getHighTemperature()));
                }

                // call on outfit logic to display outfits
                mAdapter.setOptWarmth(mOptimalOutfitWarmth);
                m_casualOutfitLogic.FetchOutfit(mOptimalOutfitWarmth);
                m_formalOutfitLogic.FetchOutfit(mOptimalOutfitWarmth);

                mEmptyStateTextView.setVisibility(View.GONE);
                formatWeather(weather);
            }

            Log.v(LOG_TAG, "onLoadFinished");
        }

        @Override
        public void onLoaderReset(@NonNull Loader<WeatherDay> loader) {
            // Clear the adapter of previous hourly weather data
            //clearWeather();

            Log.v(LOG_TAG, "onLoaderReset");
        }

        private void formatWeather(WeatherDay weather_data) {

            View weatherView = getView().findViewById(R.id.daily_weather_display);

            if (weather_data == null) {
                // clear everything
                mEmptyStateTextView.setVisibility(View.VISIBLE);
                mEmptyStateTextView.setText(R.string.no_daily_weather_data);

            } else {
                // Find the TextView with view ID temperature
                ImageView iconView = (ImageView) weatherView.findViewById(R.id.icon_view);

                int id = getResources().getIdentifier(weather_data.getWeatherIconCode(), "drawable",
                        getContext().getPackageName());
                iconView.setImageDrawable(getResources().getDrawable(id));

                // Populate the high daily temperature TextView
                TextView maxTemperatureTextView = (TextView) weatherView.findViewById(R.id.high_temperature);
                String formattedTemp = formatTemperature(weather_data.getHighTemperature());
                maxTemperatureTextView.setText(formattedTemp + " " + mTemperatureUnits);

                // Populate the low daily temperature TextView
                TextView minTemperatureTextView = (TextView) weatherView.findViewById(R.id.low_temperature);
                formattedTemp = formatTemperature(weather_data.getLowTemperature());
                minTemperatureTextView.setText(formattedTemp + " " + mTemperatureUnits);

                // Populate the weather description TextView
                TextView descriptionTextView = (TextView) weatherView.findViewById(R.id.dailyWeatherDescription);
                descriptionTextView.setText(weather_data.getWeatherDescription());
            }
        }

        /**
         * Return the formatted temperature string showing 1 decimal place (i.e. "3.2")
         * from a decimal magnitude value.
         */
        private String formatTemperature(double temperature) {
            DecimalFormat temperatureFormat = new DecimalFormat("0.0");
            return temperatureFormat.format(temperature);
        }
    };

    public OutfitFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_outfits, container, false);
        ListView outfitListView = (ListView) rootView.findViewById(R.id.outfit_list);

        // populate options menu
        setHasOptionsMenu(true);

        // populate outfit recommendations TODO
        mEmptyOutfitView = rootView.findViewById(R.id.empty_outfit_view);
        mEmptyOutfitView.setVisibility(View.VISIBLE);
        outfitListView.setEmptyView(mEmptyOutfitView);

        // Create a new adapter that takes an empty list of outfits as input
        mAdapter = new OutfitAdapter(getActivity(), new ArrayList<Outfit>());

        // Set the adapter on the {@link ListView}
        // so the list can be populated in the user interface
        outfitListView.setAdapter(mAdapter);

        m_casualOutfitLogic = new OutfitLogic(getContext(), this,  getLoaderManager(), OutfitLogic.CASUAL_TAG);
        m_formalOutfitLogic = new OutfitLogic(getContext(), this, getLoaderManager(), OutfitLogic.FORMAL_TAG);

        // Find a reference to the daily_weather_display view in the layout
        LinearLayout dailyWeatherView = (LinearLayout) rootView.findViewById(R.id.daily_weather_display);

        // Set empty view for the case there is no data to display
        mEmptyStateTextView = (TextView) rootView.findViewById(R.id.empty_daily_weather_view);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mTemperatureUnits = sharedPrefs.getString(
                getString(R.string.settings_units_key),
                getString(R.string.settings_units_default));

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
            loaderManager.initLoader(WEATHER_LOADER_ID, null, weatherLoaderListener);
        } else {
            // Otherwise, display error
            // First, hide loading indicator so error message will be visible
            View loadingIndicator = rootView.findViewById(R.id.loading_daily_weather_indicator);
            loadingIndicator.setVisibility(View.GONE);
            dailyWeatherView.setVisibility(View.GONE);
            mEmptyStateTextView.setVisibility(View.VISIBLE);

            // Update empty state with no connection error message
            mEmptyStateTextView.setText(R.string.no_internet_connection);
        }

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

    public void addOutfit(Outfit outfit) {
        View emptyView = getView().findViewById(R.id.empty_outfit_view);
        emptyView.setVisibility(View.GONE);

        mAdapter.add(outfit);
    }

}
