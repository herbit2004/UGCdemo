package com.bytecamp.herbit.ugcdemo.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.bytecamp.herbit.ugcdemo.data.entity.Follow;
import com.bytecamp.herbit.ugcdemo.data.entity.User;
import java.util.List;

@Dao
public interface FollowDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertFollow(Follow follow);

    @Query("DELETE FROM follows WHERE follower_id = :followerId AND followee_id = :followeeId")
    void deleteFollow(long followerId, long followeeId);

    @Query("SELECT COUNT(*) FROM follows WHERE follower_id = :followerId AND followee_id = :followeeId")
    LiveData<Integer> isFollowing(long followerId, long followeeId);
    
    @Query("SELECT COUNT(*) FROM follows WHERE follower_id = :followerId AND followee_id = :followeeId")
    int isFollowingSync(long followerId, long followeeId);

    @Query("SELECT COUNT(*) FROM follows WHERE follower_id = :userId")
    LiveData<Integer> getFollowingCount(long userId);

    @Query("SELECT COUNT(*) FROM follows WHERE followee_id = :userId")
    LiveData<Integer> getFollowerCount(long userId);

    @Query("SELECT u.* FROM users u INNER JOIN follows f ON u.user_id = f.followee_id WHERE f.follower_id = :userId")
    LiveData<List<User>> getFollowingList(long userId);

    @Query("SELECT u.* FROM users u INNER JOIN follows f ON u.user_id = f.follower_id WHERE f.followee_id = :userId")
    LiveData<List<User>> getFollowerList(long userId);

    @Query("SELECT followee_id FROM follows WHERE follower_id = :userId")
    LiveData<List<Long>> getFollowingIds(long userId);

    @Query("SELECT follower_id FROM follows WHERE followee_id = :userId")
    LiveData<List<Long>> getFollowerIds(long userId);
}
