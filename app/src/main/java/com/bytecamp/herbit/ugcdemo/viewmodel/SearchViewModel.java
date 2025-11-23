package com.bytecamp.herbit.ugcdemo.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import com.bytecamp.herbit.ugcdemo.data.AppDatabase;
import com.bytecamp.herbit.ugcdemo.data.dao.PostDao;
import com.bytecamp.herbit.ugcdemo.data.model.PostCardItem;
import java.util.List;

public class SearchViewModel extends AndroidViewModel {
    private PostDao postDao;
    private MutableLiveData<String> currentQuery = new MutableLiveData<>("");
    private MutableLiveData<Integer> currentSort = new MutableLiveData<>(0); // 0: Recent, 1: Popular, 2: Recent Comment
    
    private LiveData<List<PostCardItem>> searchResults;

    public SearchViewModel(Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        postDao = db.postDao();
        
        CombinedSearchFilter filter = new CombinedSearchFilter("", 0);
        MutableLiveData<CombinedSearchFilter> filterLiveData = new MutableLiveData<>(filter);

        currentQuery.observeForever(query -> {
            CombinedSearchFilter f = filterLiveData.getValue();
            if (f != null) {
                f.query = query;
                filterLiveData.setValue(f);
            }
        });

        currentSort.observeForever(sort -> {
            CombinedSearchFilter f = filterLiveData.getValue();
            if (f != null) {
                f.sort = sort;
                filterLiveData.setValue(f);
            }
        });

        searchResults = Transformations.switchMap(filterLiveData, input -> {
            if (input.query == null || input.query.trim().isEmpty()) {
                // Empty result if no query
                return new MutableLiveData<>();
            }
            switch (input.sort) {
                case 1: return postDao.searchPostCardsPopular(input.query);
                case 2: return postDao.searchPostCardsRecentComment(input.query);
                case 0:
                default: return postDao.searchPostCards(input.query);
            }
        });
    }

    public LiveData<List<PostCardItem>> getSearchResults() {
        return searchResults;
    }

    public void search(String query) {
        currentQuery.setValue(query);
    }

    public void setSort(int sort) {
        currentSort.setValue(sort);
    }

    public int getCurrentSort() {
        Integer v = currentSort.getValue();
        return v != null ? v : 0;
    }

    private static class CombinedSearchFilter {
        String query;
        int sort;
        CombinedSearchFilter(String query, int sort) {
            this.query = query;
            this.sort = sort;
        }
    }
}
