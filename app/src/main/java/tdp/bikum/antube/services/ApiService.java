package tdp.bikum.antube.services;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import tdp.bikum.antube.models.Video;


public interface ApiService {
    @GET("videos")
    Call<List<Video>> getVideos();

    @GET("videos/{id}")
    Call<Video> getVideo(@Path("id") String id);
}
