package com.example.android.whattowear;


import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper methods related to requesting and receiving weather data from the API.
 */
public final class QueryUtils {

    public static final String API_KEY = "05a060896ba8459ba335ab58c786ab4c";

    /** URL for hourly weather data from the openweathermap dataset */
    public static final String WEATHER_BIT_REQUEST_URL = "https://api.weatherbit.io/v2.0/forecast";

    /** Tag for the log messages */
    public static final String LOG_TAG = QueryUtils.class.getSimpleName();

    /**
     * Create a private constructor because no one should ever create a {@link QueryUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name QueryUtils (and an object instance of QueryUtils is not needed).
     */
    private QueryUtils() {
    }

    /**
     * Query the openweathermap dataset and return a list of {@link WeatherHour} objects.
     */
    public static List<WeatherHour> fetchHourlyForecast(String requestUrl) {
        // Create URL object
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        // Extract relevant fields from the JSON response and create a list of {@link WeatherHour}s
        List<WeatherHour> hourlyForecasts = extractFeatureFromJson(jsonResponse);

        // Return the list of {@link WeatherHour}s
        return hourlyForecasts;
    }

    /**
     * Query the openweathermap dataset and return a list of {@link WeatherHour} objects.
     */
    public static WeatherDay fetchDailyForecast(String requestUrl) {
        // Create URL object
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        // Extract relevant fields from the JSON response and create a {@link WeatherDay}
        WeatherDay dailyForecast = extractDailyWeatherFromJson(jsonResponse);

        // Return the {@link WeatherDay}
        return dailyForecast;
    }

    /**
     * Return a list of {@link WeatherHour} objects that has been built up from
     * parsing the given JSON response.
     */
    private static List<WeatherHour> extractFeatureFromJson(String hourlyForecastJSON) {
        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(hourlyForecastJSON)) {
            return null;
        }

        // Create an empty ArrayList that we can start adding {@link WeatherHour}s to
        List<WeatherHour> hourlyForecast = new ArrayList<>();

        // Try to parse the JSON response string. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {

            // Create a JSONObject from the JSON response string
            JSONObject baseJsonResponse = new JSONObject(hourlyForecastJSON);

            // Extract the JSONArray associated with the key called "features",
            // which represents a list of features (or weatherhours).
            JSONArray weatherHourArray = baseJsonResponse.getJSONArray("data");

            // For each weatherhour in the weatherhourArray, create an {@link WeatherHour} object
            for (int i = 0; i < weatherHourArray.length(); i++) {

                // Get a single weatherhours at position i within the list of weatherhours
                JSONObject currentHour = weatherHourArray.getJSONObject(i);

                JSONObject weatherCode = currentHour.getJSONObject("weather");

                // Extract the values for the keys called "temp" and "app_temp"
                double temperature = currentHour.getDouble("temp");
                double feels_like = currentHour.getDouble("app_temp");

                // Extract the value for the key called "timestamp_local"
                String date_time = currentHour.getString("timestamp_local");

                // Extract the value for the key called "icon"
                String icon_code = weatherCode.getString("icon");

                // Extract the value for the key called "description"
                String description = weatherCode.getString("description");

                // Create a new {@link WeatherHour} object with the date_time, icon_code, description,
                // temperature, and feels_like from the JSON response.
                WeatherHour hoursForecast = new WeatherHour(date_time, icon_code, description,
                        temperature, feels_like);

                // Add the new {@link WeatherHour} to the list of {@link WeatherHour}s.
                hourlyForecast.add(hoursForecast);
            }

        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e("QueryUtils", "Problem parsing the hourly weather JSON results", e);
        }

        // Return the hourly forecast list
        return hourlyForecast;
    }

    /**
     * Return a {@link WeatherDay} object that has been built up from
     * parsing the given JSON response.
     */
    private static WeatherDay extractDailyWeatherFromJson(String dailyForecastJSON) {
        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(dailyForecastJSON)) {
            return null;
        }

        // Create an empty WeatherDay
        WeatherDay dailyForecast = null;

        // Try to parse the JSON response string. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {

            // Create a JSONObject from the JSON response string
            JSONObject baseJsonResponse = new JSONObject(dailyForecastJSON);

            // Extract the JSONArray associated with the key called "features",
            // which represents a list of features.
            JSONObject weather = baseJsonResponse.getJSONArray("data").getJSONObject(0);
            JSONObject weatherCode = weather.getJSONObject("weather");

            // Extract the values for the keys "max_temp" and "min_temp"
            double high_temp = weather.getDouble("max_temp");
            double low_temp = weather.getDouble("min_temp");

            // Extract the value for the key called "datetime"
            String date_time = weather.getString("datetime");

            // Extract the value for the key called "icon"
            String icon_code = weatherCode.getString("icon");

            // Extract the value for the key called "description"
            String description = weatherCode.getString("description");

            // Create a new {@link WeatherDay} object with the date_time, icon_code, description,
            // high_temp, and low_temp from the JSON response.
            dailyForecast = new WeatherDay(date_time, icon_code, description,
                    high_temp, low_temp);

        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e("QueryUtils", "Problem parsing the weather JSON results", e);
        }

        // Return daily forecast
        return dailyForecast;
    }

    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL ", e);
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the hourly forecast JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // Closing the input stream could throw an IOException, which is why
                // the makeHttpRequest(URL url) method signature specifies than an IOException
                // could be thrown.
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

}
