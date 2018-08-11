package com.jz.image_selector.utils;

import android.content.Context;

/**
 * Created by lizheng on 2018/5/21.
 */

public class ScreenUtils {

    /**
     * 根据屏幕宽度与密度计算GridView显示的列数， 最少为4列，并获取Item宽度
     */
    public static int getImageItemWidth(Context activity) {
        int screenWidth = activity.getResources().getDisplayMetrics().widthPixels;
        int densityDpi = activity.getResources().getDisplayMetrics().densityDpi;
        int cols = screenWidth / densityDpi;
        cols = cols < 4 ? 4 : cols;
        int columnSpace = (int) (2 * activity.getResources().getDisplayMetrics().density);
        int size = (screenWidth - (cols - 1) * columnSpace) / cols;
        return size;
    }


    /**
     * 根据屏幕宽度与密度计算GridView显示的列数， 最少为4列，并获取Item宽度
     */
    public static int getColumn(Context activity) {
        int screenWidth = activity.getResources().getDisplayMetrics().widthPixels;
        int densityDpi = activity.getResources().getDisplayMetrics().densityDpi;
        int cols = screenWidth / densityDpi;
        cols = cols < 4 ? 4 : cols;
        return cols;
    }

    /**
     * dp转px
     *
     * @param dpValue dp值
     * @return px值
     */
    public static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
