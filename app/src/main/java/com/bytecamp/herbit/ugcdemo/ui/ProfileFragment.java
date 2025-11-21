package com.bytecamp.herbit.ugcdemo.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import com.bumptech.glide.Glide;
import com.bytecamp.herbit.ugcdemo.AuthActivity;
import com.bytecamp.herbit.ugcdemo.R;
import com.bytecamp.herbit.ugcdemo.viewmodel.ProfileViewModel;

public class ProfileFragment extends Fragment {
    private ProfileViewModel profileViewModel;
    private ImageView ivAvatar;
    private TextView tvUsername;
    private RecyclerView recyclerView;
    private PostsAdapter adapter;
    private long userId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        
        // Setup Views
        ivAvatar = root.findViewById(R.id.ivProfileAvatar);
        tvUsername = root.findViewById(R.id.tvProfileName);
        recyclerView = root.findViewById(R.id.rvProfilePosts);
        ImageView ivSettings = root.findViewById(R.id.ivSettings);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        adapter = new PostsAdapter();
        recyclerView.setAdapter(adapter);

        // Get User ID
        SharedPreferences prefs = requireActivity().getSharedPreferences("ugc_prefs", Context.MODE_PRIVATE);
        userId = prefs.getLong("user_id", -1);

        // ViewModel
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        
        if (userId != -1) {
            profileViewModel.getUser(userId).observe(getViewLifecycleOwner(), user -> {
                if (user != null) {
                    tvUsername.setText(user.username);
                    if (user.avatar_path != null) {
                         Glide.with(this).load(user.avatar_path).circleCrop().into(ivAvatar);
                    }
                }
            });

            profileViewModel.getUserPosts(userId).observe(getViewLifecycleOwner(), posts -> {
                adapter.setPosts(posts);
            });
        }

        ivSettings.setOnClickListener(v -> showSettingsMenu(v));

        return root;
    }

    private void showSettingsMenu(View view) {
        PopupMenu popup = new PopupMenu(getContext(), view);
        popup.getMenu().add("退出登录");
        popup.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals("退出登录")) {
                logout();
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void logout() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("ugc_prefs", Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
        startActivity(new Intent(getActivity(), AuthActivity.class));
        requireActivity().finish();
    }
}