package com.example.android.inventory.database;

import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

public abstract class InventoryAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    protected Context mContext;
    private Cursor mCursor;
    private boolean mDataValid;
    private int mRowIdColumn;
    private ItemOnClick itemOnClick;
    private DataSetObserver mDataSetObserver;

    public interface ItemOnClick {
        void onClick(int p);
    }

    public InventoryAdapter(Context context, Cursor cursor, ItemOnClick itemOnClick) {
        mContext = context;
        mCursor = cursor;
        mDataValid = cursor != null;
        mRowIdColumn = mDataValid ? mCursor.getColumnIndex("_id") : -1;

        mDataSetObserver = new NotifyingDataSetObserver(this);

        if (mCursor != null) {
            mCursor.registerDataSetObserver(mDataSetObserver);
        }

        this.itemOnClick = itemOnClick;

    }

    public void setDataValid(boolean mDataValid) {
        this.mDataValid = mDataValid;
    }

    @Override
    public long getItemId(int position) {
        if (mDataValid && mCursor != null && mCursor.moveToPosition(position)) {
            return mCursor.getLong(mRowIdColumn);
        }

        return 0;
    }

    @Override
    public void setHasStableIds(boolean hasStableIds) {
        super.setHasStableIds(true);
    }


    @Override
    public int getItemCount() {
        if (mDataValid && mCursor != null) {
            return mCursor.getCount();
        }

        return 0;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        if (!mDataValid) {
            throw new IllegalStateException("Invalid data");
        }
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("Could not move cursor to position " + position);
        }

        onBindViewHolder(holder, mCursor);
    }


    public void changeCursor(Cursor cursor) {
        Cursor old = swapCursor(cursor);
        if (old != null) {
            old.close();
        }
    }

    public Cursor swapCursor(Cursor newCursor) {
        if (newCursor == mCursor) {
            return null;
        }

        final Cursor oldCursor = mCursor;

        if (oldCursor != null && mDataSetObserver != null) {
            oldCursor.unregisterDataSetObserver(mDataSetObserver);
        }
        mCursor = newCursor;

        if (mCursor != null) {

            if (mDataSetObserver != null) {
                mCursor.registerDataSetObserver(mDataSetObserver);
            }
            mRowIdColumn = newCursor.getColumnIndexOrThrow("_id");
            mDataValid = true;
            notifyDataSetChanged();
        } else {
            mRowIdColumn = -1;
            mDataValid = false;
            notifyDataSetChanged();
        }

        return oldCursor;

    }

    public Cursor getCursor() {
        return mCursor;
    }


    public abstract void onBindViewHolder(VH viewHolder, Cursor cursor);

    private class NotifyingDataSetObserver extends DataSetObserver {
        private RecyclerView.Adapter adapter;

        public NotifyingDataSetObserver(RecyclerView.Adapter adapter) {
            this.adapter = adapter;
        }

        @Override
        public void onChanged() {
            super.onChanged();
            mDataValid = true;
            adapter.notifyDataSetChanged();
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
            mDataValid = false;
        }
    }

}
