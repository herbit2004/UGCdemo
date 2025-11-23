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
import androidx.viewpager2.widget.ViewPager2;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.annotation.Nullable;
import com.bytecamp.herbit.ugcdemo.R;
import com.bytecamp.herbit.ugcdemo.SearchActivity;
import com.bytecamp.herbit.ugcdemo.viewmodel.HomeViewModel;
import android.util.TypedValue;

/**
 * HomeFragment
 * 首页 Fragment。
 */
public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private ViewPager2 viewPager;
    private PostsAdapter adapterHome;
    private PostsAdapter adapterFollow;
    
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
        viewPager = root.findViewById(R.id.viewPager);
        
        tvTabHome = root.findViewById(R.id.tvTabHome);
        tvTabFollow = root.findViewById(R.id.tvTabFollow);
        ivSort = root.findViewById(R.id.ivSort);
        ivSearch = root.findViewById(R.id.ivSearch);
        
        adapterHome = new PostsAdapter();
        adapterFollow = new PostsAdapter();

        viewPager.setAdapter(new PagerAdapter(this));
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback(){
            @Override
            public void onPageSelected(int position) {
                homeViewModel.setTab(position);
            }
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                int secondary = getAttrColor(requireContext(), com.google.android.material.R.attr.colorSecondary);
                int muted = Color.parseColor("#999999");
                float small = 16f, large = 18f;
                float homeScale = (position == 0) ? (1f - positionOffset) : 0f;
                float followScale = (position == 0) ? positionOffset : 1f;
                tvTabHome.setTextSize(small + (large - small) * homeScale);
                tvTabFollow.setTextSize(small + (large - small) * followScale);
                tvTabHome.setTextColor(blendColors(muted, secondary, homeScale));
                tvTabFollow.setTextColor(blendColors(muted, secondary, followScale));
            }
            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    updateTabUI(viewPager.getCurrentItem());
                }
            }
        });
        
        tvTabHome.setOnClickListener(v -> viewPager.setCurrentItem(0, true));
        tvTabFollow.setOnClickListener(v -> viewPager.setCurrentItem(1, true));
        
        ivSort.setOnClickListener(this::showSortMenu);
        
        ivSearch.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), SearchActivity.class));
        });
    }

    private void setupViewModel() {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        
        homeViewModel.getPosts().observe(getViewLifecycleOwner(), posts -> {
            int tab = homeViewModel.getCurrentTab();
            if (tab == 0) {
                adapterHome.setPosts(posts);
            } else {
                adapterFollow.setPosts(posts);
            }
        });
        
        // Remove UI updates from tab LiveData to avoid double animations
        
        // Observe Sort changes to update UI high light
        homeViewModel.getCurrentSortLiveData().observe(getViewLifecycleOwner(), sort -> {
            int activeColor = getAttrColor(requireContext(), com.google.android.material.R.attr.colorSecondary);
            int inactiveColor = Color.BLACK;
            ivSort.setColorFilter(sort != 0 ? activeColor : inactiveColor);
        });
    }

    private void updateTabUI(int tabIndex) {
        int secondary = getAttrColor(requireContext(), com.google.android.material.R.attr.colorSecondary);
        int muted = Color.parseColor("#999999");
        int strong = Color.BLACK;
        if (tabIndex == 0) {
            tvTabHome.setTextColor(secondary);
            tvTabHome.setTextSize(18);
            tvTabFollow.setTextColor(muted);
            tvTabFollow.setTextSize(16);
        } else {
            tvTabHome.setTextColor(muted);
            tvTabHome.setTextSize(16);
            tvTabFollow.setTextColor(secondary);
            tvTabFollow.setTextSize(18);
        }
    }

    private int getAttrColor(android.content.Context context, int attr) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attr, typedValue, true);
        return typedValue.data;
    }

    public PostsAdapter getAdapter(int position) {
        return position == 0 ? adapterHome : adapterFollow;
    }

    private int blendColors(int from, int to, float ratio) {
        int a = (int) ((Color.alpha(from) + (Color.alpha(to) - Color.alpha(from)) * ratio));
        int r = (int) ((Color.red(from) + (Color.red(to) - Color.red(from)) * ratio));
        int g = (int) ((Color.green(from) + (Color.green(to) - Color.green(from)) * ratio));
        int b = (int) ((Color.blue(from) + (Color.blue(to) - Color.blue(from)) * ratio));
        return Color.argb(a, r, g, b);
    }

    private void showSortMenu(View v) {
        sortPopup = new PopupMenu(getContext(), v);
        Menu menu = sortPopup.getMenu();

        MenuItem itemRecent = menu.add(0, 0, 0, "最近发表");
        MenuItem itemPopular = menu.add(0, 1, 1, "最多喜欢");
        MenuItem itemComment = menu.add(0, 2, 2, "最近评论");

        int currentSort = homeViewModel.getCurrentSort();
        int secondary = getAttrColor(requireContext(), com.google.android.material.R.attr.colorSecondary);

        android.text.SpannableString sRecent = new android.text.SpannableString("最近发表");
        android.text.SpannableString sPopular = new android.text.SpannableString("最多喜欢");
        android.text.SpannableString sComment = new android.text.SpannableString("最近评论");

        if (currentSort == 0) sRecent.setSpan(new android.text.style.ForegroundColorSpan(secondary), 0, sRecent.length(), android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        else if (currentSort == 1) sPopular.setSpan(new android.text.style.ForegroundColorSpan(secondary), 0, sPopular.length(), android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        else if (currentSort == 2) sComment.setSpan(new android.text.style.ForegroundColorSpan(secondary), 0, sComment.length(), android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        itemRecent.setTitle(sRecent);
        itemPopular.setTitle(sPopular);
        itemComment.setTitle(sComment);

        sortPopup.setOnMenuItemClickListener(item -> {
            homeViewModel.setSort(item.getItemId());
            return true;
        });
        sortPopup.show();
    }

    public static class RecyclerPageFragment extends Fragment {
        public RecyclerPageFragment() {}
        public static RecyclerPageFragment newInstance(int position) {
            RecyclerPageFragment f = new RecyclerPageFragment();
            Bundle b = new Bundle();
            b.putInt("position", position);
            f.setArguments(b);
            return f;
        }
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            int position = getArguments() != null ? getArguments().getInt("position", 0) : 0;
            RecyclerView rv = new RecyclerView(requireContext());
            StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
            layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
            rv.setLayoutManager(layoutManager);
            HomeFragment parent = (HomeFragment) getParentFragment();
            if (parent != null) {
                rv.setAdapter(parent.getAdapter(position));
            }
            rv.setClipToPadding(false);
            rv.setPadding(4,4,4,4);
            return rv;
        }
    }
}

class PagerAdapter extends FragmentStateAdapter {
    public PagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return HomeFragment.RecyclerPageFragment.newInstance(position);
    }
    @Override
    public int getItemCount() {
        return 2;
    }
}
