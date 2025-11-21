package com.bytecamp.herbit.ugcdemo;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bytecamp.herbit.ugcdemo.ui.CommentsAdapter;
import com.bytecamp.herbit.ugcdemo.viewmodel.DetailViewModel;

public class DetailActivity extends AppCompatActivity {
    public static final String EXTRA_POST_ID = "extra_post_id";
    private DetailViewModel detailViewModel;
    private long postId;
    private CommentsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        postId = getIntent().getLongExtra(EXTRA_POST_ID, -1);
        if (postId == -1) {
            finish();
            return;
        }

        detailViewModel = new ViewModelProvider(this).get(DetailViewModel.class);

        initViews();
        observeData();
    }

    private void initViews() {
        ImageView ivImage = findViewById(R.id.ivDetailImage);
        TextView tvTitle = findViewById(R.id.tvDetailTitle);
        TextView tvContent = findViewById(R.id.tvDetailContent);
        RecyclerView rvComments = findViewById(R.id.rvComments);
        EditText etComment = findViewById(R.id.etComment);
        Button btnSend = findViewById(R.id.btnSendComment);

        rvComments.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CommentsAdapter();
        rvComments.setAdapter(adapter);

        btnSend.setOnClickListener(v -> {
            String content = etComment.getText().toString().trim();
            if (!TextUtils.isEmpty(content)) {
                SharedPreferences prefs = getSharedPreferences("ugc_prefs", MODE_PRIVATE);
                long userId = prefs.getLong("user_id", -1);
                detailViewModel.addComment(postId, userId, content);
                etComment.setText("");
            }
        });

        detailViewModel.getPostById(postId).observe(this, postWithUser -> {
            if (postWithUser != null) {
                tvTitle.setText(postWithUser.post.title);
                tvContent.setText(postWithUser.post.content);
                if (postWithUser.post.image_path != null) {
                    Glide.with(this).load(postWithUser.post.image_path).into(ivImage);
                }
            }
        });
    }

    private void observeData() {
        detailViewModel.getCommentsForPost(postId).observe(this, comments -> {
            adapter.setComments(comments);
        });
    }
}