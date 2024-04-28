package me.zhanghai.android.files.storage;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import me.zhanghai.android.files.databinding.LanSmbServerItemBinding;
import me.zhanghai.android.files.ui.SimpleAdapter;

public class LanSmbServerListAdapter extends SimpleAdapter<LanSmbServer, LanSmbServerListAdapter.ViewHolder> {

    private final OnItemClickListener listener;

    public LanSmbServerListAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).hashCode();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        LanSmbServerItemBinding binding = LanSmbServerItemBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LanSmbServer server = getItem(position);
        LanSmbServerItemBinding binding = holder.binding;
        binding.itemLayout.setOnClickListener(view -> listener.onItemClick(server));
        binding.hostText.setText(server.getHost());
        binding.addressText.setText(server.getAddress().getHostAddress());
    }

    @Override
    protected boolean getHasStableIds() {
        //TODO ny
        return false;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final LanSmbServerItemBinding binding;

        public ViewHolder(LanSmbServerItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public interface OnItemClickListener {
        void onItemClick(LanSmbServer server);
    }
}

