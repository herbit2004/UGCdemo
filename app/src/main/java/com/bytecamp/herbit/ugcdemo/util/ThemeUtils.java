package com.bytecamp.herbit.ugcdemo.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import com.bytecamp.herbit.ugcdemo.R;

public class ThemeUtils {
    private static final String PREFS = "ugc_prefs";
    private static final String KEY_THEME_RES = "theme_res_id";

    public static void applyTheme(Activity activity) {
        activity.setTheme(getSelectedThemeRes(activity));
    }

    public static int getSelectedThemeRes(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_THEME_RES, R.style.Theme_UGCdemo);
    }

    public static void setSelectedThemeRes(Context context, int themeResId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_THEME_RES, themeResId).apply();
    }

    public static int mapSwatchIdToTheme(int viewId) {
        if (viewId == R.id.swatchRed) return R.style.Theme_UGCdemo_AccentRed;
        if (viewId == R.id.swatchBlue) return R.style.Theme_UGCdemo_AccentBlue;
        if (viewId == R.id.swatchGreen) return R.style.Theme_UGCdemo_AccentGreen;
        if (viewId == R.id.swatchOrange) return R.style.Theme_UGCdemo_AccentOrange;
        if (viewId == R.id.swatchPink) return R.style.Theme_UGCdemo_AccentPink;
        if (viewId == R.id.swatchAmber) return R.style.Theme_UGCdemo_AccentAmber;
        if (viewId == R.id.swatchIndigo) return R.style.Theme_UGCdemo_AccentIndigo;
        if (viewId == R.id.swatchCyan) return R.style.Theme_UGCdemo_AccentCyan;
        if (viewId == R.id.swatchLime) return R.style.Theme_UGCdemo_AccentLime;
        if (viewId == R.id.swatchTeal) return R.style.Theme_UGCdemo_AccentTeal;
        return R.style.Theme_UGCdemo;
    }
}