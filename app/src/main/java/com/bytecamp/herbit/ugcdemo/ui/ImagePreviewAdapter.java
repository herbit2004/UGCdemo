package com.bytecamp.herbit.ugcdemo.ui;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bytecamp.herbit.ugcdemo.R;
import java.util.ArrayList;
import java.util.List;

public class ImagePreviewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Uri> images = new ArrayList<>();
    private OnImageClickListener listener;
    private final int maxCount;
    private static final int VIEW_IMAGE = 0;
    private static final int VIEW_ADD = 1;

    public interface OnImageClickListener {
        void onImageClick(int position);
        void onAddClick();
    }
    
    public ImagePreviewAdapter(int maxCount, OnImageClickListener listener) {
        this.maxCount = maxCount;
        this.listener = listener;
    }

    public void setImages(List<Uri> images) {
        this.images = images;
        notifyDataSetChanged();
    }
    
    public void addImage(Uri uri) {
        this.images.add(uri);
        notifyItemInserted(images.size() - 1);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_ADD) {
            View v = inflater.inflate(R.layout.item_image_add, parent, false);
            return new AddViewHolder(v);
        } else {
            View v = inflater.inflate(R.layout.item_image_preview, parent, false);
            return new ImageViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_ADD) {
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onAddClick();
            });
        } else {
            Uri uri = images.get(position);
            ImageViewHolder h = (ImageViewHolder) holder;
            Glide.with(holder.itemView)
                    .load(uri)
                    .centerCrop()
                    .into(h.imageView);
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onImageClick(position);
            });
        }
    }

    @Override
    public int getItemCount() {
        return images.size() < maxCount ? images.size() + 1 : images.size();
    }

    @Override
    public int getItemViewType(int position) {
        return (images.size() < maxCount && position == images.size()) ? VIEW_ADD : VIEW_IMAGE;
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.ivPreviewItem);
        }
    }

    static class AddViewHolder extends RecyclerView.ViewHolder {
        AddViewHolder(View itemView) {
            super(itemView);
        }
    }
}