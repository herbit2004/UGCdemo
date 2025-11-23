package com.bytecamp.herbit.ugcdemo.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.bytecamp.herbit.ugcdemo.data.AppDatabase;
import com.bytecamp.herbit.ugcdemo.data.dao.CommentDao;
import com.bytecamp.herbit.ugcdemo.data.dao.FollowDao;
import com.bytecamp.herbit.ugcdemo.data.dao.LikeDao;
import com.bytecamp.herbit.ugcdemo.data.dao.PostDao;
import com.bytecamp.herbit.ugcdemo.data.entity.Comment;
import com.bytecamp.herbit.ugcdemo.data.entity.Follow;
import com.bytecamp.herbit.ugcdemo.data.entity.Like;
import com.bytecamp.herbit.ugcdemo.data.model.CommentLikeCount;
import com.bytecamp.herbit.ugcdemo.data.model.CommentWithUser;
import com.bytecamp.herbit.ugcdemo.data.model.PostWithUser;
import java.util.List;

public class DetailViewModel extends AndroidViewModel {
    private PostDao postDao;
    private CommentDao commentDao;
    private LikeDao likeDao;
    private FollowDao followDao;
    private MutableLiveData<java.util.List<com.bytecamp.herbit.ugcdemo.data.model.CommentWithUser>> commentsPaged = new MutableLiveData<>();
    private int pageSizeTop = 10;
    private int offsetTop = 0;
    private boolean hasMoreTop = true;
    private boolean loadingComments = false;
    private long currentPostId = -1;

    public DetailViewModel(Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        postDao = db.postDao();
        commentDao = db.commentDao();
        likeDao = db.likeDao();
        followDao = db.followDao();
    }

    public LiveData<PostWithUser> getPostById(long postId) {
        return postDao.getPostById(postId);
    }

    public LiveData<List<CommentWithUser>> getCommentsForPost(long postId) {
        return commentDao.getCommentsForPost(postId);
    }

    public LiveData<java.util.List<com.bytecamp.herbit.ugcdemo.data.model.CommentWithUser>> getCommentsPaged() {
        return commentsPaged;
    }

    public void initComments(long postId) {
        currentPostId = postId;
        refreshComments();
    }

    public void refreshComments() {
        if (currentPostId == -1) return;
        offsetTop = 0;
        hasMoreTop = true;
        commentsPaged.postValue(new java.util.ArrayList<>());
        loadMoreComments();
    }

    public void loadMoreComments() {
        if (loadingComments || !hasMoreTop || currentPostId == -1) return;
        loadingComments = true;
        AppDatabase.databaseWriteExecutor.execute(() -> {
            java.util.List<com.bytecamp.herbit.ugcdemo.data.model.CommentWithUser> topPage = commentDao.getTopLevelCommentsForPostPaged(currentPostId, pageSizeTop, offsetTop);
            java.util.ArrayList<com.bytecamp.herbit.ugcdemo.data.model.CommentWithUser> flattened = new java.util.ArrayList<>();
            for (com.bytecamp.herbit.ugcdemo.data.model.CommentWithUser top : topPage) {
                flattened.add(top);
                java.util.List<com.bytecamp.herbit.ugcdemo.data.model.CommentWithUser> replies = commentDao.getRepliesForParent(top.comment.comment_id);
                flattened.addAll(replies);
            }
            try { Thread.sleep(700); } catch (InterruptedException ignored) {}
            java.util.List<com.bytecamp.herbit.ugcdemo.data.model.CommentWithUser> current = commentsPaged.getValue();
            if (current == null) current = new java.util.ArrayList<>();
            java.util.ArrayList<com.bytecamp.herbit.ugcdemo.data.model.CommentWithUser> merged = new java.util.ArrayList<>(current);
            merged.addAll(flattened);
            offsetTop += topPage.size();
            hasMoreTop = topPage.size() == pageSizeTop;
            loadingComments = false;
            commentsPaged.postValue(merged);
        });
    }

    public boolean hasMoreComments() { return hasMoreTop; }

    public void addComment(long postId, long authorId, String content, Long parentCommentId, String replyToUsername) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            Comment comment = new Comment(postId, authorId, content, System.currentTimeMillis());
            comment.parent_comment_id = parentCommentId;
            comment.reply_to_username = replyToUsername;
            commentDao.insert(comment);
        });
    }
    
    public LiveData<Integer> getLikeCount(int targetType, long targetId) {
        return likeDao.getLikeCount(targetType, targetId);
    }

    public LiveData<Boolean> isLiked(long userId, int targetType, long targetId) {
        return likeDao.isLiked(userId, targetType, targetId);
    }
    
    public LiveData<List<Long>> getLikedCommentIds(long userId) {
        return likeDao.getLikedCommentIds(userId);
    }
    
    public LiveData<List<CommentLikeCount>> getCommentLikeCounts() {
        return likeDao.getCommentLikeCounts();
    }

    public void toggleLike(long userId, int targetType, long targetId, boolean currentLiked) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            if (currentLiked) {
                likeDao.delete(userId, targetType, targetId);
            } else {
                likeDao.insert(new Like(userId, targetType, targetId, System.currentTimeMillis()));
            }
        });
    }

    public void deletePost(long postId, Runnable onSuccess) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
             postDao.deleteById(postId);
             if (onSuccess != null) {
                 onSuccess.run();
             }
        });
    }

    public void deleteComment(long commentId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            commentDao.deleteById(commentId);
        });
    }

    public LiveData<Integer> isFollowing(long followerId, long followeeId) {
        return followDao.isFollowing(followerId, followeeId);
    }

    public void toggleFollow(long followerId, long followeeId, boolean isFollowing) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            if (isFollowing) {
                followDao.deleteFollow(followerId, followeeId);
            } else {
                followDao.insertFollow(new Follow(followerId, followeeId, System.currentTimeMillis()));
            }
        });
    }
}
