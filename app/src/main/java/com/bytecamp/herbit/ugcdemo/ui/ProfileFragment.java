package com.bytecamp.herbit.ugcdemo.ui;

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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bytecamp.herbit.ugcdemo.AuthActivity;
import com.bytecamp.herbit.ugcdemo.R;
import com.bytecamp.herbit.ugcdemo.viewmodel.ProfileViewModel;

public class ProfileFragment extends Fragment {
    private static final int PICK_AVATAR_REQUEST = 2;
    
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
                    } else {
                        ivAvatar.setImageResource(R.mipmap.ic_launcher_round);
                    }
                }
            });

            profileViewModel.getUserPosts(userId).observe(getViewLifecycleOwner(), posts -> {
                adapter.setPosts(posts);
            });
        }

        ivSettings.setOnClickListener(v -> showSettingsMenu(v));
        
        // Click avatar to change
        ivAvatar.setOnClickListener(v -> pickAvatar());
        
        // Click name to change
        tvUsername.setOnClickListener(v -> showChangeNameDialog());

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
        startActivityForResult(intent, PICK_AVATAR_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_AVATAR_REQUEST && resultCode == android.app.Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            final int takeFlags = data.getFlags()
                    & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            try {
                requireContext().getContentResolver().takePersistableUriPermission(uri, takeFlags);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
            profileViewModel.updateAvatar(userId, uri.toString());
        }
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