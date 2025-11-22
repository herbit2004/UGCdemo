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

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {
    private List<CommentWithUser> comments = new ArrayList<>();
    private long postAuthorId;
    private long currentUserId;
    private Set<Long> likedCommentIds = new HashSet<>(); // Store liked comment IDs
    private Map<Long, Integer> likeCounts = new HashMap<>(); // Store like counts
    private OnCommentActionListener listener;

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
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        CommentWithUser item = comments.get(position);
        
        // Author
        String authorName = item.user != null ? item.user.username : "Unknown";
        holder.tvAuthor.setText(authorName);
        
        // Click on author name to go to profile
        holder.tvAuthor.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUserClick(item.comment.author_id);
            }
        });
        
        // Reply UI
        if (item.comment.reply_to_username != null) {
            holder.tvContent.setText("回复 " + item.comment.reply_to_username + ": " + item.comment.content);
        } else {
            holder.tvContent.setText(item.comment.content);
        }
        
        // Time
        holder.tvTime.setText(TimeUtils.formatTime(item.comment.comment_time));
        
        // Indentation (UI nesting)
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.llContainer.getLayoutParams();
        if (item.comment.parent_comment_id != null && item.comment.parent_comment_id != 0) {
            params.setMarginStart(dpToPx(32)); // Indent
        } else {
            params.setMarginStart(0);
        }
        holder.llContainer.setLayoutParams(params);

        // Badge
        if (item.comment.author_id == postAuthorId) {
            holder.tvBadge.setVisibility(View.VISIBLE);
        } else {
            holder.tvBadge.setVisibility(View.GONE);
        }
        
        // Like Status
        boolean isLiked = likedCommentIds.contains(item.comment.comment_id);
        holder.ivLike.setImageResource(isLiked ? R.drawable.ic_like_on : R.drawable.ic_like_off);
        
        // Like Count
        int count = 0;
        if (likeCounts.containsKey(item.comment.comment_id)) {
            count = likeCounts.get(item.comment.comment_id);
        }
        holder.tvLikeCount.setText(String.valueOf(count));
        
        // Listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onReply(item);
        });
        
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null && item.comment.author_id == currentUserId) {
                listener.onDelete(item);
                return true;
            }
            return false;
        });
        
        View likeContainer = ((ViewGroup) holder.ivLike.getParent());
        likeContainer.setOnClickListener(v -> {
            if (listener != null) listener.onLike(item);
        });
    }
    
    private int dpToPx(int dp) {
        return (int) (dp * android.content.res.Resources.getSystem().getDisplayMetrics().density);
    }

    @Override
    public int getItemCount() {
        return comments.size();
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
}