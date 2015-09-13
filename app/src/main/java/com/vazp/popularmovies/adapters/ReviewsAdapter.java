package com.vazp.popularmovies.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vazp.popularmovies.R;
import com.vazp.popularmovies.data.MoviesContract.ReviewEntry;

/**
 * Created by Miguel on 9/9/2015.
 */
public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewsAdapterViewHolder>
{
    private static final String LOG_TAG = ReviewsAdapter.class.getSimpleName();

    private Cursor mCursor;
    private Context mContext;

    public class ReviewsAdapterViewHolder extends RecyclerView.ViewHolder
    {
        private String mAuthor, mContent;

        public ReviewsAdapterViewHolder(View view)
        {
            super(view);
        }

        public String getAuthor()
        {
            return mAuthor;
        }

        public void setAuthor(String author)
        {
            this.mAuthor= author;
        }

        public String getContent()
        {
            return mContent;
        }

        public void setContent(String content)
        {
            this.mContent= content;
        }
    }

    public ReviewsAdapter(Context context)
    {
        mContext = context;
    }

    @Override
    public ReviewsAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        if (parent instanceof RecyclerView)
        {
            int layoutId = R.layout.list_item_review_entry;
            View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
            view.setFocusable(true);

            return new ReviewsAdapterViewHolder(view);
        }
        else
        {
            return null;
        }
    }

    @Override
    public void onBindViewHolder(ReviewsAdapterViewHolder holder, int position)
    {
        mCursor.moveToPosition(position);

        holder.setAuthor(mCursor.getString(mCursor.getColumnIndex(ReviewEntry.COLUMN_AUTHOR)));
        holder.setContent(mCursor.getString(mCursor.getColumnIndex(ReviewEntry.COLUMN_CONTENT)));
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
