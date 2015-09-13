package com.vazp.popularmovies.tasks;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.vazp.popularmovies.R;
import com.vazp.popularmovies.data.MoviesContract.ReviewEntry;
import com.vazp.popularmovies.data.MoviesContract.VideoEntry;
import com.vazp.popularmovies.retrofit.MovieDatabaseApi;
import com.vazp.popularmovies.retrofit.ReviewModel;
import com.vazp.popularmovies.retrofit.ReviewModelList;
import com.vazp.popularmovies.retrofit.VideoModel;
import com.vazp.popularmovies.retrofit.VideoModelList;

import java.util.ArrayList;

import retrofit.RestAdapter;

/**
 * Created by Miguel on 9/7/2015.
 */
public class GetVideosAndReviewsTask extends AsyncTask<Integer, Void, Void>
{
    private static final String LOG_TAG = GetVideosAndReviewsTask.class.getSimpleName();
    private final Context mContext;

    public GetVideosAndReviewsTask(Context context)
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

        int movieId = params[0];

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setEndpoint(mContext.getString(R.string.base_movie_api_url))
                .build();
        MovieDatabaseApi movieDatabaseApi = restAdapter.create(MovieDatabaseApi.class);

        Uri videosUri = VideoEntry.buildVideoUriWithMovieId(movieId);
        Cursor videosCursor = mContext.getContentResolver()
                .query(
                        videosUri,
                        new String[]{VideoEntry._ID},
                        null,
                        null,
                        null);

        if (!videosCursor.moveToFirst())
        {
            ArrayList<ContentValues> videosContentValues =
                    new ArrayList<>();

            VideoModelList videosList =
                    movieDatabaseApi.getVideos(movieId,
                                               mContext.getString(R.string.movie_database_api_key));

            for (VideoModel videoEntry : videosList.Videos)
            {
                if (videoEntry.getSite().equals(mContext.getString(R.string.videos_site)))
                {
                    ContentValues videoContentValues = new ContentValues();

                    videoContentValues.put(VideoEntry.COLUMN_VIDEO_ID, videoEntry.getId());
                    videoContentValues.put(VideoEntry.COLUMN_MOVIE_ID, movieId);
                    videoContentValues.put(VideoEntry.COLUMN_KEY, videoEntry.getKey());
                    videoContentValues.put(VideoEntry.COLUMN_NAME, videoEntry.getName());
                    videoContentValues.put(VideoEntry.COLUMN_SITE, videoEntry.getSite());

                    videosContentValues.add(videoContentValues);
                }
            }

            if (videosContentValues.size() > 0)
            {
                ContentValues[] videosContentValuesArray =
                    new ContentValues[videosContentValues.size()];
                videosContentValues.toArray(videosContentValuesArray);
                int rowsAffected = mContext.getContentResolver()
                    .bulkInsert(VideoEntry.CONTENT_URI, videosContentValuesArray);

//                Log.v(LOG_TAG, "Inserted " + rowsAffected);
            }

        }

        Uri reviewUri = ReviewEntry.buildReviewUriWithMovieId(movieId);
        Cursor reviewsCursor = mContext.getContentResolver()
                .query(
                        reviewUri,
                        new String[]{ReviewEntry._ID},
                        null,
                        null,
                        null);

        if (!reviewsCursor.moveToFirst())
        {
            ArrayList<ContentValues> reviewsContentValues =
                    new ArrayList<>();

            ReviewModelList reviewsList =
                    movieDatabaseApi.getReviews(movieId,
                                                mContext.getString(R.string.movie_database_api_key));

            for (ReviewModel reviewEntry : reviewsList.Reviews)
            {
                ContentValues reviewContentValues = new ContentValues();

                reviewContentValues.put(ReviewEntry.COLUMN_REVIEW_ID, reviewEntry.getId());
                reviewContentValues.put(ReviewEntry.COLUMN_MOVIE_ID, movieId);
                reviewContentValues.put(ReviewEntry.COLUMN_AUTHOR, reviewEntry.getAuthor());
                reviewContentValues.put(ReviewEntry.COLUMN_CONTENT, reviewEntry.getContent());

                reviewsContentValues.add(reviewContentValues);
            }

            if (reviewsContentValues.size() > 0)
            {
                ContentValues[] reviewsContentValuesArray =
                        new ContentValues[reviewsContentValues.size()];
                reviewsContentValues.toArray(reviewsContentValuesArray);
                int rowsAffected = mContext.getContentResolver()
                        .bulkInsert(ReviewEntry.CONTENT_URI, reviewsContentValuesArray);

//                Log.v(LOG_TAG, "Inserted " + rowsAffected);
            }

        }

        return null;
    }
}
