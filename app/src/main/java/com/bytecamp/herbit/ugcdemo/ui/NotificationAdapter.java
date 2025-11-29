package com.bytecamp.herbit.ugcdemo.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import com.bytecamp.herbit.ugcdemo.ui.widget.FollowButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bytecamp.herbit.ugcdemo.R;
import com.bytecamp.herbit.ugcdemo.data.entity.Notification;
import com.bytecamp.herbit.ugcdemo.data.model.NotificationWithUser;
import com.bytecamp.herbit.ugcdemo.util.TimeUtils;
import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<NotificationWithUser> items = new ArrayList<>();
    private OnItemClickListener listener;
    private OnFollowClickListener followListener;
    private java.util.Set<Long> followingIds = new java.util.HashSet<>();
    private java.util.Set<Long> followerIds = new java.util.HashSet<>();

    public interface OnItemClickListener {
        void onItemClick(NotificationWithUser item);
    }
    
    public interface OnFollowClickListener {
        void onFollowClick(NotificationWithUser item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    
    public void setOnFollowClickListener(OnFollowClickListener listener) {
        this.followListener = listener;
    }

    public void setItems(List<NotificationWithUser> items) {
        this.items = items;
        notifyDataSetChanged();
    }
    
    public void setFollowingIds(java.util.Set<Long> ids) {
        this.followingIds = ids != null ? ids : new java.util.HashSet<>();
        notifyDataSetChanged();
    }

    public void setFollowerIds(java.util.Set<Long> ids) {
        this.followerIds = ids != null ? ids : new java.util.HashSet<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class NotificationViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvName, tvAction, tvContent, tvTime;
        FollowButton btnFollow;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvAction = itemView.findViewById(R.id.tvAction);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvTime = itemView.findViewById(R.id.tvTime);
            btnFollow = itemView.findViewById(R.id.btnFollow);
            
            itemView.setOnClickListener(v -> {
                if (listener != null && getBindingAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onItemClick(items.get(getBindingAdapterPosition()));
                }
            });
            
            btnFollow.setOnClickListener(v -> {
                if (followListener != null && getBindingAdapterPosition() != RecyclerView.NO_POSITION) {
                    followListener.onFollowClick(items.get(getBindingAdapterPosition()));
                }
            });
        }

        void bind(NotificationWithUser item) {
            if (item.sourceUser != null) {
                tvName.setText(item.sourceUser.username);
                if (item.sourceUser.avatar_path != null) {
                    Glide.with(itemView.getContext()).load(item.sourceUser.avatar_path).circleCrop().into(ivAvatar);
                } else {
                    ivAvatar.setImageResource(R.mipmap.ic_launcher_round);
                }
            } else {
                tvName.setText("Unknown");
                ivAvatar.setImageResource(R.mipmap.ic_launcher_round);
            }

            switch (item.notification.type) {
                case Notification.TYPE_MENTION:
                    tvName.setText(item.sourceUser != null ? item.sourceUser.username : "Unknown");
                    if (item.notification.extra_id > 0) {
                        tvAction.setText("在评论中at了我");
                    } else {
                        tvAction.setText("在帖子中at了我");
                    }
                    tvContent.setVisibility(View.VISIBLE);
                    btnFollow.setVisibility(View.GONE);
                    break;
                case Notification.TYPE_REPLY:
                    tvName.setText(item.sourceUser != null ? item.sourceUser.username : "Unknown");
                    // Check extra_id to see if it is reply to comment (extra_id > 0) or post (extra_id == 0)
                    // In DetailViewModel we set extra_id = comment_id for comment reply, and 0 for post reply
                    if (item.notification.extra_id > 0) {
                        tvAction.setText("回复了我的评论");
                    } else {
                        tvAction.setText("回复了我的帖子");
                    }
                    tvContent.setVisibility(View.VISIBLE);
                    btnFollow.setVisibility(View.GONE);
                    break;
                case Notification.TYPE_LIKE:
                    tvName.setText(item.sourceUser != null ? item.sourceUser.username : "Unknown");
                    // Check extra_id for Like as well (extra_id = targetId if comment, 0 if post)
                    if (item.notification.extra_id > 0) {
                        tvAction.setText("点赞了我的评论");
                    } else {
                        tvAction.setText("点赞了我的帖子");
                    }
                    tvContent.setVisibility(View.VISIBLE);
                    btnFollow.setVisibility(View.GONE);
                    break;
                case Notification.TYPE_FOLLOW:
                    tvName.setText(item.sourceUser != null ? item.sourceUser.username : "Unknown");
                    tvAction.setText("关注了我");
                    tvContent.setVisibility(View.GONE);
                    btnFollow.setVisibility(View.VISIBLE);
                    
                    boolean isFollowing = item.sourceUser != null && followingIds.contains(item.sourceUser.user_id);
                    boolean isFollower = item.sourceUser != null && followerIds.contains(item.sourceUser.user_id);
                    
                    btnFollow.setState(isFollowing, isFollower);
                    break;
            }
            
            if (item.notification.content_preview != null) {
                tvContent.setText(item.notification.content_preview);
            } else {
                tvContent.setText("");
            }
            
            // tvTime.setText(TimeUtils.getRelativeTime(item.notification.created_at)); 
            // Assuming TimeUtils exists
            tvTime.setText(TimeUtils.formatTime(item.notification.created_at));
            
            // Show red dot if unread
            // But we need a view for red dot. Let's assume we added it to item_notification.xml or we can reuse tvName's drawable?
            // The requirement says "在通知列表的这条通知标题旁显示红点".
            // We can add a small dot view or drawable to tvName.
            // Let's use compound drawable on tvName or tvAction.
            if (!item.notification.is_read) {
                android.graphics.drawable.Drawable dot = itemView.getContext().getResources().getDrawable(R.drawable.bg_red_dot);
                dot.setBounds(0, 0, 20, 20); // Size
                tvAction.setCompoundDrawables(null, null, dot, null);
                tvAction.setCompoundDrawablePadding(10);
            } else {
                tvAction.setCompoundDrawables(null, null, null, null);
            }
        }
    }
}
