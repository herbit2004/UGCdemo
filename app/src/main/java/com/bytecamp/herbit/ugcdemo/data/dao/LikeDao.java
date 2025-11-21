package com.bytecamp.herbit.ugcdemo.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.bytecamp.herbit.ugcdemo.data.entity.Like;
import com.bytecamp.herbit.ugcdemo.data.model.CommentLikeCount;
import java.util.List;

@Dao
public interface LikeDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Like like);

    @Query("DELETE FROM likes WHERE user_id = :userId AND target_type = :targetType AND target_id = :targetId")
    void delete(long userId, int targetType, long targetId);

    @Query("SELECT COUNT(*) FROM likes WHERE target_type = :targetType AND target_id = :targetId")
    LiveData<Integer> getLikeCount(int targetType, long targetId);

    @Query("SELECT COUNT(*) > 0 FROM likes WHERE user_id = :userId AND target_type = :targetType AND target_id = :targetId")
    LiveData<Boolean> isLiked(long userId, int targetType, long targetId);
    
    @Query("SELECT target_id FROM likes WHERE user_id = :userId AND target_type = 1")
    LiveData<List<Long>> getLikedCommentIds(long userId);

    // 1 = Comment
    @Query("SELECT target_id, COUNT(*) as count FROM likes WHERE target_type = 1 GROUP BY target_id")
    LiveData<List<CommentLikeCount>> getCommentLikeCounts();
}