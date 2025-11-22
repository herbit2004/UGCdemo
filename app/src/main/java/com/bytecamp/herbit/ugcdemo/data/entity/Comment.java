package com.bytecamp.herbit.ugcdemo.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "comments",
        foreignKeys = {
                @ForeignKey(entity = Post.class, parentColumns = "post_id", childColumns = "post_id", onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = User.class, parentColumns = "user_id", childColumns = "author_id", onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Comment.class, parentColumns = "comment_id", childColumns = "parent_comment_id", onDelete = ForeignKey.SET_NULL)
        },
        indices = {
                @Index(value = "post_id"),
                @Index(value = "author_id"),
                @Index(value = "parent_comment_id")
        }
)
public class Comment {
    @PrimaryKey(autoGenerate = true)
    public long comment_id;

    public long post_id;
    public long author_id;
    public String content;
    public long comment_time;
    
    // 如果是回复某条一级评论，这里存一级评论的ID；如果是回复帖子，这里为 0 或 null
    public Long parent_comment_id; 

    // 记录实际回复的是哪个用户（UI显示用：回复 @User）
    public String reply_to_username; 

    public Comment(long post_id, long author_id, String content, long comment_time) {
        this.post_id = post_id;
        this.author_id = author_id;
        this.content = content;
        this.comment_time = comment_time;
    }
}