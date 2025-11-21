package com.bytecamp.herbit.ugcdemo.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.bytecamp.herbit.ugcdemo.data.AppDatabase;
import com.bytecamp.herbit.ugcdemo.data.dao.PostDao;
import com.bytecamp.herbit.ugcdemo.data.dao.UserDao;
import com.bytecamp.herbit.ugcdemo.data.entity.User;
import com.bytecamp.herbit.ugcdemo.data.model.PostWithUser;
import java.util.List;

public class ProfileViewModel extends AndroidViewModel {
    private UserDao userDao;
    private PostDao postDao;

    public ProfileViewModel(Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        userDao = db.userDao();
        postDao = db.postDao();
    }

    public LiveData<User> getUser(long userId) {
        return userDao.getUserById(userId);
    }

    public LiveData<List<PostWithUser>> getUserPosts(long userId) {
        return postDao.getPostsByUserId(userId);
    }
}