package tdp.bikum.antube.models;

import com.google.gson.annotations.SerializedName;

public class ApiResponse<T> {
    private boolean success;
    private String message;
    @SerializedName("data")
    private T data;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
}