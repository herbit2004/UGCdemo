package com.bytecamp.herbit.ugcdemo.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.bytecamp.herbit.ugcdemo.data.entity.User;

@Dao
public interface UserDao {
    @Insert
    long insert(User user);

    @Query("SELECT * FROM users WHERE username = :username")
    User findByUsername(String username);

    @Query("SELECT * FROM users WHERE username = :username AND password = :password")
    User login(String username, String password);

    @Query("SELECT * FROM users WHERE user_id = :userId")
    LiveData<User> getUserById(long userId);
    
    @Query("SELECT * FROM users WHERE user_id = :userId")
    User getUserByIdSync(long userId);

    @Query("UPDATE users SET password = :newPassword WHERE user_id = :userId")
    void updatePassword(long userId, String newPassword);

    @Query("UPDATE users SET avatar_path = :newPath WHERE user_id = :userId")
    void updateAvatar(long userId, String newPath);

    @Query("UPDATE users SET username = :newUsername WHERE user_id = :userId")
    void updateUsername(long userId, String newUsername);

    @Query("DELETE FROM users WHERE user_id = :userId")
    void deleteUser(long userId);
    
    @Update
    void update(User user);

    @Delete
    void delete(User user);
}