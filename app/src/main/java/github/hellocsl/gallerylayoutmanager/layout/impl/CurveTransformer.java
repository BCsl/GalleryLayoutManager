package github.hellocsl.gallerylayoutmanager.layout.impl;

import android.support.v7.widget.OrientationHelper;
import android.util.Log;
import android.view.View;

import github.hellocsl.gallerylayoutmanager.BuildConfig;
import github.hellocsl.gallerylayoutmanager.layout.GalleryLayoutManager;

/**
 * Created by chensuilun on 2016/12/16.
 */
public class CurveTransformer implements GalleryLayoutManager.ItemTransformer {

    private static final String TAG = "CurveTransformer";

    @Override
    public void transformItem(View item, float position, int orientation, OrientationHelper orientationHelper, int pendingOffset) {
        float toCenterFraction = (calculateDistanceCenter(item, orientation, orientationHelper, pendingOffset)) / (orientationHelper.getTotalSpace() / 2.0f);

        item.setScaleX(1 - 0.3f * Math.abs(toCenterFraction));
        item.setScaleY(1 - 0.3f * Math.abs(toCenterFraction));
        if (orientation == GalleryLayoutManager.VERTICAL) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "transformItem: position:" + position + ",pivotY:" + item.getPivotY() + ",item height:" + item.getHeight() + ",rotationX:" + (-toCenterFraction * 90));
            }
            item.setAlpha(1 - 0.8f * Math.abs(toCenterFraction));
        } else {
            item.setAlpha(1 - 0.7f * Math.abs(toCenterFraction));
        }

    }


    /**
     * @param child
     * @param orientation
     * @param orientationHelper
     * @param pendingOffset
     * @return
     */
    private int calculateDistanceCenter(View child, int orientation, OrientationHelper orientationHelper, float pendingOffset) {
        int parentCenter = (orientationHelper.getEndAfterPadding() - orientationHelper.getStartAfterPadding()) / 2 + orientationHelper.getStartAfterPadding();
        if (orientation == GalleryLayoutManager.HORIZONTAL) {
            return (int) ((child.getRight() - child.getLeft()) / 2 - pendingOffset + child.getLeft() - parentCenter);
        } else {
            return (int) ((child.getBottom() - child.getTop()) / 2 - pendingOffset + child.getTop() - parentCenter);
        }

    }
}
