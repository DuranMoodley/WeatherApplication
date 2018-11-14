package app.weather.com.weatherapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class WeatherForecastScreen extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    JSONObject data = null;
    JSONObject forecast = null;
    private GoogleApiClient googleApiClient;
    public int PERMISSION_ACCESS_COARSE_LOCATION = 0;
    TextView minTemperaturetv;
    TextView maxTemparaturetv;
    TextView currTemperaturetv;
    TextView currTempBoldTexttv;
    RelativeLayout relativeLayoutTopHalf;
    RelativeLayout relativeLayoutMiddleHalf;
    ListView listView;
    Adapter adapter;
    TextView weatherDescriptiontv;
    LinearLayout linearLayoutDaysItems;
    LinearLayout mainBackground;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_forecast_screen);

        minTemperaturetv = (TextView) findViewById(R.id.tvCurrWeatherMin);
        maxTemparaturetv = (TextView) findViewById(R.id.tvCurrWeatherMax);
        mainBackground = (LinearLayout) findViewById(R.id.mainBackground);
        currTemperaturetv = (TextView) findViewById(R.id.tvCurrWeatherDegree);
        currTempBoldTexttv = (TextView) findViewById(R.id.tvCurrWeatherBold);
        relativeLayoutTopHalf = (RelativeLayout) findViewById(R.id.topHalf);
        weatherDescriptiontv = (TextView) findViewById(R.id.tvCurrWeatherDescription);
        relativeLayoutMiddleHalf = (RelativeLayout)findViewById(R.id.middle);
        linearLayoutDaysItems = (LinearLayout) findViewById(R.id.daysItems);
        listView = (ListView) findViewById(R.id.lstDays);

        //Check permissions has been granted, if not ask user to enable it
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_COARSE_LOCATION },
                    PERMISSION_ACCESS_COARSE_LOCATION);
        }
        else{
            googleApiClient = new GoogleApiClient.Builder(this, this, this).addApi(LocationServices.API).build();
        }

    }

    @SuppressLint("StaticFieldLeak")
    public void getCurrentDayForecast(final double lat, final double longitude)
    {

        new AsyncTask<Void, Void, Void>()
        {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();

            }

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    //Request to pull weather forecast for current day
                    URL url = new URL("http://api.openweathermap.org/data/2.5/weather?lat="+lat + "&lon="+ longitude +"&units=metric&APPID=6c7eb352c2cf459010eb69f05f8fcc36");

                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                    BufferedReader reader =
                            new BufferedReader(new InputStreamReader(connection.getInputStream()));

                    StringBuffer json = new StringBuffer(1024);
                    String tmp = "";

                    while((tmp = reader.readLine()) != null)
                        json.append(tmp).append("\n");
                    reader.close();

                    data = new JSONObject(json.toString());

                    if(data.getInt("cod") != 200) {
                        System.out.println("Cancelled");
                        return null;
                    }


                } catch (Exception e) {

                    System.out.println("Exception "+ e.getMessage());
                    return null;
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void Void) {
                if(data!=null){
                    Log.d("my weather received",data.toString());

                    UpdateUI(data);
                    get5DayForecast(lat,longitude,data);
                }

            }
        }.execute();

    }

    @SuppressLint("StaticFieldLeak")
    public void get5DayForecast(final double lat, final double longitude, JSONObject jsonObject)
    {

        new AsyncTask<Void, Void, Void>()
        {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();

            }

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    URL url = new URL("http://api.openweathermap.org/data/2.5/forecast?&lat="+lat + "&lon="+ longitude +"&units=metric&APPID=6c7eb352c2cf459010eb69f05f8fcc36&cnt=5");

                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                    BufferedReader reader =
                            new BufferedReader(new InputStreamReader(connection.getInputStream()));

                    StringBuffer json = new StringBuffer(1024);
                    String tmp = "";

                    while((tmp = reader.readLine()) != null)
                        json.append(tmp).append("\n");
                    reader.close();

                    forecast = new JSONObject(json.toString());

                    if(forecast.getInt("cod") != 200) {
                        System.out.println("Cancelled");
                        return null;
                    }


                } catch (Exception e) {

                    System.out.println("Exception "+ e.getMessage());
                    return null;
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void Void) {
                if(forecast!=null){
                    Log.d("forecast",forecast.toString());
                    try {
                        UpdateBottonUI(forecast,data);
                        Log.d("forecasr2",data.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }
        }.execute();

    }

    public void UpdateBottonUI(JSONObject jsonObject,JSONObject curForecast) throws JSONException {
        ArrayList arrDays = new ArrayList();
        ArrayList maxTemp = new ArrayList();
        Date date = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int day;

        //Get current date and add 1 to it 5 times to get the days for each forecast
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        String formattedDate = df.format(date);
        Log.d("date",formattedDate);
        Locale usersLocale = Locale.getDefault();
        DateFormatSymbols dfs = new DateFormatSymbols(usersLocale);
        String weekdays[] = dfs.getWeekdays();
        Calendar calendar = Calendar.getInstance(usersLocale);
        calendar.setTime(date);
        calendar.add(Calendar.DATE,1);
        calendar.getTime();
        JSONObject details = curForecast.getJSONArray("weather").getJSONObject(0);
        String decription = details.getString("main");
        day = calendar.get(Calendar.DAY_OF_WEEK);
        arrDays.add(weekdays[day]);

        calendar.add(Calendar.DATE,1);
        day = calendar.get(Calendar.DAY_OF_WEEK);
        arrDays.add(weekdays[day]);

        calendar.add(Calendar.DATE,1);
        day = calendar.get(Calendar.DAY_OF_WEEK);
        arrDays.add(weekdays[day]);

        calendar.add(Calendar.DATE,1);
        day = calendar.get(Calendar.DAY_OF_WEEK);
        arrDays.add(weekdays[day]);

        calendar.add(Calendar.DATE,1);
        day = calendar.get(Calendar.DAY_OF_WEEK);
        arrDays.add(weekdays[day]);

        //Populate temperatures from JSON response into array list
        for(int i = 0; i < jsonObject.getJSONArray("list").length(); i++)
        {
            maxTemp.add(jsonObject.getJSONArray("list").getJSONObject(i).getJSONObject("main").getString("temp"));
            Log.d("5 day",maxTemp.get(i).toString());
        }
        adapter= new Adapter(getApplicationContext(),arrDays,maxTemp,decription);
        Log.d("desc",decription);
        listView.setAdapter(adapter);
        mainBackground.setVisibility(View.VISIBLE);
    }
    public void UpdateUI(JSONObject jsonObject)
    {
        try {
            JSONObject details = jsonObject.getJSONArray("weather").getJSONObject(0);
            String weatherDescription = details.getString("main");
            int countryId = jsonObject.getInt("id");
            JSONObject main = jsonObject.getJSONObject("main");
            Double weatherMaxTemperature = main.getDouble("temp_max");
            Double weatherMinTemperature = main.getDouble("temp_min");
            Double currentTemperature = main.getDouble("temp");

            currTemperaturetv.setText(String.format("%.0f",currentTemperature)+ (char) 0x00B0);
            maxTemparaturetv.setText(String.format("%.0f", weatherMaxTemperature)+ (char) 0x00B0);
            minTemperaturetv.setText(String.format("%.0f", weatherMinTemperature)+ (char) 0x00B0);
            currTempBoldTexttv.setText(String.format("%.0f", currentTemperature)+ (char) 0x00B0);
            weatherDescriptiontv.setText(weatherDescription);

            //Check what weather type it is and change the properties of the layout
            if(weatherDescription.equals("Clouds"))
            {
                relativeLayoutTopHalf.setVisibility(View.VISIBLE);
                relativeLayoutTopHalf.setBackgroundResource(R.drawable.forest_cloudy);
                relativeLayoutMiddleHalf.setBackgroundResource(R.color.cloudy);
                linearLayoutDaysItems.setBackgroundResource(R.color.cloudy);
                listView.setBackgroundResource(R.color.cloudy);
                mainBackground.setBackgroundResource(R.color.cloudy);
            }
            else if(weatherDescription.equals("Rain"))
            {
                relativeLayoutTopHalf.setVisibility(View.VISIBLE);
                relativeLayoutTopHalf.setBackgroundResource(R.drawable.forest_rainy);
                relativeLayoutMiddleHalf.setBackgroundResource(R.color.rainy);
                linearLayoutDaysItems.setBackgroundResource(R.color.rainy);
                listView.setBackgroundResource(R.color.rainy);
                mainBackground.setBackgroundResource(R.color.rainy);
            }
            else{
                relativeLayoutTopHalf.setVisibility(View.VISIBLE);
                relativeLayoutMiddleHalf.setBackgroundResource(R.color.sunny);
                linearLayoutDaysItems.setBackgroundResource(R.color.sunny);
                listView.setBackgroundResource(R.color.sunny);
                mainBackground.setBackgroundResource(R.color.sunny);
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
        {
            //Retrieve the current location and pass it to the methods
            Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            Toast.makeText(this, "Location Found", Toast.LENGTH_SHORT).show();
            if(lastLocation != null)
            {
                double lat = lastLocation.getLatitude(), lon = lastLocation.getLongitude();
                getCurrentDayForecast(lat, lon);
                //get5DayForecast(lat,lon);
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode == PERMISSION_ACCESS_COARSE_LOCATION)
        {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // All good!
                    googleApiClient = new GoogleApiClient.Builder(this, this, this).addApi(LocationServices.API).build();
                    Toast.makeText(this, "granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Need your location!", Toast.LENGTH_SHORT).show();
                }
        }
    }
    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, "location Suspended", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "location Failed", Toast.LENGTH_SHORT).show();
    }
}
