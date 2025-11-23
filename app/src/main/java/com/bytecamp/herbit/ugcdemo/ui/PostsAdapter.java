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
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import com.bumptech.glide.Glide;
import com.bytecamp.herbit.ugcdemo.DetailActivity;
import com.bytecamp.herbit.ugcdemo.MainActivity;
import com.bytecamp.herbit.ugcdemo.R;
import com.bytecamp.herbit.ugcdemo.UserProfileActivity;
import com.bytecamp.herbit.ugcdemo.data.model.PostCardItem;
import java.util.ArrayList;
import java.util.List;

/**
 * PostsAdapter
 * 首页瀑布流适配器。
 * 负责展示帖子卡片，包括图片封面或文字封面、作者信息以及统计数据。
 */
public class PostsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<PostCardItem> posts = new ArrayList<>();
    private static final int VIEW_POST = 0;
    private static final int VIEW_FOOTER = 1;
    private boolean loading = false;
    private boolean hasMore = true;

    /**
     * 更新帖子列表数据
     * @param posts 新的数据列表
     */
    public void setPosts(List<PostCardItem> posts) {
        this.posts = posts;
        notifyDataSetChanged();
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
        notifyItemChanged(getItemCount() - 1);
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
        notifyItemChanged(getItemCount() - 1);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == VIEW_FOOTER) {
            View view = inflater.inflate(R.layout.item_list_footer, parent, false);
            return new FooterViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_post, parent, false);
            return new PostViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_FOOTER) {
            FooterViewHolder fh = (FooterViewHolder) holder;
            if (loading) {
                fh.tvFooter.setText("正在加载...");
                fh.tvFooter.setVisibility(View.VISIBLE);
            } else if (!hasMore && posts.size() > 0) {
                fh.tvFooter.setText("没有更多了");
                fh.tvFooter.setVisibility(View.VISIBLE);
            } else {
                fh.tvFooter.setVisibility(View.GONE);
            }
        } else {
            PostViewHolder ph = (PostViewHolder) holder;
            ph.bind(posts.get(position));
        }
    }

    @Override
    public int getItemCount() {
        boolean showFooter = loading || (!hasMore && posts.size() > 0);
        return posts.size() + (showFooter ? 1 : 0);
    }

    @Override
    public int getItemViewType(int position) {
        boolean showFooter = loading || (!hasMore && posts.size() > 0);
        if (showFooter && position == getItemCount() - 1) return VIEW_FOOTER;
        return VIEW_POST;
    }

    @Override
    public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        if (holder instanceof FooterViewHolder) {
            ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
            if (lp instanceof StaggeredGridLayoutManager.LayoutParams) {
                ((StaggeredGridLayoutManager.LayoutParams) lp).setFullSpan(true);
            }
        }
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
        private final TextView tvCoverQuote;
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
            tvCoverQuote = itemView.findViewById(R.id.tvCoverQuote);
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
            
            View.OnClickListener profileListener = v -> {
                if (item.post.author_id == getCurrentUserId(context)) {
                    Intent intent = new Intent(context, MainActivity.class);
                    intent.putExtra("open_profile", true);
                    context.startActivity(intent);
                } else {
                    Intent intent = new Intent(context, UserProfileActivity.class);
                    intent.putExtra(UserProfileActivity.EXTRA_USER_ID, item.post.author_id);
                    context.startActivity(intent);
                }
            };
            
            ivAuthorAvatar.setOnClickListener(profileListener);
            tvAuthorName.setOnClickListener(profileListener);
        }
        
        private long getCurrentUserId(Context context) {
            return context.getSharedPreferences("ugc_prefs", Context.MODE_PRIVATE).getLong("user_id", -1);
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
            tvCoverQuote.setVisibility(View.GONE);
            Glide.with(context)
                    .load(path)
                    .placeholder(R.mipmap.ic_launcher)
                    .into(ivPostImage);
        }

        private void showTextCover(String title) {
            ivPostImage.setVisibility(View.GONE);
            tvCoverTitle.setVisibility(View.VISIBLE);
            tvCoverQuote.setVisibility(View.VISIBLE);
            tvCoverTitle.setText(title);
        }
    }

    static class FooterViewHolder extends RecyclerView.ViewHolder {
        TextView tvFooter;
        FooterViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFooter = itemView.findViewById(R.id.tvFooter);
        }
    }
}
