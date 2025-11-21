package com.bytecamp.herbit.ugcdemo.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.bytecamp.herbit.ugcdemo.data.AppDatabase;
import com.bytecamp.herbit.ugcdemo.data.dao.PostDao;
import com.bytecamp.herbit.ugcdemo.data.model.PostCardItem;
import java.util.List;

public class HomeViewModel extends AndroidViewModel {
    private PostDao postDao;
    private LiveData<List<PostCardItem>> allPosts;

    public HomeViewModel(Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        postDao = db.postDao();
        allPosts = postDao.getAllPostCards();
    }

    public LiveData<List<PostCardItem>> getAllPosts() {
        return allPosts;
    }
}