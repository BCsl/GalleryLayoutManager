package github.hellocsl.gallerylayoutmanager.layout;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import github.hellocsl.gallerylayoutmanager.BuildConfig;

/**
 * Created by chensuilun on 2016/11/18.
 */

public class GalleryLayoutManager extends RecyclerView.LayoutManager {
    private static final String TAG = "GalleryLayoutManager";
    private final Context mContext;
    private int mFirstVisiblePosition = -1;
    private int mLastVisiblePos = -1;


    public static final int HORIZONTAL = OrientationHelper.HORIZONTAL;

    public static final int VERTICAL = OrientationHelper.VERTICAL;

    private State mState;

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
    public RecyclerView.LayoutParams generateLayoutParams(Context c, AttributeSet attrs) {
        return new LayoutParams(c, attrs);
    }

    @Override
    public RecyclerView.LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
        if (lp instanceof ViewGroup.MarginLayoutParams) {
            return new LayoutParams((ViewGroup.MarginLayoutParams) lp);
        } else {
            return new LayoutParams(lp);
        }
    }

    @Override
    public boolean checkLayoutParams(RecyclerView.LayoutParams lp) {
        return lp instanceof LayoutParams;
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getItemCount() == 0) {
            detachAndScrapAttachedViews(recycler);
            return;
        }
        if (state.isPreLayout()) { //跳过preLayout，preLayout主要用于支持动画
            return;
        }
        detachAndScrapAttachedViews(recycler);
        mFirstVisiblePosition = 0;
        mLastVisiblePos = 0;
        fillCover(recycler, state, 0);
    }


    private void fillCover(RecyclerView.Recycler recycler, RecyclerView.State state, int scrollDelta) {
        if (getItemCount() == 0) {
            return;
        }

        if (mOrientation == HORIZONTAL) {
            fillWithHorizontal(recycler, state, scrollDelta);
        } else {
            fillWithVertical(recycler, state, scrollDelta);
        }

    }

    /**
     * @param recycler
     * @param state
     * @param dy
     */
    private void fillWithVertical(RecyclerView.Recycler recycler, RecyclerView.State state, int dy) {
        int topEdge = getOrientationHelper().getStartAfterPadding();
        int bottomEdge = getOrientationHelper().getEndAfterPadding();

        //1.根据滑动方向，回收越界子View
        View child;
        if (getChildCount() > 0) {
            if (dy >= 0) {
                for (int i = 0; i < getChildCount(); i++) {
                    child = getChildAt(i);
                    if (getDecoratedBottom(child) - dy < topEdge) {
                        removeAndRecycleView(child, recycler);
                        mFirstVisiblePosition++;
                    } else {
                        break;
                    }
                }
            } else { //dy<0
                for (int i = getChildCount() - 1; i >= 0; i--) {
                    child = getChildAt(i);
                    if (getDecoratedTop(child) - dy > bottomEdge) {
                        removeAndRecycleView(child, recycler);
                        mLastVisiblePos--;
                    }
                }
            }

        }
        //开始进行布局的位置
        int startPosition = mFirstVisiblePosition;
        int startOffset = -1;
        int scrapWidth, scrapHeight;
        Rect scrapRect = new Rect();
        int width = getHorizontalSpace();
        int leftOffset;
        View scrap;
        //2.根据不同滑动方向，为空余位置进行布局
        if (dy >= 0) {
            if (getChildCount() != 0) {
                View lastView = getChildAt(getChildCount() - 1);
                startPosition = getPosition(lastView) + 1; //下一个View的Position
                startOffset = getDecoratedBottom(lastView);
            }
            //考虑边界和数量
            for (int i = startPosition; i < getItemCount() && startOffset < bottomEdge; i++) {
                scrap = recycler.getViewForPosition(i);
                addView(scrap);
                measureChildWithMargins(scrap, 0, 0);
                scrapWidth = getDecoratedMeasuredWidth(scrap);
                scrapHeight = getDecoratedMeasuredHeight(scrap);
                leftOffset = (int) (getPaddingLeft() + (width - scrapWidth) / 2.0f);
                if (startOffset == -1 && startPosition == 0) {
                    //第0个View，居中处理
                    int top = (int) (getPaddingLeft() + (getHorizontalSpace() - scrapWidth) / 2.f);
                    scrapRect.set(leftOffset, top, leftOffset + scrapWidth, top + scrapHeight);
                } else {
                    scrapRect.set(leftOffset, startOffset, leftOffset + scrapWidth, startOffset + scrapHeight);
                }
                layoutDecorated(scrap, scrapRect.left, scrapRect.top, scrapRect.right, scrapRect.bottom);
                startOffset = scrapRect.bottom;
                ((LayoutParams) scrap.getLayoutParams()).mPosition = i;
                mLastVisiblePos = i;
                getState().mItemsFrames.put(i, scrapRect);
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "fillWithHorizontal,layout:mLastVisiblePos: " + mLastVisiblePos);
                }
            }
        } else {
            //dy<0
            if (getChildCount() > 0) {
                View firstView = getChildAt(0);
                startPosition = getPosition(firstView) - 1; //前一个View的position
                startOffset = getDecoratedTop(firstView);
            }
            //考虑边界和数量
            for (int i = startPosition; i >= 0 && startOffset > topEdge; i--) {
                scrapRect = getState().mItemsFrames.get(i);
                scrap = recycler.getViewForPosition(i);
                addView(scrap, 0);
                measureChildWithMargins(scrap, 0, 0);
                scrapWidth = scrapRect.right - scrapRect.left;
                scrapHeight = scrapRect.bottom - scrapRect.top;
                leftOffset = (int) (getPaddingLeft() + (width - scrapWidth) / 2.0f);
                layoutDecorated(scrap, leftOffset, startOffset - scrapHeight, leftOffset + scrapWidth, startOffset);
                startOffset -= scrapHeight;
                ((LayoutParams) scrap.getLayoutParams()).mPosition = i;
                mFirstVisiblePosition = i;
            }
        }
    }


    /**
     * @param recycler
     * @param state
     */
    private void fillWithHorizontal(RecyclerView.Recycler recycler, RecyclerView.State state, int dx) {
        int leftEdge = getOrientationHelper().getStartAfterPadding();
        int rightEdge = getOrientationHelper().getEndAfterPadding();
        if (BuildConfig.DEBUG) {
            Log.v(TAG, "fillWithHorizontal() called with: dx = [" + dx + "],leftEdge:" + leftEdge + ",rightEdge:" + rightEdge);
        }
        //1.根据滑动方向，回收越界子View
        View child;
        if (getChildCount() > 0) {
            if (dx >= 0) {
                for (int i = 0; i < getChildCount(); i++) {
                    child = getChildAt(i);
                    if (getDecoratedRight(child) - dx < leftEdge) {
                        removeAndRecycleView(child, recycler);
                        mFirstVisiblePosition++;
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "fillWithHorizontal:remove position:" + getPosition(child) + " mFirstVisiblePosition change to:" + mFirstVisiblePosition);
                        }
                    } else {
                        break;
                    }
                }
            } else { //dx<0
                for (int i = getChildCount() - 1; i >= 0; i--) {
                    child = getChildAt(i);
                    if (getDecoratedLeft(child) - dx > rightEdge) {
                        removeAndRecycleView(child, recycler);
                        mLastVisiblePos--;
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "fillWithHorizontal:remove position:" + getPosition(child) + "mLastVisiblePos change to:" + mLastVisiblePos);
                        }
                    }
                }
            }

        }
        //开始进行布局的位置
        int startPosition = mFirstVisiblePosition;
        int startOffset = -1;
        int scrapWidth, scrapHeight;
        Rect scrapRect = new Rect();
        int height = getVerticalSpace();
        int topOffset;
        View scrap;
        //2.根据不同滑动方向，为空余位置进行布局
        if (dx >= 0) {
            if (getChildCount() != 0) {
                View lastView = getChildAt(getChildCount() - 1);
                startPosition = getPosition(lastView) + 1; //下一个View的Position
                startOffset = getDecoratedRight(lastView);
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "fillWithHorizontal:to right startPosition:" + startPosition + ",startOffset:" + startOffset + ",rightEdge:" + rightEdge);
                }
            }
            //考虑边界和数量
            for (int i = startPosition; i < getItemCount() && startOffset < rightEdge; i++) {
                scrap = recycler.getViewForPosition(i);
                addView(scrap);
                measureChildWithMargins(scrap, 0, 0);
                scrapWidth = getDecoratedMeasuredWidth(scrap);
                scrapHeight = getDecoratedMeasuredHeight(scrap);
                topOffset = (int) (getPaddingTop() + (height - scrapHeight) / 2.0f);
                if (startOffset == -1 && startPosition == 0) {
                    //第0个View，居中处理
                    int left = (int) (getPaddingLeft() + (getHorizontalSpace() - scrapWidth) / 2.f);
                    scrapRect.set(left, topOffset, left + scrapWidth, topOffset + scrapHeight);
                } else {
                    scrapRect.set(startOffset, topOffset, startOffset + scrapWidth, topOffset + scrapHeight);
                }
                layoutDecorated(scrap, scrapRect.left, scrapRect.top, scrapRect.right, scrapRect.bottom);
                startOffset = scrapRect.right;
                ((LayoutParams) scrap.getLayoutParams()).mPosition = i;
                mLastVisiblePos = i;
                getState().mItemsFrames.put(i, scrapRect);
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "fillWithHorizontal,layout:mLastVisiblePos: " + mLastVisiblePos);
                }
            }
        } else {
            //dx<0
            if (getChildCount() > 0) {
                View firstView = getChildAt(0);
                startPosition = getPosition(firstView) - 1; //前一个View的position
                startOffset = getDecoratedLeft(firstView);
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "fillWithHorizontal:to left startPosition:" + startPosition + ",startOffset:" + startOffset + ",leftEdge:" + leftEdge + ",child count:" + getChildCount());
                }
            }
            //考虑边界和数量
            for (int i = startPosition; i >= 0 && startOffset > leftEdge; i--) {
                scrapRect = getState().mItemsFrames.get(i);
                scrap = recycler.getViewForPosition(i);
                //顺序很重要,举个例子，本来添加顺序是【0，1，2，3】没错，但手指右滑后，需要不断在左边空白填充View，不指定index的情况下就可能就会出现这样的情况【3，0，1，2】
                addView(scrap, 0);
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "fillWithHorizontal: add view for :" + i + ",child count:" + getChildCount());
                }
                measureChildWithMargins(scrap, 0, 0);
                scrapWidth = scrapRect.right - scrapRect.left;
                scrapHeight = scrapRect.bottom - scrapRect.top;
                topOffset = (int) (getPaddingTop() + (height - scrapHeight) / 2.0f);
                layoutDecorated(scrap, startOffset - scrapWidth, topOffset, startOffset, topOffset + scrapHeight);
                startOffset -= scrapWidth;
                ((LayoutParams) scrap.getLayoutParams()).mPosition = i;
                mFirstVisiblePosition = i;
            }
        }
    }

    private int getHorizontalSpace() {
        return getWidth() - getPaddingRight() - getPaddingLeft();
    }

    private int getVerticalSpace() {
        return getHeight() - getPaddingBottom() - getPaddingTop();
    }

    public State getState() {
        if (mState == null) {
            mState = new State();
        }
        return mState;
    }

    class State {
        /**
         * 存放所有item的位置和尺寸
         */
        SparseArray<Rect> mItemsFrames;

        /**
         * 当前方向的偏移量
         */
        int mScrollDelta;

        public State() {
            mItemsFrames = new SparseArray<Rect>();
            mScrollDelta = 0;
        }
    }


    @Override
    public boolean canScrollHorizontally() {
        return mOrientation == HORIZONTAL;
    }


    @Override
    public boolean canScrollVertically() {
        return mOrientation == VERTICAL;
    }

    /**
     * @param dx       dx>0，手指←移动，scrollX增加，视图视觉左移动（需要取反），右边被遮挡部分出现 ；dx<0，手指→移动，内容右滚动，scrollX减少
     * @param recycler
     * @param state
     * @return
     */
    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getChildCount() == 0 || dx == 0) {
            return 0;
        }
        int delta = -dx;
        int parentCenter = (getOrientationHelper().getEndAfterPadding() - getOrientationHelper().getStartAfterPadding()) / 2 + getOrientationHelper().getStartAfterPadding();
        View child;
        if (dx > 0) { //手势左滑
            if (((LayoutParams) getChildAt(getChildCount() - 1).getLayoutParams()).mPosition == getItemCount() - 1) {
                child = getChildAt(getChildCount() - 1);
                delta = -Math.max(0, Math.min(dx, (child.getRight() - child.getLeft()) / 2 + child.getLeft() - parentCenter));
            }
        } else {
            if (mFirstVisiblePosition == 0) {
                child = getChildAt(0);
                delta = -Math.min(0, Math.max(dx, ((child.getRight() - child.getLeft()) / 2 + child.getLeft()) - parentCenter));
            }
        }
        getState().mScrollDelta = -delta;
        fillCover(recycler, state, -delta);
        //{@link View#offsetChildrenHorizontal} ,通过修改 {@link View#mLeft}变量来实现偏移的，所以dx>0，手指左滑，mLeft变量应该减少才能实现相同的效果，所以需要取反
        offsetChildrenHorizontal(delta);
        return -delta;
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getChildCount() == 0 || dy == 0) {
            return 0;
        }
        int delta = -dy;
        int parentCenter = (getOrientationHelper().getEndAfterPadding() - getOrientationHelper().getStartAfterPadding()) / 2 + getOrientationHelper().getStartAfterPadding();
        View child;
        if (dy > 0) { //手势左滑
            if (((LayoutParams) getChildAt(getChildCount() - 1).getLayoutParams()).mPosition == getItemCount() - 1) {
                child = getChildAt(getChildCount() - 1);
                delta = -Math.max(0, Math.min(dy, (child.getBottom() - child.getTop()) / 2 + child.getTop() - parentCenter));
            }
        } else {
            if (mFirstVisiblePosition == 0) {
                child = getChildAt(0);
                delta = -Math.min(0, Math.max(dy, ((child.getBottom() - child.getTop()) / 2 + child.getTop()) - parentCenter));
            }
        }
        getState().mScrollDelta = -delta;
        fillCover(recycler, state, -delta);
        offsetChildrenVertical(delta);
        return -delta;
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
        public int mPosition;

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
