package com.bytecamp.herbit.ugcdemo;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bytecamp.herbit.ugcdemo.ui.UserListAdapter;
import com.bytecamp.herbit.ugcdemo.viewmodel.FollowListViewModel;
import com.bytecamp.herbit.ugcdemo.util.ThemeUtils;

public class FollowListActivity extends AppCompatActivity {

    public static final String EXTRA_USER_ID = "user_id";
    public static final String EXTRA_TYPE = "type"; // 0: Following, 1: Followers
    public static final String EXTRA_TITLE = "title";

    private FollowListViewModel viewModel;
    private RecyclerView recyclerView;
    private UserListAdapter adapter;
    private long userId;
    private int type;
    private long currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_list);

        userId = getIntent().getLongExtra(EXTRA_USER_ID, -1);
        type = getIntent().getIntExtra(EXTRA_TYPE, 0);
        String title = getIntent().getStringExtra(EXTRA_TITLE);
        currentUserId = getSharedPreferences("ugc_prefs", MODE_PRIVATE).getLong("user_id", -1);

        if (userId == -1) {
            finish();
            return;
        }

        initViews(title);
        setupViewModel();
    }

    private void initViews(String title) {
        ImageView ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(v -> finish());

        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText(title);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new UserListAdapter((user, isFollowing) -> {
            if (isFollowing) {
                viewModel.unfollow(currentUserId, user.user_id);
            } else {
                viewModel.follow(currentUserId, user.user_id);
            }
        });
        // Click on item to go to profile
        adapter.setOnItemClickListener(user -> {
            // Navigate to UserProfileActivity
             android.content.Intent intent = new android.content.Intent(this, UserProfileActivity.class);
             intent.putExtra(UserProfileActivity.EXTRA_USER_ID, user.user_id);
             startActivity(intent);
        });
        
        recyclerView.setAdapter(adapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(FollowListViewModel.class);
        
        if (type == 0) {
            viewModel.getFollowingList(userId).observe(this, new androidx.lifecycle.Observer<java.util.List<com.bytecamp.herbit.ugcdemo.data.entity.User>>() {
                private boolean initialized = false;
                @Override
                public void onChanged(java.util.List<com.bytecamp.herbit.ugcdemo.data.entity.User> users) {
                    if (!initialized) {
                        adapter.setUsers(users);
                        initialized = true;
                    }
                }
            });
        } else {
            viewModel.getFollowerList(userId).observe(this, users -> adapter.setUsers(users));
        }
        viewModel.getFollowingList(currentUserId).observe(this, myFollowing -> {
            java.util.Set<Long> ids = new java.util.HashSet<>();
            for (com.bytecamp.herbit.ugcdemo.data.entity.User u : myFollowing) ids.add(u.user_id);
            adapter.setFollowingIds(ids);
        });
    }
}
