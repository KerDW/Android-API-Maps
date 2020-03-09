package com.example.apitest;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import static com.example.apitest.UserActivity.mSocket;

class RoomListAdapter extends ArrayAdapter <Room>{

    Context c;
    int layout;
    List<Room> rooms;

    public RoomListAdapter(@NonNull Context context, int resource, @NonNull List<Room> objects) {
        super(context, resource, objects);

        this.c = context;
        this.layout = resource;
        this.rooms = objects;
    }

    public void updateRooms(List<Room> newlist) {
        rooms.clear();
        rooms.addAll(newlist);
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View v = LayoutInflater.from(c).inflate(layout,parent,false);

        final Room r = rooms.get(position);

        TextView name = v.findViewById(R.id.room_name);
        TextView capacity = v.findViewById(R.id.capacity);
        Button enter = v.findViewById(R.id.enter);

        name.setText(r.getName());
        capacity.setText(String.valueOf(r.getCapacity()));

        enter.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                mSocket.emit("join", name.getText().toString());

            }
        });

        return v;

    }
}
