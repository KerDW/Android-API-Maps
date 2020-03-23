package com.example.apitest;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.JsonReader;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.apitest.RoomsActivity.service;
import static com.example.apitest.UserActivity.localhost;
import static com.example.apitest.UserActivity.mSocket;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    Locale currentLocale;

    Retrofit retrofit;
    ApiService externalApiService;

    String username;
    String randomLetter;
    String requirementText;
    String requirement;

    CountDownTimer cdn;
    int timeLeft;

    TextView announcement;
    TextView timerText;
    TextView correctGuesses;
    TextView markerCount;

    int correctGuessesNo = 0;
    int markerCountNo = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onBackPressed() {}


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // this type of map is perfect since it doesn't show countries labels
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

        currentLocale = getResources().getConfiguration().locale;

        announcement = findViewById(R.id.announcementTitle);
        timerText = findViewById(R.id.timerText);

        username = getIntent().getStringExtra("USERNAME");
        randomLetter = getIntent().getStringExtra("RANDOM_LETTER");
        requirementText = getIntent().getStringExtra("REQUIREMENT");

        requirement = requirementText.replaceAll("\\s", "");

        retrofit = new Retrofit.Builder()
                .baseUrl("http://api.geonames.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        externalApiService = retrofit.create(ApiService.class);

        setLocalesText();

        createTimer();

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                mMap.addMarker(new MarkerOptions().position(point).title(username));
                mSocket.emit("newMarker", point.latitude, point.longitude, username);

                Call<JsonElement> callAsync = externalApiService.getCountry(point.latitude, point.longitude);

                Log.i("xd", callAsync.request().url().toString());

                callAsync.enqueue(new Callback<JsonElement>()
                {
                    @Override
                    public void onResponse(Call<JsonElement> call, Response<JsonElement> response)
                    {

                        JsonElement jsonElement = response.body();
                        JsonObject obj = jsonElement.getAsJsonObject();

                        if (obj != null) {

                            // check if the country meets the requirements and award the user if so
                            String countryName = obj.get("countryName").getAsString();

                            checkRequirements(countryName);

                        } else {
                            Log.e("xd","Request Error :: " + response.errorBody());
                            Log.e("xd","Request Error :: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonElement> call, Throwable t)
                    {
                        Log.e("xd", "Network Error :: " + t.getLocalizedMessage());
                    }
                });
            }
        });

        mSocket.on("marker", args -> {

            LatLng location = new LatLng((Double) args[0], (Double) args[1]);
            String markerUsername = (String) args[2];
            BitmapDescriptor color = BitmapDescriptorFactory.defaultMarker((int) args[3]);

            // update view on ui thread
            runOnUiThread(new Runnable(){
                public void run(){
                    Marker m = mMap.addMarker(new MarkerOptions().position(location).title(markerUsername).icon(color));
                    m.showInfoWindow();
                }
            });
        });

    }

    public void checkRequirements(String countryName){
        switch(requirement){
            case "startingwith":
                if(countryName.toLowerCase().startsWith(randomLetter.toLowerCase())) {
                    correctGuessesNo++;
                }
                break;
            case "endingwith":
                if(countryName.toLowerCase().endsWith(randomLetter.toLowerCase())) {
                    correctGuessesNo++;
                }
                break;
            case "containing":
                if(countryName.toLowerCase().contains(randomLetter.toLowerCase())){
                    correctGuessesNo++;
                }
                break;
            default:
                Log.e("xd", "error");
                break;
        }
    }

    public void setLocalesText(){
        switch(currentLocale.getLanguage()){
            case "es":
                switch(requirementText){
                    case "starting with":
                        requirementText = "que empiecen con";
                        break;
                    case "ending with":
                        requirementText = "que acaben con";
                        break;
                    case "containing":
                        requirementText = "que contengan";
                        break;
                }

                announcement.setText("Paises " + requirementText + " la letra " + randomLetter + " (en inglés)");
                break;
            case "ca":
                switch(requirementText){
                    case "starting with":
                        requirementText = "que comencin amb";
                        break;
                    case "ending with":
                        requirementText = "que acabin amb";
                        break;
                    case "containing":
                        requirementText = "que continguin";
                        break;
                }

                announcement.setText("Països " + requirementText + " la lletra " + randomLetter + " (en anglès)");
                break;
            case "en":
                announcement.setText("Countries " + requirementText + " the letter " + randomLetter);
                break;
            default:
                Log.e("xd", "wrong locale");
        }
    }

    public void createTimer(){
        cdn = new CountDownTimer(30000, 1000) {

            public void onTick(long millisUntilFinished) {
                timeLeft = (int) millisUntilFinished;
                timerText.setText("Time left: \n" + millisUntilFinished / 1000);
            }

            public void onFinish() {
                finishGame();
            }
        }.start();
    }

    public void finishGame(){

        mSocket.emit("gameFinished");

        Toast.makeText(
                getApplicationContext(),
                "Time's up!",
                Toast.LENGTH_LONG).show();

        Intent intent = new Intent(MapsActivity.this, RoomsActivity.class);
        intent.putExtra("USERNAME", username);

        startActivity(intent);
    }
}
