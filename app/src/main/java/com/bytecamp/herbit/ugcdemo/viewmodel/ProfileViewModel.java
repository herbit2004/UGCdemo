package com.bytecamp.herbit.ugcdemo.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.bytecamp.herbit.ugcdemo.data.AppDatabase;
import com.bytecamp.herbit.ugcdemo.data.dao.PostDao;
import com.bytecamp.herbit.ugcdemo.data.dao.UserDao;
import com.bytecamp.herbit.ugcdemo.data.entity.User;
import com.bytecamp.herbit.ugcdemo.data.model.PostCardItem;
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

    public LiveData<List<PostCardItem>> getUserPosts(long userId) {
        return postDao.getUserPostCards(userId);
    }
    
    public void updateUsername(long userId, String newUsername) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            userDao.updateUsername(userId, newUsername);
        });
    }

    public void updateAvatar(long userId, String path) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            userDao.updateAvatar(userId, path);
        });
    }

    public void verifyAndUpdatePassword(long userId, String oldPassword, String newPassword, Runnable onSuccess, Runnable onError) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            User user = userDao.getUserByIdSync(userId);
            if (user != null && user.password.equals(oldPassword)) {
                userDao.updatePassword(userId, newPassword);
                if (onSuccess != null) onSuccess.run();
            } else {
                if (onError != null) onError.run();
            }
        });
    }
    
    public void deleteUser(long userId, Runnable onComplete) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            userDao.deleteUser(userId);
            if (onComplete != null) {
                onComplete.run();
            }
        });
    }
}