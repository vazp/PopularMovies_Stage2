package com.vazp.popularmovies.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Miguel on 16/07/2015.
 */
public class MoviesContract
{
	public static final String CONTENT_AUTHORITY = "com.vazp.popularmovies";
	public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
	public static final String PATH_MOVIE = "movie";
	public static final String PATH_VIDEO = "video";
	public static final String PATH_REVIEW = "review";

	public static final class MovieEntry implements BaseColumns
	{
		public static final Uri CONTENT_URI =
				BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).build();

		public static final String CONTENT_TYPE =
				"vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;

		public static final String CONTENT_ITEM_TYPE =
				"vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;

		public static final String TABLE_NAME = "movie";

		/**
		 * INTEGER
		 */
		public static final String COLUMN_MOVIE_ID = "movie_id";
		/**
		 * TEXT
		 */
		public static final String COLUMN_RELEASE_DATE = "release_date";
		/**
		 * TEXT
		 */
		public static final String COLUMN_TITLE = "title";
		/**
		 * REAL
		 */
		public static final String COLUMN_VOTE_AVERAGE = "vote_average";
		/**
		 * REAL
		 */
		public static final String COLUMN_POPULARITY = "popularity";
		/**
		 * TEXT
		 */
		public static final String COLUMN_OVERVIEW = "overview";
		/**
		 * TEXT
		 */
		public static final String COLUMN_POSTER = "poster";
		/**
		 * TEXT
		 */
		public static final String COLUMN_BACKDROP = "backdrop";
		/**
		 * INTEGER
		 */
		public static final String COLUMN_FAVORITE = "favorite";

		public static Uri buildMovieUriWithId(long id)
		{
			return ContentUris.withAppendedId(CONTENT_URI, id);
		}
	}

	public static final class VideoEntry implements BaseColumns
	{
		public static final Uri CONTENT_URI =
				BASE_CONTENT_URI.buildUpon().appendPath(PATH_VIDEO).build();

		public static final String CONTENT_TYPE =
				"vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_VIDEO;

		public static final String CONTENT_ITEM_TYPE =
				"vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_VIDEO;

		public static final String TABLE_NAME = "video";

		/**
		 * TEXT
		 */
		public static final String COLUMN_VIDEO_ID = "video_id";
		/**
		 * INTEGER
		 */
		public static final String COLUMN_MOVIE_ID = "movie_id";
		/**
		 * TEXT
		 */
		public static final String COLUMN_KEY = "key";
		/**
		 * TEXT
		 */
		public static final String COLUMN_NAME = "name";
		/**
		 * TEXT
		 */
		public static final String COLUMN_SITE = "site";

		public static Uri buildVideoUriWithMovieId(long id)
		{
			return ContentUris.withAppendedId(CONTENT_URI, id);
		}
	}

	public static final class ReviewEntry implements BaseColumns
	{
		public static final Uri CONTENT_URI =
				BASE_CONTENT_URI.buildUpon().appendPath(PATH_REVIEW).build();

		public static final String CONTENT_TYPE =
				"vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_REVIEW;

		public static final String CONTENT_ITEM_TYPE =
				"vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_REVIEW;

		public static final String TABLE_NAME = "review";

		/**
		 * TEXT
		 */
		public static final String COLUMN_REVIEW_ID = "review_id";
		/**
		 * INTEGER
		 */
		public static final String COLUMN_MOVIE_ID = "movie_id";
		/**
		 * TEXT
		 */
		public static final String COLUMN_AUTHOR = "author";
		/**
		 * TEXT
		 */
		public static final String COLUMN_CONTENT = "content";

		public static Uri buildReviewUriWithMovieId(long id)
		{
			return ContentUris.withAppendedId(CONTENT_URI, id);
		}
	}
}
