package com.bytecamp.herbit.ugcdemo.data.model;

import androidx.room.Embedded;
import androidx.room.Relation;
import com.bytecamp.herbit.ugcdemo.data.entity.Post;
import com.bytecamp.herbit.ugcdemo.data.entity.User;

public class PostWithUser {
    @Embedded
    public Post post;

    @Relation(
            parentColumn = "author_id",
            entityColumn = "user_id"
    )
    public User user;
}