package com.bytecamp.herbit.ugcdemo.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bytecamp.herbit.ugcdemo.R;
import com.bytecamp.herbit.ugcdemo.data.entity.User;
import java.util.ArrayList;
import java.util.List;
import com.bytecamp.herbit.ugcdemo.ui.widget.FollowButton;

public class UserListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<User> users = new ArrayList<>();
    private OnActionClickListener actionListener;
    private OnItemClickListener itemClickListener;
    private java.util.Set<Long> followingIds = new java.util.HashSet<>();
    private java.util.Set<Long> followerIds = new java.util.HashSet<>();
    private long currentUserId = -1L;
    private boolean loading = false;
    private boolean hasMore = false; // Default to false since we load all at once usually, or true if paginated

    private static final int VIEW_USER = 0;
    private static final int VIEW_FOOTER = 1;

    public interface OnActionClickListener {
        void onAction(User user, boolean isFollowing);
    }
    
    public interface OnItemClickListener {
        void onItemClick(User user);
    }

    public UserListAdapter(long currentUserId, OnActionClickListener actionListener) {
        this.currentUserId = currentUserId;
        this.actionListener = actionListener;
    }
    
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public void setUsers(List<User> users) {
        this.users = users;
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

    public void setLoading(boolean loading) {
        this.loading = loading;
        notifyDataSetChanged(); // Refresh to show/hide footer
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
        // If hasMore is true, we hide footer until we load more? 
        // Or we show footer as "Loading" only when loading is true?
        // PostsAdapter logic: boolean showFooter = loading || (!hasMore && posts.size() > 0) || posts.size() == 0;
        // So if hasMore is true and not loading, footer is HIDDEN.
        notifyDataSetChanged(); 
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_FOOTER) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_footer, parent, false);
            return new FooterViewHolder(v);
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_FOOTER) {
            FooterViewHolder fh = (FooterViewHolder) holder;
            if (loading) {
                fh.tvFooter.setText("正在加载...");
                fh.tvFooter.setVisibility(View.VISIBLE);
            } else if (users.isEmpty()) {
                 fh.tvFooter.setVisibility(View.GONE); // Don't show footer if empty (handled by empty state view usually)
                 // Or show "No results"
                 fh.tvFooter.setText("未找到相关用户");
                 fh.tvFooter.setVisibility(View.VISIBLE);
            } else if (!hasMore) {
                // Only show "No more" if list is not empty and we really want to show it.
                // User said "Other pages don't have this", referring to "Loading" maybe?
                // Or maybe they don't want "No more" either if it's short?
                // But PostsAdapter shows "没有更多了".
                // The user complaint "为啥还显示正在加载" means loading state is stuck or default is wrong.
                // We fixed getItemCount/getItemViewType logic above.
                // Here we just set text.
                fh.tvFooter.setText("没有更多了");
                fh.tvFooter.setVisibility(View.VISIBLE);
            } else {
                fh.tvFooter.setVisibility(View.GONE);
            }
            return;
        }
        
        UserViewHolder uh = (UserViewHolder) holder;
        User user = users.get(position);
        uh.bind(user);
    }

    @Override
    public int getItemCount() {
        // Only show footer if necessary: loading, or if we want to show "no more" at end of a non-empty list
        boolean showFooter = loading || (!users.isEmpty() && !hasMore);
        // If users is empty and not loading, we might want to show "no results" or nothing.
        // If we want to show "No results", we keep footer.
        // Let's match PostsAdapter logic roughly.
        if (users.isEmpty() && !loading) showFooter = true; // Show "No results"
        
        return users.size() + (showFooter ? 1 : 0);
    }

    @Override
    public int getItemViewType(int position) {
        boolean showFooter = loading || (!users.isEmpty() && !hasMore) || (users.isEmpty() && !loading);
        if (showFooter && position == getItemCount() - 1) return VIEW_FOOTER;
        return VIEW_USER;
    }

    class FooterViewHolder extends RecyclerView.ViewHolder {
        TextView tvFooter;
        FooterViewHolder(View itemView) {
            super(itemView);
            tvFooter = itemView.findViewById(R.id.tvFooter);
        }
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvName;
        FollowButton btnAction;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            btnAction = itemView.findViewById(R.id.btnAction);
            
            itemView.setOnClickListener(v -> {
                if (itemClickListener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    itemClickListener.onItemClick(users.get(getAdapterPosition()));
                }
            });
        }

        void bind(User user) {
            tvName.setText(user.username);
            if (user.avatar_path != null) {
                Glide.with(itemView.getContext()).load(user.avatar_path).circleCrop().into(ivAvatar);
            } else {
                ivAvatar.setImageResource(R.mipmap.ic_launcher_round);
            }

            boolean isFollowing = followingIds.contains(user.user_id);
            boolean isFollower = followerIds.contains(user.user_id);
            
            if (user.user_id == currentUserId) {
                btnAction.setVisibility(View.GONE);
                btnAction.setOnClickListener(null);
                return;
            } else {
                btnAction.setVisibility(View.VISIBLE);
            }
            
            btnAction.setState(isFollowing, isFollower);
            
            btnAction.setOnClickListener(v -> {
                if (actionListener != null) {
                    actionListener.onAction(user, isFollowing);
                }
                // Optimistic update
                if (isFollowing) {
                    followingIds.remove(user.user_id);
                } else {
                    followingIds.add(user.user_id);
                }
                notifyItemChanged(getBindingAdapterPosition());
            });
        }
    }
}
