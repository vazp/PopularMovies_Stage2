package com.vazp.popularmovies.retrofit;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Miguel on 9/6/2015.
 */
public class VideoModelList
{
    @SerializedName("results")
    public List<VideoModel> Videos;
}
