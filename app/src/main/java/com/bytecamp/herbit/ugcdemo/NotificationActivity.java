package com.bytecamp.herbit.ugcdemo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bytecamp.herbit.ugcdemo.data.entity.Notification;
import com.bytecamp.herbit.ugcdemo.ui.NotificationAdapter;
import com.bytecamp.herbit.ugcdemo.viewmodel.NotificationViewModel;
import com.bytecamp.herbit.ugcdemo.util.ThemeUtils;

public class NotificationActivity extends AppCompatActivity {

    public static final String EXTRA_TYPE = "extra_type";
    private int type;
    private NotificationViewModel viewModel;
    private NotificationAdapter adapter;
    private ImageView ivBack;
    private ImageView ivMarkRead;
    private TextView tvTitle;

    public static void start(Context context, int type) {
        Intent intent = new Intent(context, NotificationActivity.class);
        intent.putExtra(EXTRA_TYPE, type);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_list); // Need to create this layout

        type = getIntent().getIntExtra(EXTRA_TYPE, Notification.TYPE_MENTION);

        initViews();
        setupViewModel();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(v -> finish());
        
        ivMarkRead = findViewById(R.id.ivMarkRead);
        ivMarkRead.setOnClickListener(v -> {
            viewModel.markAllRead(type);
        });
        
        tvTitle = findViewById(R.id.tvTitle);
        switch (type) {
            case Notification.TYPE_MENTION: tvTitle.setText("at我的"); break;
            case Notification.TYPE_REPLY: tvTitle.setText("新的回复"); break;
            case Notification.TYPE_LIKE: tvTitle.setText("新的点赞"); break;
            case Notification.TYPE_FOLLOW: tvTitle.setText("新的关注"); break;
        }

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter();
        adapter.setNotificationType(type);
        adapter.setOnItemClickListener(item -> {
            viewModel.markAsRead(item.notification.id);
            if (item.notification.type == Notification.TYPE_FOLLOW) {
                if (item.sourceUser != null) {
                    UserProfileActivity.start(this, item.sourceUser.user_id);
                }
            } else if (item.notification.related_id > 0) {
                // related_id stores post_id for Like, Reply, Mention (as implemented in DetailViewModel)
                Intent intent = new Intent(this, DetailActivity.class);
                intent.putExtra(DetailActivity.EXTRA_POST_ID, item.notification.related_id);
                startActivity(intent);
            }
        });
        adapter.setOnFollowClickListener(item -> {
            if (item.sourceUser != null) {
                viewModel.toggleFollow(item.sourceUser.user_id);
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(NotificationViewModel.class);
        viewModel.getNotifications(type).observe(this, list -> {
            adapter.setItems(list);
        });
        
        if (type == Notification.TYPE_FOLLOW) {
            viewModel.getFollowingIds().observe(this, ids -> {
                java.util.Set<Long> set;
                if (ids == null) set = new java.util.HashSet<>();
                else set = new java.util.HashSet<>(ids);
                adapter.setFollowingIds(set);
            });
            viewModel.getFollowerIds().observe(this, ids -> {
                java.util.Set<Long> set;
                if (ids == null) set = new java.util.HashSet<>();
                else set = new java.util.HashSet<>(ids);
                adapter.setFollowerIds(set);
            });
        }
    }
}
