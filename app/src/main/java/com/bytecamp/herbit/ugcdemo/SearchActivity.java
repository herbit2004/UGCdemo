package com.bytecamp.herbit.ugcdemo;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import com.bytecamp.herbit.ugcdemo.ui.PostsAdapter;
import com.bytecamp.herbit.ugcdemo.viewmodel.SearchViewModel;

public class SearchActivity extends AppCompatActivity {

    private SearchViewModel viewModel;
    private EditText etSearch;
    private ImageView ivSort;
    private ImageView ivBack;
    private RecyclerView recyclerView;
    private PostsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        recyclerView = findViewById(R.id.recyclerView);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        recyclerView.setLayoutManager(layoutManager);
        
        adapter = new PostsAdapter();
        recyclerView.setAdapter(adapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(SearchViewModel.class);
        
        viewModel.getSearchResults().observe(this, posts -> {
            adapter.setPosts(posts);
        });
    }

    private void performSearch() {
        String query = etSearch.getText().toString().trim();
        if (!TextUtils.isEmpty(query)) {
            viewModel.search(query);
        }
    }

    private void showSortMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.getMenu().add(0, 0, 0, "最近发表");
        popup.getMenu().add(0, 1, 1, "最多喜欢");
        popup.getMenu().add(0, 2, 2, "最近评论");
        
        popup.setOnMenuItemClickListener(item -> {
            viewModel.setSort(item.getItemId());
            return true;
        });
        popup.show();
    }
}