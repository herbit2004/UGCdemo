package com.bytecamp.herbit.ugcdemo.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "users", indices = {@Index(value = "username", unique = true)})
public class User {
    @PrimaryKey(autoGenerate = true)
    public long user_id;

    @NonNull
    public String username;

    @NonNull
    public String password;

    public String avatar_path;

    public long created_time;

    public User(@NonNull String username, @NonNull String password, long created_time) {
        this.username = username;
        this.password = password;
        this.created_time = created_time;
    }
}