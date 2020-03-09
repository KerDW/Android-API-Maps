package com.example.apitest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {

    @GET("rooms")
    Call<List<Room>> getRooms();

    @POST("rooms")
    Call<Room> newRoom(@Body Room room);

    @PUT("rooms/{id}")
    Call<Room> editRoom(@Path("id") int id, @Body Room room);

    @DELETE("rooms/{id}")
    Call<Room> destroyRoom(@Path("id") int id);

}
