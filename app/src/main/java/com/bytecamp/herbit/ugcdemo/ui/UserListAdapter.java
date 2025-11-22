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

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserViewHolder> {

    private List<User> users = new ArrayList<>();
    private boolean showUnfollowButton;
    private OnActionClickListener actionListener;
    private OnItemClickListener itemClickListener;

    public interface OnActionClickListener {
        void onAction(User user);
    }
    
    public interface OnItemClickListener {
        void onItemClick(User user);
    }

    public UserListAdapter(boolean showUnfollowButton, OnActionClickListener actionListener) {
        this.showUnfollowButton = showUnfollowButton;
        this.actionListener = actionListener;
    }
    
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public void setUsers(List<User> users) {
        this.users = users;
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

            if (showUnfollowButton) {
                btnAction.setVisibility(View.VISIBLE);
                btnAction.setText("取关");
                btnAction.setOnClickListener(v -> {
                    if (actionListener != null) {
                        actionListener.onAction(user);
                    }
                });
            } else {
                btnAction.setVisibility(View.GONE);
            }
        }
    }
}