package com.vazp.popularmovies.retrofit;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Miguel on 9/3/2015.
 */
public class MovieModelList
{
    @SerializedName("results")
    public List<MovieModel> Movies;
}
