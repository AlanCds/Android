package com.example.proyecto_ajcc.api;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class Sensor {

    @SerializedName("data")
    @Expose
    private List<Datos> data = null;

    public List<Datos> getData() {
        return data;
    }

    public void setData(List<Datos> data) {
        this.data = data;
    }

}
