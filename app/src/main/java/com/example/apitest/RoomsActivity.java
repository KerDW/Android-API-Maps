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

    ApiService service;

    EditText message;
    TextView messageReceived;
    Button button;
    ListView lv;

    RoomListAdapter adapter;
    List<Room> rooms = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.send);
        message = findViewById(R.id.message);
        lv = findViewById(R.id.lv);

        String username = getIntent().getStringExtra("USERNAME");

        adapter = new RoomListAdapter(
                RoomsActivity.this,
                R.layout.rooms,
                rooms
        );

        lv.setAdapter(adapter);

//        mSocket.on("message", args -> {
//
//            Log.i("xd", args[0].toString());
//
//        });

        mSocket.on("ready", args -> {

            Intent intent = new Intent(this, MapsActivity.class);
            intent.putExtra("USERNAME", username);

            startActivity(intent);

        });

        getRooms();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSocket.disconnect();

    }

    public void sendMessage(View view) {

        mSocket.emit("message",message.getText().toString());

    }

    public void getRooms(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(localhost + ":80/laravelrestapi/public/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(ApiService.class);

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

                    Log.i("xd", rooms.toString());

                } else {
                    Log.e("xd","Request Error :: " + response.errorBody());
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