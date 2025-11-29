package com.bytecamp.herbit.ugcdemo.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import com.bytecamp.herbit.ugcdemo.R;

public class FollowButton extends AppCompatButton {

    private boolean isFollowing = false;
    private boolean isFollower = false;

    public FollowButton(Context context) {
        super(context);
        init();
    }

    public FollowButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FollowButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setBackgroundResource(R.drawable.bg_follow_btn);
        updateText();
    }

    public void setState(boolean isFollowing, boolean isFollower) {
        this.isFollowing = isFollowing;
        this.isFollower = isFollower;
        updateText();
    }

    private void updateText() {
        // Set text color based on theme attr
        int colorOnSecondary = getAttrColor(getContext(), com.google.android.material.R.attr.colorOnSecondary);
        setTextColor(colorOnSecondary);
        
        if (isFollowing) {
            if (isFollower) {
                setText("互相关注");
            } else {
                setText("已关注");
            }
        } else {
            if (isFollower) {
                setText("回关");
            } else {
                setText("关注");
            }
        }
    }

    private int getAttrColor(Context context, int attr) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attr, typedValue, true);
        return typedValue.data;
    }
}
