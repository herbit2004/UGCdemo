package com.bytecamp.herbit.ugcdemo.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.bytecamp.herbit.ugcdemo.data.AppDatabase;
import com.bytecamp.herbit.ugcdemo.data.dao.NotificationDao;
import com.bytecamp.herbit.ugcdemo.data.model.NotificationWithUser;
import com.bytecamp.herbit.ugcdemo.data.repository.FollowRepository;
import java.util.List;

public class NotificationViewModel extends AndroidViewModel {
    private NotificationDao notificationDao;
    private FollowRepository followRepository;
    private long currentUserId; 

    public NotificationViewModel(Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        notificationDao = db.notificationDao();
        followRepository = new FollowRepository(application);
        // Get user ID from prefs
        currentUserId = application.getSharedPreferences("ugc_prefs", android.content.Context.MODE_PRIVATE).getLong("user_id", -1);
    }

    public LiveData<List<NotificationWithUser>> getNotifications(int type) {
        return notificationDao.getNotificationsByType(currentUserId, type);
    }
    
    public LiveData<List<Long>> getFollowingIds() {
        return followRepository.getFollowingIds(currentUserId);
    }

    public LiveData<List<Long>> getFollowerIds() {
        return followRepository.getFollowerIds(currentUserId);
    }
    
    public void toggleFollow(long targetUserId) {
        followRepository.toggleFollow(currentUserId, targetUserId);
    }

    public void markAllRead(int type) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            notificationDao.markAllReadByType(currentUserId, type);
        });
    }

    public void markAsRead(long notificationId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            notificationDao.markAsRead(notificationId);
        });
    }
    
    public LiveData<Integer> getUnreadCount(int type) {
        return notificationDao.getUnreadCountByType(currentUserId, type);
    }
    
    public LiveData<Integer> getTotalUnreadCount() {
        return notificationDao.getUnreadCount(currentUserId);
    }
}
