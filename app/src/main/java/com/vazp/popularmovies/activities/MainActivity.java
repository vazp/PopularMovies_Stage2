package com.vazp.popularmovies.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.vazp.popularmovies.R;
import com.vazp.popularmovies.adapters.MoviesAdapter;
import com.vazp.popularmovies.fragments.DetailsFragment;
import com.vazp.popularmovies.fragments.MainDiscoveryFragment;
import com.vazp.popularmovies.tasks.GetVideosAndReviewsTask;

public class MainActivity extends AppCompatActivity implements MainDiscoveryFragment.Callback
{
	private static final String LOG_TAG = MainActivity.class.getSimpleName();

	private boolean mTwoPane;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		if (findViewById(R.id.movie_detail_container) != null)
		{
			mTwoPane = true;
		}
		else
		{
			mTwoPane = false;
		}
	}

	@Override
	public void onItemSelected(MoviesAdapter.MoviesAdapterViewHolder viewHolder)
	{
		GetVideosAndReviewsTask videosAndReviewsTask = new GetVideosAndReviewsTask(this);
		videosAndReviewsTask.execute(viewHolder.getIdMovie());
		Bundle arguments = new Bundle();
		arguments.putInt(getString(R.string.id_movie_key), viewHolder.getIdMovie());
		arguments.putString(getString(R.string.title_key), viewHolder.getTitle());
		arguments.putInt(getString(R.string.favorite_key), viewHolder.getFavorite());
		arguments.putString(getString(R.string.backdrop_key), viewHolder.getBackdrop());
		arguments.putBoolean(getString(R.string.two_pane_key), mTwoPane);

		if (mTwoPane)
		{
			DetailsFragment detailsFragment = new DetailsFragment();
			detailsFragment.setArguments(arguments);

			getSupportFragmentManager().beginTransaction()
					.replace(R.id.movie_detail_container, detailsFragment)
					.commit();
		}
		else
		{
			Intent detailsIntent = new Intent(this, DetailsActivity.class);
			detailsIntent.putExtras(arguments);
			startActivity(detailsIntent);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings)
		{
			startActivity(new Intent(MainActivity.this, SettingsActivity.class));
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}
