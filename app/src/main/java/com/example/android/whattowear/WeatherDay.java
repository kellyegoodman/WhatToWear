package com.example.android.whattowear;

public class WeatherDay {

    /** Time of weather point */
    private String mDateTime;

    /** icon reference code */
    private String mWeatherIconCode;

    /** weather description */
    private String mWeatherDescription;

    /** High Temperature*/
    private double mHighTemperature;

    /** Low Temperature */
    private double mLowTemperature;


    public WeatherDay(String date_time, String weather_icon_code, String description,
                       double high, double low) {
        mDateTime = date_time;
        mWeatherIconCode = weather_icon_code;
        mWeatherDescription = description;
        mHighTemperature = high;
        mLowTemperature = low;
    }

    public String getLocalTime() { return mDateTime; }

    public String getWeatherIconCode() { return mWeatherIconCode; }

    public String getWeatherDescription() { return mWeatherDescription; }

    public double getHighTemperature() { return mHighTemperature; }

    public double getLowTemperature() { return mLowTemperature; }

    // TODO: remove, for testing
    public void setHighTemperature(double new_high) { mHighTemperature = new_high; }
}
