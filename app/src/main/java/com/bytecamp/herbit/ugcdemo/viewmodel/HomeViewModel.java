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

    // 0: All, 1: Follow
    private MutableLiveData<Integer> currentTab = new MutableLiveData<>(0);
    
    // 0: Recent Publish, 1: Popular, 2: Recent Comment
    private MutableLiveData<Integer> currentSort = new MutableLiveData<>(0);
    
    private long currentUserId = -1;

    private LiveData<List<PostCardItem>> posts;

    public HomeViewModel(Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        postDao = db.postDao();
        loadCurrentUserId();

        // Combine Tab and Sort changes to refresh posts
        CombinedFilter filter = new CombinedFilter(0, 0);
        MutableLiveData<CombinedFilter> filterLiveData = new MutableLiveData<>(filter);

        // Helper to update filter
        currentTab.observeForever(tab -> {
            CombinedFilter f = filterLiveData.getValue();
            if (f != null) {
                f.tab = tab;
                filterLiveData.setValue(f);
            }
        });

        currentSort.observeForever(sort -> {
            CombinedFilter f = filterLiveData.getValue();
            if (f != null) {
                f.sort = sort;
                filterLiveData.setValue(f);
            }
        });

        posts = Transformations.switchMap(filterLiveData, input -> {
            if (input.tab == 1) {
                // Follow tab - only show followed user's posts
                // Sorting for followed posts
                switch (input.sort) {
                    case 1: return postDao.getFollowedPostCardsPopular(currentUserId);
                    case 2: return postDao.getFollowedPostCardsRecentComment(currentUserId);
                    case 0:
                    default: return postDao.getFollowedPostCards(currentUserId);
                }
            } else {
                // All posts tab
                switch (input.sort) {
                    case 1: return postDao.getAllPostCardsPopular();
                    case 2: return postDao.getAllPostCardsRecentComment();
                    case 0:
                    default: return postDao.getAllPostCards();
                }
            }
        });
    }
    
    private void loadCurrentUserId() {
        SharedPreferences sp = getApplication().getSharedPreferences("ugc_prefs", Context.MODE_PRIVATE);
        currentUserId = sp.getLong("user_id", -1);
    }

    public LiveData<List<PostCardItem>> getPosts() {
        return posts;
    }

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

    private static class CombinedFilter {
        int tab;
        int sort;
        CombinedFilter(int tab, int sort) {
            this.tab = tab;
            this.sort = sort;
        }
    }
}
