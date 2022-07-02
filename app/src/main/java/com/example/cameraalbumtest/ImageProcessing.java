package com.example.cameraalbumtest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageProcessing {

    public static Bitmap resizeImage(Bitmap bm, int height, int width){
        int origin_width = bm.getWidth();
        int origin_height = bm.getHeight();
        System.out.println("image size: " + origin_width + " * " + origin_height);
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
    public static Bitmap file2bitmap(String filePath){
        Bitmap bitmap= BitmapFactory.decodeFile(filePath);
        return bitmap;
    }
    public static File bitmap2file(Bitmap bitmap, String filePath){
        File file = new File(filePath);
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }
    public static File imageFileCompress(String originPath, String newPath){
        Bitmap bitmap = file2bitmap(originPath);
        bitmap = resizeImage(bitmap, 640, 640);
        return bitmap2file(bitmap, newPath);
    }
}
