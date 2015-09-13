package com.vazp.popularmovies.tasks;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.vazp.popularmovies.R;
import com.vazp.popularmovies.data.MoviesContract.MovieEntry;
import com.vazp.popularmovies.retrofit.MovieDatabaseApi;
import com.vazp.popularmovies.retrofit.MovieModel;
import com.vazp.popularmovies.retrofit.MovieModelList;

import java.util.ArrayList;

import retrofit.RestAdapter;

/**
 * Created by Miguel on 24/07/2015.
 */
public class GetMoviesTask extends AsyncTask<String, Void, Void>
{
    private static final String LOG_TAG = GetMoviesTask.class.getSimpleName();
    private final Context mContext;

    public GetMoviesTask(Context context)
    {
        mContext = context;
    }

    @Override
    protected Void doInBackground(String... params)
    {
        if (params.length == 0)
        {
            return null;
        }

        String sortBy = params[0];

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setEndpoint(mContext.getString(R.string.base_movie_api_url))
                .build();

        MovieDatabaseApi movieDatabaseApi = restAdapter.create(MovieDatabaseApi.class);
        MovieModelList movieList =
                movieDatabaseApi.getMovies(sortBy, mContext.getString(R.string.movie_database_api_key), 1);

        ArrayList<ContentValues> moviesContentValues =
                new ArrayList<>(movieList.Movies.size());

        for (MovieModel movieEntry : movieList.Movies)
        {
            ContentValues movieContentValues = new ContentValues();

            movieContentValues.put(MovieEntry.COLUMN_MOVIE_ID, movieEntry.getId());
            movieContentValues.put(MovieEntry.COLUMN_RELEASE_DATE, movieEntry.getReleaseDate());
            movieContentValues.put(MovieEntry.COLUMN_TITLE, movieEntry.getOriginalTitle());
            movieContentValues.put(MovieEntry.COLUMN_VOTE_AVERAGE, movieEntry.getVoteAverage());
            movieContentValues.put(MovieEntry.COLUMN_POPULARITY, movieEntry.getPopularity());
            movieContentValues.put(MovieEntry.COLUMN_OVERVIEW, movieEntry.getOverview());
            movieContentValues.put(MovieEntry.COLUMN_POSTER, movieEntry.getPosterPath());
            movieContentValues.put(MovieEntry.COLUMN_BACKDROP, movieEntry.getBackdropPath());

            moviesContentValues.add(movieContentValues);
        }

        if (moviesContentValues.size() > 0)
        {
            mContext.getContentResolver().delete(
                    MovieEntry.CONTENT_URI,
                    MovieEntry.COLUMN_FAVORITE + " = ?",
                    new String[]{"0"});
            ContentValues[] moviesContentValuesArray =
                    new ContentValues[moviesContentValues.size()];
            moviesContentValues.toArray(moviesContentValuesArray);

            int inserted = mContext.getContentResolver()
                    .bulkInsert(MovieEntry.CONTENT_URI, moviesContentValuesArray);

//            Log.v(LOG_TAG, "Inserted " + inserted);
        }

        return null;
    }
}
