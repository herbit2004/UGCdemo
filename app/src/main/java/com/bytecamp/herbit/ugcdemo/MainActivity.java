package com.bytecamp.herbit.ugcdemo;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.bytecamp.herbit.ugcdemo.ui.HomeFragment;
import com.bytecamp.herbit.ugcdemo.ui.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.bytecamp.herbit.ugcdemo.util.ThemeUtils;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        } else {
             // Handle potential intent when activity is recreated or singleTop/singleTask
             // But usually onCreate is for fresh start or rotation
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
