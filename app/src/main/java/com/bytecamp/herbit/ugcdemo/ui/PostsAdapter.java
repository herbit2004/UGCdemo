package com.bytecamp.herbit.ugcdemo.ui;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
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
import com.bytecamp.herbit.ugcdemo.data.model.PostCardItem;
import java.util.ArrayList;
import java.util.List;

/**
 * PostsAdapter
 * 首页瀑布流适配器。
 * 负责展示帖子卡片，包括图片封面或文字封面、作者信息以及统计数据。
 */
public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.PostViewHolder> {

    private List<PostCardItem> posts = new ArrayList<>();

    /**
     * 更新帖子列表数据
     * @param posts 新的数据列表
     */
    public void setPosts(List<PostCardItem> posts) {
        this.posts = posts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        holder.bind(posts.get(position));
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    /**
     * ViewHolder 内部类，封装数据绑定逻辑
     */
    static class PostViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivPostImage;
        private final ImageView ivAuthorAvatar;
        private final TextView tvTitle;
        private final TextView tvAuthorName;
        private final TextView tvCoverTitle;
        private final TextView tvLikeCount;
        private final TextView tvCommentCount;
        private final Context context;

        PostViewHolder(@NonNull View itemView) {
            super(itemView);
            this.context = itemView.getContext();
            
            ivPostImage = itemView.findViewById(R.id.ivPostImage);
            ivAuthorAvatar = itemView.findViewById(R.id.ivAuthorAvatar);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvAuthorName = itemView.findViewById(R.id.tvAuthorName);
            tvCoverTitle = itemView.findViewById(R.id.tvCoverTitle);
            tvLikeCount = itemView.findViewById(R.id.tvLikeCount);
            tvCommentCount = itemView.findViewById(R.id.tvCommentCount);
        }

        void bind(PostCardItem item) {
            // 1. 绑定文本内容
            tvTitle.setText(item.post.title);
            tvAuthorName.setText(item.user != null ? item.user.username : "Unknown");
            tvLikeCount.setText(String.valueOf(item.likeCount));
            tvCommentCount.setText(String.valueOf(item.commentCount));

            // 2. 处理封面图显示逻辑
            String firstImagePath = extractFirstImage(item.post.image_path);

            if (firstImagePath != null) {
                showImageCover(firstImagePath);
            } else {
                showTextCover(item.post.title);
            }

            // 3. 加载作者头像
            if (item.user != null && item.user.avatar_path != null) {
                Glide.with(context)
                        .load(item.user.avatar_path)
                        .circleCrop()
                        .into(ivAuthorAvatar);
            } else {
                ivAuthorAvatar.setImageResource(R.mipmap.ic_launcher_round);
            }

            // 4. 设置点击跳转
            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, DetailActivity.class);
                intent.putExtra(DetailActivity.EXTRA_POST_ID, item.post.post_id);
                context.startActivity(intent);
            });
        }

        private String extractFirstImage(String imagePath) {
            if (!TextUtils.isEmpty(imagePath)) {
                String[] paths = imagePath.split(";");
                if (paths.length > 0 && !TextUtils.isEmpty(paths[0])) {
                    return paths[0];
                }
            }
            return null;
        }

        private void showImageCover(String path) {
            ivPostImage.setVisibility(View.VISIBLE);
            tvCoverTitle.setVisibility(View.GONE);
            Glide.with(context)
                    .load(path)
                    .placeholder(R.mipmap.ic_launcher)
                    .into(ivPostImage);
        }

        private void showTextCover(String title) {
            ivPostImage.setVisibility(View.GONE);
            tvCoverTitle.setVisibility(View.VISIBLE);
            tvCoverTitle.setText(title);
        }
    }
}