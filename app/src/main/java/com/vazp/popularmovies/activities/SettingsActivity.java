package com.vazp.popularmovies.activities;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;

import com.vazp.popularmovies.NetworkUtility;
import com.vazp.popularmovies.R;
import com.vazp.popularmovies.tasks.GetMoviesTask;

/**
 * Created by Miguel on 8/11/2015.
 */
public class SettingsActivity extends Activity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    public static class SettingsFragment extends PreferenceFragment
            implements SharedPreferences.OnSharedPreferenceChangeListener
    {
        boolean fetchMovies;

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.settings_main);

            fetchMovies = false;
            onSharedPreferenceChanged(
                    getPreferenceScreen().getSharedPreferences(),
                    getString(R.string.settings_sort_key));
            fetchMovies = true;
        }

        @Override
        public void onResume()
        {
            super.onResume();
            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause()
        {
            super.onPause();
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
        {
            if (fetchMovies && NetworkUtility.checkConnection(getActivity()))
            {
                GetMoviesTask getMoviesTask = new GetMoviesTask(getActivity());
                getMoviesTask.execute(sharedPreferences.getString(key, ""));
            }

            ListPreference listPreference = (ListPreference) findPreference(key);
            int index = listPreference.findIndexOfValue(
                    sharedPreferences.getString(key, ""));

            if (index >= 0)
            {
                listPreference.setSummary(listPreference.getEntries()[index]);
            }
        }
    }

}
