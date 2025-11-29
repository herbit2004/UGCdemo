package com.bytecamp.herbit.ugcdemo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bytecamp.herbit.ugcdemo.ui.ImagePreviewAdapter;
import com.bytecamp.herbit.ugcdemo.ui.UserSearchDialogFragment;
import com.bytecamp.herbit.ugcdemo.viewmodel.PublishViewModel;
import com.bytecamp.herbit.ugcdemo.util.ThemeUtils;
import java.util.ArrayList;
import java.util.List;

public class PublishActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int MAX_IMAGE_COUNT = 9;

    // ViewModel
    private PublishViewModel publishViewModel;
    
    // Data
    private List<Uri> selectedImages = new ArrayList<>();
    
    // UI
    private EditText etTitle, etContent;
    private Button btnPublish;
    private TextView tvImageCount;
    private ImagePreviewAdapter adapter;

    private View targetForReveal;
    private int revealRx = -1, revealRy = -1;
    private boolean finishingAnimRunning = false;
    private View scrimView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish);

        initViews();
        setupMentionListener();
        publishViewModel = new ViewModelProvider(this).get(PublishViewModel.class);

        applyEnterRevealIfRequested();
    }
    
    private void initViews() {
        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
        btnPublish = findViewById(R.id.btnPublish);
        tvImageCount = findViewById(R.id.tvImageCount);
        
        RecyclerView rvPreview = findViewById(R.id.rvImagePreview);
        rvPreview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        adapter = new ImagePreviewAdapter(MAX_IMAGE_COUNT, new ImagePreviewAdapter.OnImageClickListener() {
            @Override
            public void onImageClick(int position) {
                selectedImages.remove(position);
                adapter.setImages(selectedImages);
                updateImageCount();
            }
            @Override
            public void onAddClick() {
                if (selectedImages.size() >= MAX_IMAGE_COUNT) {
                    Toast.makeText(PublishActivity.this, "最多只能上传" + MAX_IMAGE_COUNT + "张图片", Toast.LENGTH_SHORT).show();
                    return;
                }
                openGallery();
            }
        });
        rvPreview.setAdapter(adapter);

        btnPublish.setOnClickListener(v -> publishPost());
        
        updateImageCount();
    }
    
    private void setupMentionListener() {
        etContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (count > 0 && after == 0) {
                    if (s instanceof Spannable) {
                        Spannable sp = (Spannable) s;
                        MentionSpan[] spans = sp.getSpans(start, start + count, MentionSpan.class);
                        // We can't easily delete whole span here without complex logic
                    }
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count == 1 && s.toString().substring(start, start + 1).equals("@")) {
                    showUserSearchDialog();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                MentionSpan[] spans = s.getSpans(0, s.length(), MentionSpan.class);
                for (MentionSpan span : spans) {
                    int start = s.getSpanStart(span);
                    int end = s.getSpanEnd(span);
                    String text = s.toString().substring(start, end);
                    if (!text.equals("@" + span.username + " ")) {
                        // If content changed (e.g. space removed), remove span
                        // Actually we added space at end.
                        // Simple check: if text doesn't match expectation
                         if (!text.startsWith("@" + span.username)) {
                            s.removeSpan(span);
                            s.removeSpan(span.colorSpan);
                         }
                    }
                }
            }
        });
        
        etContent.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                int selectionStart = etContent.getSelectionStart();
                int selectionEnd = etContent.getSelectionEnd();
                Editable text = etContent.getText();
                MentionSpan[] spans = text.getSpans(selectionStart, selectionEnd, MentionSpan.class);
                for (MentionSpan span : spans) {
                    int spanStart = text.getSpanStart(span);
                    int spanEnd = text.getSpanEnd(span);
                    if (selectionStart > spanStart && selectionStart <= spanEnd) {
                        text.replace(spanStart, spanEnd, "");
                        return true;
                    }
                }
            }
            return false;
        });
    }

    private void showUserSearchDialog() {
        UserSearchDialogFragment dialog = new UserSearchDialogFragment();
        dialog.setOnUserSelectedListener(user -> {
            int start = etContent.getSelectionStart();
            String content = etContent.getText().toString();
            int atIndex = content.lastIndexOf("@", start - 1);
            if (atIndex != -1) {
                Editable editable = etContent.getText();
                String mentionText = "@" + user.username + " ";
                editable.replace(atIndex, start, mentionText);
                
                MentionSpan span = new MentionSpan(user.username);
                int color = getResources().getColor(R.color.purple_500);
                ForegroundColorSpan colorSpan = new ForegroundColorSpan(color);
                span.colorSpan = colorSpan;
                
                editable.setSpan(span, atIndex, atIndex + mentionText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                editable.setSpan(colorSpan, atIndex, atIndex + mentionText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        });
        dialog.show(getSupportFragmentManager(), "UserSearch");
    }

    private void applyEnterRevealIfRequested() {
        int cx = getIntent().getIntExtra("reveal_cx", -1);
        int cy = getIntent().getIntExtra("reveal_cy", -1);
        if (android.os.Build.VERSION.SDK_INT < 21 || cx <= 0 || cy <= 0) return;

        final ViewGroup content = (ViewGroup) findViewById(android.R.id.content);
        if (content == null || content.getChildCount() == 0) return;
        final View target = content.getChildAt(0);

        final int[] tloc = new int[2];
        target.getLocationOnScreen(tloc);
        final int rx = cx - tloc[0];
        int ryTmp = cy - tloc[1];
        int dy = Math.round(dp(32));
        final int ry = Math.max(0, ryTmp - dy);
        targetForReveal = target;
        revealRx = rx;
        revealRy = ry;

        getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        getWindow().getDecorView().setBackgroundColor(android.graphics.Color.TRANSPARENT);
        target.setBackgroundColor(android.graphics.Color.WHITE);

        scrimView = new View(this);
        scrimView.setBackgroundColor(0xFF000000);
        scrimView.setAlpha(0f);
        content.addView(scrimView, new android.widget.FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        target.bringToFront();

        target.setScaleX(0.96f);
        target.setScaleY(0.96f);
        target.setTranslationY(dp(16));
        target.setVisibility(View.INVISIBLE);

        target.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override public boolean onPreDraw() {
                target.getViewTreeObserver().removeOnPreDrawListener(this);
                int w = target.getWidth();
                int h = target.getHeight();
                int endRadius = (int) Math.hypot(w, h);

                android.animation.Animator reveal = android.view.ViewAnimationUtils.createCircularReveal(target, rx, ry, dp(24), endRadius);
                reveal.setDuration(520);
                reveal.setInterpolator(AnimationUtils.loadInterpolator(PublishActivity.this, android.R.interpolator.fast_out_slow_in));

                target.setVisibility(View.VISIBLE);
                reveal.start();

                android.animation.TimeInterpolator interpolator = AnimationUtils.loadInterpolator(PublishActivity.this, android.R.interpolator.fast_out_slow_in);
                android.animation.ObjectAnimator sx = android.animation.ObjectAnimator.ofFloat(target, "scaleX", 1f);
                android.animation.ObjectAnimator sy = android.animation.ObjectAnimator.ofFloat(target, "scaleY", 1f);
                android.animation.ObjectAnimator ty = android.animation.ObjectAnimator.ofFloat(target, "translationY", 0f);
                sx.setDuration(520); sy.setDuration(520); ty.setDuration(520);
                sx.setInterpolator(interpolator); sy.setInterpolator(interpolator); ty.setInterpolator(interpolator);
                sx.start(); sy.start(); ty.start();

                android.animation.ObjectAnimator scrimAnim = android.animation.ObjectAnimator.ofFloat(scrimView, "alpha", 0f, 0.5f);
                scrimAnim.setDuration(520);
                scrimAnim.setInterpolator(interpolator);
                scrimAnim.start();
                return true;
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (!startExitReveal()) {
            super.onBackPressed();
        }
    }

    @Override
    public void finish() {
        if (!startExitReveal()) {
            super.finish();
            overridePendingTransition(0, 0);
        }
    }

    private boolean startExitReveal() {
        if (android.os.Build.VERSION.SDK_INT < 21) return false;
        if (targetForReveal == null || revealRx <= 0 || revealRy <= 0 || finishingAnimRunning) return false;
        finishingAnimRunning = true;

        final View target = targetForReveal;
        int endRadius = (int) Math.hypot(target.getWidth(), target.getHeight());
        android.animation.Animator reveal = android.view.ViewAnimationUtils.createCircularReveal(target, revealRx, revealRy, endRadius, dp(24));
        reveal.setDuration(520);
        android.animation.TimeInterpolator interpolator = AnimationUtils.loadInterpolator(PublishActivity.this, android.R.interpolator.fast_out_slow_in);
        reveal.setInterpolator(interpolator);

        android.animation.ObjectAnimator sx = android.animation.ObjectAnimator.ofFloat(target, "scaleX", 0.96f);
        android.animation.ObjectAnimator sy = android.animation.ObjectAnimator.ofFloat(target, "scaleY", 0.96f);
        android.animation.ObjectAnimator ty = android.animation.ObjectAnimator.ofFloat(target, "translationY", dp(16));
        sx.setDuration(520); sy.setDuration(520); ty.setDuration(520);
        sx.setInterpolator(interpolator); sy.setInterpolator(interpolator); ty.setInterpolator(interpolator);

        android.animation.ObjectAnimator scrimAnim = null;
        if (scrimView != null) {
            scrimAnim = android.animation.ObjectAnimator.ofFloat(scrimView, "alpha", scrimView.getAlpha(), 0f);
            scrimAnim.setDuration(520);
            scrimAnim.setInterpolator(interpolator);
        }

        android.animation.AnimatorSet set = new android.animation.AnimatorSet();
        if (scrimAnim != null) {
            set.playTogether(reveal, sx, sy, ty, scrimAnim);
        } else {
            set.playTogether(reveal, sx, sy, ty);
        }
        set.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(android.animation.Animator animation) {
                target.setVisibility(View.INVISIBLE);
                PublishActivity.super.finish();
                overridePendingTransition(0, 0);
                finishingAnimRunning = false;
            }
        });
        set.start();
        return true;
    }

    private float dp(int v) { return v * getResources().getDisplayMetrics().density; }

    private void updateImageCount() {
        tvImageCount.setText(selectedImages.size() + "/" + MAX_IMAGE_COUNT);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            if (data.getClipData() != null) {
                 int count = data.getClipData().getItemCount();
                 for (int i = 0; i < count; i++) {
                     if (selectedImages.size() < MAX_IMAGE_COUNT) {
                         Uri uri = data.getClipData().getItemAt(i).getUri();
                         persistUriPermission(uri);
                         selectedImages.add(uri);
                     }
                 }
             } else if (data.getData() != null) {
                 Uri uri = data.getData();
                 persistUriPermission(uri);
                 selectedImages.add(uri);
             }
             adapter.setImages(selectedImages);
             updateImageCount();
        }
    }
    
    private void persistUriPermission(Uri uri) {
        final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
        try {
            getContentResolver().takePersistableUriPermission(uri, takeFlags);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void publishPost() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(content)) {
            Toast.makeText(this, "标题和正文不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        String imagePath = "";
        if (!selectedImages.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Uri uri : selectedImages) {
                sb.append(uri.toString()).append(";");
            }
            if (sb.length() > 0) sb.setLength(sb.length() - 1);
            imagePath = sb.toString();
        }

        SharedPreferences prefs = getSharedPreferences("ugc_prefs", MODE_PRIVATE);
        long userId = prefs.getLong("user_id", -1);

        publishViewModel.publishPost(userId, title, content, imagePath, new PublishViewModel.OnPublishListener() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(PublishActivity.this, "发布成功", Toast.LENGTH_SHORT).show();
                    getSharedPreferences("ugc_prefs", MODE_PRIVATE)
                            .edit()
                            .putBoolean("home_refresh_after_publish", true)
                            .apply();
                    finish();
                });
            }

            @Override
            public void onError(String msg) {
                runOnUiThread(() -> Toast.makeText(PublishActivity.this, "发布失败: " + msg, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private static class MentionSpan {
        String username;
        ForegroundColorSpan colorSpan;
        public MentionSpan(String username) { this.username = username; }
    }
}
