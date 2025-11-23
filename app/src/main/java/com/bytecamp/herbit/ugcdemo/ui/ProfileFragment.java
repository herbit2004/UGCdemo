package com.bytecamp.herbit.ugcdemo.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bytecamp.herbit.ugcdemo.AuthActivity;
import com.bytecamp.herbit.ugcdemo.FollowListActivity;
import com.bytecamp.herbit.ugcdemo.R;
import com.bytecamp.herbit.ugcdemo.PublishActivity;
import com.bytecamp.herbit.ugcdemo.viewmodel.ProfileViewModel;
import com.google.android.material.tabs.TabLayout;

public class ProfileFragment extends Fragment {
    
    private ProfileViewModel profileViewModel;
    private ImageView ivAvatar;
    private TextView tvUsername;
    private RecyclerView recyclerView;
    private PostsAdapter adapter;
    private long userId;
    
    private TextView tvPostCount, tvFollowCount, tvFanCount;
    private TabLayout tabLayout;
    private androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeRefresh;

    

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        
        // Setup Views
        ivAvatar = root.findViewById(R.id.ivProfileAvatar);
        tvUsername = root.findViewById(R.id.tvProfileName);
        recyclerView = root.findViewById(R.id.rvProfilePosts);
        swipeRefresh = root.findViewById(R.id.swipeRefresh);
        ImageView ivSettings = root.findViewById(R.id.ivSettings);
        
        tvPostCount = root.findViewById(R.id.tvPostCount);
        tvFollowCount = root.findViewById(R.id.tvFollowCount);
        tvFanCount = root.findViewById(R.id.tvFanCount);
        tabLayout = root.findViewById(R.id.tabLayout);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        adapter = new PostsAdapter();
        adapter.setEmptyStateText("你还没有发布内容，去发布试试");
        adapter.setOnEmptyActionListener(() -> {
            startActivity(new Intent(getActivity(), PublishActivity.class));
        });
        recyclerView.setAdapter(adapter);

        // Get User ID
        SharedPreferences prefs = requireActivity().getSharedPreferences("ugc_prefs", Context.MODE_PRIVATE);
        userId = prefs.getLong("user_id", -1);

        // ViewModel
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        profileViewModel.setUserId(userId);
        
        if (userId != -1) {
            profileViewModel.getUser(userId).observe(getViewLifecycleOwner(), user -> {
                if (user != null) {
                    tvUsername.setText(user.username);
                    if (user.avatar_path != null) {
                         Glide.with(this).load(user.avatar_path).circleCrop().into(ivAvatar);
                    } else {
                        ivAvatar.setImageResource(R.mipmap.ic_launcher_round);
                    }
                }
            });

            profileViewModel.getPosts().observe(getViewLifecycleOwner(), posts -> {
                adapter.setPosts(posts);
                if (tabLayout.getSelectedTabPosition() == 0) {
                    tvPostCount.setText(String.valueOf(posts != null ? posts.size() : 0));
                }
                adapter.setLoading(false);
                adapter.setHasMore(profileViewModel.hasMore());
            });
            
            profileViewModel.getFollowingCount(userId).observe(getViewLifecycleOwner(), count -> tvFollowCount.setText(String.valueOf(count)));
            profileViewModel.getFollowerCount(userId).observe(getViewLifecycleOwner(), count -> tvFanCount.setText(String.valueOf(count)));
        }

