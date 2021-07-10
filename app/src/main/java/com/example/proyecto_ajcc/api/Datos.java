package com.example.proyecto_ajcc.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class Datos {

    @SerializedName("id")
    @Expose
    private Integer id;
    //@SerializedName("dato")
    //@Expose
    //private Integer dato;
    @SerializedName("time")
    @Expose
    private String time;
    @SerializedName("type")
    @Expose
    private int type;
    @SerializedName("location")
    @Expose
    private String location;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    //public Integer getDato() { return dato; }

    //public void setDato(Integer dato) { this.dato = dato; }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) { this.type = type; }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

}
