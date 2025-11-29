package com.bytecamp.herbit.ugcdemo;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.bytecamp.herbit.ugcdemo.ui.HomeFragment;
import com.bytecamp.herbit.ugcdemo.ui.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.bytecamp.herbit.ugcdemo.util.ThemeUtils;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import android.view.View;
import android.widget.TextView;
import com.bytecamp.herbit.ugcdemo.data.entity.Notification;
import com.bytecamp.herbit.ugcdemo.viewmodel.NotificationViewModel;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NotificationViewModel notificationViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        drawerLayout = findViewById(R.id.drawer_layout);
        setupDrawer();

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
    }
    
    public void openDrawer() {
        if (drawerLayout != null) {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }
    
    private void setupDrawer() {
        notificationViewModel = new ViewModelProvider(this).get(NotificationViewModel.class);
        
        View drawer = findViewById(R.id.notification_drawer);
        if (drawer == null) return;
        
        drawer.findViewById(R.id.itemMention).setOnClickListener(v -> {
            NotificationActivity.start(this, Notification.TYPE_MENTION);
            drawerLayout.closeDrawer(GravityCompat.START);
        });
        drawer.findViewById(R.id.itemReply).setOnClickListener(v -> {
            NotificationActivity.start(this, Notification.TYPE_REPLY);
            drawerLayout.closeDrawer(GravityCompat.START);
        });
        drawer.findViewById(R.id.itemLike).setOnClickListener(v -> {
            NotificationActivity.start(this, Notification.TYPE_LIKE);
            drawerLayout.closeDrawer(GravityCompat.START);
        });
        drawer.findViewById(R.id.itemFollow).setOnClickListener(v -> {
            NotificationActivity.start(this, Notification.TYPE_FOLLOW);
            drawerLayout.closeDrawer(GravityCompat.START);
        });
        
        notificationViewModel.getUnreadCount(Notification.TYPE_MENTION).observe(this, count -> {
            updateBadge(drawer, R.id.tvCountMention, count);
        });
        notificationViewModel.getUnreadCount(Notification.TYPE_REPLY).observe(this, count -> {
            updateBadge(drawer, R.id.tvCountReply, count);
        });
        notificationViewModel.getUnreadCount(Notification.TYPE_LIKE).observe(this, count -> {
            updateBadge(drawer, R.id.tvCountLike, count);
        });
        notificationViewModel.getUnreadCount(Notification.TYPE_FOLLOW).observe(this, count -> {
            updateBadge(drawer, R.id.tvCountFollow, count);
        });
    }
    
    private void updateBadge(View root, int id, Integer count) {
        TextView tv = root.findViewById(id);
        if (tv != null) {
            if (count != null && count > 0) {
                tv.setVisibility(View.VISIBLE);
                tv.setText(String.valueOf(count));
            } else {
                tv.setVisibility(View.GONE);
            }
        }
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
