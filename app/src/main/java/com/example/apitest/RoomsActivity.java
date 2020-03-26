package com.example.apitest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
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
import static com.example.apitest.UserActivity.service;
import static java.lang.Integer.parseInt;

public class RoomsActivity extends AppCompatActivity {

    Locale currentLocale;

    EditText newRoomText;
    EditText capacity;
    Button newRoom;
    ListView lv;

    RoomListAdapter adapter;
    List<Room> rooms = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        newRoom = findViewById(R.id.newRoom);
        newRoomText = findViewById(R.id.newRoomText);
        capacity = findViewById(R.id.capacity);
        lv = findViewById(R.id.lv);

        currentLocale = getResources().getConfiguration().locale;

        String username = getIntent().getStringExtra("USERNAME");

        adapter = new RoomListAdapter(
                RoomsActivity.this,
                R.layout.rooms,
                rooms
        );

        lv.setAdapter(adapter);

        getRooms();

        mSocket.on("ready", args -> {

            String randomLetter = (String) args[0];
            String requirement = (String) args[1];

            Intent intent = new Intent(this, MapsActivity.class);
            intent.putExtra("USERNAME", username);
            intent.putExtra("RANDOM_LETTER", randomLetter);
            intent.putExtra("REQUIREMENT", requirement);

            startActivity(intent);

        });

        mSocket.on("roomUpdate", args -> {

            getRooms();

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

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSocket.disconnect();

    }

    @Override
    public void onBackPressed() {}

    public void newRoom(View view) {

        Room room = new Room(newRoomText.getText().toString(), parseInt(capacity.getText().toString()));

        Thread thread = new Thread(() -> {

            Call<Room> call = service.newRoom(room);

            try {
                Response response = call.execute();
                Log.i("xd", response.code()+"");
                if(response.code() == 400){
                    // update view on ui thread
                    runOnUiThread(new Runnable(){
                        public void run() {
                            switch (currentLocale.getLanguage()) {
                                case "en":
                                    Toast.makeText(
                                            getApplicationContext(),
                                            "This room name is already in use, pick a different one.",
                                            Toast.LENGTH_LONG).show();
                                    break;
                                case "ca":
                                    Toast.makeText(
                                            getApplicationContext(),
                                            "Aquest nom de sala ja está en ús, escull un altre.",
                                            Toast.LENGTH_LONG).show();
                                    break;
                                case "es":
                                    Toast.makeText(
                                            getApplicationContext(),
                                            "Este nombre de sala ya está en uso, escoge otro.",
                                            Toast.LENGTH_LONG).show();
                                    break;
                                default:
                                    Log.e("xd", "error");
                            }
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.i("xd", e.getMessage()+"");
                return;
            }

            //this will just inform others of the new room
            mSocket.emit("newRoom");

        });

        thread.start();

    }

    public void getRooms(){

        Call<List<Room>> callAsync = service.getRooms();

        callAsync.enqueue(new Callback<List<Room>>()
        {
            @Override
            public void onResponse(Call<List<Room>> call, Response<List<Room>> response)
            {
                if (response.body() != null)
                {
                    rooms = response.body();
                    adapter.updateRooms(rooms);

                } else {
                    Log.e("xd","Request Error :: " + response.errorBody());
                    Log.e("xd","Request Error :: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Room>> call, Throwable t)
            {
                Log.e("xd", "Network Error :: " + t.getLocalizedMessage());
            }
        });
    }

}