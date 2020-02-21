package com.example.apitest;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    ApiService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                        Log.e("xd",car.toString());
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

        SocketClient socks = null;
        try {
            socks = new SocketClient(new URI("ws://172.16.12.12:6001/app/undostres"));
            Log.i("xd", socks.getURI().toString());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        socks.connect();

    }

    public static class SocketClient extends WebSocketClient {

        public SocketClient( URI serverUri , Draft draft ) {
            super( serverUri, draft );
        }

        public SocketClient( URI serverURI ) {
            super( serverURI );
        }

        public SocketClient( URI serverUri, Map<String, String> httpHeaders ) {
            super(serverUri, httpHeaders);
        }

        @Override
        public void onOpen( ServerHandshake handshakedata ) {
            send("{\"event\": \"xd\"}");
            Log.i("xd", "opened connection" );
            // if you plan to refuse connection based on ip or httpfields overload: onWebsocketHandshakeReceivedAsClient
        }

        @Override
        public void onMessage( String message ) {
            Log.i("xd",  "received: " + message );
        }

        @Override
        public void onClose( int code, String reason, boolean remote ) {
            // The codecodes are documented in class org.java_websocket.framing.CloseFrame
            Log.i("xd", "Connection closed by " + ( remote ? "remote peer" : "us" ) + " Code: " + code + " Reason: " + reason );
        }

        @Override
        public void onError( Exception ex ) {
            Log.i("xd", ex.toString());
            // if the error is fatal then onClose will be called additionally
        }
    }

}