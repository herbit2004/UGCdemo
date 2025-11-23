package com.bytecamp.herbit.ugcdemo.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.bytecamp.herbit.ugcdemo.data.AppDatabase;
import com.bytecamp.herbit.ugcdemo.data.dao.PostDao;
import com.bytecamp.herbit.ugcdemo.data.model.PostCardItem;
import java.util.List;

public class SearchViewModel extends AndroidViewModel {
    private PostDao postDao;
    private MutableLiveData<String> currentQuery = new MutableLiveData<>("");
    private MutableLiveData<Integer> currentSort = new MutableLiveData<>(0); // 0: Recent, 1: Popular, 2: Recent Comment
    private MutableLiveData<List<PostCardItem>> searchResults = new MutableLiveData<>();
    private int pageSize = 20;
    private int offset = 0;
    private boolean hasMore = false;
    private boolean loading = false;

    public SearchViewModel(Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        postDao = db.postDao();
        
        currentQuery.observeForever(q -> refresh());
        currentSort.observeForever(s -> refresh());
    }

    public LiveData<List<PostCardItem>> getSearchResults() {
        return searchResults;
    }

    public void search(String query) {
        currentQuery.setValue(query);
    }

    public void setSort(int sort) {
        currentSort.setValue(sort);
    }

    public int getCurrentSort() {
        Integer v = currentSort.getValue();
        return v != null ? v : 0;
    }

    public void refresh() {
        String q = currentQuery.getValue();
        if (q == null || q.trim().isEmpty()) {
            searchResults.postValue(new java.util.ArrayList<>());
            hasMore = false;
            return;
        }
        offset = 0;
        hasMore = true;
        searchResults.postValue(new java.util.ArrayList<>());
        loadMore();
    }

    public void loadMore() {
        String q = currentQuery.getValue();
        if (loading || !hasMore || q == null || q.trim().isEmpty()) return;
        loading = true;
        AppDatabase.databaseWriteExecutor.execute(() -> {
            int sort = getCurrentSort();
            List<PostCardItem> page;
            switch (sort) {
                case 1:
                    page = postDao.searchPostCardsPopularPaged(q, pageSize, offset);
                    break;
                case 2:
                    page = postDao.searchPostCardsRecentCommentPaged(q, pageSize, offset);
                    break;
                case 0:
                default:
                    page = postDao.searchPostCardsPaged(q, pageSize, offset);
                    break;
            }
            try { Thread.sleep(700); } catch (InterruptedException ignored) {}
            List<PostCardItem> current = searchResults.getValue();
            if (current == null) current = new java.util.ArrayList<>();
            java.util.ArrayList<PostCardItem> merged = new java.util.ArrayList<>(current);
            merged.addAll(page);
            offset += page.size();
            hasMore = page.size() == pageSize;
            loading = false;
            searchResults.postValue(merged);
        });
    }

    public boolean hasMore() {
        return hasMore;
    }
}
