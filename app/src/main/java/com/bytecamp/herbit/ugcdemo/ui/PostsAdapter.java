package com.bytecamp.herbit.ugcdemo.ui;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bytecamp.herbit.ugcdemo.DetailActivity;
import com.bytecamp.herbit.ugcdemo.R;
import com.bytecamp.herbit.ugcdemo.data.model.PostWithUser;
import java.util.ArrayList;
import java.util.List;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.PostViewHolder> {

    private List<PostWithUser> posts = new ArrayList<>();
    private Context context;

    public void setPosts(List<PostWithUser> posts) {
        this.posts = posts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        PostWithUser item = posts.get(position);
        holder.tvTitle.setText(item.post.title);
        holder.tvAuthorName.setText(item.user != null ? item.user.username : "Unknown");

        if (item.post.image_path != null) {
            Glide.with(context)
                    .load(item.post.image_path)
                    .placeholder(R.mipmap.ic_launcher)
                    .into(holder.ivPostImage);
        } else {
            holder.ivPostImage.setImageResource(R.mipmap.ic_launcher);
        }

        if (item.user != null && item.user.avatar_path != null) {
             Glide.with(context)
                    .load(item.user.avatar_path)
                    .circleCrop()
                    .into(holder.ivAuthorAvatar);
        }
        
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra(DetailActivity.EXTRA_POST_ID, item.post.post_id);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPostImage, ivAuthorAvatar;
        TextView tvTitle, tvAuthorName;

        PostViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPostImage = itemView.findViewById(R.id.ivPostImage);
            ivAuthorAvatar = itemView.findViewById(R.id.ivAuthorAvatar);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvAuthorName = itemView.findViewById(R.id.tvAuthorName);
        }
    }
}