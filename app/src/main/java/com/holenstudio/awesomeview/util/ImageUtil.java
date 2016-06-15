package com.holenstudio.awesomeview.util;

import android.graphics.Bitmap;
import android.graphics.Matrix;

/**
 * Created by Holen on 2016/6/12.
 */
public class ImageUtil {
    private static final String TAG = "ImageUtil";

    /*
     * 旋转图片
     * @param angle
     * @param bitmap
     * @return Bitmap
     */
    public static Bitmap rotatingImageView(int angle , Bitmap bitmap) {
        //旋转图片 动作
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizedBitmap;
    }
}
