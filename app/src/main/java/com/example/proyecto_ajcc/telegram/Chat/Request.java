
package com.example.proyecto_ajcc.telegram.Chat;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Request {

    @SerializedName("ok")
    @Expose
    private String ok;
    @SerializedName("result")
    @Expose
    private List<Result> result = null;

    public String getOk() {
        return ok;
    }

    public void setOk(String ok) {
        this.ok = ok;
    }

    public List<Result> getResult() {
        return result;
    }

    public void setResult(List<Result> result) {
        this.result = result;
    }

}
