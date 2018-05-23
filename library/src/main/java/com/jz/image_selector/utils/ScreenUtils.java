package com.jz.image_selector.utils;

import android.content.Context;

/**
 * Created by lizheng on 2018/5/21.
 */

public class ScreenUtils {

    /** 根据屏幕宽度与密度计算GridView显示的列数， 最少为4列，并获取Item宽度 */
    public static int getImageItemWidth(Context activity) {
        int screenWidth = activity.getResources().getDisplayMetrics().widthPixels;
        int densityDpi = activity.getResources().getDisplayMetrics().densityDpi;
        int cols = screenWidth / densityDpi;
        cols = cols < 4 ? 4 : cols;
        int columnSpace = (int) (2 * activity.getResources().getDisplayMetrics().density);
        return (screenWidth - columnSpace * (cols - 1)) / cols;
    }
}