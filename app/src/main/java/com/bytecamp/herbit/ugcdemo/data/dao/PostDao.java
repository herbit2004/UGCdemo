package com.bytecamp.herbit.ugcdemo.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import com.bytecamp.herbit.ugcdemo.data.entity.Post;
import com.bytecamp.herbit.ugcdemo.data.model.PostWithUser;
import java.util.List;

@Dao
public interface PostDao {
    @Insert
    void insert(Post post);

    @Transaction
    @Query("SELECT * FROM posts ORDER BY publish_time DESC")
    LiveData<List<PostWithUser>> getAllPosts();

    @Transaction
    @Query("SELECT * FROM posts WHERE author_id = :userId ORDER BY publish_time DESC")
    LiveData<List<PostWithUser>> getPostsByUserId(long userId);

    @Transaction
    @Query("SELECT * FROM posts WHERE post_id = :postId")
    LiveData<PostWithUser> getPostById(long postId);
}