package com.bytecamp.herbit.ugcdemo.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import com.bytecamp.herbit.ugcdemo.data.AppDatabase;
import com.bytecamp.herbit.ugcdemo.data.dao.FollowDao;
import com.bytecamp.herbit.ugcdemo.data.dao.PostDao;
import com.bytecamp.herbit.ugcdemo.data.dao.UserDao;
import com.bytecamp.herbit.ugcdemo.data.entity.User;
import com.bytecamp.herbit.ugcdemo.data.model.PostCardItem;
import java.util.List;

public class ProfileViewModel extends AndroidViewModel {
    private UserDao userDao;
    private PostDao postDao;
    private FollowDao followDao;
    
    private MutableLiveData<Integer> currentTab = new MutableLiveData<>(0);
    private long userId = -1;
    private MutableLiveData<java.util.List<com.bytecamp.herbit.ugcdemo.data.model.PostCardItem>> posts = new MutableLiveData<>();
    private int pageSize = 20;
    private int offsetMy = 0;
    private int offsetLiked = 0;
    private boolean hasMoreMy = true;
    private boolean hasMoreLiked = true;
    private boolean loading = false;

    public ProfileViewModel(Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        userDao = db.userDao();
        postDao = db.postDao();
        followDao = db.followDao();
        currentTab.observeForever(tab -> refresh());
    }

    public LiveData<User> getUser(long userId) {
        return userDao.getUserById(userId);
    }

    public LiveData<java.util.List<com.bytecamp.herbit.ugcdemo.data.model.PostCardItem>> getPosts() {
        return posts;
    }
    
    public void setUserId(long userId) {
        this.userId = userId;
        refresh();
    }
    
    public void setTab(int tab) {
        currentTab.setValue(tab);
    }
    
    public int getCurrentTab() {
        Integer v = currentTab.getValue();
        return v == null ? 0 : v;
    }
    
    public void refresh() {
        if (userId == -1) return;
        if (getCurrentTab() == 1) {
            offsetLiked = 0;
            hasMoreLiked = true;
        } else {
            offsetMy = 0;
            hasMoreMy = true;
        }
        posts.postValue(new java.util.ArrayList<>());
        loadMore();
    }
    
    public void loadMore() {
        if (loading) return;
        if (userId == -1) return;
        int tab = getCurrentTab();
        boolean hasMore = tab == 1 ? hasMoreLiked : hasMoreMy;
        if (!hasMore) return;
        loading = true;
        AppDatabase.databaseWriteExecutor.execute(() -> {
            java.util.List<com.bytecamp.herbit.ugcdemo.data.model.PostCardItem> page;
            if (tab == 1) {
                page = postDao.getLikedPostCardsPaged(userId, pageSize, offsetLiked);
            } else {
                page = postDao.getUserPostCardsPaged(userId, pageSize, offsetMy);
            }
            try { Thread.sleep(700); } catch (InterruptedException ignored) {}
            java.util.List<com.bytecamp.herbit.ugcdemo.data.model.PostCardItem> current = posts.getValue();
            if (current == null) current = new java.util.ArrayList<>();
            java.util.ArrayList<com.bytecamp.herbit.ugcdemo.data.model.PostCardItem> merged = new java.util.ArrayList<>(current);
            merged.addAll(page);
            if (tab == 1) {
                offsetLiked += page.size();
                hasMoreLiked = page.size() == pageSize;
            } else {
                offsetMy += page.size();
                hasMoreMy = page.size() == pageSize;
            }
            loading = false;
            posts.postValue(merged);
        });
    }
    
    public boolean hasMore() {
        return getCurrentTab() == 1 ? hasMoreLiked : hasMoreMy;
    }
    
    public boolean isLoading() {
        return loading;
    }
    
    public LiveData<Integer> getFollowingCount(long userId) {
        return followDao.getFollowingCount(userId);
    }

    public LiveData<Integer> getFollowerCount(long userId) {
        return followDao.getFollowerCount(userId);
    }

    public void updateUsername(long userId, String newUsername) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            userDao.updateUsername(userId, newUsername);
        });
    }

    public void updateAvatar(long userId, String path) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            userDao.updateAvatar(userId, path);
        });
    }

    public void verifyAndUpdatePassword(long userId, String oldPassword, String newPassword, Runnable onSuccess, Runnable onError) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            User user = userDao.getUserByIdSync(userId);
            if (user != null && user.password.equals(oldPassword)) {
                userDao.updatePassword(userId, newPassword);
                if (onSuccess != null) onSuccess.run();
            } else {
                if (onError != null) onError.run();
            }
        });
    }
    
    public void deleteUser(long userId, Runnable onComplete) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            userDao.deleteUser(userId);
            if (onComplete != null) {
                onComplete.run();
            }
        });
    }
}
