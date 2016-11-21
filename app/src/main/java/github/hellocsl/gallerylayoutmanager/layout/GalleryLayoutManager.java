package github.hellocsl.gallerylayoutmanager.layout;

import android.content.Context;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by chensuilun on 2016/11/18.
 */

public class GalleryLayoutManager extends RecyclerView.LayoutManager {

    private final Context mContext;
    /* Consistent size applied to all child views */
    private int mDecoratedChildWidth;
    private int mDecoratedChildHeight;

    private int mSelectionPosition = 0;
    private int mFirstVisiblePosition = 0;
    private int mSelectedIndex;


    public static final int HORIZONTAL = OrientationHelper.HORIZONTAL;

    public static final int VERTICAL = OrientationHelper.VERTICAL;

    /**
     * Current orientation. Either {@link #HORIZONTAL} or {@link #VERTICAL}
     */
    private int mOrientation = HORIZONTAL;

    private OrientationHelper mHorizontalHelper;
    private OrientationHelper mVerticalHelper;

    public GalleryLayoutManager(Context context, int orientation) {
        mContext = context;
        mOrientation = orientation;
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new GalleryLayoutManager.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getItemCount() == 0) {
            detachAndScrapAttachedViews(recycler);
            return;
        }
        if (state.isPreLayout()) { //跳过preLayout，preLayout主要用于支持动画，暂时先使用自带的简单的fading
            return;
        }
        if (getChildCount() == 0) {
            //Scrap measure one child
            View scrap = recycler.getViewForPosition(0);
            addView(scrap);
            measureChildWithMargins(scrap, 0, 0);

            mDecoratedChildWidth = getDecoratedMeasuredWidth(scrap);
            mDecoratedChildHeight = getDecoratedMeasuredHeight(scrap);

            detachAndScrapView(scrap, recycler);
        }

        //Clear all attached views into the recycle bin
        detachAndScrapAttachedViews(recycler);
        fillCover(recycler, state);
    }

    private void fillCover(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getItemCount() == 0) {
            detachAndScrapAttachedViews(recycler);
            return;
        }
        SparseArray<View> viewCache = new SparseArray<View>(getChildCount());
        for (int i = 0; i < getChildCount(); i++) {
            int position = positionOfIndex(i);
            final View child = getChildAt(i);
            viewCache.put(position, child);
        }

        for (int i = 0; i < viewCache.size(); i++) {
            detachView(viewCache.valueAt(i));
        }

        if (mOrientation == HORIZONTAL) {
            fillWithHorizontal(recycler, state, viewCache);
        } else {
            fillWithVertical(recycler, state, viewCache);
        }

        //...回收没有被重新attach的View
        for (int i = 0; i < viewCache.size(); i++) {
            final View removingView = viewCache.valueAt(i);
            recycler.recycleView(removingView);

        }
    }

    private int positionOfIndex(int i) {
        return mFirstVisiblePosition + i;
    }

    private void fillWithVertical(RecyclerView.Recycler recycler, RecyclerView.State state, SparseArray<View> viewCache) {
        // TODO: 2016/11/19
    }

    private void fillWithHorizontal(RecyclerView.Recycler recycler, RecyclerView.State state, SparseArray<View> viewCache) {
        int rightEdge = getOrientationHelper().getEndAfterPadding();
        int leftOffset = (getHorizontalSpace() - mDecoratedChildWidth) / 2;
        int topOffset = (getVerticalSpace() - mDecoratedChildHeight) / 2;
        View scrap;
        int curPosition = mSelectionPosition;
        //layout from Center to RightEdge
        for (int curRight = 0; curRight < rightEdge && curPosition < getItemCount() - 1; curPosition++) {
            scrap = viewCache.get(curPosition);
            if (scrap == null) {
                scrap = recycler.getViewForPosition(0);
                addView(scrap);
            } else {
                attachView(scrap);
                viewCache.remove(curPosition);
            }
            layoutDecorated(scrap, leftOffset, topOffset, leftOffset + mDecoratedChildWidth, topOffset + mDecoratedChildHeight);
            curRight = getOrientationHelper().getDecoratedEnd(scrap);
        }
        curPosition = mSelectionPosition - 1;
        //layout from Center to left
        for (int curRight = leftOffset; curRight > getOrientationHelper().getStartAfterPadding() && curPosition > 0; curPosition--) {
            scrap = viewCache.get(curPosition);
            if (scrap == null) {
                scrap = recycler.getViewForPosition(curPosition);
                addView(scrap);
            } else {
                attachView(scrap);
                viewCache.remove(curPosition);
            }
            layoutDecorated(scrap, curRight - mDecoratedChildWidth, topOffset, curRight, topOffset + mDecoratedChildHeight);
            curRight = getOrientationHelper().getDecoratedStart(scrap);
        }
    }

    private int getHorizontalSpace() {
        return getWidth() - getPaddingRight() - getPaddingLeft();
    }

    private int getVerticalSpace() {
        return getHeight() - getPaddingBottom() - getPaddingTop();
    }


    @Override
    public boolean canScrollHorizontally() {
        return mOrientation == HORIZONTAL;
    }


    @Override
    public boolean canScrollVertically() {
        return mOrientation == VERTICAL;
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getChildCount() == 0) {
            return 0;
        }
        final View leftView = getChildAt(0);
        final View rightView = getChildAt(getChildCount() - 1);
        //Optimize the case where the entire data set is too small to scroll
        int viewSpan = getDecoratedBottom(rightView) - getDecoratedTop(leftView);
        if (viewSpan <= getHorizontalSpace()) {
            //We cannot scroll in either direction
            return 0;
        }
        int delta = -dx;
        //整体偏移，便宜后还需要根据当前的情况决定移动视图后，ItemView的添加/移除
        offsetChildrenHorizontal(delta);


        return super.scrollHorizontallyBy(dx, recycler, state);
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getChildCount() == 0) {
            return 0;
        }
        final View topView = getChildAt(0);
        final View bottomView = getChildAt(getChildCount() - 1);

        int viewSpan = getDecoratedBottom(bottomView) - getDecoratedTop(topView);
        if (viewSpan < getVerticalSpace()) {
            return 0;
        }
        return super.scrollVerticallyBy(dy, recycler, state);
    }

    private OrientationHelper getOrientationHelper() {
        if (mOrientation == HORIZONTAL) {
            if (mHorizontalHelper == null) {
                mHorizontalHelper = OrientationHelper.createHorizontalHelper(this);
            }
            return mHorizontalHelper;
        } else {
            if (mVerticalHelper == null) {
                mVerticalHelper = OrientationHelper.createVerticalHelper(this);
            }
            return mVerticalHelper;
        }
    }

    public static class LayoutParams extends RecyclerView.LayoutParams {

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(RecyclerView.LayoutParams source) {
            super(source);
        }
    }
}
