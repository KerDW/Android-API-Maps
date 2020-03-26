package com.example.apitest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UserActivity extends AppCompatActivity {

    static Socket mSocket;
    static String localhost = "http://10.0.2.2";

    Retrofit retrofit;
    static ApiService service;

    EditText username;
    EditText password;
    Button join;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        join = findViewById(R.id.join);

        retrofit = new Retrofit.Builder()
                .baseUrl(localhost + ":80/laravelrestapi/public/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(ApiService.class);

        try {
            mSocket = IO.socket(localhost+":5000");
            mSocket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            Log.e("xd", e.getMessage()+"xd");
        }
        
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSocket.disconnect();

    }

    public void join(View view) {

        Log.i("xd", mSocket.connected()+"");
        mSocket.emit("join", username.getText().toString(), password.getText().toString());

        Intent intent = new Intent(UserActivity.this, RoomsActivity.class);
        intent.putExtra("USERNAME", username.getText().toString());

        startActivity(intent);

    }
}
