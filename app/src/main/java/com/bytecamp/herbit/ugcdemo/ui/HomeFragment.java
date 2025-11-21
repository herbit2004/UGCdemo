package com.bytecamp.herbit.ugcdemo.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.bytecamp.herbit.ugcdemo.R;
import com.bytecamp.herbit.ugcdemo.viewmodel.HomeViewModel;

/**
 * HomeFragment
 * 首页 Fragment。
 * 功能：
 * 1. 展示所有用户的帖子瀑布流。
 * 2. 支持下拉刷新（目前仅模拟 UI 效果，实际数据由 LiveData 自动更新）。
 */
public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private PostsAdapter adapter;

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
        
        // 使用瀑布流布局管理器，2列
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        // 防止 Item 乱跳
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        recyclerView.setLayoutManager(layoutManager);
        
        adapter = new PostsAdapter();
        recyclerView.setAdapter(adapter);

        // 下拉刷新监听
        swipeRefreshLayout.setOnRefreshListener(() -> {
             // 由于使用了 LiveData，数据变动会自动推送。
             // 这里仅模拟刷新结束动画，实际项目中可能触发网络请求。
             swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void setupViewModel() {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        
        // 观察所有帖子数据
        homeViewModel.getAllPosts().observe(getViewLifecycleOwner(), posts -> {
            adapter.setPosts(posts);
            swipeRefreshLayout.setRefreshing(false);
        });
    }
}