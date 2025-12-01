package com.bytecamp.herbit.ugcdemo.ui.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bytecamp.herbit.ugcdemo.R;

import java.util.ArrayList;
import java.util.List;

public class CustomPopupMenu {

    private Context context;
    private PopupWindow popupWindow;
    private RecyclerView recyclerView;
    private MenuAdapter adapter;
    private List<MenuItem> menuItems = new ArrayList<>();
    private OnMenuItemClickListener listener;

    public interface OnMenuItemClickListener {
        void onItemClick(MenuItem item);
    }

    public static class MenuItem {
        public int id;
        public String title;
        public int iconResId; // 0 if no icon
        public int badgeCount; // 0 if no badge
        public boolean isChecked; // for selection state

        public MenuItem(int id, String title) {
            this.id = id;
            this.title = title;
        }

        public MenuItem(int id, String title, int iconResId) {
            this.id = id;
            this.title = title;
            this.iconResId = iconResId;
        }
    }

    public CustomPopupMenu(Context context) {
        this.context = context;
        init();
    }

    private void init() {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_custom_popup_menu, null);
        recyclerView = view.findViewById(R.id.rvMenu);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        adapter = new MenuAdapter(menuItems);
        recyclerView.setAdapter(adapter);

        popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setElevation(10);
        popupWindow.setOutsideTouchable(true);
    }

    public void add(int id, String title) {
        menuItems.add(new MenuItem(id, title));
        adapter.notifyDataSetChanged();
    }

    public void add(int id, String title, int iconResId) {
        menuItems.add(new MenuItem(id, title, iconResId));
        adapter.notifyDataSetChanged();
    }
    
    public void updateBadge(int id, int count) {
        for (int i = 0; i < menuItems.size(); i++) {
            if (menuItems.get(i).id == id) {
                menuItems.get(i).badgeCount = count;
                adapter.notifyItemChanged(i);
                return;
            }
        }
    }

    public void setChecked(int id) {
        for (MenuItem item : menuItems) {
            item.isChecked = (item.id == id);
        }
        adapter.notifyDataSetChanged();
    }

    public void setOnMenuItemClickListener(OnMenuItemClickListener listener) {
        this.listener = listener;
    }

    public void show(View anchor) {
        popupWindow.showAsDropDown(anchor, 0, 0);
    }
    
    // Show with offset
    public void show(View anchor, int xOff, int yOff) {
        popupWindow.showAsDropDown(anchor, xOff, yOff);
    }

    public void dismiss() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
    }

    private class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.ViewHolder> {
        private List<MenuItem> items;

        public MenuAdapter(List<MenuItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_custom_menu, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            MenuItem item = items.get(position);
            holder.tvTitle.setText(item.title);
            
            if (item.iconResId != 0) {
                holder.ivIcon.setImageResource(item.iconResId);
                holder.ivIcon.setVisibility(View.VISIBLE);
            } else {
                holder.ivIcon.setVisibility(View.GONE);
            }

            if (item.badgeCount > 0) {
                holder.tvBadge.setText(item.badgeCount > 99 ? "99+" : String.valueOf(item.badgeCount));
                holder.tvBadge.setVisibility(View.VISIBLE);
            } else {
                holder.tvBadge.setVisibility(View.GONE);
            }

            if (item.isChecked) {
                holder.ivCheck.setVisibility(View.VISIBLE);
                holder.tvTitle.setTextColor(getAttrColor(com.google.android.material.R.attr.colorSecondary));
            } else {
                holder.ivCheck.setVisibility(View.GONE);
                holder.tvTitle.setTextColor(0xFF333333);
            }

            holder.itemView.setOnClickListener(v -> {
                // Delay to show ripple
                v.postDelayed(() -> {
                    if (listener != null) {
                        listener.onItemClick(item);
                    }
                    popupWindow.dismiss();
                }, 200);
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
        
        private int getAttrColor(int attr) {
            android.util.TypedValue typedValue = new android.util.TypedValue();
            context.getTheme().resolveAttribute(attr, typedValue, true);
            return typedValue.data;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivIcon;
            TextView tvTitle;
            TextView tvBadge;
            ImageView ivCheck;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                ivIcon = itemView.findViewById(R.id.ivIcon);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvBadge = itemView.findViewById(R.id.tvBadge);
                ivCheck = itemView.findViewById(R.id.ivCheck);
            }
        }
    }
}
