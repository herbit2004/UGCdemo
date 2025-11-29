package com.bytecamp.herbit.ugcdemo.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "notifications")
public class Notification {
    public static final int TYPE_MENTION = 1;
    public static final int TYPE_REPLY = 2;
    public static final int TYPE_LIKE = 3;
    public static final int TYPE_FOLLOW = 4;

    @PrimaryKey(autoGenerate = true)
    public long id;

    public int type;
    public long target_user_id; // The user who receives the notification
    public long source_user_id; // The user who performed the action
    public long related_id; // post_id or comment_id depending on context
    public long extra_id; // comment_id if related_id is post_id, for scrolling
    public String content_preview; // Snippet of the comment/post/reply
    public boolean is_read;
    public long created_at;

    public Notification(int type, long target_user_id, long source_user_id, long related_id, String content_preview, long created_at) {
        this.type = type;
        this.target_user_id = target_user_id;
        this.source_user_id = source_user_id;
        this.related_id = related_id;
        this.extra_id = 0;
        this.content_preview = content_preview;
        this.is_read = false;
        this.created_at = created_at;
    }
    
    @androidx.room.Ignore
    public Notification(int type, long target_user_id, long source_user_id, long related_id, long extra_id, String content_preview, long created_at) {
        this.type = type;
        this.target_user_id = target_user_id;
        this.source_user_id = source_user_id;
        this.related_id = related_id;
        this.extra_id = extra_id;
        this.content_preview = content_preview;
        this.is_read = false;
        this.created_at = created_at;
    }
}
