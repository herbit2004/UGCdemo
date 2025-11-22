package com.bytecamp.herbit.ugcdemo;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.bytecamp.herbit.ugcdemo.ui.HomeFragment;
import com.bytecamp.herbit.ugcdemo.ui.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
            } else if (itemId == R.id.navigation_publish) {
                startActivity(new Intent(this, PublishActivity.class));
                return false; // Don't select the item
            } else if (itemId == R.id.navigation_profile) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.nav_host_fragment, new ProfileFragment())
                        .commit();
                return true;
            }
            return false;
        });
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getBooleanExtra("open_profile", false)) {
            BottomNavigationView navView = findViewById(R.id.nav_view);
            navView.setSelectedItemId(R.id.navigation_profile);
        }
    }
}