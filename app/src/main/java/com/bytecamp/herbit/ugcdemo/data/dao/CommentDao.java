package com.bytecamp.herbit.ugcdemo.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import com.bytecamp.herbit.ugcdemo.data.entity.Comment;
import com.bytecamp.herbit.ugcdemo.data.model.CommentWithUser;
import java.util.List;

@Dao
public interface CommentDao {
    @Insert
    void insert(Comment comment);

    @Transaction
    @Query("SELECT * FROM comments WHERE post_id = :postId ORDER BY comment_time ASC")
    LiveData<List<CommentWithUser>> getCommentsForPost(long postId);
    
    @Query("DELETE FROM comments WHERE comment_id = :commentId")
    void deleteById(long commentId);
}