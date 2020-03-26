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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

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

    int timerSeconds = 90;
    int correctGuessesNo = 0;
    int markersUsedNo = 0;
    ArrayList<String> countriesGuessed = new ArrayList<>();

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
        correctGuesses = findViewById(R.id.correctGuesses);
        markerCount = findViewById(R.id.markerLimit);

        username = getIntent().getStringExtra("USERNAME");
        randomLetter = getIntent().getStringExtra("RANDOM_LETTER");
        requirementText = getIntent().getStringExtra("REQUIREMENT");

        requirement = requirementText.replaceAll("\\s", "");

        retrofit = new Retrofit.Builder()
                .baseUrl("http://api.geonames.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        externalApiService = retrofit.create(ApiService.class);

        setLocalesTexts();

        createTimer();

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                markersUsedNo++;

                Marker marker = mMap.addMarker(new MarkerOptions().position(point).title(username));

                // send the marker to the other clients
                mSocket.emit("newMarker", point.latitude, point.longitude, username);

                // check the country where these coordinates belong using an external api, then check if it meets the requirements
                Call<JsonElement> callAsync = externalApiService.getCountry(point.latitude, point.longitude);

                callAsync.enqueue(new Callback<JsonElement>()
                {
                    @Override
                    public void onResponse(Call<JsonElement> call, Response<JsonElement> response)
                    {

                        JsonElement jsonElement = response.body();
                        JsonObject obj = jsonElement.getAsJsonObject();

                        if (obj != null) {

                            // check if the country meets the requirements and award the user if so
                            String countryName = obj.has("countryName") ? obj.get("countryName").getAsString() : null;

                            if(countryName == null){
                                marker.setTitle("API error");
                                return;
                            }

                            if(countriesGuessed.contains(countryName)){
                                switch(currentLocale.getLanguage()){
                                    case "es":
                                        marker.setTitle("Este pais ya ha sido marcado");
                                        break;
                                    case "ca":
                                        marker.setTitle("Aquest païs ja ha sigut marcat");
                                        break;
                                    case "en":
                                        marker.setTitle("This country was marked already");
                                        break;
                                    default:
                                        Log.e("xd", "error");
                                        break;
                                }
                                marker.showInfoWindow();
                                Log.i("xd", "repeated country");
                                return;
                            }

                            countriesGuessed.add(countryName);

                            if(meetsRequirements(countryName)){
                                switch(currentLocale.getLanguage()){
                                    case "es":
                                        marker.setTitle("Acierto! (" + countryName + ")");
                                        markerCount.setText("Marcadors: " + markersUsedNo);
                                        correctGuesses.setText("Aciertos: " + correctGuessesNo);
                                        break;
                                    case "ca":
                                        marker.setTitle("Encert! (" + countryName + ")");
                                        markerCount.setText("Marcadores: " + markersUsedNo);
                                        correctGuesses.setText("Encerts: " + correctGuessesNo);
                                        break;
                                    case "en":
                                        marker.setTitle("Guessed right! (" + countryName + ")");
                                        markerCount.setText("Markers: " + markersUsedNo);
                                        correctGuesses.setText("Correct guesses: " + correctGuessesNo);
                                        break;
                                    default:
                                        Log.e("xd", "error");
                                        break;
                                }
                            } else {
                                switch(currentLocale.getLanguage()){
                                    case "es":
                                        markerCount.setText("Marcadors: " + markersUsedNo);
                                        marker.setTitle("Intentalo de nuevo");
                                        break;
                                    case "ca":
                                        markerCount.setText("Marcadores: " + markersUsedNo);
                                        marker.setTitle("Torna a provar");
                                        break;
                                    case "en":
                                        markerCount.setText("Markers: " + markersUsedNo);
                                        marker.setTitle("Try again");
                                        break;
                                    default:
                                        Log.e("xd", "error");
                                        break;
                                }
                            }
                            marker.showInfoWindow();

                        } else {
                            Log.e("xd","Request Error :: " + response.errorBody());
                            Log.e("xd","Request Error :: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonElement> call, Throwable t)
                    {
                        marker.setTitle("API error");
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
                    mMap.addMarker(new MarkerOptions().position(location).icon(color));
                }
            });
        });

        mSocket.on("winner", args -> {

            Log.i("xd", "winner received");

            String winnerName = (String) args[0];
            int winnerPoints = (int) args[1];

            // update view on ui thread
            runOnUiThread(new Runnable(){
                public void run(){

                    switch(currentLocale.getLanguage()){
                        case "en":
                            Toast.makeText(
                                    getApplicationContext(),
                                    "The winner is " + winnerName + " with " + winnerPoints + " correct guesses.",
                                    Toast.LENGTH_LONG).show();
                            break;
                        case "ca":
                            Toast.makeText(
                                    getApplicationContext(),
                                    "El guanyador es " + winnerName + " amb " + winnerPoints + " encerts.",
                                    Toast.LENGTH_LONG).show();
                            break;
                        case "es":
                            Toast.makeText(
                                    getApplicationContext(),
                                    "El ganador es " + winnerName + " con " + winnerPoints + " aciertos.",
                                    Toast.LENGTH_LONG).show();
                            break;
                        default:
                            Log.e("xd", "error");
                    }


                }
            });

            mSocket.emit("gameFinished");
        });

    }

    public boolean meetsRequirements(String countryName){
        switch(requirement){
            case "startingwith":
                if(countryName.toLowerCase().startsWith(randomLetter.toLowerCase())) {
                    correctGuessesNo++;
                    return true;
                }
                break;
            case "endingwith":
                if(countryName.toLowerCase().endsWith(randomLetter.toLowerCase())) {
                    correctGuessesNo++;
                    return true;
                }
                break;
            case "containing":
                if(countryName.toLowerCase().contains(randomLetter.toLowerCase())){
                    correctGuessesNo++;
                    return true;
                }
                break;
            default:
                Log.e("xd", "error");
                break;
        }
        return false;
    }

    public void setLocalesTexts(){
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
                markerCount.setText("Marcadores: 0");
                correctGuesses.setText("Aciertos: 0");
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
                markerCount.setText("Marcadors: 0");
                correctGuesses.setText("Encerts: 0");
                announcement.setText("Països " + requirementText + " la lletra " + randomLetter + " (en anglès)");
                break;
            case "en":
                markerCount.setText("Markers: 0");
                correctGuesses.setText("Correct guesses: 0");
                announcement.setText("Countries " + requirementText + " the letter " + randomLetter);
                break;
            default:
                Log.e("xd", "wrong locale");
        }
    }

    public void createTimer(){
        cdn = new CountDownTimer(timerSeconds * 1000, 1000) {

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

        mSocket.emit("points", correctGuessesNo);

        Intent intent = new Intent(MapsActivity.this, RoomsActivity.class);
        intent.putExtra("USERNAME", username);

        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSocket.disconnect();

    }
}
