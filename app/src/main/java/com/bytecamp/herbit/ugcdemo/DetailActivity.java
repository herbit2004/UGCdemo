package com.bytecamp.herbit.ugcdemo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.bytecamp.herbit.ugcdemo.util.ThemeUtils;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.bumptech.glide.Glide;
import com.bytecamp.herbit.ugcdemo.data.model.CommentWithUser;
import com.bytecamp.herbit.ugcdemo.data.model.PostWithUser;
import com.bytecamp.herbit.ugcdemo.ui.CommentsAdapter;
import com.bytecamp.herbit.ugcdemo.ui.DetailImageAdapter;
import com.bytecamp.herbit.ugcdemo.util.TimeUtils;
import com.bytecamp.herbit.ugcdemo.viewmodel.DetailViewModel;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import java.util.ArrayList;
import java.util.List;

/**
 * DetailActivity
 * 帖子详情页。
 * 功能：
 * 1. 展示帖子多图轮播、标题、正文。
 * 2. 展示评论列表，支持回复评论、删除自己的评论。
 * 3. 支持帖子和评论的点赞功能（实时更新状态和数量）。
 * 4. 楼主可删除自己的帖子。
 * 5. 支持关注楼主。
 */
public class DetailActivity extends AppCompatActivity {
    public static final String EXTRA_POST_ID = "extra_post_id";

    // ViewModel & Data
    private DetailViewModel detailViewModel;
    private long postId;
    private long currentUserId;
    private long authorId = -1;
    private List<Long> likedCommentIds = new ArrayList<>();
    private Long replyToCommentId = null;
    private String replyToUsername = null;

    // Adapters
    private CommentsAdapter commentsAdapter;
    private DetailImageAdapter imageAdapter;

    // Views
    private ViewPager2 vpImages;
    private TabLayout tabLayout;
    private TextView tvDetailCoverTitle;
    private TextView tvDetailCoverQuote;
    private TextView tvTitle, tvContent, tvPublishTime;
    private ImageView ivHeaderAvatar;
    private TextView tvHeaderName;
    private Button btnFollow;
    private ImageView ivMoreOptions;
    private RecyclerView rvComments;
    private androidx.core.widget.NestedScrollView nestedScrollView;
    private EditText etComment;
    private Button btnSend;
    private ImageView ivCancelReply;
    private LinearLayout llPostLike;
    private ImageView ivLike;
    private TextView tvLikeCount;
    private LinearLayout llCommentIndicator;
    private ImageView ivCommentIcon;
    private TextView tvCommentCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // 1. 验证参数和用户状态
        postId = getIntent().getLongExtra(EXTRA_POST_ID, -1);
        if (postId == -1) {
            finish();
            return;
        }
        SharedPreferences prefs = getSharedPreferences("ugc_prefs", MODE_PRIVATE);
        currentUserId = prefs.getLong("user_id", -1);

        // 2. 初始化 ViewModel
        detailViewModel = new ViewModelProvider(this).get(DetailViewModel.class);

