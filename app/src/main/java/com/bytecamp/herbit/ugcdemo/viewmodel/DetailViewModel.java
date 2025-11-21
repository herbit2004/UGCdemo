package com.bytecamp.herbit.ugcdemo.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.bytecamp.herbit.ugcdemo.data.AppDatabase;
import com.bytecamp.herbit.ugcdemo.data.dao.CommentDao;
import com.bytecamp.herbit.ugcdemo.data.dao.PostDao;
import com.bytecamp.herbit.ugcdemo.data.entity.Comment;
import com.bytecamp.herbit.ugcdemo.data.model.CommentWithUser;
import com.bytecamp.herbit.ugcdemo.data.model.PostWithUser;
import java.util.List;

public class DetailViewModel extends AndroidViewModel {
    private PostDao postDao;
    private CommentDao commentDao;

    public DetailViewModel(Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        postDao = db.postDao();
        commentDao = db.commentDao();
    }

    public LiveData<PostWithUser> getPostById(long postId) {
        return postDao.getPostById(postId);
    }

    public LiveData<List<CommentWithUser>> getCommentsForPost(long postId) {
        return commentDao.getCommentsForPost(postId);
    }

    public void addComment(long postId, long authorId, String content) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            Comment comment = new Comment(postId, authorId, content, System.currentTimeMillis());
            commentDao.insert(comment);
        });
    }
}