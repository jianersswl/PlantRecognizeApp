package com.example.cameraalbumtest;

import android.graphics.Bitmap;
import android.graphics.Matrix;

public class ImageProcessing {

    public static Bitmap resizeImage(Bitmap bm, int height, int width){
        int origin_width = bm.getWidth();
        int origin_height = bm.getHeight();

        int newWidth = width;
        int newHeight = height;

        if (newWidth < 600)
            newWidth = 600;
        if (newHeight < 600)
            newHeight = 600;

        // 计算缩放比例
        float scaleWidth = ((float) newWidth) / origin_width;
        float scaleHeight = ((float) newHeight) / origin_height;
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片
        Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, origin_width, origin_height, matrix, true);
        return newbm;
    }

}
