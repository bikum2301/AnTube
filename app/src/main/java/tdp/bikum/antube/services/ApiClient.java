package tdp.bikum.antube.services;


import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static Retrofit retrofit = null;
    private static final String BASE_URL = "http://192.168.1.58:2201/api/"; // Đảm bảo BASE_URL đúng

    public static ApiService getApiService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    // **THÊM GSON CONVERTER FACTORY VÀO ĐÂY**
                    .addConverterFactory(GsonConverterFactory.create()) // Sử dụng Gson để parse JSON
                    .build();
        }
        return retrofit.create(ApiService.class);
    }
}