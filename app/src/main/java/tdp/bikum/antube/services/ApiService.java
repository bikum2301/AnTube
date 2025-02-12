package tdp.bikum.antube.services;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import tdp.bikum.antube.models.ApiResponse;
import tdp.bikum.antube.models.LoginRequest;
import tdp.bikum.antube.models.OTPRequest;
import tdp.bikum.antube.models.RegisterRequest;
import tdp.bikum.antube.models.User;
import tdp.bikum.antube.models.Video;


public interface ApiService {
    @GET("/api/videos") // Thêm /api/ vào GET videos
    Call<List<Video>> getVideos();

    @GET("/api/videos/{id}") // Thêm /api/ vào GET video by id
    Call<Video> getVideo(@Path("id") String id);

    @POST("/api/register") // Thêm /api/ vào POST register
    Call<ApiResponse<User>> registerUser(@Body RegisterRequest registerRequest);

    @POST("/api/login") // Thêm /api/ vào POST login
    Call<ApiResponse<User>> loginUser(@Body LoginRequest loginRequest);

    @POST("/api/verify-otp") // Thêm /api/ vào POST verify-otp
    Call<ApiResponse<User>> verifyOtp(@Body OTPRequest otpRequest);

    @POST("/api/forgot-password") // Thêm /api/ vào POST forgot-password
    Call<ApiResponse<Void>> forgotPassword(@Body String email);

    @POST("/api/reset-password") // Thêm /api/ vào POST reset-password
    Call<ApiResponse<Void>> resetPassword(@Body OTPRequest otpRequest);

    @GET("/api/profile") // Thêm /api/ vào GET profile
    Call<ApiResponse<User>> getProfile(); // Cần header xác thực (sẽ thêm sau)
}