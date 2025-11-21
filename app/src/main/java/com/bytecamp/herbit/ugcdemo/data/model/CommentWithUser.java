package com.bytecamp.herbit.ugcdemo.data.model;

import androidx.room.Embedded;
import androidx.room.Relation;
import com.bytecamp.herbit.ugcdemo.data.entity.Comment;
import com.bytecamp.herbit.ugcdemo.data.entity.User;

public class CommentWithUser {
    @Embedded
    public Comment comment;

    @Relation(
            parentColumn = "author_id",
            entityColumn = "user_id"
    )
    public User user;
}