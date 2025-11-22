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

public class FollowListActivity extends AppCompatActivity {

    public static final String EXTRA_USER_ID = "user_id";
    public static final String EXTRA_TYPE = "type"; // 0: Following, 1: Followers
    public static final String EXTRA_TITLE = "title";

    private FollowListViewModel viewModel;
    private RecyclerView recyclerView;
    private UserListAdapter adapter;
    private long userId;
    private int type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_list);

        userId = getIntent().getLongExtra(EXTRA_USER_ID, -1);
        type = getIntent().getIntExtra(EXTRA_TYPE, 0);
        String title = getIntent().getStringExtra(EXTRA_TITLE);

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
        
        boolean canUnfollow = (type == 0 && userId == getSharedPreferences("ugc_prefs", MODE_PRIVATE).getLong("user_id", -1));
        adapter = new UserListAdapter(canUnfollow, user -> {
             // Toggle follow logic if needed or open profile
             // For simplicity, let's just allow unfollowing in list if it's "My Following" list
             if (canUnfollow) {
                 viewModel.unfollow(getSharedPreferences("ugc_prefs", MODE_PRIVATE).getLong("user_id", -1), user.user_id);
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
            viewModel.getFollowingList(userId).observe(this, users -> adapter.setUsers(users));
        } else {
            viewModel.getFollowerList(userId).observe(this, users -> adapter.setUsers(users));
        }
    }
}