package com.bytecamp.herbit.ugcdemo.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "posts",
        foreignKeys = @ForeignKey(entity = User.class,
                parentColumns = "user_id",
                childColumns = "author_id",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index(value = "author_id")}
)
public class Post {
    @PrimaryKey(autoGenerate = true)
    public long post_id;

    public long author_id;
    public String title;
    public String content;
    public String image_path;
    public long publish_time;

    public Post(long author_id, String title, String content, String image_path, long publish_time) {
        this.author_id = author_id;
        this.title = title;
        this.content = content;
        this.image_path = image_path;
        this.publish_time = publish_time;
    }
}