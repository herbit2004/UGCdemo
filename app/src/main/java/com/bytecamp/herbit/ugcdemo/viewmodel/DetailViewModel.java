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
import com.bytecamp.herbit.ugcdemo.data.dao.NotificationDao;
import com.bytecamp.herbit.ugcdemo.data.entity.Notification;
import com.bytecamp.herbit.ugcdemo.data.repository.FollowRepository;

public class DetailViewModel extends AndroidViewModel {
    private PostDao postDao;
    private CommentDao commentDao;
    private LikeDao likeDao;
    private FollowRepository followRepository;
    private NotificationDao notificationDao;
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
        followRepository = new FollowRepository(application);
        notificationDao = db.notificationDao();
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
            long newCommentId = commentDao.insert(comment);
            comment.comment_id = newCommentId;

            // 1. Reply Notification
            if (replyToUsername != null) {
                if (parentCommentId != null && parentCommentId > 0) {
                    Comment parent = commentDao.getCommentByIdSync(parentCommentId);
                    // ALLOW SELF NOTIFICATION as requested
                    if (parent != null) {
                        Notification notif = new Notification(
                            Notification.TYPE_REPLY,
                            parent.author_id,
                            authorId,
                            postId,
                            newCommentId, // extra_id = comment_id for reply
                            content,
                            System.currentTimeMillis()
                        );
                        notificationDao.insert(notif);
                    }
                }
            } else {
                com.bytecamp.herbit.ugcdemo.data.entity.Post post = postDao.getPostByIdSync(postId);
                // ALLOW SELF NOTIFICATION
                if (post != null) {
                    Notification notif = new Notification(
                        Notification.TYPE_REPLY,
                        post.author_id,
                        authorId,
                        postId,
                        0, // extra_id = 0 for post reply
                        content,
                        System.currentTimeMillis()
                    );
                    notificationDao.insert(notif);
                }
            }
            
            // 2. Mention Notification logic
            // We need to parse @username from content and notify them
            java.util.regex.Pattern p = java.util.regex.Pattern.compile("@[\\u4e00-\\u9fa5\\w]+");
            java.util.regex.Matcher m = p.matcher(content);
            com.bytecamp.herbit.ugcdemo.data.dao.UserDao userDao = AppDatabase.getDatabase(getApplication()).userDao();
            
            java.util.Set<String> notifiedUsers = new java.util.HashSet<>();
            
            while (m.find()) {
                String username = m.group().substring(1);
                if (notifiedUsers.contains(username)) continue;
                
                com.bytecamp.herbit.ugcdemo.data.entity.User u = userDao.findByUsername(username);
                if (u != null) {
                    Notification mentionNotif = new Notification(
                        Notification.TYPE_MENTION,
                        u.user_id,
                        authorId,
                        postId,
                        newCommentId, // extra_id = comment_id where mention happened
                        content,
                        System.currentTimeMillis()
                    );
                    notificationDao.insert(mentionNotif);
                    notifiedUsers.add(username);
                }
            }
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

    public void toggleLike(long userId, int targetType, long targetId, boolean ignoredCurrentLiked) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            // Check actual state in DB to prevent race conditions and reliance on potentially stale UI state
            Like existingLike = likeDao.getLikeSync(userId, targetType, targetId);
            
            if (existingLike != null) {
                likeDao.delete(userId, targetType, targetId);
            } else {
                likeDao.insert(new Like(userId, targetType, targetId, System.currentTimeMillis()));

                long targetUserId = -1;
                String preview = "";
                long relatedId = -1;
                long extraId = 0;

                if (targetType == 0) { // Post
                    com.bytecamp.herbit.ugcdemo.data.entity.Post post = postDao.getPostByIdSync(targetId);
                    if (post != null) {
                        targetUserId = post.author_id;
                        preview = post.title;
                        relatedId = targetId;
                        extraId = 0;
                    }
                } else { // Comment
                    Comment comment = commentDao.getCommentByIdSync(targetId);
                    if (comment != null) {
                        targetUserId = comment.author_id;
                        preview = comment.content;
                        relatedId = comment.post_id;
                        extraId = targetId; // comment_id
                    }
                }

                // ALLOW SELF NOTIFICATION
                if (targetUserId != -1) {
                    Notification notif = new Notification(
                        Notification.TYPE_LIKE,
                        targetUserId,
                        userId,
                        relatedId,
                        extraId,
                        preview,
                        System.currentTimeMillis()
                    );
                    notificationDao.insert(notif);
                }
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
        return followRepository.isFollowing(followerId, followeeId);
    }

    public void toggleFollow(long followerId, long followeeId, boolean isFollowing) {
        followRepository.toggleFollow(followerId, followeeId);
    }
}
