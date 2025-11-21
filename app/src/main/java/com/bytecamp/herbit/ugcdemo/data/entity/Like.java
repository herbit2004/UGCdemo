package com.bytecamp.herbit.ugcdemo.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "likes",
        foreignKeys = {
            @ForeignKey(entity = User.class, parentColumns = "user_id", childColumns = "user_id", onDelete = ForeignKey.CASCADE)
        },
        indices = {
            @Index(value = "user_id"),
            @Index(value = {"target_type", "target_id"}), // 用于快速查询某个对象的点赞数
            @Index(value = {"user_id", "target_type", "target_id"}, unique = true) // 防止重复点赞
        }
)
public class Like {
    @PrimaryKey(autoGenerate = true)
    public long like_id;

    public long user_id;
    
    // 0 = Post, 1 = Comment
    public int target_type; 
    
    public long target_id;
    
    public long create_time;

    public Like(long user_id, int target_type, long target_id, long create_time) {
        this.user_id = user_id;
        this.target_type = target_type;
        this.target_id = target_id;
        this.create_time = create_time;
    }
}