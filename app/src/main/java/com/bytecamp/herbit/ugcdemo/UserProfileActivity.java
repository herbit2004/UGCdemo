package com.bytecamp.herbit.ugcdemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import com.bumptech.glide.Glide;
import com.bytecamp.herbit.ugcdemo.ui.PostsAdapter;
import com.bytecamp.herbit.ugcdemo.viewmodel.UserProfileViewModel;
import com.google.android.material.tabs.TabLayout;
import com.bytecamp.herbit.ugcdemo.util.ThemeUtils;

public class UserProfileActivity extends AppCompatActivity {
    public static final String EXTRA_USER_ID = "user_id";

    private UserProfileViewModel viewModel;
    private long userId;
    private long currentUserId;
    
    private ImageView ivAvatar, ivBack;
    private TextView tvUsername;
    private TextView tvPostCount, tvFollowCount, tvFanCount;
    private Button btnFollow;
    private RecyclerView recyclerView;
    private TabLayout tabLayout;
    private PostsAdapter adapter;
    private androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeRefresh;
    
    private LinearLayout llFollowContainer, llFanContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        userId = getIntent().getLongExtra(EXTRA_USER_ID, -1);
        if (userId == -1) {
            finish();
            return;
        }
        
        currentUserId = getSharedPreferences("ugc_prefs", MODE_PRIVATE).getLong("user_id", -1);
        
        initViews();
        setupViewModel();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(v -> finish());

        ivAvatar = findViewById(R.id.ivProfileAvatar);
        tvUsername = findViewById(R.id.tvProfileName);
        tvPostCount = findViewById(R.id.tvPostCount);
        tvFollowCount = findViewById(R.id.tvFollowCount);
        tvFanCount = findViewById(R.id.tvFanCount);
        
        llFollowContainer = findViewById(R.id.llFollowContainer);
        llFanContainer = findViewById(R.id.llFanContainer);
        
        btnFollow = findViewById(R.id.btnFollow);
        if (userId == currentUserId) {
            btnFollow.setVisibility(View.GONE); // Should not happen if logic is correct
        }

        swipeRefresh = findViewById(R.id.swipeRefresh);
        recyclerView = findViewById(R.id.rvProfilePosts);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        adapter = new PostsAdapter();
        recyclerView.setAdapter(adapter);

        tabLayout = findViewById(R.id.tabLayout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewModel.setTab(tab.getPosition());
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
        swipeRefresh.setOnRefreshListener(() -> {
            swipeRefresh.setRefreshing(false);
            viewModel.refresh();
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener(){
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
                        if (viewModel.hasMore() && now - lastLoadTime > 700) {
                            if (a instanceof com.bytecamp.herbit.ugcdemo.ui.PostsAdapter) {
                                ((com.bytecamp.herbit.ugcdemo.ui.PostsAdapter)a).setLoading(true);
                            }
                            viewModel.loadMore();
                            lastLoadTime = now;
                        }
                    }
                }
            }
        });
        
        // Click listeners for lists
        View.OnClickListener followListener = v -> {
            Intent intent = new Intent(this, FollowListActivity.class);
            intent.putExtra(FollowListActivity.EXTRA_USER_ID, userId);
            intent.putExtra(FollowListActivity.EXTRA_TYPE, 0);
            intent.putExtra(FollowListActivity.EXTRA_TITLE, "关注列表");
            startActivity(intent);
        };
        llFollowContainer.setOnClickListener(followListener);
        
        View.OnClickListener fanListener = v -> {
            Intent intent = new Intent(this, FollowListActivity.class);
            intent.putExtra(FollowListActivity.EXTRA_USER_ID, userId);
            intent.putExtra(FollowListActivity.EXTRA_TYPE, 1);
            intent.putExtra(FollowListActivity.EXTRA_TITLE, "粉丝列表");
            startActivity(intent);
        };
        llFanContainer.setOnClickListener(fanListener);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(UserProfileViewModel.class);
        
        viewModel.getUser(userId).observe(this, user -> {
            if (user != null) {
                tvUsername.setText(user.username);
                if (user.avatar_path != null) {
                    Glide.with(this).load(user.avatar_path).circleCrop().into(ivAvatar);
                }
            }
        });
        
        viewModel.getPosts().observe(this, posts -> {
            adapter.setPosts(posts);
            if (viewModel.getCurrentTab() == 0) {
                tvPostCount.setText(String.valueOf(posts.size()));
            }
            adapter.setLoading(false);
            adapter.setHasMore(viewModel.hasMore());
        });
        viewModel.init(userId);
        
        viewModel.getFollowingCount(userId).observe(this, count -> tvFollowCount.setText(String.valueOf(count)));
        viewModel.getFollowerCount(userId).observe(this, count -> tvFanCount.setText(String.valueOf(count)));
        
        viewModel.isFollowing(currentUserId, userId).observe(this, count -> {
            boolean isFollowing = count != null && count > 0;
            updateFollowButtonState(isFollowing);
            btnFollow.setOnClickListener(v -> viewModel.toggleFollow(currentUserId, userId, isFollowing));
        });
    }
    
    private void updateFollowButtonState(boolean isFollowing) {
        if (isFollowing) {
            btnFollow.setText("已关注");
        } else {
            btnFollow.setText("关注");
        }
    }
}