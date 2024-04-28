package me.zhanghai.android.files.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.recyclerview.widget.RecyclerView;

public class StaticAdapter extends RecyclerView.Adapter<StaticAdapter.ViewHolder> {

    @LayoutRes
    private final int layoutRes;
    private final View.OnClickListener listener;
    private int itemCount = 1;

    public StaticAdapter(@LayoutRes int layoutRes, View.OnClickListener listener) {
        this.layoutRes = layoutRes;
        this.listener = listener;
        setHasStableIds(true);
    }

    public StaticAdapter(@LayoutRes int layoutRes) {
        this(layoutRes, null);
    }

    public void setItemCount(int itemCount) {
        if (this.itemCount == itemCount) {
            return;
        }
        int oldValue = this.itemCount;
        this.itemCount = itemCount;
        if (itemCount < oldValue) {
            notifyItemRangeRemoved(itemCount, oldValue - itemCount);
        } else {
            notifyItemRangeInserted(oldValue, itemCount - oldValue);
        }
    }

    @Override
    public int getItemCount() {
        return itemCount;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(layoutRes, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (listener != null) {
            holder.itemView.setOnClickListener(view -> listener.onClick(view));
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}

