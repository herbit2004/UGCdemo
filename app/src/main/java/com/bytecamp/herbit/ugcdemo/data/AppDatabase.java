package com.bytecamp.herbit.ugcdemo.data;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import com.bytecamp.herbit.ugcdemo.data.dao.CommentDao;
import com.bytecamp.herbit.ugcdemo.data.dao.FollowDao;
import com.bytecamp.herbit.ugcdemo.data.dao.LikeDao;
import com.bytecamp.herbit.ugcdemo.data.dao.PostDao;
import com.bytecamp.herbit.ugcdemo.data.dao.UserDao;
import com.bytecamp.herbit.ugcdemo.data.entity.Comment;
import com.bytecamp.herbit.ugcdemo.data.entity.Follow;
import com.bytecamp.herbit.ugcdemo.data.entity.Like;
import com.bytecamp.herbit.ugcdemo.data.entity.Post;
import com.bytecamp.herbit.ugcdemo.data.entity.User;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.bytecamp.herbit.ugcdemo.data.dao.NotificationDao;
import com.bytecamp.herbit.ugcdemo.data.entity.Notification;

/**
 * AppDatabase
 * 全局唯一的 Room 数据库实例。
 * 包含 User, Post, Comment, Like, Follow, Notification 六张表。
 * 配置了 fallbackToDestructiveMigration 允许在开发阶段破坏性升级。
 */
@Database(entities = {User.class, Post.class, Comment.class, Like.class, Follow.class, Notification.class}, version = 6, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    
    // DAO 访问接口
    public abstract UserDao userDao();
    public abstract PostDao postDao();
    public abstract CommentDao commentDao();
    public abstract LikeDao likeDao();
    public abstract FollowDao followDao();
    public abstract NotificationDao notificationDao();

    private static volatile AppDatabase INSTANCE;

    static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE notifications ADD COLUMN extra_id INTEGER NOT NULL DEFAULT 0");
        }
    };
    
    // 固定的线程池，用于执行数据库写操作，避免阻塞主线程
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "ugc_demo_db")
                            .addMigrations(MIGRATION_5_6)
                            .fallbackToDestructiveMigration()
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static final RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            // 可以在这里预置初始数据
        }
    };
}