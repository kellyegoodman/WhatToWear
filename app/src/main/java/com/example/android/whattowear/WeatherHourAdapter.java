package com.example.android.whattowear;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class WeatherHourAdapter extends ArrayAdapter<WeatherHour> {

    private Activity mContext;

    private String mTemperatureUnits;

    public WeatherHourAdapter(Activity context, ArrayList<WeatherHour> input, String units) {
        super(context, 0, input);
        mContext = context;
        mTemperatureUnits = units;
    }

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Check if the existing view is being reused, otherwise inflate the view
        View listItemView = convertView;
        if(listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_item, parent, false);
        }
        // Get the {@link WeatherHour} object located at this position in the list
        WeatherHour currentWeatherHour = getItem(position);

        // Find the TextView with view ID temperature
        ImageView iconView = (ImageView) listItemView.findViewById(R.id.icon_view);

        int id = mContext.getResources().getIdentifier(currentWeatherHour.getWeatherIconCode(), "drawable",
                mContext.getPackageName());
        iconView.setImageDrawable(mContext.getResources().getDrawable(id));

        // Find the TextView in the list_item.xml layout with the ID location_text_view
        TextView temperatureTextView = (TextView) listItemView.findViewById(R.id.temperature);
        // Format the temperature to show 1 decimal place
        String formattedTemp = formatTemperature(currentWeatherHour.getTemperature());
        // Display the temperature of the current weather hour in that TextView
        temperatureTextView.setText(formattedTemp + " " + mTemperatureUnits);

        // Find the TextView in the list_item.xml layout with the ID location_qualifier
        TextView descriptionTextView = (TextView) listItemView.findViewById(R.id.weatherDescription);

        descriptionTextView.setText(currentWeatherHour.getWeatherDescription());

        // Manipulate weatherCode into string
        //int weatherCode = currentWeatherHour.getWeatherIconCode();

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // Create a new Date object from the time in milliseconds of the earthquake
        Date dateObject;
        try {
            dateObject = df.parse(currentWeatherHour.getLocalTime().replace('T',' '));

            // Find the TextView with view ID date
            TextView dateView = (TextView) listItemView.findViewById(R.id.date);
            // Format the date string (i.e. "Mar 3, 1984")
            String formattedDate = formatDate(dateObject);
            // Display the date of the current earthquake in that TextView
            dateView.setText(formattedDate);

            // Find the TextView with view ID time
            TextView timeView = (TextView) listItemView.findViewById(R.id.time);
            // Format the time string (i.e. "4:30PM")
            String formattedTime = formatTime(dateObject);
            // Display the time of the current earthquake in that TextView
            timeView.setText(formattedTime);
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }

        // Return the whole list item layout (containing 3 TextViews)
        // so that it can be shown in the ListView
        return listItemView;
    }

    /**
     * Return the formatted temperature string showing 1 decimal place (i.e. "3.2")
     * from a decimal magnitude value.
     */
    private String formatTemperature(double temperature) {
        DecimalFormat temperatureFormat = new DecimalFormat("0.0");
        return temperatureFormat.format(temperature);
    }

    /**
     * Return the formatted date string (i.e. "Mar 3, 1984") from a Date object.
     */
    private String formatDate(Date dateObject) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("LLL dd, yyyy");
        return dateFormat.format(dateObject);
    }

    /**
     * Return the formatted date string (i.e. "4:30 PM") from a Date object.
     */
    private String formatTime(Date dateObject) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("h a");
        return timeFormat.format(dateObject);
    }

}
