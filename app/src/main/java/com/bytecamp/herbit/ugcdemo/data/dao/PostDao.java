package com.bytecamp.herbit.ugcdemo.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RewriteQueriesToDropUnusedColumns;
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
    long insert(Post post);
    
    @Query("SELECT * FROM posts WHERE post_id = :id")
    Post getPostByIdSync(long id);

    /**
     * 获取所有帖子，包含统计信息（点赞数、评论数）。
     * 默认按发布时间倒序排列 (Recent Published)。
     */
    @Transaction
    @Query("SELECT posts.*, " +
           "(SELECT COUNT(*) FROM likes WHERE target_type = 0 AND target_id = posts.post_id) as likeCount, " +
           "(SELECT COUNT(*) FROM comments WHERE post_id = posts.post_id) as commentCount " +
           "FROM posts ORDER BY publish_time DESC")
    LiveData<List<PostCardItem>> getAllPostCards();

    /**
     * 获取所有帖子，按热门程度（点赞数）排序。
     */
    @Transaction
    @Query("SELECT posts.*, " +
           "(SELECT COUNT(*) FROM likes WHERE target_type = 0 AND target_id = posts.post_id) as likeCount, " +
           "(SELECT COUNT(*) FROM comments WHERE post_id = posts.post_id) as commentCount " +
           "FROM posts ORDER BY likeCount DESC, commentCount DESC")
    LiveData<List<PostCardItem>> getAllPostCardsPopular();

    /**
     * 获取所有帖子，按最近评论时间排序。
     * 添加 @RewriteQueriesToDropUnusedColumns 注解以忽略未使用的 lastCommentTime 列警告
     */
    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT posts.*, " +
           "(SELECT COUNT(*) FROM likes WHERE target_type = 0 AND target_id = posts.post_id) as likeCount, " +
           "(SELECT COUNT(*) FROM comments WHERE post_id = posts.post_id) as commentCount, " +
           "(SELECT MAX(comment_time) FROM comments WHERE post_id = posts.post_id) as lastCommentTime " +
           "FROM posts ORDER BY lastCommentTime DESC, publish_time DESC")
    LiveData<List<PostCardItem>> getAllPostCardsRecentComment();

    /**
     * 获取关注的人的帖子（最新发布）。
     */
    @Transaction
    @Query("SELECT posts.*, " +
           "(SELECT COUNT(*) FROM likes WHERE target_type = 0 AND target_id = posts.post_id) as likeCount, " +
           "(SELECT COUNT(*) FROM comments WHERE post_id = posts.post_id) as commentCount " +
           "FROM posts " +
           "INNER JOIN follows ON posts.author_id = follows.followee_id " +
           "WHERE follows.follower_id = :currentUserId " +
           "ORDER BY publish_time DESC")
    LiveData<List<PostCardItem>> getFollowedPostCards(long currentUserId);

    /**
     * 获取关注的人的帖子（最热）。
     */
    @Transaction
    @Query("SELECT posts.*, " +
           "(SELECT COUNT(*) FROM likes WHERE target_type = 0 AND target_id = posts.post_id) as likeCount, " +
           "(SELECT COUNT(*) FROM comments WHERE post_id = posts.post_id) as commentCount " +
           "FROM posts " +
           "INNER JOIN follows ON posts.author_id = follows.followee_id " +
           "WHERE follows.follower_id = :currentUserId " +
           "ORDER BY likeCount DESC")
    LiveData<List<PostCardItem>> getFollowedPostCardsPopular(long currentUserId);

    /**
     * 获取关注的人的帖子（最近评论）。
     */
    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT posts.*, " +
           "(SELECT COUNT(*) FROM likes WHERE target_type = 0 AND target_id = posts.post_id) as likeCount, " +
           "(SELECT COUNT(*) FROM comments WHERE post_id = posts.post_id) as commentCount, " +
           "(SELECT MAX(comment_time) FROM comments WHERE post_id = posts.post_id) as lastCommentTime " +
           "FROM posts " +
           "INNER JOIN follows ON posts.author_id = follows.followee_id " +
           "WHERE follows.follower_id = :currentUserId " +
           "ORDER BY lastCommentTime DESC")
    LiveData<List<PostCardItem>> getFollowedPostCardsRecentComment(long currentUserId);

    @Transaction
    @Query("SELECT posts.*, " +
           "(SELECT COUNT(*) FROM likes WHERE target_type = 0 AND target_id = posts.post_id) as likeCount, " +
           "(SELECT COUNT(*) FROM comments WHERE post_id = posts.post_id) as commentCount " +
           "FROM posts ORDER BY publish_time DESC LIMIT :limit OFFSET :offset")
    List<PostCardItem> getAllPostCardsPaged(int limit, int offset);

    @Transaction
    @Query("SELECT posts.*, " +
           "(SELECT COUNT(*) FROM likes WHERE target_type = 0 AND target_id = posts.post_id) as likeCount, " +
           "(SELECT COUNT(*) FROM comments WHERE post_id = posts.post_id) as commentCount " +
           "FROM posts ORDER BY likeCount DESC, commentCount DESC LIMIT :limit OFFSET :offset")
    List<PostCardItem> getAllPostCardsPopularPaged(int limit, int offset);

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT posts.*, " +
           "(SELECT COUNT(*) FROM likes WHERE target_type = 0 AND target_id = posts.post_id) as likeCount, " +
           "(SELECT COUNT(*) FROM comments WHERE post_id = posts.post_id) as commentCount, " +
           "(SELECT MAX(comment_time) FROM comments WHERE post_id = posts.post_id) as lastCommentTime " +
           "FROM posts ORDER BY lastCommentTime DESC, publish_time DESC LIMIT :limit OFFSET :offset")
    List<PostCardItem> getAllPostCardsRecentCommentPaged(int limit, int offset);

    @Transaction
    @Query("SELECT posts.*, " +
           "(SELECT COUNT(*) FROM likes WHERE target_type = 0 AND target_id = posts.post_id) as likeCount, " +
           "(SELECT COUNT(*) FROM comments WHERE post_id = posts.post_id) as commentCount " +
           "FROM posts INNER JOIN follows ON posts.author_id = follows.followee_id WHERE follows.follower_id = :currentUserId " +
           "ORDER BY publish_time DESC LIMIT :limit OFFSET :offset")
    List<PostCardItem> getFollowedPostCardsPaged(long currentUserId, int limit, int offset);

    @Transaction
    @Query("SELECT posts.*, " +
           "(SELECT COUNT(*) FROM likes WHERE target_type = 0 AND target_id = posts.post_id) as likeCount, " +
           "(SELECT COUNT(*) FROM comments WHERE post_id = posts.post_id) as commentCount " +
           "FROM posts INNER JOIN follows ON posts.author_id = follows.followee_id WHERE follows.follower_id = :currentUserId " +
           "ORDER BY likeCount DESC LIMIT :limit OFFSET :offset")
    List<PostCardItem> getFollowedPostCardsPopularPaged(long currentUserId, int limit, int offset);

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT posts.*, " +
           "(SELECT COUNT(*) FROM likes WHERE target_type = 0 AND target_id = posts.post_id) as likeCount, " +
           "(SELECT COUNT(*) FROM comments WHERE post_id = posts.post_id) as commentCount, " +
           "(SELECT MAX(comment_time) FROM comments WHERE post_id = posts.post_id) as lastCommentTime " +
           "FROM posts INNER JOIN follows ON posts.author_id = follows.followee_id WHERE follows.follower_id = :currentUserId " +
           "ORDER BY lastCommentTime DESC LIMIT :limit OFFSET :offset")
    List<PostCardItem> getFollowedPostCardsRecentCommentPaged(long currentUserId, int limit, int offset);
    
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
     * 获取指定用户点赞过的帖子。
     */
    @Transaction
    @Query("SELECT posts.*, " +
           "(SELECT COUNT(*) FROM likes WHERE target_type = 0 AND target_id = posts.post_id) as likeCount, " +
           "(SELECT COUNT(*) FROM comments WHERE post_id = posts.post_id) as commentCount " +
           "FROM posts " +
           "INNER JOIN likes ON posts.post_id = likes.target_id " +
           "WHERE likes.user_id = :userId AND likes.target_type = 0 " +
           "ORDER BY likes.create_time DESC")
    LiveData<List<PostCardItem>> getLikedPostCards(long userId);

    @Transaction
    @Query("SELECT posts.*, " +
           "(SELECT COUNT(*) FROM likes WHERE target_type = 0 AND target_id = posts.post_id) as likeCount, " +
           "(SELECT COUNT(*) FROM comments WHERE post_id = posts.post_id) as commentCount " +
           "FROM posts WHERE author_id = :userId ORDER BY publish_time DESC LIMIT :limit OFFSET :offset")
    List<PostCardItem> getUserPostCardsPaged(long userId, int limit, int offset);

    @Transaction
    @Query("SELECT posts.*, " +
           "(SELECT COUNT(*) FROM likes WHERE target_type = 0 AND target_id = posts.post_id) as likeCount, " +
           "(SELECT COUNT(*) FROM comments WHERE post_id = posts.post_id) as commentCount " +
           "FROM posts INNER JOIN likes ON posts.post_id = likes.target_id WHERE likes.user_id = :userId AND likes.target_type = 0 ORDER BY likes.create_time DESC LIMIT :limit OFFSET :offset")
    List<PostCardItem> getLikedPostCardsPaged(long userId, int limit, int offset);

    /**
     * 搜索帖子 (标题或内容包含关键词)。
     */
    @Transaction
    @Query("SELECT posts.*, " +
           "(SELECT COUNT(*) FROM likes WHERE target_type = 0 AND target_id = posts.post_id) as likeCount, " +
           "(SELECT COUNT(*) FROM comments WHERE post_id = posts.post_id) as commentCount " +
           "FROM posts " +
           "WHERE title LIKE '%' || :keyword || '%' OR content LIKE '%' || :keyword || '%' " +
           "ORDER BY publish_time DESC")
    LiveData<List<PostCardItem>> searchPostCards(String keyword);

    /**
     * 搜索帖子并按热度排序
     */
    @Transaction
    @Query("SELECT posts.*, " +
            "(SELECT COUNT(*) FROM likes WHERE target_type = 0 AND target_id = posts.post_id) as likeCount, " +
            "(SELECT COUNT(*) FROM comments WHERE post_id = posts.post_id) as commentCount " +
            "FROM posts " +
            "WHERE title LIKE '%' || :keyword || '%' OR content LIKE '%' || :keyword || '%' " +
            "ORDER BY likeCount DESC")
    LiveData<List<PostCardItem>> searchPostCardsPopular(String keyword);

    /**
     * 搜索帖子并按最近评论排序
     * 添加 @RewriteQueriesToDropUnusedColumns 注解以忽略未使用的 lastCommentTime 列警告
     */
    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT posts.*, " +
            "(SELECT COUNT(*) FROM likes WHERE target_type = 0 AND target_id = posts.post_id) as likeCount, " +
            "(SELECT COUNT(*) FROM comments WHERE post_id = posts.post_id) as commentCount, " +
            "(SELECT MAX(comment_time) FROM comments WHERE post_id = posts.post_id) as lastCommentTime " +
            "FROM posts " +
            "WHERE title LIKE '%' || :keyword || '%' OR content LIKE '%' || :keyword || '%' " +
            "ORDER BY lastCommentTime DESC")
    LiveData<List<PostCardItem>> searchPostCardsRecentComment(String keyword);

    @Transaction
    @Query("SELECT posts.*, " +
           "(SELECT COUNT(*) FROM likes WHERE target_type = 0 AND target_id = posts.post_id) as likeCount, " +
           "(SELECT COUNT(*) FROM comments WHERE post_id = posts.post_id) as commentCount " +
           "FROM posts WHERE title LIKE '%' || :keyword || '%' OR content LIKE '%' || :keyword || '%' ORDER BY publish_time DESC LIMIT :limit OFFSET :offset")
    List<PostCardItem> searchPostCardsPaged(String keyword, int limit, int offset);

    @Transaction
    @Query("SELECT posts.*, " +
           "(SELECT COUNT(*) FROM likes WHERE target_type = 0 AND target_id = posts.post_id) as likeCount, " +
           "(SELECT COUNT(*) FROM comments WHERE post_id = posts.post_id) as commentCount " +
           "FROM posts WHERE title LIKE '%' || :keyword || '%' OR content LIKE '%' || :keyword || '%' ORDER BY likeCount DESC LIMIT :limit OFFSET :offset")
    List<PostCardItem> searchPostCardsPopularPaged(String keyword, int limit, int offset);

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT posts.*, " +
           "(SELECT COUNT(*) FROM likes WHERE target_type = 0 AND target_id = posts.post_id) as likeCount, " +
           "(SELECT COUNT(*) FROM comments WHERE post_id = posts.post_id) as commentCount, " +
           "(SELECT MAX(comment_time) FROM comments WHERE post_id = posts.post_id) as lastCommentTime " +
           "FROM posts WHERE title LIKE '%' || :keyword || '%' OR content LIKE '%' || :keyword || '%' ORDER BY lastCommentTime DESC LIMIT :limit OFFSET :offset")
    List<PostCardItem> searchPostCardsRecentCommentPaged(String keyword, int limit, int offset);

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
