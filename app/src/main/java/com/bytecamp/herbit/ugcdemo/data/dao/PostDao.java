package com.bytecamp.herbit.ugcdemo.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import com.bytecamp.herbit.ugcdemo.data.entity.Post;
import com.bytecamp.herbit.ugcdemo.data.model.PostCardItem;
import com.bytecamp.herbit.ugcdemo.data.model.PostWithUser;
import java.util.List;

/**
 * PostDao
 * 帖子数据访问对象。
 * 负责 Post 实体及其关联数据的增删改查。
 */
@Dao
public interface PostDao {

    @Insert
    void insert(Post post);

    /**
     * 获取所有帖子，包含统计信息（点赞数、评论数）。
     * 使用 SQL 子查询来聚合统计数据。
     * 结果按发布时间倒序排列。
     */
    @Transaction
    @Query("SELECT posts.*, " +
           "(SELECT COUNT(*) FROM likes WHERE target_type = 0 AND target_id = posts.post_id) as likeCount, " +
           "(SELECT COUNT(*) FROM comments WHERE post_id = posts.post_id) as commentCount " +
           "FROM posts ORDER BY publish_time DESC")
    LiveData<List<PostCardItem>> getAllPostCards();
    
    /**
     * 获取指定用户的所有帖子，包含统计信息。
     */
    @Transaction
    @Query("SELECT posts.*, " +
           "(SELECT COUNT(*) FROM likes WHERE target_type = 0 AND target_id = posts.post_id) as likeCount, " +
           "(SELECT COUNT(*) FROM comments WHERE post_id = posts.post_id) as commentCount " +
           "FROM posts WHERE author_id = :userId ORDER BY publish_time DESC")
    LiveData<List<PostCardItem>> getUserPostCards(long userId);

    /**
     * 获取单条帖子的详细信息（包含作者信息）。
     */
    @Transaction
    @Query("SELECT * FROM posts WHERE post_id = :postId")
    LiveData<PostWithUser> getPostById(long postId);

    /**
     * 删除指定帖子。
     * 由于数据库设置了 ForeignKey.CASCADE，该操作会自动级联删除关联的评论和点赞。
     */
    @Query("DELETE FROM posts WHERE post_id = :postId")
    void deleteById(long postId);
}