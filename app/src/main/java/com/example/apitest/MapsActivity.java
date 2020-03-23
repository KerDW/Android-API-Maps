package com.example.apitest;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
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

import java.util.Locale;

import static com.example.apitest.UserActivity.mSocket;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    String username;

    CountDownTimer cdn;
    int timeLeft;

    TextView announcement;
    TextView timerText;

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

        Locale currentLocale = getResources().getConfiguration().locale;

        announcement = findViewById(R.id.announcementTitle);
        timerText = findViewById(R.id.timerText);

        username = getIntent().getStringExtra("USERNAME");
        String randomLetter = getIntent().getStringExtra("RANDOM_LETTER");
        String requirement = getIntent().getStringExtra("REQUIREMENT");

        switch(currentLocale.getLanguage()){
            case "es":
                switch(requirement){
                    case "starting with":
                        requirement = "que empiecen con";
                        break;
                    case "ending with":
                        requirement = "que acaben con";
                        break;
                    case "containing":
                        requirement = "que contengan";
                        break;
                }

                announcement.setText("Paises " + requirement + " la letra " + randomLetter + " (en inglés)");
                break;
            case "ca":
                switch(requirement){
                    case "starting with":
                        requirement = "que comencin amb";
                        break;
                    case "ending with":
                        requirement = "que acabin amb";
                        break;
                    case "containing":
                        requirement = "que continguin";
                        break;
                }

                announcement.setText("Països " + requirement + " la lletra " + randomLetter + " (en anglès)");
                break;
            case "en":
                announcement.setText("Countries " + requirement + " the letter " + randomLetter);
                break;
            default:
                Log.e("xd", "wrong locale");
        }

        createTimer();

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                mMap.addMarker(new MarkerOptions().position(point).title(username));
                mSocket.emit("newMarker", point.latitude, point.longitude, username);
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

    public void createTimer(){
        cdn = new CountDownTimer(60000, 1000) {

            public void onTick(long millisUntilFinished) {
                timeLeft = (int) millisUntilFinished;
                timerText.setText("Time left: \n" + millisUntilFinished / 1000);
            }

            public void onFinish() {
                Toast.makeText(
                        getApplicationContext(),
                        "Time's up!",
                        Toast.LENGTH_LONG).show();

                Intent intent = new Intent(MapsActivity.this, RoomsActivity.class);
                intent.putExtra("USERNAME", username);

                startActivity(intent);
            }
        }.start();
    }
}