        // 3. 初始化 UI 和 观察者
        initViews();
        setupListeners();
        setupObservers();
    }

    private void initViews() {
        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Header
        ivHeaderAvatar = findViewById(R.id.ivHeaderAvatar);
        tvHeaderName = findViewById(R.id.tvHeaderName);
        btnFollow = findViewById(R.id.btnFollow);
        ivMoreOptions = findViewById(R.id.ivMoreOptions);

        // Content
        tvTitle = findViewById(R.id.tvDetailTitle);
        tvContent = findViewById(R.id.tvDetailContent);
        tvPublishTime = findViewById(R.id.tvPublishTime);

        // Images
        vpImages = findViewById(R.id.vpImages);
        tabLayout = findViewById(R.id.tabLayout);
        tvDetailCoverTitle = findViewById(R.id.tvDetailCoverTitle);
        tvDetailCoverQuote = findViewById(R.id.tvDetailCoverQuote);
        imageAdapter = new DetailImageAdapter();
        vpImages.setAdapter(imageAdapter);
        new TabLayoutMediator(tabLayout, vpImages, (tab, position) -> {}).attach();

        // Comments List
        rvComments = findViewById(R.id.rvComments);
        rvComments.setLayoutManager(new LinearLayoutManager(this));
        commentsAdapter = new CommentsAdapter();
        commentsAdapter.setCurrentUserId(currentUserId);
        rvComments.setAdapter(commentsAdapter);
        rvComments.addOnScrollListener(new RecyclerView.OnScrollListener(){
            private long lastLoadTime = 0L;
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if (lm == null) return;
                    int lastPos = lm.findLastVisibleItemPosition();
                    RecyclerView.Adapter a = recyclerView.getAdapter();
                    if (a != null && lastPos >= Math.max(0, a.getItemCount() - 3)) {
                        long now = System.currentTimeMillis();
                        if (detailViewModel.hasMoreComments() && now - lastLoadTime > 700) {
                            if (a instanceof CommentsAdapter) {
                                ((CommentsAdapter) a).setLoading(true);
                            }
                            detailViewModel.loadMoreComments();
                            lastLoadTime = now;
                        }
                    }
                }
            }
        });

        // Input Area
        nestedScrollView = findViewById(R.id.nestedScroll);
        etComment = findViewById(R.id.etComment);
        btnSend = findViewById(R.id.btnSendComment);
        ivCancelReply = findViewById(R.id.ivCancelReply);

        // Post Like Area
        llPostLike = findViewById(R.id.llPostLike);
        ivLike = findViewById(R.id.ivPostLike);
        tvLikeCount = findViewById(R.id.tvPostLikeCount);

        // Comment Indicator Area
        llCommentIndicator = findViewById(R.id.llCommentIndicator);
        ivCommentIcon = findViewById(R.id.ivCommentIcon);
        tvCommentCount = findViewById(R.id.tvCommentCount);
    }

    private void setupListeners() {
        // 取消回复模式
        ivCancelReply.setOnClickListener(v -> exitReplyMode());

        // 发送评论
        btnSend.setOnClickListener(v -> {
            String content = etComment.getText().toString().trim();
            if (!TextUtils.isEmpty(content)) {
                detailViewModel.addComment(postId, currentUserId, content, replyToCommentId, replyToUsername);
                exitReplyMode();
                detailViewModel.refreshComments();
            }
        });

        // 帖子点赞 (初始状态由 observer 更新)
        llPostLike.setOnClickListener(v -> detailViewModel.toggleLike(currentUserId, 0, postId, false));

        // 点击评论指示器，滚动到评论区顶部
        llCommentIndicator.setOnClickListener(v -> {
            if (nestedScrollView != null && rvComments != null) {
                nestedScrollView.smoothScrollTo(0, rvComments.getTop());
            }
        });

        // 评论列表交互
        commentsAdapter.setListener(new CommentsAdapter.OnCommentActionListener() {
            @Override
            public void onReply(CommentWithUser comment) {
                enterReplyMode(comment);
            }

            @Override
            public void onLike(CommentWithUser comment) {
                boolean isLiked = likedCommentIds.contains(comment.comment.comment_id);
                detailViewModel.toggleLike(currentUserId, 1, comment.comment.comment_id, isLiked);
            }

            @Override
            public void onDelete(CommentWithUser comment) {
                showDeleteCommentDialog(comment);
            }
            
            @Override
            public void onUserClick(long userId) {
                navigateToUserProfile(userId);
            }
        });
        
        // 点击头像跳转个人主页
        ivHeaderAvatar.setOnClickListener(v -> {
            if (authorId != -1) {
                navigateToUserProfile(authorId);
            }
        });
        tvHeaderName.setOnClickListener(v -> {
            if (authorId != -1) {
                navigateToUserProfile(authorId);
            }
        });
    }

    private void setupObservers() {
        // 1. 帖子详情
        detailViewModel.getPostById(postId).observe(this, this::updatePostUI);

        // 2. 评论列表（分页，顶层+子回复展平）
        detailViewModel.getCommentsPaged().observe(this, comments -> {
            commentsAdapter.setComments(comments);
            commentsAdapter.setLoading(false);
            commentsAdapter.setHasMore(detailViewModel.hasMoreComments());
            if (tvCommentCount != null) {
                tvCommentCount.setText(String.valueOf(comments != null ? comments.size() : 0));
            }
        });
        detailViewModel.initComments(postId);

        // 3. 帖子点赞数
        detailViewModel.getLikeCount(0, postId).observe(this, count -> tvLikeCount.setText(String.valueOf(count)));

        // 4. 当前用户是否点赞帖子
        detailViewModel.isLiked(currentUserId, 0, postId).observe(this, isLiked -> {
            boolean liked = isLiked != null && isLiked;
            ivLike.setImageResource(liked ? R.drawable.ic_like_on : R.drawable.ic_like_off);
            llPostLike.setOnClickListener(v -> detailViewModel.toggleLike(currentUserId, 0, postId, liked));
        });

        // 5. 用户点赞过的评论ID列表
        detailViewModel.getLikedCommentIds(currentUserId).observe(this, ids -> {
            this.likedCommentIds = ids;
            commentsAdapter.setLikedCommentIds(ids);
        });

        // 6. 所有评论的点赞统计
        detailViewModel.getCommentLikeCounts().observe(this, counts -> commentsAdapter.setLikeCounts(counts));
    }

    private void updatePostUI(PostWithUser postWithUser) {
        if (postWithUser == null || postWithUser.post == null) return;
        
        authorId = postWithUser.post.author_id;

        // 设置内容
        tvTitle.setText(postWithUser.post.title);
        tvContent.setText(postWithUser.post.content);
        
        // 设置发布时间
        tvPublishTime.setText("发布于 " + TimeUtils.formatTime(postWithUser.post.publish_time));

        // 处理图片
        List<String> images = new ArrayList<>();
        if (postWithUser.post.image_path != null && !postWithUser.post.image_path.isEmpty()) {
            String[] paths = postWithUser.post.image_path.split(";");
            for (String p : paths) {
                if (!p.isEmpty()) images.add(p);
            }
        }

        if (!images.isEmpty()) {
            vpImages.setVisibility(View.VISIBLE);
            tvDetailCoverTitle.setVisibility(View.GONE);
            tvDetailCoverQuote.setVisibility(View.GONE);
            imageAdapter.setImageUrls(images);
            tabLayout.setVisibility(images.size() > 1 ? View.VISIBLE : View.GONE);
        } else {
            vpImages.setVisibility(View.GONE);
            tabLayout.setVisibility(View.GONE);
            tvDetailCoverTitle.setVisibility(View.VISIBLE);
            tvDetailCoverQuote.setVisibility(View.VISIBLE);
            tvDetailCoverTitle.setText(postWithUser.post.title);
        }

        // 处理作者信息
        commentsAdapter.setPostAuthorId(authorId);
        if (postWithUser.user != null) {
            tvHeaderName.setText(postWithUser.user.username);
            if (postWithUser.user.avatar_path != null) {
                Glide.with(this).load(postWithUser.user.avatar_path).circleCrop().into(ivHeaderAvatar);
            }
        }

        // 处理更多菜单 (删除帖子)
        if (authorId == currentUserId) {
            ivMoreOptions.setVisibility(View.VISIBLE);
            ivMoreOptions.setOnClickListener(this::showPostMenu);
            btnFollow.setVisibility(View.GONE);
        } else {
            ivMoreOptions.setVisibility(View.GONE);
            // Setup Follow Button
            setupFollowButton();
        }
    }
    
    private void setupFollowButton() {
        btnFollow.setVisibility(View.VISIBLE);
        detailViewModel.isFollowing(currentUserId, authorId).observe(this, count -> {
            boolean isFollowing = count != null && count > 0;
            updateFollowButtonState(isFollowing);
            btnFollow.setOnClickListener(v -> detailViewModel.toggleFollow(currentUserId, authorId, isFollowing));
        });
    }
    
    private void updateFollowButtonState(boolean isFollowing) {
        if (isFollowing) {
            btnFollow.setText("已关注");
            // Removed color change, keeping default/xml white text
        } else {
            btnFollow.setText("关注");
            // Removed color change
        }
    }

    private void enterReplyMode(CommentWithUser comment) {
        replyToCommentId = (comment.comment.parent_comment_id == null || comment.comment.parent_comment_id == 0)
                ? comment.comment.comment_id
                : comment.comment.parent_comment_id;
        replyToUsername = comment.user.username;
        etComment.setHint("回复 " + replyToUsername + ":");
        etComment.requestFocus();
        ivCancelReply.setVisibility(View.VISIBLE);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(etComment, InputMethodManager.SHOW_IMPLICIT);
    }

    private void exitReplyMode() {
        replyToCommentId = null;
        replyToUsername = null;
        etComment.setText("");
        etComment.setHint("说点什么...");
        ivCancelReply.setVisibility(View.GONE);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etComment.getWindowToken(), 0);
    }

    private void showPostMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.getMenu().add("删除帖子");
        popup.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals("删除帖子")) {
                new AlertDialog.Builder(this)
                        .setTitle("删除帖子")
                        .setMessage("确定要删除这个帖子吗？")
                        .setPositiveButton("删除", (d, w) -> detailViewModel.deletePost(postId, this::finish))
                        .setNegativeButton("取消", null)
                        .show();
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void showDeleteCommentDialog(CommentWithUser comment) {
        new AlertDialog.Builder(this)
                .setTitle("删除评论")
                .setMessage("确定要删除这条评论吗？")
                .setPositiveButton("删除", (d, w) -> detailViewModel.deleteComment(comment.comment.comment_id))
                .setNegativeButton("取消", null)
                .show();
    }
    
    private void navigateToUserProfile(long targetUserId) {
        if (targetUserId == currentUserId) {
            // It's the current user, maybe we want to switch to main activity profile tab?
            // For now, just finish to go back or start MainActivity with extra
             Intent intent = new Intent(this, MainActivity.class);
             intent.putExtra("open_profile", true);
             startActivity(intent);
        } else {
            Intent intent = new Intent(this, UserProfileActivity.class);
            intent.putExtra(UserProfileActivity.EXTRA_USER_ID, targetUserId);
            startActivity(intent);
        }
    }
}
