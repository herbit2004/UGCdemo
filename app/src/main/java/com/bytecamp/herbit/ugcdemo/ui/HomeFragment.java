package com.bytecamp.herbit.ugcdemo.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.bytecamp.herbit.ugcdemo.R;
import com.bytecamp.herbit.ugcdemo.SearchActivity;
import com.bytecamp.herbit.ugcdemo.viewmodel.HomeViewModel;

/**
 * HomeFragment
 * 首页 Fragment。
 */
public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private PostsAdapter adapter;
    
    private TextView tvTabHome, tvTabFollow;
    private ImageView ivSort, ivSearch;
    private PopupMenu sortPopup;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        
        initViews(root);
        setupViewModel();
        
        return root;
    }

    private void initViews(View root) {
        recyclerView = root.findViewById(R.id.recyclerView);
        swipeRefreshLayout = root.findViewById(R.id.swipeRefresh);
        
        tvTabHome = root.findViewById(R.id.tvTabHome);
        tvTabFollow = root.findViewById(R.id.tvTabFollow);
        ivSort = root.findViewById(R.id.ivSort);
        ivSearch = root.findViewById(R.id.ivSearch);
        
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        recyclerView.setLayoutManager(layoutManager);
        
        adapter = new PostsAdapter();
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(() -> {
             swipeRefreshLayout.setRefreshing(false);
        });
        
        tvTabHome.setOnClickListener(v -> homeViewModel.setTab(0));
        tvTabFollow.setOnClickListener(v -> homeViewModel.setTab(1));
        
        ivSort.setOnClickListener(this::showSortMenu);
        
        ivSearch.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), SearchActivity.class));
        });
    }

    private void setupViewModel() {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        
        homeViewModel.getPosts().observe(getViewLifecycleOwner(), posts -> {
            adapter.setPosts(posts);
            swipeRefreshLayout.setRefreshing(false);
        });
        
        // Observe Tab changes to update UI
        homeViewModel.getCurrentTabLiveData().observe(getViewLifecycleOwner(), this::updateTabUI);
        
        // Observe Sort changes to update UI high light
        homeViewModel.getCurrentSortLiveData().observe(getViewLifecycleOwner(), sort -> {
            if (sortPopup != null) {
                // We can't easily update popup menu items check state if it's not showing,
                // but we can update the icon tint to show active state if not default
                if (sort != 0) {
                    ivSort.setColorFilter(Color.parseColor("#FF2442")); // Active color
                } else {
                    ivSort.setColorFilter(Color.BLACK);
                }
            } else {
                if (sort != 0) {
                    ivSort.setColorFilter(Color.parseColor("#FF2442"));
                } else {
                    ivSort.setColorFilter(Color.BLACK);
                }
            }
        });
    }

    private void updateTabUI(int tabIndex) {
        if (tabIndex == 0) {
            tvTabHome.setTextColor(Color.BLACK);
            tvTabHome.setTextSize(18);
            tvTabFollow.setTextColor(Color.parseColor("#999999"));
            tvTabFollow.setTextSize(16);
        } else {
            tvTabHome.setTextColor(Color.parseColor("#999999"));
            tvTabHome.setTextSize(16);
            tvTabFollow.setTextColor(Color.BLACK);
            tvTabFollow.setTextSize(18);
        }
    }

    private void showSortMenu(View v) {
        sortPopup = new PopupMenu(getContext(), v);
        Menu menu = sortPopup.getMenu();
        
        // Add items with checkable behavior manually or just highlight the selected one by title
        MenuItem itemRecent = menu.add(0, 0, 0, "最近发表");
        MenuItem itemPopular = menu.add(0, 1, 1, "最多喜欢");
        MenuItem itemComment = menu.add(0, 2, 2, "最近评论");
        
        int currentSort = homeViewModel.getCurrentSort();
        
        // Mark current selection (simple way: add checkmark or change text color in custom view, but standard popup limited)
        // Here we just check standard checkable
        menu.setGroupCheckable(0, true, true);
        if (currentSort == 0) itemRecent.setChecked(true);
        else if (currentSort == 1) itemPopular.setChecked(true);
        else if (currentSort == 2) itemComment.setChecked(true);
        
        sortPopup.setOnMenuItemClickListener(item -> {
            homeViewModel.setSort(item.getItemId());
            return true;
        });
        sortPopup.show();
    }
}
