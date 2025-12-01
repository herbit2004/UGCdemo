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
import com.bumptech.glide.Glide;
import com.bytecamp.herbit.ugcdemo.data.model.CommentLikeCount;
import com.bytecamp.herbit.ugcdemo.data.model.CommentWithUser;
import com.bytecamp.herbit.ugcdemo.util.TimeUtils;
import android.text.method.LinkMovementMethod;
import com.bytecamp.herbit.ugcdemo.util.SpanUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CommentsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<CommentWithUser> comments = new ArrayList<>();
    private List<CommentWithUser> rootComments = new ArrayList<>();
    private Map<Long, List<CommentWithUser>> childrenByRoot = new HashMap<>();
    private Map<Long, CommentWithUser> byId = new HashMap<>();
    private long postAuthorId;
    private long currentUserId;
    private Set<Long> likedCommentIds = new HashSet<>(); // Store liked comment IDs
    private Map<Long, Integer> likeCounts = new HashMap<>(); // Store like counts
    private Map<String, String> mentionAvatarMap = new HashMap<>();
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
        this.comments = comments != null ? comments : new ArrayList<>();
        rebuildThreading();
        notifyDataSetChanged();
    }

    private void rebuildThreading() {
        rootComments.clear();
        childrenByRoot.clear();
        byId.clear();
        for (CommentWithUser c : comments) {
            byId.put(c.comment.comment_id, c);
        }
        for (CommentWithUser c : comments) {
            Long pid = c.comment.parent_comment_id;
            if (pid == null || pid == 0) {
                rootComments.add(c);
            } else {
                long rootId = resolveRootId(pid);
                childrenByRoot.computeIfAbsent(rootId, k -> new ArrayList<>()).add(c);
            }
        }
    }

    private long resolveRootId(Long startParentId) {
        Long current = startParentId;
        int guard = 0;
        while (current != null && current != 0 && guard < 20) {
            CommentWithUser parent = byId.get(current);
            if (parent == null) break;
            Long ppid = parent.comment.parent_comment_id;
            if (ppid == null || ppid == 0) {
                return parent.comment.comment_id;
            }
            current = ppid;
            guard++;
        }
        return startParentId;
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

    public void setMentionAvatarMap(Map<String, String> map) {
        this.mentionAvatarMap = map != null ? map : new HashMap<>();
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
        CommentWithUser item = rootComments.get(position);
        CommentViewHolder holderC = (CommentViewHolder) holder;
        
        // Author
        String authorName = item.user != null ? item.user.username : "Unknown";
        holderC.tvAuthor.setText(authorName);
        
        // Target Indicator removed
        holderC.tvAuthor.setCompoundDrawables(null, null, null, null);
        
        // Click on author name to go to profile
        holderC.tvAuthor.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUserClick(item.comment.author_id);
            }
        });
        
        if (item.comment.reply_to_username != null) {
            String text = "回复 " + item.comment.reply_to_username + ": " + item.comment.content;
            holderC.tvContent.setText(SpanUtils.getSpannableText(holder.itemView.getContext(), text));
        } else {
            holderC.tvContent.setText(SpanUtils.getSpannableText(holder.itemView.getContext(), item.comment.content));
        }
        holderC.tvContent.setMovementMethod(LinkMovementMethod.getInstance());

        holderC.ivMentionAvatarRootHeader.setVisibility(View.VISIBLE);
        String authorAvatar = (item.user != null) ? item.user.avatar_path : null;
        if (authorAvatar != null && !authorAvatar.isEmpty()) {
            Glide.with(holder.itemView.getContext()).load(authorAvatar).circleCrop().into(holderC.ivMentionAvatarRootHeader);
        } else {
            Glide.with(holder.itemView.getContext()).load(R.mipmap.ic_launcher).circleCrop().into(holderC.ivMentionAvatarRootHeader);
        }

        if (holderC.ivMentionAvatarRoot != null) holderC.ivMentionAvatarRoot.setVisibility(View.GONE);
        
        // Time
        holderC.tvTime.setText(TimeUtils.formatTime(item.comment.comment_time));
        
        // No indentation for root; replies shown inside card
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holderC.llContainer.getLayoutParams();
        params.setMarginStart(0);
        holderC.llContainer.setLayoutParams(params);

        // Badge
        if (item.comment.author_id == postAuthorId) {
            holderC.tvBadge.setVisibility(View.VISIBLE);
        } else {
            holderC.tvBadge.setVisibility(View.GONE);
        }
        
        // Like Status
        boolean isLiked = likedCommentIds.contains(item.comment.comment_id);
        holderC.ivLike.setImageResource(isLiked ? R.drawable.ic_like_filled : R.drawable.ic_like_outline);
        if (isLiked) {
            holderC.ivLike.setColorFilter(android.graphics.Color.RED);
            holderC.ivLike.setAlpha(1.0f);
        } else {
            holderC.ivLike.clearColorFilter();
            holderC.ivLike.setAlpha(1.0f);
        }
        
        // Like Count
        int count = 0;
        if (likeCounts.containsKey(item.comment.comment_id)) {
            count = likeCounts.get(item.comment.comment_id);
        }
        holderC.tvLikeCount.setText(String.valueOf(count));
        
        // Listeners on container to avoid child interception
        holderC.llContainer.setOnClickListener(v -> {
            if (listener != null) listener.onReply(item);
        });
        
        holderC.llContainer.setOnLongClickListener(v -> {
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
        holderC.ivLike.setOnClickListener(v -> {
            if (listener != null) listener.onLike(item);
        });

        LinearLayout replies = holderC.llReplies;
        replies.removeAllViews();
        List<CommentWithUser> children = childrenByRoot.get(item.comment.comment_id);
        if (children != null && !children.isEmpty()) {
            for (CommentWithUser child : children) {
                android.content.Context ctx = holder.itemView.getContext();
                View row = LayoutInflater.from(ctx).inflate(R.layout.item_comment_reply, replies, false);

                TextView tvReplyContent = row.findViewById(R.id.tvReplyContent);
                TextView tvReplyTime = row.findViewById(R.id.tvReplyTime);
                LinearLayout llReplyLike = row.findViewById(R.id.llReplyLike);
                TextView tvReplyLikeCount = row.findViewById(R.id.tvReplyLikeCount);
                ImageView ivReplyLike = row.findViewById(R.id.ivReplyLike);
                ImageView ivMentionAvatar = row.findViewById(R.id.ivMentionAvatar);

                String author = child.user != null ? child.user.username : "";
                String body;
                if (child.comment.reply_to_username != null) {
                    body = author + " 回复 " + child.comment.reply_to_username + ": " + child.comment.content;
                } else {
                    body = author + ": " + child.comment.content;
                }
                ivMentionAvatar.setVisibility(View.VISIBLE);
                String authorAvatarChild = (child.user != null) ? child.user.avatar_path : null;
                if (authorAvatarChild != null && !authorAvatarChild.isEmpty()) {
                    Glide.with(row.getContext()).load(authorAvatarChild).circleCrop().into(ivMentionAvatar);
                } else {
                    Glide.with(row.getContext()).load(R.mipmap.ic_launcher).circleCrop().into(ivMentionAvatar);
                }
                tvReplyContent.setText(SpanUtils.getSpannableText(ctx, body));
                tvReplyContent.setMovementMethod(LinkMovementMethod.getInstance());

                tvReplyTime.setText(TimeUtils.formatTime(child.comment.comment_time));

                int countChild = likeCounts.containsKey(child.comment.comment_id) ? likeCounts.get(child.comment.comment_id) : 0;
                tvReplyLikeCount.setText(String.valueOf(countChild));
                boolean isLikedChild = likedCommentIds.contains(child.comment.comment_id);
                ivReplyLike.setImageResource(isLikedChild ? R.drawable.ic_like_filled : R.drawable.ic_like_outline);
                if (isLikedChild) {
                    ivReplyLike.setColorFilter(android.graphics.Color.RED);
                    ivReplyLike.setAlpha(1.0f);
                } else {
                    ivReplyLike.clearColorFilter();
                    ivReplyLike.setAlpha(1.0f);
                }

                llReplyLike.setOnClickListener(v -> { if (listener != null) listener.onLike(child); });
                ivReplyLike.setOnClickListener(v -> { if (listener != null) listener.onLike(child); });

                row.setOnClickListener(v -> { if (listener != null) listener.onReply(child); });
                row.setOnLongClickListener(v -> {
                    if (listener != null && child.comment.author_id == currentUserId) { listener.onDelete(child); return true; }
                    return false;
                });

                replies.addView(row);
            }
        }
    }
    
    private int dpToPx(int dp) {
        return (int) (dp * android.content.res.Resources.getSystem().getDisplayMetrics().density);
    }

    @Override
    public int getItemCount() {
        boolean showFooter = loading || (!hasMore && rootComments.size() > 0);
        return rootComments.size() + (showFooter ? 1 : 0);
    }

    @Override
    public int getItemViewType(int position) {
        boolean showFooter = loading || (!hasMore && rootComments.size() > 0);
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
        ImageView ivLike, ivMentionAvatarRoot, ivMentionAvatarRootHeader;
        View llContainer;
        LinearLayout llReplies;
        
        CommentViewHolder(View itemView) {
            super(itemView);
            tvAuthor = itemView.findViewById(R.id.tvCommentAuthor);
            tvContent = itemView.findViewById(R.id.tvCommentContent);
            tvBadge = itemView.findViewById(R.id.tvAuthorBadge);
            tvLikeCount = itemView.findViewById(R.id.tvCommentLikeCount);
            tvTime = itemView.findViewById(R.id.tvCommentTime);
            ivLike = itemView.findViewById(R.id.ivCommentLike);
            ivMentionAvatarRoot = itemView.findViewById(R.id.ivMentionAvatarRoot);
            ivMentionAvatarRootHeader = itemView.findViewById(R.id.ivMentionAvatarRootHeader);
            llContainer = itemView.findViewById(R.id.llCommentContainer);
            llReplies = itemView.findViewById(R.id.llReplies);
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
