package achraf.taffah.meteo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {
    private RelativeLayout home;
    private ProgressBar loading;
    private TextView cityNameTV, temperatureTV,timeTV;
    private TextInputEditText cityEDT;
    private ImageView  searchIV;
    private ArrayList<Model> arrayList = new ArrayList<>();
    private RVAdapter adapter;
    private LocationManager locationManager;
    private int PERMISSION_CODE = 1;
    private String cityName;
    public static Double theLat, theLon;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_main);

        home = findViewById(R.id.home);
        loading = findViewById(R.id.pbLoading);
        cityNameTV = findViewById(R.id.cityname);
        temperatureTV =findViewById(R.id.idTVtemperature);
        timeTV = findViewById(R.id.idTVtime);
        cityEDT =findViewById(R.id.idEtCity);
        searchIV =findViewById(R.id.idTVsearch);
        adapter = new RVAdapter(this,arrayList);


        //to handle the current location
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!=
                 PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (this,Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION},PERMISSION_CODE);
        }
        Criteria criteria = new Criteria();
        String bestProvider = String.valueOf(locationManager.getBestProvider(criteria,true));
        Location location = locationManager.getLastKnownLocation(bestProvider);
        theLat = location.getLatitude();
        theLon = location.getLongitude();


        cityName = "Casablanca";
        getWeatherInfo(cityName);

        searchIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String city = cityEDT.getText().toString();
                if (city.isEmpty()){
                    Toast.makeText(MainActivity.this, "Please make sure of entering a city name", Toast.LENGTH_SHORT).show();
                }else {
                    cityNameTV.setText(cityName);
                    getWeatherInfo(city);
                }
            }
        });

        Button view = findViewById(R.id.viewMap);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, MapsActivity.class);
                i.putExtra("lat", theLat+ "");
                i.putExtra("lon", theLon+"");
                startActivity(i);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==PERMISSION_CODE){
            if(grantResults.length>0 && grantResults[0] ==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this, "Permissions are not granted\nPlease grant permissions first!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private String getCityName(double longitude, double latitude){
        String cityName = "City not found";
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
        try {
            List<Address> addresses = gcd.getFromLocation(latitude,longitude,10);
            for (Address adr: addresses) {
                if (adr != null){
                    String city = adr.getLocality();
                    if (city != null && !city.equals("")){
                        cityName = city;
                    }else {
                        Log.d("TAG","CITY NOT FOUND") ;
                        Toast.makeText(this, "User City not found", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return cityName;
    }

    private void getWeatherInfo(String cityName){
        String URL = "http://api.weatherapi.com/v1/forecast.json?key=af9360a7724c4cc792e235920221305&q="+cityName+"&days=1&aqi=no&alerts=no";
        cityNameTV.setText(cityName);
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,URL,null, new Response.Listener<JSONObject>() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onResponse(JSONObject response) {
                loading.setVisibility(View.GONE);
                home.setVisibility(View.VISIBLE);
                arrayList.clear();

                try {
                    String temperature = response.getJSONObject("current").getString("temp_c");
                    temperatureTV.setText(temperature+"°C");
                    int isDay = response.getJSONObject("current").getInt("is_day");
                    String timeLocal = response.getJSONObject("location").getString("localtime");

                    theLat = response.getJSONObject("location").getDouble("lat");
                    theLon = response.getJSONObject("location").getDouble("lon");

                    String condition = response.getJSONObject("current").getJSONObject("condition").getString("text");
                    String conditionIcon = response.getJSONObject("current").getJSONObject("condition").getString("icon");
                    timeTV.setText(timeLocal);


                    JSONObject forecastObj = response.getJSONObject("forecast");
                    JSONObject forecastO = forecastObj.getJSONArray("forecastday").getJSONObject(0);
                    JSONArray hourArray = forecastO.getJSONArray("hour");
                    for (int i = 0; i < hourArray.length(); i++) {
                        JSONObject hourObj = hourArray.getJSONObject(i);
                        String time = hourObj.getString("time");
                        String temp = hourObj.getString("temp_c");
                        String img = hourObj.getJSONObject("condition").getString("icon");
                        String windS = hourObj.getString("wind_kph");
                        JSONObject day = forecastO.getJSONObject("day");
                        String humid = day.getString("avghumidity");
                        arrayList.add(new Model(time,temp,img,windS,humid));
                    }
                    JSONObject astro = forecastO.getJSONObject("astro");

                    adapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                arrayList.clear();
                Toast.makeText(MainActivity.this, "Please enter a valid city name", Toast.LENGTH_SHORT).show();
            }
        });

        requestQueue.add(jsonObjectRequest);
        arrayList.clear();
    }
}