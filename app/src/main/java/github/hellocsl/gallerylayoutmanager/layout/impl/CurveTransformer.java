package github.hellocsl.gallerylayoutmanager.layout.impl;

import android.util.Log;
import android.view.View;

import github.hellocsl.gallerylayoutmanager.BuildConfig;
import github.hellocsl.layoutmanager.gallery.GalleryLayoutManager;

/**
 * Created by chensuilun on 2016/12/16.
 */
public class CurveTransformer implements GalleryLayoutManager.ItemTransformer {

    private static final String TAG = "CurveTransformer";


    @Override
    public void transformItem(GalleryLayoutManager layoutManager, View item, float fraction) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "transformItem() called with:  fraction = [" + fraction + "]");
        }
        if (layoutManager.getOrientation() == GalleryLayoutManager.VERTICAL) {
            return;
        }
        item.setPivotX(item.getWidth() / 2.f);
        item.setPivotY(item.getHeight());
        float scale = 1 - 0.1f * Math.abs(fraction);
        item.setScaleX(scale);
        item.setScaleY(scale);
        item.setRotation(10 * fraction);
        item.setTranslationY(30 * Math.abs(fraction));
        item.setTranslationX(50 * -fraction);
        item.setAlpha(1 - 0.2f * Math.abs(fraction));
    }
}
