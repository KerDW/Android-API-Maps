package com.example.apitest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

    RoomListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.send);
        message = findViewById(R.id.message);

//        mSocket.on("message", args -> {
//
//            Log.i("xd", args[0].toString());
//
//        });

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
                    List<Room> rooms = response.body();

                    adapter = new RoomListAdapter(
                            RoomsActivity.this,
                            R.layout.rooms,
                            rooms
                    );

                    Log.i("xd", rooms.toString());

                } else
                {
                    System.out.println("Request Error :: " + response.errorBody());
                    Toast.makeText(
                            getApplicationContext(),
                            "Name not found.",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Room>> call, Throwable t)
            {
                Log.e("xd", "Network Error :: " + t.getLocalizedMessage());
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSocket.disconnect();

    }

    public void sendMessage(View view) {

        mSocket.emit("message",message.getText().toString());

    }
}