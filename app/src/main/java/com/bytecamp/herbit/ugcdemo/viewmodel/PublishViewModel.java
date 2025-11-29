package com.bytecamp.herbit.ugcdemo.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import com.bytecamp.herbit.ugcdemo.data.AppDatabase;
import com.bytecamp.herbit.ugcdemo.data.dao.PostDao;
import com.bytecamp.herbit.ugcdemo.data.entity.Post;

import com.bytecamp.herbit.ugcdemo.data.dao.NotificationDao;
import com.bytecamp.herbit.ugcdemo.data.dao.UserDao;
import com.bytecamp.herbit.ugcdemo.data.entity.Notification;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PublishViewModel extends AndroidViewModel {
    private PostDao postDao;
    private NotificationDao notificationDao;
    private UserDao userDao;

    public PublishViewModel(Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        postDao = db.postDao();
        notificationDao = db.notificationDao();
        userDao = db.userDao();
    }

    public void publishPost(long authorId, String title, String content, String imagePath, OnPublishListener listener) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                Post post = new Post(authorId, title, content, imagePath, System.currentTimeMillis());
                long postId = postDao.insert(post); // Need to change insert to return long
                
                // Check for mentions in content
                Pattern p = Pattern.compile("@[\\u4e00-\\u9fa5\\w]+");
                Matcher m = p.matcher(content);
                Set<String> notifiedUsers = new HashSet<>();
                
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
                            0, // extra_id for post mention is 0
                            content, // Preview content
                            System.currentTimeMillis()
                        );
                        notificationDao.insert(mentionNotif);
                        notifiedUsers.add(username);
                    }
                }
                
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