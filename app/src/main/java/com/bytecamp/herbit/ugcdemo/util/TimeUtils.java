package com.bytecamp.herbit.ugcdemo.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * TimeUtils
 * 处理时间格式化的工具类。
 */
public class TimeUtils {
    // 使用 ThreadLocal 避免 SimpleDateFormat 的线程安全问题
    private static final ThreadLocal<SimpleDateFormat> DATE_FORMAT = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        }
    };

    /**
     * 将时间戳格式化为字符串。
     *
     * @param timestamp 毫秒级时间戳
     * @return 格式化后的时间字符串，若时间戳无效则返回空字符串
     */
    public static String formatTime(long timestamp) {
        if (timestamp <= 0) {
            return "";
        }
        return DATE_FORMAT.get().format(new Date(timestamp));
    }
}