        ivSettings.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), com.bytecamp.herbit.ugcdemo.SettingsActivity.class);
            startActivity(intent);
        });
        
        
        
        // Tab Layout
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                profileViewModel.setTab(tab.getPosition());
                profileViewModel.refresh();
                if (tab.getPosition() == 0) {
                    adapter.setEmptyStateText("你还没有发布内容，去发布试试");
                    adapter.setOnEmptyActionListener(() -> startActivity(new Intent(getActivity(), PublishActivity.class)));
                } else {
                    adapter.setEmptyStateText("你还没有点赞任何内容");
                    adapter.setOnEmptyActionListener(null);
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Click listeners for lists
        View.OnClickListener followListener = v -> {
            Intent intent = new Intent(getActivity(), FollowListActivity.class);
            intent.putExtra(FollowListActivity.EXTRA_USER_ID, userId);
            intent.putExtra(FollowListActivity.EXTRA_TYPE, 0);
            intent.putExtra(FollowListActivity.EXTRA_TITLE, "我的关注");
            startActivity(intent);
        };
        
        // Bind listeners to container
        root.findViewById(R.id.llFollowContainer).setOnClickListener(followListener);
        
        View.OnClickListener fanListener = v -> {
            Intent intent = new Intent(getActivity(), FollowListActivity.class);
            intent.putExtra(FollowListActivity.EXTRA_USER_ID, userId);
            intent.putExtra(FollowListActivity.EXTRA_TYPE, 1);
            intent.putExtra(FollowListActivity.EXTRA_TITLE, "我的粉丝");
            startActivity(intent);
        };
        
        // Bind listeners to container
        root.findViewById(R.id.llFanContainer).setOnClickListener(fanListener);
        
        swipeRefresh.setOnRefreshListener(() -> {
            swipeRefresh.setRefreshing(false);
            profileViewModel.refresh();
        });
        
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener(){
            private boolean loadingMore = false;
            private long lastLoadTime = 0L;
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                if (dy > 0) {
                    StaggeredGridLayoutManager lm = (StaggeredGridLayoutManager) rv.getLayoutManager();
                    if (lm == null) return;
                    int[] last = new int[2];
                    lm.findLastVisibleItemPositions(last);
                    int lastPos = Math.max(last[0], last[1]);
                    RecyclerView.Adapter a = rv.getAdapter();
                    if (a != null && lastPos >= Math.max(0, a.getItemCount() - 3)) {
                        long now = System.currentTimeMillis();
                        if (profileViewModel.hasMore() && now - lastLoadTime > 700) {
                            loadingMore = true;
                            if (a instanceof PostsAdapter) {
                                ((PostsAdapter) a).setLoading(true);
                            }
                            profileViewModel.loadMore();
                            lastLoadTime = now;
                        }
                    }
                }
            }
        });

        return root;
    }

    

    

    private void showChangePasswordDialog() {
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText oldPass = new EditText(getContext());
        oldPass.setHint("旧密码");
        oldPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(oldPass);

        final EditText newPass = new EditText(getContext());
        newPass.setHint("新密码");
        newPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(newPass);

        new AlertDialog.Builder(requireContext())
            .setTitle("修改密码")
            .setView(layout)
            .setPositiveButton("确定", (dialog, which) -> {
                String oldP = oldPass.getText().toString().trim();
                String newP = newPass.getText().toString().trim();
                if (!oldP.isEmpty() && !newP.isEmpty()) {
                    profileViewModel.verifyAndUpdatePassword(userId, oldP, newP, 
                        () -> requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "密码修改成功", Toast.LENGTH_SHORT).show()),
                        () -> requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "旧密码错误", Toast.LENGTH_SHORT).show())
                    );
                }
            })
            .setNegativeButton("取消", null)
            .show();
    }

    private void showDeleteAccountDialog() {
        new AlertDialog.Builder(requireContext())
            .setTitle("注销账号")
            .setMessage("确定要注销当前账号吗？所有数据将被永久删除！")
            .setPositiveButton("注销", (dialog, which) -> {
                profileViewModel.deleteUser(userId, () -> {
                    requireActivity().runOnUiThread(() -> logout());
                });
            })
            .setNegativeButton("取消", null)
            .show();
    }

    private void logout() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("ugc_prefs", Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
        startActivity(new Intent(getActivity(), AuthActivity.class));
        requireActivity().finish();
    }
}
