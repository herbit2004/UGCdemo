package com.bytecamp.herbit.ugcdemo.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.bytecamp.herbit.ugcdemo.data.AppDatabase;
import com.bytecamp.herbit.ugcdemo.data.dao.FollowDao;
import com.bytecamp.herbit.ugcdemo.data.dao.PostDao;
import com.bytecamp.herbit.ugcdemo.data.dao.UserDao;
import com.bytecamp.herbit.ugcdemo.data.dao.UserDao;
import com.bytecamp.herbit.ugcdemo.data.entity.User;
import com.bytecamp.herbit.ugcdemo.data.model.PostCardItem;
import com.bytecamp.herbit.ugcdemo.data.repository.FollowRepository;
import java.util.List;

public class UserProfileViewModel extends AndroidViewModel {
    private UserDao userDao;
    private PostDao postDao;
    private FollowRepository followRepository;
    
    private MutableLiveData<Integer> currentTab = new MutableLiveData<>(0); // 0: Posts, 1: Liked
    private MutableLiveData<List<PostCardItem>> posts = new MutableLiveData<>();
    private int pageSize = 20;
    private int offset = 0;
    private boolean hasMore = true;
    private boolean loading = false;
    private long profileUserId = -1;

    public UserProfileViewModel(Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        userDao = db.userDao();
        postDao = db.postDao();
        followRepository = new FollowRepository(application);
    }

    public LiveData<User> getUser(long userId) {
        return userDao.getUserById(userId);
    }

    public LiveData<List<PostCardItem>> getPosts() {
        return posts;
    }
    
    public void init(long userId) {
        this.profileUserId = userId;
        refresh();
    }
    
    public void refresh() {
        if (profileUserId == -1) return;
        offset = 0;
        hasMore = true;
        posts.postValue(new java.util.ArrayList<>());
        loadMore();
    }
    
    public void loadMore() {
        if (loading || !hasMore || profileUserId == -1) return;
        loading = true;
        AppDatabase.databaseWriteExecutor.execute(() -> {
            int tab = getCurrentTab();
            List<PostCardItem> page;
            if (tab == 1) {
                page = postDao.getLikedPostCardsPaged(profileUserId, pageSize, offset);
            } else {
                page = postDao.getUserPostCardsPaged(profileUserId, pageSize, offset);
            }
            try { Thread.sleep(700); } catch (InterruptedException ignored) {}
            List<PostCardItem> current = posts.getValue();
            if (current == null) current = new java.util.ArrayList<>();
            java.util.ArrayList<PostCardItem> merged = new java.util.ArrayList<>(current);
            merged.addAll(page);
            offset += page.size();
            hasMore = page.size() == pageSize;
            loading = false;
            posts.postValue(merged);
        });
    }
    
    public boolean hasMore() {
        return hasMore;
    }
    
    public void setTab(int tab) {
        currentTab.setValue(tab);
        refresh();
    }
    
    public int getCurrentTab() {
        return currentTab.getValue() != null ? currentTab.getValue() : 0;
    }
    
    public LiveData<Integer> getFollowingCount(long userId) {
        return followRepository.getFollowingCount(userId);
    }

    public LiveData<Integer> getFollowerCount(long userId) {
        return followRepository.getFollowerCount(userId);
    }
    
    public LiveData<Integer> isFollowing(long followerId, long followeeId) {
        return followRepository.isFollowing(followerId, followeeId);
    }

    public void toggleFollow(long followerId, long followeeId, boolean isFollowing) {
        followRepository.toggleFollow(followerId, followeeId);
    }
}
