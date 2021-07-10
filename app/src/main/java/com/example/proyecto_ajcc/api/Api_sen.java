package com.example.proyecto_ajcc.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface Api_sen {
    @GET("sensor?from=0&limit=100")
    public Call<Sensor> getdata();
}
