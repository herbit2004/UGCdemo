package com.bytecamp.herbit.ugcdemo.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bytecamp.herbit.ugcdemo.R;
import com.bytecamp.herbit.ugcdemo.data.AppDatabase;
import com.bytecamp.herbit.ugcdemo.data.entity.User;
import java.util.List;

public class UserSearchDialogFragment extends DialogFragment {

    private OnUserSelectedListener listener;
    private UserListAdapter adapter;
    private AppDatabase db;

    public interface OnUserSelectedListener {
        void onUserSelected(User user);
    }

    public void setOnUserSelectedListener(OnUserSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                // Set to transparent so margin in layout creates the "floating" effect
                window.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
                // Use MATCH_PARENT for width to allow margin to control width, and WRAP_CONTENT for height?
                // User complained "not what I saw" - maybe they want a fixed size centered box?
                // But "margin" with MATCH_PARENT works for width.
                // For height, if we use MATCH_PARENT, it fills screen.
                // Let's use WRAP_CONTENT for height so it doesn't take full screen if list is short, 
                // but layout has minHeight 400dp so it will be at least that.
                // And we need to ensure it is centered.
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                window.setGravity(android.view.Gravity.CENTER);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_user_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        db = AppDatabase.getDatabase(requireContext());

        EditText etSearch = view.findViewById(R.id.etSearchUser);
        view.findViewById(R.id.ivClose).setOnClickListener(v -> dismiss());

        RecyclerView rv = view.findViewById(R.id.rvUserList);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        
        long currentUserId = requireContext().getSharedPreferences("ugc_prefs", android.content.Context.MODE_PRIVATE).getLong("user_id", -1);
        adapter = new UserListAdapter(currentUserId, (user, isFollowing) -> {
            // Follow action
            new com.bytecamp.herbit.ugcdemo.data.repository.FollowRepository(requireActivity().getApplication()).toggleFollow(currentUserId, user.user_id);
        });
        
        com.bytecamp.herbit.ugcdemo.data.dao.FollowDao followDao = db.followDao();
        followDao.getFollowingIds(currentUserId).observe(getViewLifecycleOwner(), ids -> {
            adapter.setFollowingIds(new java.util.HashSet<>(ids));
        });
        followDao.getFollowerIds(currentUserId).observe(getViewLifecycleOwner(), ids -> {
            adapter.setFollowerIds(new java.util.HashSet<>(ids));
        });

        adapter.setOnItemClickListener(user -> {
            if (listener != null) {
                listener.onUserSelected(user);
            }
            dismiss();
        });
        rv.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                if (!query.isEmpty()) {
                    db.userDao().searchUsers(query).observe(getViewLifecycleOwner(), users -> {
                        adapter.setUsers(users);
                    });
                } else {
                    adapter.setUsers(new java.util.ArrayList<>());
                }
            }
        });
    }
}
