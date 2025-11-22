package com.bytecamp.herbit.ugcdemo.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.bytecamp.herbit.ugcdemo.data.AppDatabase;
import com.bytecamp.herbit.ugcdemo.data.dao.FollowDao;
import com.bytecamp.herbit.ugcdemo.data.entity.User;
import java.util.List;

public class FollowListViewModel extends AndroidViewModel {
    private FollowDao followDao;

    public FollowListViewModel(Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        followDao = db.followDao();
    }

    public LiveData<List<User>> getFollowingList(long userId) {
        return followDao.getFollowingList(userId);
    }

    public LiveData<List<User>> getFollowerList(long userId) {
        return followDao.getFollowerList(userId);
    }
    
    public void unfollow(long followerId, long followeeId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            followDao.deleteFollow(followerId, followeeId);
        });
    }
}
