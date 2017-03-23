package github.hellocsl.gallerylayoutmanager.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Created by chensuilun on 2017/3/22.
 */

public class BitmapUtils {
    private static int sDesiredWidth;

    private static int sDesiredHeight;

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);
        options = getBestOptions(options, reqWidth, reqHeight);
        Bitmap src = BitmapFactory.decodeResource(res, resId, options);
        return createScaleBitmap(src, sDesiredWidth, sDesiredHeight);
    }

    private static Bitmap createScaleBitmap(Bitmap tempBitmap, int desiredWidth, int desiredHeight) {
        // If necessary, scale down to the maximal acceptable size.
        if (tempBitmap != null && (tempBitmap.getWidth() > desiredWidth || tempBitmap.getHeight() > desiredHeight)) {
            Bitmap bitmap = Bitmap.createScaledBitmap(tempBitmap, desiredWidth, desiredHeight, true);
            tempBitmap.recycle();
            return bitmap;
        } else {
            return tempBitmap;
        }
    }

    private static BitmapFactory.Options getBestOptions(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int actualWidth = options.outWidth;
        int actualHeight = options.outHeight;
        // Then compute the dimensions we would ideally like to decode to.
        sDesiredWidth = getResizedDimension(reqWidth, reqHeight, actualWidth, actualHeight);
        sDesiredHeight = getResizedDimension(reqHeight, reqWidth, actualHeight, actualWidth);
        // 根据现在得到计算inSampleSize
        options.inSampleSize = calculateBestInSampleSize(actualWidth, actualHeight, sDesiredWidth, sDesiredHeight);
        // 使用获取到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false;
        return options;
    }

    /**
     * Returns the largest power-of-two divisor for use in downscaling a bitmap
     * that will not result in the scaling past the desired dimensions.
     *
     * @param actualWidth   Actual width of the bitmap
     * @param actualHeight  Actual height of the bitmap
     * @param desiredWidth  Desired width of the bitmap
     * @param desiredHeight Desired height of the bitmap
     */
    // Visible for testing.
    private static int calculateBestInSampleSize(int actualWidth, int actualHeight, int desiredWidth, int desiredHeight) {
        double wr = (double) actualWidth / desiredWidth;
        double hr = (double) actualHeight / desiredHeight;
        double ratio = Math.min(wr, hr);
        float inSampleSize = 1.0f;
        while ((inSampleSize * 2) <= ratio) {
            inSampleSize *= 2;
        }

        return (int) inSampleSize;
    }

    /**
     * Scales one side of a rectangle to fit aspect ratio.
     *
     * @param maxPrimary      Maximum size of the primary dimension (i.e. width for max
     *                        width), or zero to maintain aspect ratio with secondary
     *                        dimension
     * @param maxSecondary    Maximum size of the secondary dimension, or zero to maintain
     *                        aspect ratio with primary dimension
     * @param actualPrimary   Actual size of the primary dimension
     * @param actualSecondary Actual size of the secondary dimension
     */
    private static int getResizedDimension(int maxPrimary, int maxSecondary, int actualPrimary, int actualSecondary) {
        double ratio = (double) actualSecondary / (double) actualPrimary;
        int resized = maxPrimary;
        if (resized * ratio > maxSecondary) {
            resized = (int) (maxSecondary / ratio);
        }
        return resized;
    }
}
