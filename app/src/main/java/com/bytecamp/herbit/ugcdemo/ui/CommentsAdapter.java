package com.bytecamp.herbit.ugcdemo.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bytecamp.herbit.ugcdemo.R;
import com.bytecamp.herbit.ugcdemo.data.model.CommentLikeCount;
import com.bytecamp.herbit.ugcdemo.data.model.CommentWithUser;
import com.bytecamp.herbit.ugcdemo.utils.TimeUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CommentsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<CommentWithUser> comments = new ArrayList<>();
    private long postAuthorId;
    private long currentUserId;
    private Set<Long> likedCommentIds = new HashSet<>(); // Store liked comment IDs
    private Map<Long, Integer> likeCounts = new HashMap<>(); // Store like counts
    private OnCommentActionListener listener;
    private static final int VIEW_COMMENT = 0;
    private static final int VIEW_FOOTER = 1;
    private boolean loading = false;
    private boolean hasMore = true;

    public interface OnCommentActionListener {
        void onReply(CommentWithUser comment);
        void onLike(CommentWithUser comment);
        void onDelete(CommentWithUser comment);
        void onUserClick(long userId);
    }
    
    public void setListener(OnCommentActionListener listener) {
        this.listener = listener;
    }

    public void setComments(List<CommentWithUser> comments) {
        this.comments = comments;
        notifyDataSetChanged();
    }
    
    public void setPostAuthorId(long postAuthorId) {
        this.postAuthorId = postAuthorId;
        notifyDataSetChanged();
    }
    
    public void setCurrentUserId(long currentUserId) {
        this.currentUserId = currentUserId;
        notifyDataSetChanged();
    }

    public void setLikedCommentIds(List<Long> ids) {
        this.likedCommentIds = new HashSet<>(ids);
        notifyDataSetChanged();
    }
    
    public void setLikeCounts(List<CommentLikeCount> counts) {
        this.likeCounts.clear();
        for (CommentLikeCount item : counts) {
            this.likeCounts.put(item.target_id, item.count);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_FOOTER) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_footer, parent, false);
            return new FooterViewHolder(v);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
            return new CommentViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_FOOTER) {
            FooterViewHolder fh = (FooterViewHolder) holder;
            if (loading) {
                fh.tvFooter.setText("正在加载...");
                fh.tvFooter.setVisibility(View.VISIBLE);
            } else if (!hasMore && comments.size() > 0) {
                fh.tvFooter.setText("没有更多了");
                fh.tvFooter.setVisibility(View.VISIBLE);
            } else {
                fh.tvFooter.setVisibility(View.GONE);
            }
            return;
        }
        CommentWithUser item = comments.get(position);
        CommentViewHolder holderC = (CommentViewHolder) holder;
        
        // Author
        String authorName = item.user != null ? item.user.username : "Unknown";
        holderC.tvAuthor.setText(authorName);
        
        // Click on author name to go to profile
        holderC.tvAuthor.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUserClick(item.comment.author_id);
            }
        });
        
        // Reply UI
        if (item.comment.reply_to_username != null) {
            holderC.tvContent.setText("回复 " + item.comment.reply_to_username + ": " + item.comment.content);
        } else {
            holderC.tvContent.setText(item.comment.content);
        }
        
        // Time
        holderC.tvTime.setText(TimeUtils.formatTime(item.comment.comment_time));
        
        // Indentation (UI nesting)
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holderC.llContainer.getLayoutParams();
        if (item.comment.parent_comment_id != null && item.comment.parent_comment_id != 0) {
            params.setMarginStart(dpToPx(32));
        } else {
            params.setMarginStart(0);
        }
        holderC.llContainer.setLayoutParams(params);

        // Badge
        if (item.comment.author_id == postAuthorId) {
            holderC.tvBadge.setVisibility(View.VISIBLE);
        } else {
            holderC.tvBadge.setVisibility(View.GONE);
        }
        
        // Like Status
        boolean isLiked = likedCommentIds.contains(item.comment.comment_id);
        holderC.ivLike.setImageResource(isLiked ? R.drawable.ic_like_on : R.drawable.ic_like_off);
        
        // Like Count
        int count = 0;
        if (likeCounts.containsKey(item.comment.comment_id)) {
            count = likeCounts.get(item.comment.comment_id);
        }
        holderC.tvLikeCount.setText(String.valueOf(count));
        
        // Listeners
        holderC.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onReply(item);
        });
        
        holderC.itemView.setOnLongClickListener(v -> {
            if (listener != null && item.comment.author_id == currentUserId) {
                listener.onDelete(item);
                return true;
            }
            return false;
        });
        
        View likeContainer = ((ViewGroup) holderC.ivLike.getParent());
        likeContainer.setOnClickListener(v -> {
            if (listener != null) listener.onLike(item);
        });
    }
    
    private int dpToPx(int dp) {
        return (int) (dp * android.content.res.Resources.getSystem().getDisplayMetrics().density);
    }

    @Override
    public int getItemCount() {
        boolean showFooter = loading || (!hasMore && comments.size() > 0);
        return comments.size() + (showFooter ? 1 : 0);
    }

    @Override
    public int getItemViewType(int position) {
        boolean showFooter = loading || (!hasMore && comments.size() > 0);
        if (showFooter && position == getItemCount() - 1) return VIEW_FOOTER;
        return VIEW_COMMENT;
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
        notifyItemChanged(getItemCount() - 1);
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
        notifyItemChanged(getItemCount() - 1);
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView tvAuthor, tvContent, tvBadge, tvLikeCount, tvTime;
        ImageView ivLike;
        LinearLayout llContainer;
        
        CommentViewHolder(View itemView) {
            super(itemView);
            tvAuthor = itemView.findViewById(R.id.tvCommentAuthor);
            tvContent = itemView.findViewById(R.id.tvCommentContent);
            tvBadge = itemView.findViewById(R.id.tvAuthorBadge);
            tvLikeCount = itemView.findViewById(R.id.tvCommentLikeCount);
            tvTime = itemView.findViewById(R.id.tvCommentTime);
            ivLike = itemView.findViewById(R.id.ivCommentLike);
            llContainer = itemView.findViewById(R.id.llCommentContainer);
        }
    }
    static class FooterViewHolder extends RecyclerView.ViewHolder {
        TextView tvFooter;
        FooterViewHolder(View itemView) {
            super(itemView);
            tvFooter = itemView.findViewById(R.id.tvFooter);
        }
    }
}