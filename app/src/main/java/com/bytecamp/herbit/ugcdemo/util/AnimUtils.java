package com.bytecamp.herbit.ugcdemo.util;

import android.view.MotionEvent;
import android.view.View;

public class AnimUtils {

    /**
     * Sets a touch listener that scales the view down when pressed and restores it when released.
     * Returns false in onTouch to allow standard click listeners to function.
     *
     * @param view The view to animate
     */
    public static void setScaleTouchListener(View view) {
        view.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                    break;
            }
            return false; // Let the event propagate to standard click listeners
        });
    }
}
