package com.vazp.popularmovies.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vazp.popularmovies.R;
import com.vazp.popularmovies.data.MoviesContract.VideoEntry;

/**
 * Created by Miguel on 9/9/2015.
 */
public class VideosAdapter extends RecyclerView.Adapter<VideosAdapter.VideosAdapterViewHolder>
{
    private static final String LOG_TAG = VideosAdapter.class.getSimpleName();

    private Cursor mCursor;
    private Context mContext;

    public class VideosAdapterViewHolder extends RecyclerView.ViewHolder
    {
        private String mKey, mName, mSite;

        public VideosAdapterViewHolder(View view)
        {
            super(view);
        }

        public String getKey()
        {
            return mKey;
        }

        public void setKey(String key)
        {
            this.mKey = key;
        }

        public String getName()
        {
            return mName;
        }

        public void setName(String name)
        {
            this.mName = name;
        }

        public String getSite()
        {
            return mSite;
        }

        public void setSite(String site)
        {
            this.mSite = site;
        }
    }

    public VideosAdapter(Context context)
    {
        mContext = context;
    }

    @Override
    public VideosAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        if (parent instanceof RecyclerView)
        {
            int layoutId = R.layout.list_item_video_entry;
            View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
            view.setFocusable(true);

            return new VideosAdapterViewHolder(view);
        }
        else
        {
            return null;
        }
    }

    @Override
    public void onBindViewHolder(VideosAdapterViewHolder holder, int position)
    {
        mCursor.moveToPosition(position);

        holder.setKey(mCursor.getString(mCursor.getColumnIndex(VideoEntry.COLUMN_KEY)));
        holder.setName(mCursor.getString(mCursor.getColumnIndex(VideoEntry.COLUMN_NAME)));
        holder.setSite(mCursor.getString(mCursor.getColumnIndex(VideoEntry.COLUMN_SITE)));
    }

    @Override
    public int getItemCount()
    {
        if (mCursor == null)
        {
            return 0;
        }
        else
        {
            return mCursor.getCount();
        }
    }

    public void swapCursor(Cursor newCursor)
    {
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    public Cursor getCursor()
    {
        return mCursor;
    }
}
