package com.bytecamp.herbit.ugcdemo.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import com.bytecamp.herbit.ugcdemo.data.AppDatabase;
import com.bytecamp.herbit.ugcdemo.data.dao.PostDao;
import com.bytecamp.herbit.ugcdemo.data.entity.Post;

public class PublishViewModel extends AndroidViewModel {
    private PostDao postDao;

    public PublishViewModel(Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        postDao = db.postDao();
    }

    public void publishPost(long authorId, String title, String content, String imagePath, OnPublishListener listener) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                Post post = new Post(authorId, title, content, imagePath, System.currentTimeMillis());
                postDao.insert(post);
                listener.onSuccess();
            } catch (Exception e) {
                listener.onError(e.getMessage());
            }
        });
    }

    public interface OnPublishListener {
        void onSuccess();
        void onError(String msg);
    }
}