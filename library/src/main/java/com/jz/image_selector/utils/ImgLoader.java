package com.jz.image_selector.utils;

import android.app.Activity;
import android.widget.ImageView;

/**
 * Created by lizheng on 2018/5/21.
 */

public interface ImgLoader {

    void displayImage(Activity activity, String path, ImageView imageView, int width, int height);
}
