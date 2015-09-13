package com.vazp.popularmovies.tasks;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.vazp.popularmovies.data.MoviesContract.MovieEntry;

/**
 * Created by Miguel on 9/11/2015.
 */
public class UpdateMovieTask extends AsyncTask<Integer, Void, Void>
{
    private static final String LOG_TAG = UpdateMovieTask.class.getSimpleName();
    private final Context mContext;

    public UpdateMovieTask(Context context)
    {
        mContext = context;
    }

    @Override
    protected Void doInBackground(Integer... params)
    {
        if (params.length == 0)
        {
            return null;
        }

        int idMovie = params[0];
        int favorite = params[1];

        Uri videosUri = MovieEntry.CONTENT_URI;
        ContentValues contentValues = new ContentValues();
        contentValues.put(MovieEntry.COLUMN_FAVORITE, favorite);

        int rowsUpdated = mContext.getContentResolver().update(
                videosUri,
                contentValues,
                MovieEntry.COLUMN_MOVIE_ID + " = ?",
                new String[]{Integer.toString(idMovie)});

//        Log.v(LOG_TAG, "Updated " + rowsUpdated);

        return null;
    }
}
