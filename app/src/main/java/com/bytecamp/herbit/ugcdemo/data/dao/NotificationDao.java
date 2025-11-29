package com.bytecamp.herbit.ugcdemo.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import com.bytecamp.herbit.ugcdemo.data.entity.Notification;
import com.bytecamp.herbit.ugcdemo.data.model.NotificationWithUser;
import java.util.List;

@Dao
public interface NotificationDao {
    @Insert
    void insert(Notification notification);

    @Transaction
    @Query("SELECT * FROM notifications WHERE target_user_id = :userId AND type = :type ORDER BY created_at DESC")
    LiveData<List<NotificationWithUser>> getNotificationsByType(long userId, int type);

    @Transaction
    @Query("SELECT * FROM notifications WHERE target_user_id = :userId ORDER BY created_at DESC")
    LiveData<List<NotificationWithUser>> getAllNotifications(long userId);

    @Query("SELECT COUNT(*) FROM notifications WHERE target_user_id = :userId AND is_read = 0")
    LiveData<Integer> getUnreadCount(long userId);
    
    @Query("SELECT COUNT(*) FROM notifications WHERE target_user_id = :userId AND type = :type AND is_read = 0")
    LiveData<Integer> getUnreadCountByType(long userId, int type);

    @Query("UPDATE notifications SET is_read = 1 WHERE target_user_id = :userId AND type = :type")
    void markAllReadByType(long userId, int type);
    
    @Query("UPDATE notifications SET is_read = 1 WHERE id = :notificationId")
    void markAsRead(long notificationId);
}
