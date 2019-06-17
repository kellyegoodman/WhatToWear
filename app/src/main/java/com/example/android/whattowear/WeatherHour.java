package com.example.android.whattowear;

public class WeatherHour {

    /** Time of weather point */
    private String mDateTime;

    /** icon reference code */
    private String mWeatherIconCode;

    /** weather description */
    private String mWeatherDescription;

    /** Temperature*/
    private double mTemperature;

    /** FeelsLike */
    private double mApparentTemperature;


    public WeatherHour(String date_time, String weather_icon_code, String description,
                       double temperature, double feelslike) {
        mDateTime = date_time;
        mWeatherIconCode = weather_icon_code;
        mTemperature = temperature;
        mWeatherDescription = description;
        mApparentTemperature = feelslike;
    }

    public String getLocalTime() { return mDateTime; }

    public String getWeatherIconCode() { return mWeatherIconCode; }

    public String getWeatherDescription() { return mWeatherDescription; }

    public double getTemperature() { return mTemperature; }

    public double getApparentTemperature() { return mApparentTemperature; }
}
