package com.bytecamp.herbit.ugcdemo.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "comments",
        foreignKeys = {
                @ForeignKey(entity = Post.class, parentColumns = "post_id", childColumns = "post_id", onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = User.class, parentColumns = "user_id", childColumns = "author_id", onDelete = ForeignKey.CASCADE)
        },
        indices = {
                @Index(value = "post_id"),
                @Index(value = "author_id")
        }
)
public class Comment {
    @PrimaryKey(autoGenerate = true)
    public long comment_id;

    public long post_id;
    public long author_id;
    public String content;
    public long comment_time;

    public Comment(long post_id, long author_id, String content, long comment_time) {
        this.post_id = post_id;
        this.author_id = author_id;
        this.content = content;
        this.comment_time = comment_time;
    }
}