package com.bytecamp.herbit.ugcdemo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.bytecamp.herbit.ugcdemo.viewmodel.PublishViewModel;
import com.bytecamp.herbit.ugcdemo.util.ThemeUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * PublishActivity
 * 发布页面。
 * 功能：
 * 1. 撰写帖子标题和正文。
 * 2. 选择图片（最多 9 张，可多选）。
 * 3. 将帖子保存至本地数据库。
 */
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish);

        initViews();
        publishViewModel = new ViewModelProvider(this).get(PublishViewModel.class);
    }

    private void initViews() {
        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
        btnPublish = findViewById(R.id.btnPublish);
        tvImageCount = findViewById(R.id.tvImageCount);
        
        // 设置图片预览列表
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

        // 移除顶部添加图片按钮，改为列表最后一项的“加号”按钮
        
        // 发布按钮
        btnPublish.setOnClickListener(v -> publishPost());
        
        updateImageCount();
    }

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
                 // 多选情况
                 int count = data.getClipData().getItemCount();
                 for (int i = 0; i < count; i++) {
                     if (selectedImages.size() < MAX_IMAGE_COUNT) {
                         Uri uri = data.getClipData().getItemAt(i).getUri();
                         persistUriPermission(uri);
                         selectedImages.add(uri);
                     }
                 }
             } else if (data.getData() != null) {
                 // 单选情况
                 Uri uri = data.getData();
                 persistUriPermission(uri);
                 selectedImages.add(uri);
             }
             adapter.setImages(selectedImages);
             updateImageCount();
        }
    }
    
    /**
     * 获取 URI 的持久化读取权限，确保应用重启后仍能访问图片。
     */
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

        // 拼接图片路径
        String imagePath = "";
        if (!selectedImages.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Uri uri : selectedImages) {
                sb.append(uri.toString()).append(";");
            }
            if (sb.length() > 0) sb.setLength(sb.length() - 1);
            imagePath = sb.toString();
        }

        // 获取当前用户ID
        SharedPreferences prefs = getSharedPreferences("ugc_prefs", MODE_PRIVATE);
        long userId = prefs.getLong("user_id", -1);

        // 执行发布
        publishViewModel.publishPost(userId, title, content, imagePath, new PublishViewModel.OnPublishListener() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(PublishActivity.this, "发布成功", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onError(String msg) {
                runOnUiThread(() -> Toast.makeText(PublishActivity.this, "发布失败: " + msg, Toast.LENGTH_SHORT).show());
            }
        });
    }
}