package com.bytecamp.herbit.ugcdemo.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.bytecamp.herbit.ugcdemo.data.repository.FollowRepository;
import com.bytecamp.herbit.ugcdemo.data.entity.User;
import java.util.List;

public class FollowListViewModel extends AndroidViewModel {
    private FollowRepository followRepository;

    public FollowListViewModel(Application application) {
        super(application);
        followRepository = new FollowRepository(application);
    }

    public LiveData<List<User>> getFollowingList(long userId) {
        return followRepository.getFollowingList(userId);
    }

    public LiveData<List<User>> getFollowerList(long userId) {
        return followRepository.getFollowerList(userId);
    }
    
    public LiveData<List<Long>> getFollowingIds(long userId) {
        return followRepository.getFollowingIds(userId);
    }

    public LiveData<List<Long>> getFollowerIds(long userId) {
        return followRepository.getFollowerIds(userId);
    }
    
    public void unfollow(long followerId, long followeeId) {
        followRepository.toggleFollow(followerId, followeeId);
    }
    
    public void follow(long followerId, long followeeId) {
        followRepository.toggleFollow(followerId, followeeId);
    }
}
