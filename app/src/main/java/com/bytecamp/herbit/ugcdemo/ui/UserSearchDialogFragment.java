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
                // Use the unified background drawable
                window.setBackgroundDrawableResource(R.drawable.bg_dialog_unified);
                // Dim amount is usually set in theme, but can be set here
                window.setDimAmount(0.2f);
                
                // Layout params
                WindowManager.LayoutParams params = window.getAttributes();
                params.width = ViewGroup.LayoutParams.MATCH_PARENT; // Will be constrained by margin in layout if root has padding, but DialogFragment window usually matches width minus margin if not set
                // Actually, to match "unified" look, let's make it have some margin.
                // Standard AlertDialog has width margin.
                // Here we can set a fixed width or match parent with padding in layout.
                // The layout XML has padding=24dp.
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                params.gravity = android.view.Gravity.CENTER;
                window.setAttributes(params);
                
                // To effectively use the padding in XML as "margin" around the dialog window content,
                // we need the window background to be transparent OR set the background on the content view and transparent on window.
                // BUT user wants unified style.
                // If we set window background to bg_dialog_unified, it applies to the window.
                // If our layout has padding, the background will be outside the padding? No.
                // Let's look at the layout again.
                // FrameLayout (padding 24dp) -> LinearLayout (bg_dialog_rounded).
                // This structure effectively creates a margin (padding of FrameLayout) and the inner Linear is the "dialog".
                // So the Window background should be TRANSPARENT.
                window.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
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
