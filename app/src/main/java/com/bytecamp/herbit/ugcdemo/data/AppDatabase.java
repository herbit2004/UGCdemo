package com.bytecamp.herbit.ugcdemo.data;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;
import com.bytecamp.herbit.ugcdemo.data.dao.CommentDao;
import com.bytecamp.herbit.ugcdemo.data.dao.LikeDao;
import com.bytecamp.herbit.ugcdemo.data.dao.PostDao;
import com.bytecamp.herbit.ugcdemo.data.dao.UserDao;
import com.bytecamp.herbit.ugcdemo.data.entity.Comment;
import com.bytecamp.herbit.ugcdemo.data.entity.Like;
import com.bytecamp.herbit.ugcdemo.data.entity.Post;
import com.bytecamp.herbit.ugcdemo.data.entity.User;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * AppDatabase
 * 全局唯一的 Room 数据库实例。
 * 包含 User, Post, Comment, Like 四张表。
 * 配置了 fallbackToDestructiveMigration 允许在开发阶段破坏性升级。
 */
@Database(entities = {User.class, Post.class, Comment.class, Like.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    
    // DAO 访问接口
    public abstract UserDao userDao();
    public abstract PostDao postDao();
    public abstract CommentDao commentDao();
    public abstract LikeDao likeDao();

    private static volatile AppDatabase INSTANCE;
    
    // 固定的线程池，用于执行数据库写操作，避免阻塞主线程
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "RedBookClone.db")
                            // 注意：开发阶段允许数据库版本不匹配时清空数据重建
                            // 正式发布时应实现具体的 Migration
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