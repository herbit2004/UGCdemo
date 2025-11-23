package com.bytecamp.herbit.ugcdemo.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import com.bytecamp.herbit.ugcdemo.data.AppDatabase;
import com.bytecamp.herbit.ugcdemo.data.dao.PostDao;
import com.bytecamp.herbit.ugcdemo.data.model.PostCardItem;
import java.util.List;
import android.content.Context;
import android.content.SharedPreferences;

public class HomeViewModel extends AndroidViewModel {
    private PostDao postDao;

    private MutableLiveData<Integer> currentTab = new MutableLiveData<>(0);
    private MutableLiveData<Integer> currentSort = new MutableLiveData<>(0);
    private long currentUserId = -1;

    private MutableLiveData<List<PostCardItem>> postsHome = new MutableLiveData<>();
    private MutableLiveData<List<PostCardItem>> postsFollow = new MutableLiveData<>();
    private int pageSize = 20;
    private int offsetHome = 0;
    private int offsetFollow = 0;
    private boolean hasMoreHome = true;
    private boolean hasMoreFollow = true;
    private boolean loadingHome = false;
    private boolean loadingFollow = false;

    public HomeViewModel(Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        postDao = db.postDao();
        loadCurrentUserId();

        currentSort.observeForever(sort -> refresh());
    }
    
    private void loadCurrentUserId() {
        SharedPreferences sp = getApplication().getSharedPreferences("ugc_prefs", Context.MODE_PRIVATE);
        currentUserId = sp.getLong("user_id", -1);
    }

    public LiveData<List<PostCardItem>> getHomePosts() { return postsHome; }
    public LiveData<List<PostCardItem>> getFollowPosts() { return postsFollow; }

    public void setTab(int tabIndex) {
        currentTab.setValue(tabIndex);
    }

    public void setSort(int sortIndex) {
        currentSort.setValue(sortIndex);
    }
    
    public int getCurrentTab() {
        return currentTab.getValue() != null ? currentTab.getValue() : 0;
    }

    public int getCurrentSort() {
        return currentSort.getValue() != null ? currentSort.getValue() : 0;
    }
    
    // Expose as LiveData for UI observation
    public LiveData<Integer> getCurrentTabLiveData() {
        return currentTab;
    }
    
    public LiveData<Integer> getCurrentSortLiveData() {
        return currentSort;
    }

    public void refresh() { refresh(getCurrentTab()); }
    public void refresh(int tab) {
        if (tab == 1) {
            offsetFollow = 0;
            hasMoreFollow = true;
            postsFollow.postValue(new java.util.ArrayList<>());
        } else {
            offsetHome = 0;
            hasMoreHome = true;
            postsHome.postValue(new java.util.ArrayList<>());
        }
        loadMore(tab);
    }

    public void loadMore() { loadMore(getCurrentTab()); }
    public void loadMore(int tab) {
        if (tab == 1) {
            if (loadingFollow || !hasMoreFollow) return;
            loadingFollow = true;
        } else {
            if (loadingHome || !hasMoreHome) return;
            loadingHome = true;
        }
        AppDatabase.databaseWriteExecutor.execute(() -> {
            int sort = getCurrentSort();
            List<PostCardItem> page;
            if (tab == 1) {
                int offset = offsetFollow;
                switch (sort) {
                    case 1: page = postDao.getFollowedPostCardsPopularPaged(currentUserId, pageSize, offset); break;
                    case 2: page = postDao.getFollowedPostCardsRecentCommentPaged(currentUserId, pageSize, offset); break;
                    case 0:
                    default: page = postDao.getFollowedPostCardsPaged(currentUserId, pageSize, offset); break;
                }
            } else {
                int offset = offsetHome;
                switch (sort) {
                    case 1: page = postDao.getAllPostCardsPopularPaged(pageSize, offset); break;
                    case 2: page = postDao.getAllPostCardsRecentCommentPaged(pageSize, offset); break;
                    case 0:
                    default: page = postDao.getAllPostCardsPaged(pageSize, offset); break;
                }
            }
            try { Thread.sleep(700); } catch (InterruptedException ignored) {}
            if (tab == 1) {
                List<PostCardItem> current = postsFollow.getValue();
                if (current == null) current = new java.util.ArrayList<>();
                java.util.ArrayList<PostCardItem> merged = new java.util.ArrayList<>(current);
                merged.addAll(page);
                offsetFollow += page.size();
                hasMoreFollow = page.size() == pageSize;
                loadingFollow = false;
                postsFollow.postValue(merged);
            } else {
                List<PostCardItem> current = postsHome.getValue();
                if (current == null) current = new java.util.ArrayList<>();
                java.util.ArrayList<PostCardItem> merged = new java.util.ArrayList<>(current);
                merged.addAll(page);
                offsetHome += page.size();
                hasMoreHome = page.size() == pageSize;
                loadingHome = false;
                postsHome.postValue(merged);
            }
        });
    }

    public boolean hasMore() { return hasMore(getCurrentTab()); }
    public boolean hasMore(int tab) { return tab == 1 ? hasMoreFollow : hasMoreHome; }
    public boolean isLoading() { return isLoading(getCurrentTab()); }
    public boolean isLoading(int tab) { return tab == 1 ? loadingFollow : loadingHome; }
}
