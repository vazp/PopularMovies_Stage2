package com.vazp.popularmovies.retrofit;

import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by Miguel on 9/2/2015.
 */
public interface MovieDatabaseApi
{
    @GET("/discover/movie")
    MovieModelList getMovies(@Query("sort_by") String sortBy, @Query("api_key") String apiKey,
                             @Query("page") int page);

    @GET("/movie/{id}/reviews")
    ReviewModelList getReviews(@Path("id") int movieId, @Query("api_key") String apiKey);

    @GET("/movie/{id}/videos")
    VideoModelList getVideos(@Path("id") int movieId, @Query("api_key") String apiKey);
}
