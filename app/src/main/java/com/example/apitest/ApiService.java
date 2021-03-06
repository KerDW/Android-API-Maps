package com.example.apitest;

import com.google.gson.JsonElement;

import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @GET("rooms")
    Call<List<Room>> getRooms();

    @POST("rooms")
    Call<Room> newRoom(@Body Room room);

    @PUT("rooms/{id}")
    Call<Room> editRoom(@Path("id") int id, @Body Room room);

    @DELETE("rooms/{id}")
    Call<Void> destroyRoom(@Path("id") int id);

    @GET("countries")
    Call<List<Country>> getCountries(@Query("q") String q);

    @GET("countryCodeJSON?username=potato")
    Call<JsonElement> getCountry(@Query("lat") double lat, @Query("lng") double lng);

}
