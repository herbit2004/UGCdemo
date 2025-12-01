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

import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import com.bytecamp.herbit.ugcdemo.ui.UserSearchDialogFragment;
import android.text.method.LinkMovementMethod;
import com.bytecamp.herbit.ugcdemo.util.SpanUtils;
import com.bytecamp.herbit.ugcdemo.ui.widget.FollowButton;
import com.bytecamp.herbit.ugcdemo.ui.widget.CustomPopupMenu;
import com.bytecamp.herbit.ugcdemo.ui.widget.UnifiedDialog;

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
    public static final String EXTRA_TARGET_COMMENT_ID = "extra_target_comment_id";

    // ViewModel & Data
    private DetailViewModel detailViewModel;
    private long postId;
    private long targetCommentId = -1;
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
    private FollowButton btnFollow;
    private ImageView ivDeletePost;
    private RecyclerView rvComments;
    private androidx.core.widget.NestedScrollView nestedScrollView;
    private EditText etComment;
    private Button btnSend;
    private ImageView ivCancelReply;
    private View bottomBar;
    private LinearLayout llPostLike;
    private ImageView ivPostLike;
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
        // targetCommentId removed
        
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
        ivDeletePost = findViewById(R.id.ivDeletePost);

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
        bottomBar = findViewById(R.id.bottomBar);
        etComment = findViewById(R.id.etComment);
        btnSend = findViewById(R.id.btnSendComment);
        ivCancelReply = findViewById(R.id.ivCancelReply);

        // Post Like Area
        llPostLike = findViewById(R.id.llPostLike);
        ivPostLike = findViewById(R.id.ivPostLike);
        tvLikeCount = findViewById(R.id.tvPostLikeCount);

        // Comment Indicator Area
        llCommentIndicator = findViewById(R.id.llCommentIndicator);
        ivCommentIcon = findViewById(R.id.ivCommentIcon);
        tvCommentCount = findViewById(R.id.tvCommentCount);

        if (bottomBar != null && nestedScrollView != null) {
            bottomBar.getViewTreeObserver().addOnGlobalLayoutListener(new android.view.ViewTreeObserver.OnGlobalLayoutListener() {
                @Override public void onGlobalLayout() {
                    int h = bottomBar.getHeight();
                    android.view.ViewGroup.MarginLayoutParams lp = (android.view.ViewGroup.MarginLayoutParams) nestedScrollView.getLayoutParams();
                    if (lp.bottomMargin != h) {
                        lp.bottomMargin = h;
                        nestedScrollView.setLayoutParams(lp);
                    }
                }
            });
        }

        etComment.setOnTouchListener((v, ev) -> {
            boolean canScroll = v.canScrollVertically(1) || v.canScrollVertically(-1);
            if (canScroll) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                if (ev.getAction() == android.view.MotionEvent.ACTION_UP || ev.getAction() == android.view.MotionEvent.ACTION_CANCEL) {
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                }
            }
            return false;
        });
        
        setupMentionListener();
    }
    
    private void setupMentionListener() {
        etComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

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
                    if (!text.startsWith("@" + span.username)) {
                        s.removeSpan(span);
                        s.removeSpan(span.colorSpan);
                    }
                }
            }
        });
        
        etComment.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == android.view.KeyEvent.KEYCODE_DEL && event.getAction() == android.view.KeyEvent.ACTION_DOWN) {
                int selectionStart = etComment.getSelectionStart();
                int selectionEnd = etComment.getSelectionEnd();
                Editable text = etComment.getText();
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
            int start = etComment.getSelectionStart();
            String content = etComment.getText().toString();
            int atIndex = content.lastIndexOf("@", start - 1);
            if (atIndex != -1) {
                Editable editable = etComment.getText();
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
        // dialog.show(getSupportFragmentManager(), "UserSearch"); // Using standard DialogFragment show
        // To make it appear from bottom or as a dialog with specific style, we can use BottomSheetDialogFragment or just standard dialog.
        // User asked for rounded corners on the "background board".
        // Since UserSearchDialogFragment extends DialogFragment, it shows as a center dialog or full screen depending on theme.
        // In onStart we set MATCH_PARENT.
        // Let's make sure it behaves like a bottom sheet if that's what "at user popup" implies, or a center dialog.
        // Usually @mention popups are small windows or bottom sheets.
        // Given the layout minWidth/minHeight, it looks like a dialog.
        // I'll just show it.
        dialog.show(getSupportFragmentManager(), "UserSearch");
    }
    
    private static class MentionSpan {
        String username;
        ForegroundColorSpan colorSpan;
        public MentionSpan(String username) { this.username = username; }
    }

    private void setupListeners() {
// ...
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
        llPostLike.setOnClickListener(v -> {
            Boolean currentLiked = detailViewModel.isLiked(currentUserId, 0, postId).getValue();
            boolean isLiked = currentLiked != null && currentLiked;
            detailViewModel.toggleLike(currentUserId, 0, postId, isLiked);
        });

        // 点击评论指示器，滚动到评论区顶部
        llCommentIndicator.setOnClickListener(v -> {
            if (nestedScrollView != null && rvComments != null) {
                nestedScrollView.smoothScrollTo(0, rvComments.getTop());
            }
        });

        etComment.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                enterInputState();
            } else {
                exitInputState();
            }
        });

        nestedScrollView.setOnTouchListener((v, ev) -> {
            if (etComment != null && etComment.hasFocus()) {
                if (replyToCommentId != null || replyToUsername != null) {
                    exitReplyMode();
                } else {
                    exitInputState();
                }
            }
            return false;
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

        // 3. 帖子点赞状态和数量
        detailViewModel.isLiked(currentUserId, 0, postId).observe(this, isLiked -> {
             if (isLiked != null) {
                 ivPostLike.setImageResource(isLiked ? R.drawable.ic_like_filled : R.drawable.ic_like_outline);
                 if (isLiked) {
                     ivPostLike.setColorFilter(android.graphics.Color.RED);
                     ivPostLike.setAlpha(1.0f);
                 } else {
                     ivPostLike.clearColorFilter();
                     ivPostLike.setAlpha(1.0f);
                 }
             }
        });
        
        detailViewModel.getLikeCount(0, postId).observe(this, count -> {
            tvLikeCount.setText(String.valueOf(count != null ? count : 0));
        });
        
        // 4. 评论点赞状态和数量
        detailViewModel.getLikedCommentIds(currentUserId).observe(this, ids -> {
            commentsAdapter.setLikedCommentIds(ids);
        });
        detailViewModel.getCommentLikeCounts().observe(this, counts -> {
            commentsAdapter.setLikeCounts(counts);
        });
        
        // 2. 评论列表（分页，顶层+子回复展平）
        detailViewModel.getCommentsPaged().observe(this, comments -> {
            commentsAdapter.setComments(comments);
            commentsAdapter.setLoading(false);
            commentsAdapter.setHasMore(detailViewModel.hasMoreComments());
            if (tvCommentCount != null) {
                tvCommentCount.setText(String.valueOf(comments != null ? comments.size() : 0));
            }
            
            if (comments != null && !comments.isEmpty()) {
                // ... (mention avatar logic) ...
            }
        });
        detailViewModel.initComments(postId);
        
        // ...
    }
    
    // Remove scrollToPosition method

    private void updatePostUI(PostWithUser postWithUser) {
        if (postWithUser == null || postWithUser.post == null) return;
        
        authorId = postWithUser.post.author_id;

        // 设置内容
        tvTitle.setText(SpanUtils.getSpannableText(this, postWithUser.post.title));
        tvContent.setText(SpanUtils.getSpannableText(this, postWithUser.post.content));
        tvContent.setMovementMethod(LinkMovementMethod.getInstance());
        // tvTitle usually isn't clickable for mentions but we can enable it if desired.
        // But for now only content is critical.
        
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
            ivDeletePost.setVisibility(View.VISIBLE);
            ivDeletePost.setOnClickListener(v -> {
                UnifiedDialog.showConfirm(this, "删除帖子", "确定要删除这个帖子吗？", "删除", (d, w) -> detailViewModel.deletePost(postId, this::finish));
            });
            btnFollow.setVisibility(View.GONE);
        } else {
            ivDeletePost.setVisibility(View.GONE);
            setupFollowButton();
        }
    }
    
    private void setupFollowButton() {
        btnFollow.setVisibility(View.VISIBLE);
        
        androidx.lifecycle.LiveData<Integer> followingData = detailViewModel.isFollowing(currentUserId, authorId);
        androidx.lifecycle.LiveData<Integer> followerData = detailViewModel.isFollowing(authorId, currentUserId);
        
        final boolean[] state = new boolean[]{false, false}; // [isFollowing, isFollower]
        
        followingData.observe(this, count -> {
            state[0] = count != null && count > 0;
            updateFollowButtonState(state[0], state[1]);
        });
        
        followerData.observe(this, count -> {
            state[1] = count != null && count > 0;
            updateFollowButtonState(state[0], state[1]);
        });
        
        btnFollow.setOnClickListener(v -> detailViewModel.toggleFollow(currentUserId, authorId, state[0]));
    }

    private void updateFollowButtonState(boolean isFollowing, boolean isFollower) {
        btnFollow.setState(isFollowing, isFollower);
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
        enterInputState();
    }

    private void exitReplyMode() {
        replyToCommentId = null;
        replyToUsername = null;
        etComment.setText("");
        etComment.setHint("说点什么...");
        ivCancelReply.setVisibility(View.GONE);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etComment.getWindowToken(), 0);
        exitInputState();
    }

    private void enterInputState() {
        if (llPostLike != null) llPostLike.setVisibility(View.GONE);
        if (llCommentIndicator != null) llCommentIndicator.setVisibility(View.GONE);
    }

    private void exitInputState() {
        if (llPostLike != null) llPostLike.setVisibility(View.VISIBLE);
        if (llCommentIndicator != null) llCommentIndicator.setVisibility(View.VISIBLE);
        if (etComment != null) {
            etComment.clearFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(etComment.getWindowToken(), 0);
        }
    }

    private void showDeleteCommentDialog(CommentWithUser comment) {
        UnifiedDialog.showConfirm(this, "删除评论", "确定要删除这条评论吗？", "删除", (d, w) -> detailViewModel.deleteComment(comment.comment.comment_id));
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
