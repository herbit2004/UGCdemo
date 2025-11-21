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

public class ImagePreviewAdapter extends RecyclerView.Adapter<ImagePreviewAdapter.ViewHolder> {
    private List<Uri> images = new ArrayList<>();
    private OnImageClickListener listener;

    public interface OnImageClickListener {
        void onImageClick(int position);
    }
    
    public ImagePreviewAdapter(OnImageClickListener listener) {
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
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image_preview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Uri uri = images.get(position);
        Glide.with(holder.itemView)
                .load(uri)
                .centerCrop()
                .into(holder.imageView);
                
        holder.itemView.setOnClickListener(v -> {
             if (listener != null) listener.onImageClick(position);
        });
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.ivPreviewItem);
        }
    }
}