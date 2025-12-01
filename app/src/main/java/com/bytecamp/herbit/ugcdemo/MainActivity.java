package com.bytecamp.herbit.ugcdemo;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.bytecamp.herbit.ugcdemo.ui.HomeFragment;
import com.bytecamp.herbit.ugcdemo.ui.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.bytecamp.herbit.ugcdemo.util.ThemeUtils;

import com.bytecamp.herbit.ugcdemo.ui.widget.CustomPopupMenu;
import com.bytecamp.herbit.ugcdemo.data.entity.Notification;
import com.bytecamp.herbit.ugcdemo.viewmodel.NotificationViewModel;
import android.view.View;
import android.widget.TextView;
import androidx.lifecycle.ViewModelProvider;

public class MainActivity extends AppCompatActivity {

    private NotificationViewModel notificationViewModel;
    private CustomPopupMenu notificationMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        setupNotificationMenu();

        BottomNavigationView navView = findViewById(R.id.nav_view);
        
        // Load HomeFragment initially or Profile if requested
        if (savedInstanceState == null) {
            if (getIntent().getBooleanExtra("open_profile", false)) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.nav_host_fragment, new ProfileFragment())
                        .commit();
                navView.setSelectedItemId(R.id.navigation_profile);
            } else {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.nav_host_fragment, new HomeFragment())
                        .commit();
            }
        }

        navView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.nav_host_fragment, new HomeFragment())
                        .commit();
                return true;
            } else if (itemId == R.id.navigation_profile) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.nav_host_fragment, new ProfileFragment())
                        .commit();
                return true;
            }
            return false;
        });

        findViewById(R.id.btnPublishMain).setOnClickListener(v -> {
            int[] loc = new int[2];
            v.getLocationOnScreen(loc);
            int cx = loc[0] + v.getWidth() / 2;
            int cy = loc[1] + v.getHeight() / 2;
            Intent intent = new Intent(this, PublishActivity.class);
            intent.putExtra("reveal_cx", cx);
            intent.putExtra("reveal_cy", cy);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });
        
        // Add sink animation to Publish Button
        com.bytecamp.herbit.ugcdemo.util.AnimUtils.setScaleTouchListener(findViewById(R.id.btnPublishMain));

        // Add sink animation to Bottom Navigation Items
        // BottomNavigationView -> BottomNavigationMenuView -> BottomNavigationItemView
        for (int i = 0; i < navView.getChildCount(); i++) {
            View child = navView.getChildAt(i);
            if (child instanceof android.view.ViewGroup) {
                android.view.ViewGroup menuView = (android.view.ViewGroup) child;
                for (int j = 0; j < menuView.getChildCount(); j++) {
                    View itemView = menuView.getChildAt(j);
                    com.bytecamp.herbit.ugcdemo.util.AnimUtils.setScaleTouchListener(itemView);
                }
            }
        }
    }
    
    public void openNotificationMenu(View anchor) {
        if (notificationMenu != null) {
            notificationMenu.show(anchor, 0, 20); // Offset slightly down
        }
    }
    
    private void setupNotificationMenu() {
        notificationViewModel = new ViewModelProvider(this).get(NotificationViewModel.class);
        
        notificationMenu = new CustomPopupMenu(this);
        // Add items with IDs matching Notification types if possible, or custom IDs
        // Notification types: MENTION=1, REPLY=2, LIKE=3, FOLLOW=4
        notificationMenu.add(Notification.TYPE_MENTION, getString(R.string.label_mention));
        notificationMenu.add(Notification.TYPE_REPLY, getString(R.string.label_new_reply));
        notificationMenu.add(Notification.TYPE_LIKE, getString(R.string.label_new_like));
        notificationMenu.add(Notification.TYPE_FOLLOW, getString(R.string.label_new_follow));
        
        notificationMenu.setOnMenuItemClickListener(item -> {
            NotificationActivity.start(this, item.id);
        });
        
        notificationViewModel.getUnreadCount(Notification.TYPE_MENTION).observe(this, count -> {
            notificationMenu.updateBadge(Notification.TYPE_MENTION, count != null ? count : 0);
        });
        notificationViewModel.getUnreadCount(Notification.TYPE_REPLY).observe(this, count -> {
            notificationMenu.updateBadge(Notification.TYPE_REPLY, count != null ? count : 0);
        });
        notificationViewModel.getUnreadCount(Notification.TYPE_LIKE).observe(this, count -> {
            notificationMenu.updateBadge(Notification.TYPE_LIKE, count != null ? count : 0);
        });
        notificationViewModel.getUnreadCount(Notification.TYPE_FOLLOW).observe(this, count -> {
            notificationMenu.updateBadge(Notification.TYPE_FOLLOW, count != null ? count : 0);
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getBooleanExtra("open_profile", false)) {
            BottomNavigationView navView = findViewById(R.id.nav_view);
            navView.setSelectedItemId(R.id.navigation_profile);
        }
        if (intent.getBooleanExtra("restart_theme", false)) {
            recreate();
        }
    }
}
