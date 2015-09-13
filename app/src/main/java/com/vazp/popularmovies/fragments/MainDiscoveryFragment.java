package com.vazp.popularmovies.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.vazp.popularmovies.NetworkUtility;
import com.vazp.popularmovies.R;
import com.vazp.popularmovies.adapters.MoviesAdapter;
import com.vazp.popularmovies.data.MoviesContract;
import com.vazp.popularmovies.tasks.GetMoviesTask;

/**
 * Created by Miguel on 15/07/2015.
 */
public class MainDiscoveryFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>
{
    private static final String LOG_TAG = MainDiscoveryFragment.class.getSimpleName();
    private static final int MOVIES_LOADER = 0;

    private MoviesAdapter mMoviesAdapter;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private CoordinatorLayout mCoordinatorLayout;
    private int mPosition = RecyclerView.NO_POSITION;

    private static final String[] MOVIE_COLUMNS = {
            MoviesContract.MovieEntry.COLUMN_MOVIE_ID,
            MoviesContract.MovieEntry.COLUMN_TITLE,
            MoviesContract.MovieEntry.COLUMN_POSTER,
            MoviesContract.MovieEntry.COLUMN_BACKDROP,
            MoviesContract.MovieEntry.COLUMN_FAVORITE
    };

    private boolean mAutoSelectView, mTwoPane;

    public interface Callback
    {
        void onItemSelected(MoviesAdapter.MoviesAdapterViewHolder viewHolder);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onInflate(Context context, AttributeSet attrs, Bundle savedInstanceState)
    {
        super.onInflate(context, attrs, savedInstanceState);

        TypedArray array = context.obtainStyledAttributes(attrs,
                                                          R.styleable.MainDiscoveryFragment,
                                                          0,
                                                          0);
        mAutoSelectView = array.getBoolean(R.styleable.MainDiscoveryFragment_autoSelectView, false);
        mTwoPane = array.getBoolean(R.styleable.MainDiscoveryFragment_twoPane, false);
        array.recycle();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_main_discovery, container, false);

        mCoordinatorLayout =
                (CoordinatorLayout) rootView.findViewById(R.id.main_discovery_coordinatorlayout);
        mSwipeRefreshLayout =
                (SwipeRefreshLayout) rootView.findViewById(R.id.main_discovery_swiperefresh);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.main_discovery_recyclerview);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                fetchMovies();
                getLoaderManager().restartLoader(MOVIES_LOADER, null, MainDiscoveryFragment.this);
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        View emptyView = rootView.findViewById(R.id.main_discovery_empty);

        mRecyclerView.setHasFixedSize(true);

        int orientation = getResources().getConfiguration().orientation;
        int spanCount = 2;
        int recyclerOrientation;

        if (mTwoPane)
        {
            if (orientation == Configuration.ORIENTATION_PORTRAIT)
            {
                recyclerOrientation = LinearLayoutManager.HORIZONTAL;
            }
            else
            {
                spanCount = 3;
                recyclerOrientation = LinearLayoutManager.VERTICAL;
            }
        }
        else
        {
            if (orientation == Configuration.ORIENTATION_PORTRAIT)
            {
                recyclerOrientation = LinearLayoutManager.VERTICAL;
            }
            else
            {
                recyclerOrientation = LinearLayoutManager.HORIZONTAL;
            }
        }

        mRecyclerView.setLayoutManager(new GridLayoutManager(
                getActivity(),
                spanCount,
                recyclerOrientation,
                false));
        mMoviesAdapter = new MoviesAdapter(getActivity(), new MoviesAdapter.MoviesAdapterOnClickHandler()
        {
            @Override
            public void onClick(MoviesAdapter.MoviesAdapterViewHolder viewHolder)
            {
                mPosition = viewHolder.getAdapterPosition();
                ((Callback) getActivity()).onItemSelected(viewHolder);
            }
        }, emptyView);
        mRecyclerView.setAdapter(mMoviesAdapter);

        if (savedInstanceState != null &&
            savedInstanceState.containsKey(getString(R.string.selected_position_key)))
        {
            mPosition = savedInstanceState.getInt(getString(R.string.selected_position_key));
        }

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        if (savedInstanceState == null)
        {
            fetchMovies();
        }

        getLoaderManager().initLoader(MOVIES_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume()
    {
        if (getLoaderManager().getLoader(MOVIES_LOADER) != null)
        {
            mPosition = 0;
            getLoaderManager().restartLoader(MOVIES_LOADER, null, this);
        }
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        if (mPosition != RecyclerView.NO_POSITION)
        {
            outState.putInt(getString(R.string.selected_position_key), mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        Uri moviesUri = MoviesContract.MovieEntry.CONTENT_URI;
        String sortBy;
        String where = null;
        String[] arguments = null;
        String sortPreference = getSortPreference();

        if (sortPreference.equals(getString(R.string.settings_sort_popularity)))
        {
            sortBy = MoviesContract.MovieEntry.COLUMN_POPULARITY + " DESC";
        }
        else if (sortPreference.equals(getString(R.string.settings_sort_rating)))
        {
            sortBy = MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE + " DESC";
        }
        else
        {
            sortBy = MoviesContract.MovieEntry.COLUMN_POPULARITY + " DESC";
            where = MoviesContract.MovieEntry.COLUMN_FAVORITE + " = ?";
            arguments = new String[]{"1"};
        }

        return new CursorLoader(
                getActivity(),
                moviesUri,
                MOVIE_COLUMNS,
                where,
                arguments,
                sortBy);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data)
    {
        mMoviesAdapter.swapCursor(data);

        if (data.getCount() > 0 && mAutoSelectView)
        {
            mRecyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener()
            {
                @Override
                public boolean onPreDraw()
                {
                    if(mRecyclerView.getChildCount() > 0)
                    {
                        mRecyclerView.getViewTreeObserver().removeOnPreDrawListener(this);

                        if (mPosition == RecyclerView.NO_POSITION)
                        {
                            mPosition = 0;
                        }

                        mRecyclerView.smoothScrollToPosition(mPosition);
                        RecyclerView.ViewHolder viewHolder =
                                mRecyclerView.findViewHolderForAdapterPosition(mPosition);

                        if (viewHolder != null)
                        {
                            mMoviesAdapter.selectView(viewHolder);
                        }

                        return true;
                    }

                    return false;
                }
            });
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {
        mMoviesAdapter.swapCursor(null);
    }

    public void fetchMovies()
    {
        if (NetworkUtility.checkConnection(getActivity()))
        {
            GetMoviesTask getMoviesTask = new GetMoviesTask(getActivity());
            getMoviesTask.execute(getSortPreference());
        }
        else
        {
            Snackbar.make(mCoordinatorLayout,
                          getString(R.string.no_connection),
                          Snackbar.LENGTH_LONG)
                    .setAction(
                            getString(R.string.retry),
                            new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View v)
                                {
                                    fetchMovies();
                                }
                            }).show();
        }
    }

    private String getSortPreference()
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return preferences.getString(getString(R.string.settings_sort_key),
                                     getString(R.string.settings_sort_popularity));
    }

}
