package com.vazp.popularmovies.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeStandalonePlayer;
import com.google.android.youtube.player.YouTubeThumbnailLoader;
import com.google.android.youtube.player.YouTubeThumbnailView;
import com.squareup.picasso.Picasso;
import com.vazp.popularmovies.NetworkUtility;
import com.vazp.popularmovies.R;
import com.vazp.popularmovies.activities.DetailsActivity;
import com.vazp.popularmovies.data.MoviesContract.MovieEntry;
import com.vazp.popularmovies.data.MoviesContract.ReviewEntry;
import com.vazp.popularmovies.data.MoviesContract.VideoEntry;
import com.vazp.popularmovies.tasks.UpdateMovieTask;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Miguel on 15/07/2015.
 */
public class DetailsFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>, YouTubeThumbnailView.OnInitializedListener
{
    private static final String LOG_TAG = DetailsFragment.class.getSimpleName();
    private static final String MOVIES_SHARE_HASHTAG = " #PopularMoviesApp";

    private TextView mReleaseDateTextView, mRatingTextView, mOverviewTextView;
    private ImageView mPoster, mBackdropImageView;
    private LinearLayout mVideoContentLayout, mReviewContentLayout, mTrailersDividerLayout,
            mReviewsDividerLayout;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private CardView mEmptyVideoCardView, mEmptyReviewCardView, mDetailsCardView;
    private CoordinatorLayout mCoordinatorLayout;

    private List<YouTubeThumbnailLoader> mThumbnailLoaders;
    private int mIdMovie, mFavorite;
    private String mTitle, mBackdrop, mYoutubeLink;
    private boolean mTwoPane;

    private static final int DETAIL_LOADER = 0;
    private static final int VIDEO_LOADER = 1;
    private static final int REVIEW_LOADER = 2;

    private static final String[] MOVIE_COLUMNS = {
            MovieEntry.COLUMN_TITLE,
            MovieEntry.COLUMN_RELEASE_DATE,
            MovieEntry.COLUMN_VOTE_AVERAGE,
            MovieEntry.COLUMN_OVERVIEW,
            MovieEntry.COLUMN_POSTER,
            MovieEntry.COLUMN_BACKDROP,
            MovieEntry.COLUMN_FAVORITE
    };

    private static final String[] VIDEO_COLUMNS = {
            VideoEntry.COLUMN_KEY,
            VideoEntry.COLUMN_NAME,
            VideoEntry.COLUMN_SITE
    };

    private static final String[] REVIEW_COLUMNS = {
            ReviewEntry.COLUMN_AUTHOR,
            ReviewEntry.COLUMN_CONTENT
    };

    static class VideoViewHolder
    {
        YouTubeThumbnailView thumbnailView;
        TextView title;
    }

    static class ReviewViewHolder
    {
        TextView author;
        TextView content;
    }

