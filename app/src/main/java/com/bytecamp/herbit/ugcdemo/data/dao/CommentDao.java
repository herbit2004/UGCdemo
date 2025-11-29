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
    long insert(Comment comment);

    @Query("SELECT * FROM comments WHERE comment_id = :id")
    Comment getCommentByIdSync(long id);

    @Transaction
    @Query("SELECT * FROM comments WHERE post_id = :postId ORDER BY comment_time ASC")
    LiveData<List<CommentWithUser>> getCommentsForPost(long postId);
    
    @Query("DELETE FROM comments WHERE comment_id = :commentId")
    void deleteById(long commentId);

    @Transaction
    @Query("SELECT * FROM comments WHERE post_id = :postId AND (parent_comment_id IS NULL OR parent_comment_id = 0) ORDER BY comment_time ASC LIMIT :limit OFFSET :offset")
    List<CommentWithUser> getTopLevelCommentsForPostPaged(long postId, int limit, int offset);

    @Transaction
    @Query("SELECT * FROM comments WHERE parent_comment_id = :parentId ORDER BY comment_time ASC")
    List<CommentWithUser> getRepliesForParent(long parentId);
}