package com.bytecamp.herbit.ugcdemo.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.bytecamp.herbit.ugcdemo.data.AppDatabase;
import com.bytecamp.herbit.ugcdemo.data.dao.CommentDao;
import com.bytecamp.herbit.ugcdemo.data.dao.LikeDao;
import com.bytecamp.herbit.ugcdemo.data.dao.PostDao;
import com.bytecamp.herbit.ugcdemo.data.entity.Comment;
import com.bytecamp.herbit.ugcdemo.data.entity.Like;
import com.bytecamp.herbit.ugcdemo.data.model.CommentLikeCount;
import com.bytecamp.herbit.ugcdemo.data.model.CommentWithUser;
import com.bytecamp.herbit.ugcdemo.data.model.PostWithUser;
import java.util.List;

public class DetailViewModel extends AndroidViewModel {
    private PostDao postDao;
    private CommentDao commentDao;
    private LikeDao likeDao;

    public DetailViewModel(Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        postDao = db.postDao();
        commentDao = db.commentDao();
        likeDao = db.likeDao();
    }

    public LiveData<PostWithUser> getPostById(long postId) {
        return postDao.getPostById(postId);
    }

    public LiveData<List<CommentWithUser>> getCommentsForPost(long postId) {
        return commentDao.getCommentsForPost(postId);
    }

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
}