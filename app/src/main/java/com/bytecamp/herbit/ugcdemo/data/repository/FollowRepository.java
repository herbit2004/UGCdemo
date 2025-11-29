package com.bytecamp.herbit.ugcdemo.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.bytecamp.herbit.ugcdemo.data.AppDatabase;
import com.bytecamp.herbit.ugcdemo.data.dao.FollowDao;
import com.bytecamp.herbit.ugcdemo.data.dao.NotificationDao;
import com.bytecamp.herbit.ugcdemo.data.entity.Follow;
import com.bytecamp.herbit.ugcdemo.data.entity.Notification;
import com.bytecamp.herbit.ugcdemo.data.entity.User;
import java.util.List;

public class FollowRepository {
    private FollowDao followDao;
    private NotificationDao notificationDao;

    public FollowRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        followDao = db.followDao();
        notificationDao = db.notificationDao();
    }

    public LiveData<List<Long>> getFollowingIds(long userId) {
        return followDao.getFollowingIds(userId);
    }

    public LiveData<List<Long>> getFollowerIds(long userId) {
        return followDao.getFollowerIds(userId);
    }

    public LiveData<Integer> isFollowing(long followerId, long followeeId) {
        return followDao.isFollowing(followerId, followeeId);
    }

    public LiveData<List<User>> getFollowingList(long userId) {
        return followDao.getFollowingList(userId);
    }

    public LiveData<List<User>> getFollowerList(long userId) {
        return followDao.getFollowerList(userId);
    }
    
    public LiveData<Integer> getFollowingCount(long userId) {
        return followDao.getFollowingCount(userId);
    }

    public LiveData<Integer> getFollowerCount(long userId) {
        return followDao.getFollowerCount(userId);
    }

    public void toggleFollow(long followerId, long followeeId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            int count = followDao.isFollowingSync(followerId, followeeId);
            if (count > 0) {
                followDao.deleteFollow(followerId, followeeId);
            } else {
                followDao.insertFollow(new Follow(followerId, followeeId, System.currentTimeMillis()));

                // Send Notification
                Notification notif = new Notification(
                    Notification.TYPE_FOLLOW,
                    followeeId,
                    followerId,
                    0,
                    null,
                    System.currentTimeMillis()
                );
                notificationDao.insert(notif);
            }
        });
    }
}
