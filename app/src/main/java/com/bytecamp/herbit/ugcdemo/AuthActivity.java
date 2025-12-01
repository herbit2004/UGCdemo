package com.bytecamp.herbit.ugcdemo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.bytecamp.herbit.ugcdemo.data.entity.User;
import com.bytecamp.herbit.ugcdemo.viewmodel.AuthViewModel;
import com.bytecamp.herbit.ugcdemo.util.ThemeUtils;

public class AuthActivity extends AppCompatActivity {
    private AuthViewModel authViewModel;
    private EditText etUsername, etPassword;
    private Button btnAuth;
    private TextView tvSwitchMode;
    private TextView tvWelcomeTitle, tvWelcomeSubtitle;
    private boolean isLoginMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applyTheme(this);
        super.onCreate(savedInstanceState);

        // Check if logged in
        SharedPreferences prefs = getSharedPreferences("ugc_prefs", MODE_PRIVATE);
        long userId = prefs.getLong("user_id", -1);
        if (userId != -1) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_auth);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnAuth = findViewById(R.id.btnAuth);
        tvSwitchMode = findViewById(R.id.tvSwitchMode);
        tvWelcomeTitle = findViewById(R.id.tvWelcomeTitle);
        tvWelcomeSubtitle = findViewById(R.id.tvWelcomeSubtitle);

        btnAuth.setOnClickListener(v -> handleAuth());
        tvSwitchMode.setOnClickListener(v -> toggleMode());
        
        startEnterAnimation();
    }

    private void startEnterAnimation() {
        // Reduce flying distance to 150dp for a slower, more subtle effect
        float density = getResources().getDisplayMetrics().density;
        float distance = 150 * density;
        
        tvWelcomeTitle.setTranslationX(-distance);
        tvWelcomeTitle.setAlpha(0f);
        
        tvWelcomeSubtitle.setTranslationX(-distance);
        tvWelcomeSubtitle.setAlpha(0f);

        tvWelcomeTitle.animate()
                .translationX(0f)
                .alpha(1f)
                .setDuration(800)
                .setStartDelay(100)
                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                .start();

        tvWelcomeSubtitle.animate()
                .translationX(0f)
                .alpha(1f)
                .setDuration(800)
                .setStartDelay(300)
                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                .start();
    }

    private void toggleMode() {
        isLoginMode = !isLoginMode;
        btnAuth.setText(isLoginMode ? "登录" : "注册");
        tvSwitchMode.setText(isLoginMode ? "去注册" : "去登录");
    }

    private void handleAuth() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "请输入用户名和密码", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthViewModel.OnLoginResultListener listener = new AuthViewModel.OnLoginResultListener() {
            @Override
            public void onSuccess(User user) {
                runOnUiThread(() -> {
                    SharedPreferences prefs = getSharedPreferences("ugc_prefs", MODE_PRIVATE);
                    prefs.edit().putLong("user_id", user.user_id).apply();
                    Toast.makeText(AuthActivity.this, isLoginMode ? "登录成功" : "注册成功", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(AuthActivity.this, MainActivity.class));
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(AuthActivity.this, error, Toast.LENGTH_SHORT).show());
            }
        };

        if (isLoginMode) {
            authViewModel.login(username, password, listener);
        } else {
            authViewModel.register(username, password, listener);
        }
    }
}