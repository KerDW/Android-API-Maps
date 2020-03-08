package com.example.apitest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;

public class UserActivity extends AppCompatActivity {

    static Socket mSocket;

    EditText username;
    EditText password;
    Button join;
    TextView waiting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        waiting = findViewById(R.id.waiting);
        join = findViewById(R.id.join);

        try {
            mSocket = IO.socket("http://10.0.2.2:5000");
            mSocket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            Log.e("xd", e.getMessage()+"xd");
        }

        mSocket.on("ready", args -> {

            Intent intent = new Intent(UserActivity.this, ChatActivity.class);
            intent.putExtra(username.getText().toString(), 1);

            startActivity(intent);

        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSocket.disconnect();

    }

    public void join(View view) {

        Log.i("xd", mSocket.connected()+"");
        mSocket.emit("join", username.getText().toString(), password.getText().toString());
        waiting.setVisibility(View.VISIBLE);

    }
}
