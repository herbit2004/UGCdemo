package com.bytecamp.herbit.ugcdemo;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.util.TypedValue;
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import com.bytecamp.herbit.ugcdemo.ui.PostsAdapter;
import com.bytecamp.herbit.ugcdemo.viewmodel.SearchViewModel;
import com.bytecamp.herbit.ugcdemo.util.ThemeUtils;

public class SearchActivity extends AppCompatActivity {

    private SearchViewModel viewModel;
    private EditText etSearch;
    private ImageView ivSort;
    private ImageView ivBack;
    private RecyclerView recyclerView;
    private androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeRefresh;
    private PostsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initViews();
        setupViewModel();
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

        swipeRefresh = findViewById(R.id.swipeRefresh);
        recyclerView = findViewById(R.id.recyclerView);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        recyclerView.setLayoutManager(layoutManager);
        
        adapter = new PostsAdapter();
        adapter.setEmptyStateText("未找到结果，试试换个关键词");
        recyclerView.setAdapter(adapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(SearchViewModel.class);
        
        viewModel.getSearchResults().observe(this, posts -> {
            adapter.setPosts(posts);
            adapter.setLoading(false);
            adapter.setHasMore(viewModel.hasMore());
        });

        swipeRefresh.setOnRefreshListener(() -> {
            swipeRefresh.setRefreshing(false);
            viewModel.refresh();
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener(){
            private long lastLoadTime = 0L;
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
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
                            adapter.setLoading(true);
                            viewModel.loadMore();
                            lastLoadTime = now;
                        }
                    }
                }
            }
        });
    }

    private void performSearch() {
        String query = etSearch.getText().toString().trim();
        if (!TextUtils.isEmpty(query)) {
            viewModel.search(query);
            viewModel.refresh();
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
