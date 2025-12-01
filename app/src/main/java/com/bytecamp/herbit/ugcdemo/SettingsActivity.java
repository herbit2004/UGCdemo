package com.bytecamp.herbit.ugcdemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.bytecamp.herbit.ugcdemo.viewmodel.ProfileViewModel;
import com.bytecamp.herbit.ugcdemo.util.ThemeUtils;
import com.bytecamp.herbit.ugcdemo.ui.widget.UnifiedDialog;

public class SettingsActivity extends AppCompatActivity {

    private long userId;
    private ProfileViewModel profileViewModel;

    private ImageView ivAvatar;
    private TextView tvUsername;
    private EditText etNickname;

    private final ActivityResultLauncher<Intent> pickAvatarLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    if (data.getData() != null) {
                        Uri uri = data.getData();
                        try {
                            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } catch (SecurityException e) {
                            e.printStackTrace();
                        }
                        profileViewModel.updateAvatar(userId, uri.toString());
                        Glide.with(this).load(uri).circleCrop().into(ivAvatar);
                    }
                }
            }
    );

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ThemeUtils.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        SharedPreferences prefs = getSharedPreferences("ugc_prefs", Context.MODE_PRIVATE);
        userId = prefs.getLong("user_id", -1);

        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        ivAvatar = findViewById(R.id.ivSettingsAvatar);
        tvUsername = findViewById(R.id.tvSettingsUsername);
        etNickname = findViewById(R.id.etSettingsNickname);

        Button btnSaveNickname = findViewById(R.id.btnSaveNickname);
        Button btnChangePassword = findViewById(R.id.btnChangePassword);
        Button btnDeleteAccount = findViewById(R.id.btnDeleteAccount);
        Button btnLogout = findViewById(R.id.btnLogout);

        View swatchRed = findViewById(R.id.swatchRed);
        View swatchBlue = findViewById(R.id.swatchBlue);
        View swatchGreen = findViewById(R.id.swatchGreen);
        View swatchOrange = findViewById(R.id.swatchOrange);
        View swatchPink = findViewById(R.id.swatchPink);
        View swatchAmber = findViewById(R.id.swatchAmber);
        View swatchIndigo = findViewById(R.id.swatchIndigo);
        View swatchCyan = findViewById(R.id.swatchCyan);
        View swatchLime = findViewById(R.id.swatchLime);
        View swatchTeal = findViewById(R.id.swatchTeal);
        View indicatorRed = findViewById(R.id.indicatorRed);
        View indicatorBlue = findViewById(R.id.indicatorBlue);
        View indicatorGreen = findViewById(R.id.indicatorGreen);
        View indicatorOrange = findViewById(R.id.indicatorOrange);
        View indicatorPink = findViewById(R.id.indicatorPink);
        View indicatorAmber = findViewById(R.id.indicatorAmber);
        View indicatorIndigo = findViewById(R.id.indicatorIndigo);
        View indicatorCyan = findViewById(R.id.indicatorCyan);
        View indicatorLime = findViewById(R.id.indicatorLime);
        View indicatorTeal = findViewById(R.id.indicatorTeal);

        if (userId != -1) {
            profileViewModel.getUser(userId).observe(this, user -> {
                if (user != null) {
                    tvUsername.setText(user.username);
                    etNickname.setText(user.username);
                    if (user.avatar_path != null) {
                        Glide.with(this).load(user.avatar_path).circleCrop().into(ivAvatar);
                    } else {
                        ivAvatar.setImageResource(R.mipmap.ic_launcher);
                    }
                }
            });
        }

        ivAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            pickAvatarLauncher.launch(intent);
        });

        btnSaveNickname.setOnClickListener(v -> {
            String newName = etNickname.getText().toString().trim();
            if (!TextUtils.isEmpty(newName)) {
                profileViewModel.updateUsername(userId, newName);
                Toast.makeText(this, "昵称已更新", Toast.LENGTH_SHORT).show();
            }
        });

        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        btnDeleteAccount.setOnClickListener(v -> showDeleteAccountDialog());
        btnLogout.setOnClickListener(v -> logout());

        View.OnClickListener themeClick = v -> {
            int id = v.getId();
            int themeRes = ThemeUtils.mapSwatchIdToTheme(id);
            ThemeUtils.setSelectedThemeRes(this, themeRes);
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("restart_theme", true);
            startActivity(intent);
        };

        swatchRed.setOnClickListener(themeClick);
        swatchBlue.setOnClickListener(themeClick);
        swatchGreen.setOnClickListener(themeClick);
        swatchOrange.setOnClickListener(themeClick);
        swatchPink.setOnClickListener(themeClick);
        swatchAmber.setOnClickListener(themeClick);
        swatchIndigo.setOnClickListener(themeClick);
        swatchCyan.setOnClickListener(themeClick);
        swatchLime.setOnClickListener(themeClick);
        swatchTeal.setOnClickListener(themeClick);
        highlightSelectedIndicator(ThemeUtils.getSelectedThemeRes(this), indicatorRed, indicatorBlue, indicatorGreen, indicatorOrange, indicatorPink, indicatorAmber, indicatorIndigo, indicatorCyan, indicatorLime, indicatorTeal);
    }

    private void highlightSelectedIndicator(int themeRes,
            View indicatorRed, View indicatorBlue, View indicatorGreen, View indicatorOrange, View indicatorPink,
            View indicatorAmber, View indicatorIndigo, View indicatorCyan, View indicatorLime, View indicatorTeal) {
        View[] arr = new View[]{indicatorRed, indicatorBlue, indicatorGreen, indicatorOrange, indicatorPink, indicatorAmber, indicatorIndigo, indicatorCyan, indicatorLime, indicatorTeal};
        for (View v : arr) { if (v != null) v.setVisibility(View.GONE); }
        if (themeRes == R.style.Theme_UGCdemo_AccentRed) { if (indicatorRed != null) indicatorRed.setVisibility(View.VISIBLE); return; }
        if (themeRes == R.style.Theme_UGCdemo_AccentBlue) { if (indicatorBlue != null) indicatorBlue.setVisibility(View.VISIBLE); return; }
        if (themeRes == R.style.Theme_UGCdemo_AccentGreen) { if (indicatorGreen != null) indicatorGreen.setVisibility(View.VISIBLE); return; }
        if (themeRes == R.style.Theme_UGCdemo_AccentOrange) { if (indicatorOrange != null) indicatorOrange.setVisibility(View.VISIBLE); return; }
        if (themeRes == R.style.Theme_UGCdemo_AccentPink) { if (indicatorPink != null) indicatorPink.setVisibility(View.VISIBLE); return; }
        if (themeRes == R.style.Theme_UGCdemo_AccentAmber) { if (indicatorAmber != null) indicatorAmber.setVisibility(View.VISIBLE); return; }
        if (themeRes == R.style.Theme_UGCdemo_AccentIndigo) { if (indicatorIndigo != null) indicatorIndigo.setVisibility(View.VISIBLE); return; }
        if (themeRes == R.style.Theme_UGCdemo_AccentCyan) { if (indicatorCyan != null) indicatorCyan.setVisibility(View.VISIBLE); return; }
        if (themeRes == R.style.Theme_UGCdemo_AccentLime) { if (indicatorLime != null) indicatorLime.setVisibility(View.VISIBLE); return; }
        if (themeRes == R.style.Theme_UGCdemo_AccentTeal) { if (indicatorTeal != null) indicatorTeal.setVisibility(View.VISIBLE); }
    }

    private void showChangePasswordDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText oldPass = new EditText(this);
        oldPass.setHint("旧密码");
        final EditText newPass = new EditText(this);
        newPass.setHint("新密码");

        layout.addView(oldPass);
        layout.addView(newPass);

        UnifiedDialog.showCustom(this, "修改密码", layout, "确定", (dialog, which) -> {
            String oldP = oldPass.getText().toString().trim();
            String newP = newPass.getText().toString().trim();
            if (!TextUtils.isEmpty(oldP) && !TextUtils.isEmpty(newP)) {
                profileViewModel.verifyAndUpdatePassword(userId, oldP, newP,
                        () -> runOnUiThread(() -> Toast.makeText(this, "密码修改成功", Toast.LENGTH_SHORT).show()),
                        () -> runOnUiThread(() -> Toast.makeText(this, "旧密码错误", Toast.LENGTH_SHORT).show())
                );
            }
        });
    }

    private void showDeleteAccountDialog() {
        UnifiedDialog.showConfirm(this, "注销账号", "确定要注销当前账号吗？所有数据将被永久删除！", "注销", (dialog, which) -> {
            profileViewModel.deleteUser(userId, () -> runOnUiThread(this::logout));
        });
    }

    private void logout() {
        SharedPreferences prefs = getSharedPreferences("ugc_prefs", Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
        Intent intent = new Intent(this, AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}