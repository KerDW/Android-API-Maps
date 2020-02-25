package com.example.apitest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    SocketClient socks = null;
    ApiService service;

    EditText message;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.send);
        message = findViewById(R.id.message);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://"+getString(R.string.ip)+":80/laravelrestapi/public/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(ApiService.class);

        Call<List<Car>> callAsync = service.getCars();

        Log.e("xd",callAsync.request().url().toString());

        callAsync.enqueue(new Callback<List<Car>>()
        {
            @Override
            public void onResponse(Call<List<Car>> call, Response<List<Car>> response)
            {
                if (response.body() != null)
                {
                    List<Car> cars = response.body();

                    for (Car car : cars) {
//                        Log.e("xd",car.toString());
                    }

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
            public void onFailure(Call<List<Car>> call, Throwable t)
            {
                Log.e("xd", "Network Error :: " + t.getLocalizedMessage());
            }
        });

        try {
            socks = new SocketClient(new URI("ws://"+getString(R.string.ip)+":8080"));
            Log.i("xd", socks.getURI().toString());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        socks.connect();

    }

    public void sendMessage(View view) {

        socks.send(message.getText().toString());

    }
}