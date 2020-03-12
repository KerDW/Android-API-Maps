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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.apitest.UserActivity.localhost;
import static com.example.apitest.UserActivity.mSocket;

public class RoomsActivity extends AppCompatActivity {

    Retrofit retrofit;
    ApiService service;

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

        retrofit = new Retrofit.Builder()
                .baseUrl(localhost + ":80/laravelrestapi/public/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(ApiService.class);

        String username = getIntent().getStringExtra("USERNAME");

        adapter = new RoomListAdapter(
                RoomsActivity.this,
                R.layout.rooms,
                rooms
        );

        lv.setAdapter(adapter);

        getRooms();

        mSocket.on("ready", args -> {

            Intent intent = new Intent(this, MapsActivity.class);
            intent.putExtra("USERNAME", username);

            startActivity(intent);

        });

        mSocket.on("roomUpdate", args -> {

            getRooms();

        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSocket.disconnect();

    }

    public void newRoom(View view) {

        Room room = new Room(newRoomText.getText().toString(), Integer.parseInt(capacity.getText().toString()));

        Thread thread = new Thread(() -> {

            Call<Room> call = service.newRoom(room);

            try {
                call.execute();
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