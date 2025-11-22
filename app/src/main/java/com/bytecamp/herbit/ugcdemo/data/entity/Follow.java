package com.bytecamp.herbit.ugcdemo.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(tableName = "follows",
        primaryKeys = {"follower_id", "followee_id"},
        foreignKeys = {
                @ForeignKey(entity = User.class,
                        parentColumns = "user_id",
                        childColumns = "follower_id",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = User.class,
                        parentColumns = "user_id",
                        childColumns = "followee_id",
                        onDelete = ForeignKey.CASCADE)
        },
        indices = {
                @Index(value = "follower_id"),
                @Index(value = "followee_id")
        }
)
public class Follow {
    public long follower_id; // The user who is following
    public long followee_id; // The user being followed

    public long created_time;

    public Follow(long follower_id, long followee_id, long created_time) {
        this.follower_id = follower_id;
        this.followee_id = followee_id;
        this.created_time = created_time;
    }
}
