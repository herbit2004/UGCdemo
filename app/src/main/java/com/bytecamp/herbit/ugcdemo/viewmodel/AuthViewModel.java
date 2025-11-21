package com.bytecamp.herbit.ugcdemo.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import com.bytecamp.herbit.ugcdemo.data.AppDatabase;
import com.bytecamp.herbit.ugcdemo.data.dao.UserDao;
import com.bytecamp.herbit.ugcdemo.data.entity.User;

public class AuthViewModel extends AndroidViewModel {
    private UserDao userDao;

    public AuthViewModel(Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        userDao = db.userDao();
    }

    public void login(String username, String password, OnLoginResultListener listener) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            User user = userDao.login(username, password);
            if (user != null) {
                listener.onSuccess(user);
            } else {
                listener.onError("账号或密码错误");
            }
        });
    }

    public void register(String username, String password, OnLoginResultListener listener) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            User existing = userDao.findByUsername(username);
            if (existing != null) {
                listener.onError("用户名已被占用");
            } else {
                User newUser = new User(username, password, System.currentTimeMillis());
                long id = userDao.insert(newUser);
                newUser.user_id = id;
                listener.onSuccess(newUser);
            }
        });
    }

    public interface OnLoginResultListener {
        void onSuccess(User user);
        void onError(String error);
    }
}