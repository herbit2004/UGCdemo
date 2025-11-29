package com.bytecamp.herbit.ugcdemo.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.bytecamp.herbit.ugcdemo.data.AppDatabase;
import com.bytecamp.herbit.ugcdemo.data.dao.PostDao;
import com.bytecamp.herbit.ugcdemo.data.model.PostCardItem;
import java.util.List;

import com.bytecamp.herbit.ugcdemo.data.dao.UserDao;
import com.bytecamp.herbit.ugcdemo.data.entity.User;
import com.bytecamp.herbit.ugcdemo.data.repository.FollowRepository;

public class SearchViewModel extends AndroidViewModel {
    private PostDao postDao;
    private UserDao userDao;
    private FollowRepository followRepository;
    private MutableLiveData<String> currentQuery = new MutableLiveData<>("");
    private MutableLiveData<Integer> currentSort = new MutableLiveData<>(0); // 0: Recent, 1: Popular, 2: Recent Comment
    private MutableLiveData<Integer> searchType = new MutableLiveData<>(0); // 0: Note, 1: User
    private MutableLiveData<List<PostCardItem>> searchResults = new MutableLiveData<>();
    private LiveData<List<User>> userSearchResults;
    private int pageSize = 20;
    private int offset = 0;
    private boolean hasMore = false;
    private boolean loading = false;

    public SearchViewModel(Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        postDao = db.postDao();
        userDao = db.userDao();
        followRepository = new FollowRepository(application);
        
        // currentQuery.observeForever(q -> refresh()); // REMOVED to prevent infinite loop
        // currentSort.observeForever(s -> refresh()); // REMOVED to prevent infinite loop
        // searchType.observeForever(t -> refresh()); // REMOVED to prevent infinite loop
        
        // Use switchMap or similar if we want reactive search, but here we trigger manually via refresh() for posts
        // For users, we can use Transformation.switchMap
        userSearchResults = androidx.lifecycle.Transformations.switchMap(currentQuery, query -> {
             if (query == null || query.trim().isEmpty()) {
                 MutableLiveData<List<User>> empty = new MutableLiveData<>();
                 empty.setValue(new java.util.ArrayList<>());
                 return empty;
             }
             return userDao.searchUsers(query);
        });
    }

    public LiveData<List<PostCardItem>> getSearchResults() {
        return searchResults;
    }

    public LiveData<List<User>> getUserSearchResults() {
        return userSearchResults;
    }

    public void setSearchType(int type) {
        if (searchType.getValue() == null || searchType.getValue() != type) {
            searchType.setValue(type);
            refresh();
        }
    }

    public int getSearchType() {
        Integer t = searchType.getValue();
        return t != null ? t : 0;
    }

    public void search(String query) {
        if (currentQuery.getValue() == null || !currentQuery.getValue().equals(query)) {
            currentQuery.setValue(query);
            refresh();
        }
    }

    public void setSort(int sort) {
        if (currentSort.getValue() == null || currentSort.getValue() != sort) {
            currentSort.setValue(sort);
            refresh();
        }
    }

    public int getCurrentSort() {
        Integer v = currentSort.getValue();
        return v != null ? v : 0;
    }

    public void refresh() {
        String q = currentQuery.getValue();
        
        if (getSearchType() == 1) {
            // User search: Force refresh by re-setting currentQuery
            // triggers switchMap which handles empty query returning empty list
            currentQuery.setValue(q);
        } else {
            // Post search
            if (q == null || q.trim().isEmpty()) {
                searchResults.postValue(new java.util.ArrayList<>());
                hasMore = false;
            } else {
                offset = 0;
                hasMore = true;
                searchResults.postValue(new java.util.ArrayList<>());
                loadMore();
            }
        }
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

    public void toggleFollow(long followerId, long followeeId) {
        followRepository.toggleFollow(followerId, followeeId);
    }

    public LiveData<List<Long>> getFollowingIds(long userId) {
        return followRepository.getFollowingIds(userId);
    }

    public LiveData<List<Long>> getFollowerIds(long userId) {
        return followRepository.getFollowerIds(userId);
    }
}
