package com.bytecamp.herbit.ugcdemo;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.util.TypedValue;
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import com.bytecamp.herbit.ugcdemo.ui.PostsAdapter;
import com.bytecamp.herbit.ugcdemo.ui.UserListAdapter;
import com.bytecamp.herbit.ugcdemo.viewmodel.SearchViewModel;
import com.bytecamp.herbit.ugcdemo.util.ThemeUtils;

public class SearchActivity extends AppCompatActivity {

    private SearchViewModel viewModel;
    private EditText etSearch;
    private ImageView ivSort;
    private ImageView ivBack;
    private RecyclerView recyclerView;
    private androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeRefresh;
    private PostsAdapter postsAdapter;
    private UserListAdapter userAdapter;
    private long currentUserId;
    
    private TextView tvTypeNote;
    private TextView tvTypeUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initViews();
        setupViewModel();
        
        // Set initial tab color correctly
        updateTypeUI(0);
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(v -> finish());

        etSearch = findViewById(R.id.etSearch);
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                performSearch();
                return true;
            }
            return false;
        });

        ivSort = findViewById(R.id.ivSort);
        ivSort.setOnClickListener(this::showSortMenu);

        tvTypeNote = findViewById(R.id.tvTypeNote);
        tvTypeUser = findViewById(R.id.tvTypeUser);
        
        tvTypeNote.setOnClickListener(v -> {
            if (viewModel.getSearchType() != 0) {
                viewModel.setSearchType(0);
                updateTypeUI(0);
            }
        });
        tvTypeUser.setOnClickListener(v -> {
            if (viewModel.getSearchType() != 1) {
                viewModel.setSearchType(1);
                updateTypeUI(1);
            }
        });

        swipeRefresh = findViewById(R.id.swipeRefresh);
        recyclerView = findViewById(R.id.recyclerView);
        
        postsAdapter = new PostsAdapter();
        postsAdapter.setEmptyStateText("未找到结果，试试换个关键词");
        
        android.content.SharedPreferences prefs = getSharedPreferences("ugc_prefs", MODE_PRIVATE);
        currentUserId = prefs.getLong("user_id", -1);
        userAdapter = new UserListAdapter(currentUserId, (user, isFollowing) -> {
            viewModel.toggleFollow(currentUserId, user.user_id);
        });
        userAdapter.setOnItemClickListener(user -> {
            com.bytecamp.herbit.ugcdemo.UserProfileActivity.start(this, user.user_id);
        });
        
        // Default to Note
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        recyclerView.setAdapter(postsAdapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(SearchViewModel.class);
        
        viewModel.getSearchResults().observe(this, posts -> {
            if (viewModel.getSearchType() == 0) {
                postsAdapter.setPosts(posts);
                postsAdapter.setLoading(false);
                postsAdapter.setHasMore(viewModel.hasMore());
            }
        });
        
        viewModel.getUserSearchResults().observe(this, users -> {
            if (viewModel.getSearchType() == 1) {
                userAdapter.setLoading(false);
                userAdapter.setUsers(users);
                // Assuming User Search doesn't support pagination for now or loads all at once.
                // We can hide footer if list is populated, or show "No more" if we know it's end.
                // Since we don't have hasMore logic for users in ViewModel fully exposed, let's assume end.
                userAdapter.setHasMore(false);
            }
        });

        viewModel.getFollowingIds(currentUserId).observe(this, ids -> {
            java.util.Set<Long> set;
            if (ids == null) {
                set = new java.util.HashSet<>();
            } else {
                set = new java.util.HashSet<>(ids);
            }
            userAdapter.setFollowingIds(set);
        });

        viewModel.getFollowerIds(currentUserId).observe(this, ids -> {
            java.util.Set<Long> set;
            if (ids == null) {
                set = new java.util.HashSet<>();
            } else {
                set = new java.util.HashSet<>(ids);
            }
            userAdapter.setFollowerIds(set);
        });

        swipeRefresh.setOnRefreshListener(() -> {
            swipeRefresh.setRefreshing(false);
            viewModel.refresh();
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener(){
            private long lastLoadTime = 0L;
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                if (viewModel.getSearchType() == 0) {
                    if (dy > 0) {
                        StaggeredGridLayoutManager lm = (StaggeredGridLayoutManager) rv.getLayoutManager();
                        if (lm == null) return;
                        int[] last = new int[2];
                        lm.findLastVisibleItemPositions(last);
                        int lastPos = Math.max(last[0], last[1]);
                        RecyclerView.Adapter a = rv.getAdapter();
                        if (a != null && lastPos >= Math.max(0, a.getItemCount() - 3)) {
                            long now = System.currentTimeMillis();
                            if (viewModel.hasMore() && now - lastLoadTime > 700) {
                                postsAdapter.setLoading(true);
                                viewModel.loadMore();
                                lastLoadTime = now;
                            }
                        }
                    }
                }
            }
        });
    }
    
    private void updateTypeUI(int type) {
        int primary = getAttrColor(com.google.android.material.R.attr.colorSecondary); 
        if (primary == 0) primary = 0xFF6200EE; // Fallback purple
        int gray = 0xFF888888; 
        
        if (type == 0) {
            tvTypeNote.setTextColor(primary);
            tvTypeNote.setTypeface(null, android.graphics.Typeface.BOLD);
            tvTypeUser.setTextColor(gray);
            tvTypeUser.setTypeface(null, android.graphics.Typeface.NORMAL);
            
            StaggeredGridLayoutManager lm = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
            lm.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
            recyclerView.setLayoutManager(lm);
            recyclerView.setAdapter(postsAdapter);
        } else {
            tvTypeUser.setTextColor(primary);
            tvTypeUser.setTypeface(null, android.graphics.Typeface.BOLD);
            tvTypeNote.setTextColor(gray);
            tvTypeNote.setTypeface(null, android.graphics.Typeface.NORMAL);
            
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(userAdapter);
            // Ensure loading state is reset or correct
            userAdapter.setLoading(false);
        }
        // Force refresh REMOVED - viewModel.setSearchType triggers refresh now
        // viewModel.refresh(); 
    }

    private void performSearch() {
        String query = etSearch.getText().toString().trim();
        if (!TextUtils.isEmpty(query)) {
            viewModel.search(query);
            // viewModel.refresh(); // REMOVED - search() triggers refresh
        }
    }

    private void showSortMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        int currentSort = viewModel.getCurrentSort();
        int secondary = getAttrColor(com.google.android.material.R.attr.colorSecondary);

        android.text.SpannableString sRecent = new android.text.SpannableString("最近发表");
        android.text.SpannableString sPopular = new android.text.SpannableString("最多喜欢");
        android.text.SpannableString sComment = new android.text.SpannableString("最近评论");

        if (currentSort == 0) sRecent.setSpan(new android.text.style.ForegroundColorSpan(secondary), 0, sRecent.length(), android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        else if (currentSort == 1) sPopular.setSpan(new android.text.style.ForegroundColorSpan(secondary), 0, sPopular.length(), android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        else if (currentSort == 2) sComment.setSpan(new android.text.style.ForegroundColorSpan(secondary), 0, sComment.length(), android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        android.view.Menu menu = popup.getMenu();
        android.view.MenuItem itemRecent = menu.add(0, 0, 0, sRecent);
        android.view.MenuItem itemPopular = menu.add(0, 1, 1, sPopular);
        android.view.MenuItem itemComment = menu.add(0, 2, 2, sComment);
        
        popup.setOnMenuItemClickListener(item -> {
            viewModel.setSort(item.getItemId());
            return true;
        });
        popup.show();
    }

    private int getAttrColor(int attr) {
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(attr, typedValue, true);
        return typedValue.data;
    }
}
