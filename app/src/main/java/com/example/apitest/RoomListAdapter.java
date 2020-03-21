package com.example.apitest;

import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;

import static com.example.apitest.RoomsActivity.service;
import static com.example.apitest.UserActivity.mSocket;
import static java.lang.Integer.parseInt;

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
        TextView user_count = v.findViewById(R.id.user_count);
        TextView capacity = v.findViewById(R.id.capacity);
        Button enter = v.findViewById(R.id.enter);
        Button edit = v.findViewById(R.id.edit);

        name.setText(r.getName());
        capacity.setText(String.valueOf(r.getCapacity()));
        user_count.setText(String.valueOf(r.getUser_count()));

        View.OnClickListener enterButton = v1 -> {

            mSocket.emit("joinRoom", r.getId(), name.getText().toString(), Integer.parseInt(capacity.getText().toString()));
        };

        View.OnClickListener editButton = v1 -> {

            LayoutInflater inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View dialog_view = inflater.inflate(R.layout.dialog_edit_room, null);

            EditText new_room_name = dialog_view.findViewById(R.id.new_room_name);
            EditText new_room_capacity = dialog_view.findViewById(R.id.new_room_capacity);

            new_room_name.setText(r.getName());
            new_room_capacity.setText(String.valueOf(r.getCapacity()));

            new AlertDialog.Builder(c).setTitle("Edit room")
            .setView(dialog_view)
            // Add action buttons
            .setPositiveButton(R.string.edit, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {

                    Room room = new Room(new_room_name.getText().toString(), parseInt(new_room_capacity.getText().toString()));

                    Thread thread = new Thread(() -> {

                        Call<Room> call = service.editRoom(r.getId(), room);

                        try {
                            call.execute();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.i("xd", "edit failed");
                            Log.i("xd", e.getMessage() + "");
                            return;
                        }

                        //this will just inform others of the new room
                        mSocket.emit("newRoom");

                    });

                    thread.start();

                }
            })
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            })
            .setNeutralButton(R.string.delete, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                    Thread thread = new Thread(() -> {

                        Call<Void> call = service.destroyRoom(r.getId());

                        try {
                            call.execute();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.i("xd", "delete failed");
                            Log.i("xd", e.getMessage() + "");
                            return;
                        }

                        //this will just inform others of the new room
                        mSocket.emit("newRoom");

                    });

                    thread.start();
                    dialog.cancel();
                }
            })
            .show();


        };

        enter.setOnClickListener(enterButton);
        edit.setOnClickListener(editButton);

        return v;

    }
}
