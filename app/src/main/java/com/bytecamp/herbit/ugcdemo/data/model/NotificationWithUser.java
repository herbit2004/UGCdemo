package com.bytecamp.herbit.ugcdemo.data.model;

import androidx.room.Embedded;
import androidx.room.Relation;
import com.bytecamp.herbit.ugcdemo.data.entity.Notification;
import com.bytecamp.herbit.ugcdemo.data.entity.User;

public class NotificationWithUser {
    @Embedded
    public Notification notification;

    @Relation(parentColumn = "source_user_id", entityColumn = "user_id")
    public User sourceUser;
}
