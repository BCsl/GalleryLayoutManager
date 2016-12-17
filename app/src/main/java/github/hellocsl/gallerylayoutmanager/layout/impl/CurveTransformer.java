package github.hellocsl.gallerylayoutmanager.layout.impl;

import android.support.v7.widget.OrientationHelper;
import android.view.View;

import github.hellocsl.gallerylayoutmanager.layout.GalleryLayoutManager;

/**
 * Created by chensuilun on 2016/12/16.
 */
public class CurveTransformer implements GalleryLayoutManager.ItemTransformer {


    @Override
    public void transformItem(View item, float position, int orientation, OrientationHelper orientationHelper, int pendingOffset) {
        float toCenterFraction = (calculateDistanceCenter(item, orientation, orientationHelper, pendingOffset)) / (orientationHelper.getTotalSpace() / 2.0f);

        if (orientation == GalleryLayoutManager.VERTICAL) {
            item.setScaleX(1 - 0.7f * Math.abs(toCenterFraction));
            item.setAlpha(1 - 0.7f * Math.abs(toCenterFraction));
            item.setRotationX(-toCenterFraction * 90);
        } else {
            item.setScaleY(1 - 0.5f * Math.abs(toCenterFraction));
            item.setRotationY(toCenterFraction * 45);
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
