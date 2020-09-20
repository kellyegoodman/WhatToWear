package com.example.android.whattowear;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

public class WeatherDayLoader extends AsyncTaskLoader<WeatherDay> {
    /** Tag for log messages */
    private static final String LOG_TAG = WeatherDayLoader.class.getName();

    /** Query URL */
    private String mUrl;

    /**
     * Constructs a new {@link WeatherDayLoader}.
     *
     * @param context of the activity
     * @param url to load data from
     */
    public WeatherDayLoader(Context context, String url) {
        super(context);
        mUrl = url;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
        Log.v(LOG_TAG, "onStartLoading");
    }

    /**
     * This is on a background thread.
     */
    @Override
    public WeatherDay loadInBackground() {
        Log.v(LOG_TAG, "loadInBackground");
        if (mUrl == null) {
            return null;
        }

        // Perform the network request, parse the response, and extract the current day's weather.
        WeatherDay dailyforecast = QueryUtils.fetchDailyForecast(mUrl);
        return dailyforecast;
    }

}
