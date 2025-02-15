package tdp.bikum.antube.models;

import com.google.gson.annotations.SerializedName;

public class ApiResponse<T> {
    private boolean success; // Field này có vẻ không được dùng trong response mẫu, có thể bỏ hoặc để nguyên
    private String message;
    @SerializedName("user")  // **SỬA THÀNH @SerializedName("user") ĐỂ KHỚP VỚI KEY JSON**
    private T data; // Bạn có thể giữ tên field là 'data' hoặc đổi thành 'user', tùy bạn

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public T getData() { // Bạn có thể giữ tên method là 'getData' hoặc đổi thành 'getUser', tùy bạn
        return data;
    }
}