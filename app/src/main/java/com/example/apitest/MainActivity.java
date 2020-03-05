package com.example.apitest;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    ApiService service;
    Socket mSocket;

    EditText message;
    TextView messageReceived;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.send);
        message = findViewById(R.id.message);
        messageReceived = findViewById(R.id.messageReceived);

        try {
            mSocket = IO.socket("http://"+getString(R.string.ip)+":5000");
            mSocket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            Log.e("xd", e.getMessage()+"xd");
        }


//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl("http://"+getString(R.string.ip)+":80/laravelrestapi/public/api/")
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//
//        service = retrofit.create(ApiService.class);
//
//        Call<List<Car>> callAsync = service.getCars();
//
//        Log.e("xd",callAsync.request().url().toString());
//
//        callAsync.enqueue(new Callback<List<Car>>()
//        {
//            @Override
//            public void onResponse(Call<List<Car>> call, Response<List<Car>> response)
//            {
//                if (response.body() != null)
//                {
//                    List<Car> cars = response.body();
//
//                    for (Car car : cars) {
////                        Log.e("xd",car.toString());
//                    }
//
//                } else
//                {
//                    System.out.println("Request Error :: " + response.errorBody());
//                    Toast.makeText(
//                            getApplicationContext(),
//                            "Name not found.",
//                            Toast.LENGTH_LONG).show();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<List<Car>> call, Throwable t)
//            {
//                Log.e("xd", "Network Error :: " + t.getLocalizedMessage());
//            }
//        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mSocket.disconnect();
    }

    public void sendMessage(View view) {

        Log.i("xd","sending message");
        mSocket.emit("message",message.getText().toString());

    }
}