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
import com.bytecamp.herbit.ugcdemo.data.entity.Follow;
import com.bytecamp.herbit.ugcdemo.data.entity.User;
import com.bytecamp.herbit.ugcdemo.data.model.PostCardItem;
import java.util.List;

public class UserProfileViewModel extends AndroidViewModel {
    private UserDao userDao;
    private PostDao postDao;
    private FollowDao followDao;
    
    private MutableLiveData<Integer> currentTab = new MutableLiveData<>(0); // 0: Posts, 1: Liked

    public UserProfileViewModel(Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        userDao = db.userDao();
        postDao = db.postDao();
        followDao = db.followDao();
    }

    public LiveData<User> getUser(long userId) {
        return userDao.getUserById(userId);
    }

    public LiveData<List<PostCardItem>> getPosts(long userId) {
        return Transformations.switchMap(currentTab, tab -> {
            if (tab == 1) {
                return postDao.getLikedPostCards(userId);
            } else {
                return postDao.getUserPostCards(userId);
            }
        });
    }
    
    public void setTab(int tab) {
        currentTab.setValue(tab);
    }
    
    public int getCurrentTab() {
        return currentTab.getValue() != null ? currentTab.getValue() : 0;
    }
    
    public LiveData<Integer> getFollowingCount(long userId) {
        return followDao.getFollowingCount(userId);
    }

    public LiveData<Integer> getFollowerCount(long userId) {
        return followDao.getFollowerCount(userId);
    }
    
    public LiveData<Integer> isFollowing(long followerId, long followeeId) {
        return followDao.isFollowing(followerId, followeeId);
    }

    public void toggleFollow(long followerId, long followeeId, boolean isFollowing) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            if (isFollowing) {
                followDao.deleteFollow(followerId, followeeId);
            } else {
                followDao.insertFollow(new Follow(followerId, followeeId, System.currentTimeMillis()));
            }
        });
    }
}
