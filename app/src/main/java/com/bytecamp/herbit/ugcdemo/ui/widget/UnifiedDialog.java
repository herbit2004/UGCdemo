package com.bytecamp.herbit.ugcdemo.ui.widget;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import com.bytecamp.herbit.ugcdemo.R;

public class UnifiedDialog {

    public interface OnPositiveClickListener {
        void onClick(DialogInterface dialog, int which);
    }

    public static void showConfirm(Context context, String title, String message, String positiveText, OnPositiveClickListener listener) {
        new AlertDialog.Builder(context, R.style.UnifiedDialogTheme)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveText, listener::onClick)
                .setNegativeButton("取消", null)
                .show();
    }
    
    public static void showConfirm(Context context, String title, String message, String positiveText, String negativeText, OnPositiveClickListener positiveListener, OnPositiveClickListener negativeListener) {
        new AlertDialog.Builder(context, R.style.UnifiedDialogTheme)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveText, positiveListener::onClick)
                .setNegativeButton(negativeText, negativeListener != null ? negativeListener::onClick : null)
                .show();
    }

    public static void showCustom(Context context, String title, View view, String positiveText, OnPositiveClickListener listener) {
        new AlertDialog.Builder(context, R.style.UnifiedDialogTheme)
                .setTitle(title)
                .setView(view)
                .setPositiveButton(positiveText, listener::onClick)
                .setNegativeButton("取消", null)
                .show();
    }
    
    public static void showCustom(Context context, String title, View view, String positiveText, String negativeText, OnPositiveClickListener positiveListener) {
        new AlertDialog.Builder(context, R.style.UnifiedDialogTheme)
                .setTitle(title)
                .setView(view)
                .setPositiveButton(positiveText, positiveListener::onClick)
                .setNegativeButton(negativeText, null)
                .show();
    }
}
