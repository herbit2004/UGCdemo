package com.bytecamp.herbit.ugcdemo.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
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
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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

    // Use Activity Result API instead of deprecated startActivityForResult
    private final ActivityResultLauncher<Intent> pickAvatarLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    if (data.getData() != null) {
                        Uri uri = data.getData();
                        final int takeFlags = data.getFlags()
                                & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        try {
                            requireContext().getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } catch (SecurityException e) {
                            e.printStackTrace();
                        }
                        if (profileViewModel != null) {
                            profileViewModel.updateAvatar(userId, uri.toString());
                        }
                    }
                }
            }
    );

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        
        // Setup Views
        ivAvatar = root.findViewById(R.id.ivProfileAvatar);
        tvUsername = root.findViewById(R.id.tvProfileName);
        recyclerView = root.findViewById(R.id.rvProfilePosts);
        ImageView ivSettings = root.findViewById(R.id.ivSettings);
        
        tvPostCount = root.findViewById(R.id.tvPostCount);
        tvFollowCount = root.findViewById(R.id.tvFollowCount);
        tvFanCount = root.findViewById(R.id.tvFanCount);
        tabLayout = root.findViewById(R.id.tabLayout);

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
                    } else {
                        ivAvatar.setImageResource(R.mipmap.ic_launcher_round);
                    }
                }
            });

            profileViewModel.getUserPosts(userId).observe(getViewLifecycleOwner(), posts -> {
                adapter.setPosts(posts);
                if (profileViewModel.getUserPosts(userId) != null && tabLayout.getSelectedTabPosition() == 0) {
                    // A bit tricky to update count only for "My Posts" tab from here without more logic,
                    // but we can just update if current tab is 0 or rely on list size
                    if (tabLayout.getSelectedTabPosition() == 0) {
                        tvPostCount.setText(String.valueOf(posts.size()));
                    }
                }
            });
            
            profileViewModel.getFollowingCount(userId).observe(getViewLifecycleOwner(), count -> tvFollowCount.setText(String.valueOf(count)));
            profileViewModel.getFollowerCount(userId).observe(getViewLifecycleOwner(), count -> tvFanCount.setText(String.valueOf(count)));
        }

        ivSettings.setOnClickListener(v -> showSettingsMenu(v));
        
        // Click avatar to change
        ivAvatar.setOnClickListener(v -> pickAvatar());
        
        // Click name to change
        tvUsername.setOnClickListener(v -> showChangeNameDialog());
        
        // Tab Layout
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                profileViewModel.setTab(tab.getPosition());
                if (tab.getPosition() == 0) {
                    // Re-fetch or observe already does it? 
                    // The switchMap in VM will handle data source change
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

        return root;
    }

    private void showSettingsMenu(View view) {
        PopupMenu popup = new PopupMenu(getContext(), view);
        popup.getMenu().add("修改密码");
        popup.getMenu().add("注销账号"); // Delete account
        popup.getMenu().add("退出登录"); // Logout
        
        popup.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            if (title.equals("修改密码")) {
                showChangePasswordDialog();
                return true;
            } else if (title.equals("注销账号")) {
                showDeleteAccountDialog();
                return true;
            } else if (title.equals("退出登录")) {
                logout();
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void pickAvatar() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        pickAvatarLauncher.launch(intent);
    }

    private void showChangeNameDialog() {
        final EditText input = new EditText(getContext());
        input.setHint("输入新昵称");
        new AlertDialog.Builder(requireContext())
            .setTitle("修改昵称")
            .setView(input)
            .setPositiveButton("确定", (dialog, which) -> {
                String newName = input.getText().toString().trim();
                if (!newName.isEmpty()) {
                    profileViewModel.updateUsername(userId, newName);
                }
            })
            .setNegativeButton("取消", null)
            .show();
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