    public DetailsFragment()
    {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey(getResources().getString(R.string.id_movie_key)))
        {
            mIdMovie = arguments.getInt(getString(R.string.id_movie_key), 0);
            mTitle = arguments.getString(getString(R.string.title_key), "");
            mFavorite = arguments.getInt(getString(R.string.favorite_key), 0);
            mBackdrop = arguments.getString(getString(R.string.backdrop_key));
            mTwoPane = arguments.getBoolean(getString(R.string.two_pane_key), false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_detail_start, container, false);

        mCoordinatorLayout = (CoordinatorLayout) rootView.findViewById(R.id.details_content);
        mReleaseDateTextView = (TextView) rootView.findViewById(R.id.details_release_date_textview);
        mRatingTextView = (TextView) rootView.findViewById(R.id.details_rating_textview);
        mOverviewTextView = (TextView) rootView.findViewById(R.id.details_overview_textview);
        mPoster = (ImageView) rootView.findViewById(R.id.details_poster_imageview);
        mVideoContentLayout =
                (LinearLayout) rootView.findViewById(R.id.details_videos_content_layout);
        mReviewContentLayout =
                (LinearLayout) rootView.findViewById(R.id.details_reviews_content_layout);
        mEmptyVideoCardView = (CardView) rootView.findViewById(R.id.details_empty_videos_cardview);
        mEmptyReviewCardView =
                (CardView) rootView.findViewById(R.id.details_empty_reviews_cardview);
        mCollapsingToolbarLayout =
                (CollapsingToolbarLayout) rootView.findViewById(R.id.details_collapsing_toolbar);
        mBackdropImageView =
                (ImageView) rootView.findViewById(R.id.backdrop_imageview);
        mDetailsCardView = (CardView) rootView.findViewById(R.id.details_details_cardview);
        mTrailersDividerLayout =
                (LinearLayout) rootView.findViewById(R.id.details_trailers_layout);
        mReviewsDividerLayout =
                (LinearLayout) rootView.findViewById(R.id.details_reviews_layout);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        mThumbnailLoaders = new ArrayList<>();
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        getLoaderManager().initLoader(VIDEO_LOADER, null, this);
        getLoaderManager().initLoader(REVIEW_LOADER, null, this);

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroy()
    {
        for (YouTubeThumbnailLoader loader : mThumbnailLoaders)
        {
            loader.release();
            loader = null;
        }
        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        if (getActivity() instanceof DetailsActivity)
        {
            inflater.inflate(R.menu.menu_details, menu);
            finishCreatingMenu(menu);
        }

    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args)
    {
        switch (loaderId)
        {
            case DETAIL_LOADER:
                Uri movieUri = MovieEntry.buildMovieUriWithId(mIdMovie);

                return new CursorLoader(
                        getActivity(),
                        movieUri,
                        MOVIE_COLUMNS,
                        null,
                        null,
                        null);
            case VIDEO_LOADER:
                Uri videoUri = VideoEntry.buildVideoUriWithMovieId(mIdMovie);

                return new CursorLoader(
                        getActivity(),
                        videoUri,
                        VIDEO_COLUMNS,
                        null,
                        null,
                        null);
            case REVIEW_LOADER:
                Uri reviewUri = ReviewEntry.buildReviewUriWithMovieId(mIdMovie);

                return new CursorLoader(
                        getActivity(),
                        reviewUri,
                        REVIEW_COLUMNS,
                        null,
                        null,
                        null);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)
    {
        switch (loader.getId())
        {
            case DETAIL_LOADER:
                if (cursor.moveToFirst())
                {
                    String releaseDate = cursor.getString(
                            cursor.getColumnIndex(MovieEntry.COLUMN_RELEASE_DATE));
                    double rating = cursor.getDouble(
                            cursor.getColumnIndex(MovieEntry.COLUMN_VOTE_AVERAGE));
                    String overview = cursor.getString(
                            cursor.getColumnIndex(MovieEntry.COLUMN_OVERVIEW));
                    String poster = cursor.getString(
                            cursor.getColumnIndex(MovieEntry.COLUMN_POSTER));

                    if (releaseDate == null)
                    {
                        mReleaseDateTextView.setText(R.string.no_release_date);
                    }
                    else
                    {
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        Date date;
                        try
                        {
                            date = simpleDateFormat.parse(releaseDate);
                            simpleDateFormat = new SimpleDateFormat("MMMM W, yyyy");
                            mReleaseDateTextView.setText(simpleDateFormat.format(date));
                        }
                        catch (ParseException e)
                        {
                            Log.e(LOG_TAG, e.getMessage());
                        }
                    }

                    mRatingTextView.setText(String.format("%.1f / 10.0", rating));

                    if (overview == null)
                    {
                        mOverviewTextView.setText(R.string.no_overview);
                    }
                    else
                    {
                        mOverviewTextView.setText(overview);
                    }

                    if (poster == null || !NetworkUtility.checkConnection(getActivity()))
                    {
                        mPoster.setImageResource(R.drawable.image_not_available);
                    }
                    else
                    {
                        Picasso.with(getActivity()).load(
                                getActivity().getResources().getString(R.string.base_movie_image_url) +
                                getActivity().getResources().getString(R.string.poster_image_size) +
                                poster).into(mPoster);
                    }
                }

                mCollapsingToolbarLayout.setTitle(mTitle);

                mDetailsCardView.setVisibility(View.VISIBLE);

                AppCompatActivity activity = (AppCompatActivity) getActivity();
                Toolbar toolbar = (Toolbar) getView().findViewById(R.id.details_toolbar);

                if (mTwoPane)
                {
                    Picasso.with(getActivity()).load(
                            getString(R.string.base_movie_image_url) +
                            getString(R.string.backdrop_image_size_landscape) +
                            mBackdrop).into(mBackdropImageView);

                    if (toolbar != null)
                    {
                        Menu menu = toolbar.getMenu();
                        if (menu != null)
                        {
                            menu.clear();
                        }

                        toolbar.inflateMenu(R.menu.menu_details);
                        finishCreatingMenu(toolbar.getMenu());
                    }
                }
                else
                {
                    Picasso.with(getActivity()).load(
                            getString(R.string.base_movie_image_url) +
                            getString(R.string.backdrop_image_size) +
                            mBackdrop).into(mBackdropImageView);

                    if (toolbar != null)
                    {
                        activity.setSupportActionBar(toolbar);
                        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    }
                }

                break;
            case VIDEO_LOADER:
                if (cursor.getCount() > 0)
                {
                    mEmptyVideoCardView.setVisibility(View.GONE);

                    while (cursor.moveToNext())
                    {
                        if (cursor.isFirst())
                        {
                            mYoutubeLink =
                                    getString(R.string.youtube_base_url) +
                                    cursor.getString(
                                            cursor.getColumnIndex(VideoEntry.COLUMN_KEY));
                        }

                        View view = LayoutInflater.from(getActivity())
                                .inflate(R.layout.list_item_video_entry, null);
                        LinearLayout.LayoutParams params =
                                new LinearLayout.LayoutParams
                                        (ViewGroup.LayoutParams.MATCH_PARENT,
                                         (int) getResources().getDimension(R.dimen.thumbnail_card_height));
                        params.topMargin =
                                (int) getResources().getDimension(R.dimen.card_vertical_margin);
                        params.bottomMargin =
                                (int) getResources().getDimension(R.dimen.card_vertical_margin);
                        mVideoContentLayout.addView(view, params);

                        final VideoViewHolder viewHolder = new VideoViewHolder();
                        final String videoKey =
                                cursor.getString(cursor.getColumnIndex(VideoEntry.COLUMN_KEY));

                        viewHolder.title =
                                (TextView) view.findViewById(R.id.video_entry_title_textview);
                        viewHolder.thumbnailView =
                                (YouTubeThumbnailView) view.findViewById(R.id.video_entry_thumbnail);

                        viewHolder.title.setText(
                                cursor.getString(cursor.getColumnIndex(VideoEntry.COLUMN_NAME)));

                        viewHolder.thumbnailView.setTag(videoKey);
                        viewHolder.thumbnailView.initialize(
                                getString(R.string.youtube_api_key),
                                this);

                        view.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                Intent intent = YouTubeStandalonePlayer.createVideoIntent(
                                        getActivity(),
                                        getString(R.string.youtube_api_key),
                                        videoKey);
                                startActivity(intent);
                            }
                        });
                    }
                }

                mTrailersDividerLayout.setVisibility(View.VISIBLE);
                break;
            case REVIEW_LOADER:
                if (cursor.getCount() > 0)
                {
                    mEmptyReviewCardView.setVisibility(View.GONE);

                    while (cursor.moveToNext())
                    {

                        View view = LayoutInflater.from(getActivity())
                                .inflate(R.layout.list_item_review_entry, null);
                        LinearLayout.LayoutParams params =
                                new LinearLayout.LayoutParams
                                        (ViewGroup.LayoutParams.MATCH_PARENT,
                                         ViewGroup.LayoutParams.WRAP_CONTENT);
                        params.topMargin =
                                (int) getResources().getDimension(R.dimen.card_vertical_margin);
                        params.bottomMargin =
                                (int) getResources().getDimension(R.dimen.card_vertical_margin);
                        mReviewContentLayout.addView(view, params);

                        ReviewViewHolder viewHolder = new ReviewViewHolder();

                        viewHolder.author =
                                (TextView) view.findViewById(R.id.review_entry_reviewer_textview);
                        viewHolder.content =
                                (TextView) view.findViewById(R.id.review_entry_content_textview);

                        viewHolder.author.setText(
                                cursor.getString(cursor.getColumnIndex(ReviewEntry.COLUMN_AUTHOR)));
                        viewHolder.content.setText(
                                cursor.getString(cursor.getColumnIndex(ReviewEntry.COLUMN_CONTENT)));

                    }
                }

                mReviewsDividerLayout.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {
        //Do nothing
    }

    @Override
    public void onInitializationSuccess(YouTubeThumbnailView view, YouTubeThumbnailLoader loader)
    {
        loader.setVideo((String) view.getTag());
        mThumbnailLoaders.add(loader);
    }

    @Override
    public void onInitializationFailure(YouTubeThumbnailView view, YouTubeInitializationResult result)
    {
        Log.e(LOG_TAG, result.toString());
    }

    private void finishCreatingMenu(Menu menu)
    {
        MenuItem shareMenuItem = menu.findItem(R.id.action_share);
        MenuItem favoriteMenuItem = menu.findItem(R.id.action_favorites);
        MenuItem backMenuItem = menu.findItem(R.id.home);

        shareMenuItem.setIntent(createShareIntent());
        favoriteMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem item)
            {
                if (mFavorite == 0)
                {
                    mFavorite = 1;
                    item.setIcon(R.drawable.ic_star_white_48dp);
                }
                else
                {
                    mFavorite = 0;
                    item.setIcon(R.drawable.ic_star_border_white_48dp);
                }

                UpdateMovieTask updateMovieTask = new UpdateMovieTask(getActivity());
                updateMovieTask.execute(mIdMovie, mFavorite);
                return true;
            }
        });

        if (backMenuItem != null)
        {
            backMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
            {
                @Override
                public boolean onMenuItemClick(MenuItem item)
                {
                    getActivity().onBackPressed();
                    return true;
                }
            });
        }

        if (mFavorite == 1)
        {
            favoriteMenuItem.setIcon(R.drawable.ic_star_white_48dp);
        }
    }

    private Intent createShareIntent()
    {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mYoutubeLink + MOVIES_SHARE_HASHTAG);

        return shareIntent;
    }

}
