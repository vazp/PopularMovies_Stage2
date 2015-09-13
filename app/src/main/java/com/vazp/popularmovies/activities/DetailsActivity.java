package com.vazp.popularmovies.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.vazp.popularmovies.R;
import com.vazp.popularmovies.fragments.DetailsFragment;

/**
 * Created by Miguel on 8/15/2015.
 */
public class DetailsActivity extends AppCompatActivity
{
    private static final String LOG_TAG = DetailsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        int idMovie = getIntent().getIntExtra(getResources().getString(R.string.id_movie_key), 0);
        String title = getIntent().getStringExtra(getResources().getString(R.string.title_key));
        int favorite = getIntent().getIntExtra(getResources().getString(R.string.favorite_key), 0);
        String backdrop = getIntent().getStringExtra(getString(R.string.backdrop_key));

        Bundle arguments = new Bundle();
        arguments.putInt(getString(R.string.id_movie_key), idMovie);
        arguments.putString(getString(R.string.title_key), title);
        arguments.putInt(getString(R.string.favorite_key), favorite);
        arguments.putString(getString(R.string.backdrop_key), backdrop);

        DetailsFragment detailsFragment = new DetailsFragment();
        detailsFragment.setArguments(arguments);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.details_content_frame, detailsFragment)
                .commit();
    }
}
