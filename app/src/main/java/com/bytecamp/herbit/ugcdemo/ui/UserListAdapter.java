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
import android.util.TypedValue;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserViewHolder> {

    private List<User> users = new ArrayList<>();
    private OnActionClickListener actionListener;
    private OnItemClickListener itemClickListener;
    private java.util.Set<Long> followingIds = new java.util.HashSet<>();

    public interface OnActionClickListener {
        void onAction(User user, boolean isFollowing);
    }
    
    public interface OnItemClickListener {
        void onItemClick(User user);
    }

    public UserListAdapter(OnActionClickListener actionListener) {
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

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvName;
        Button btnAction;

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
            btnAction.setVisibility(View.VISIBLE);
            btnAction.setBackgroundResource(R.drawable.bg_follow_btn);
            int colorOnSecondary = getAttrColor(itemView.getContext(), com.google.android.material.R.attr.colorOnSecondary);
            btnAction.setTextColor(colorOnSecondary);
            btnAction.setText(isFollowing ? "已关注" : "关注");
            btnAction.setOnClickListener(v -> {
                if (actionListener != null) {
                    actionListener.onAction(user, isFollowing);
                }
                if (isFollowing) {
                    followingIds.remove(user.user_id);
                } else {
                    followingIds.add(user.user_id);
                }
                notifyItemChanged(getBindingAdapterPosition());
            });
        }

        private int getAttrColor(android.content.Context context, int attr) {
            TypedValue typedValue = new TypedValue();
            context.getTheme().resolveAttribute(attr, typedValue, true);
            return typedValue.data;
        }
    }
}
