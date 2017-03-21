package github.hellocsl.gallerylayoutmanager.layout;

import android.content.Context;
import android.graphics.PointF;
import android.graphics.Rect;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;

/**
 * A custom LayoutManager to build a {@link android.widget.Gallery} or a {@link android.support.v4.view.ViewPager}like {@link RecyclerView} and
 * support both {@link GalleryLayoutManager#HORIZONTAL} and {@link GalleryLayoutManager#VERTICAL} scroll.
 * Created by chensuilun on 2016/11/18.
 */
public class GalleryLayoutManager extends RecyclerView.LayoutManager implements RecyclerView.SmoothScroller.ScrollVectorProvider {
    private static final boolean DEBUG = true;
    private static final String TAG = "GalleryLayoutManager";
    private final Context mContext;
    final static int LAYOUT_START = -1;

    final static int LAYOUT_END = 1;

    public static final int HORIZONTAL = OrientationHelper.HORIZONTAL;

    public static final int VERTICAL = OrientationHelper.VERTICAL;

    private int mFirstVisiblePosition = -1;
    private int mLastVisiblePos = -1;
    private int mInitialSelectedPosition = -1;

    /**
     * Scroll state
     */
    private State mState;

    private LinearSnapHelper mSnapHelper = new LinearSnapHelper();

    private InnerScrollListener mInnerScrollListener = new InnerScrollListener();

    private boolean mCallbackInFling = false;

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
        if (mOrientation == VERTICAL) {
            return new GalleryLayoutManager.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        } else {
            return new GalleryLayoutManager.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
        }
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
        if (DEBUG) {
            Log.d(TAG, "onLayoutChildren() called with: state = [" + state + "]");
        }
        if (getItemCount() == 0) {
            mFirstVisiblePosition = -1;
            mLastVisiblePos = -1;
            if (mState != null) {
                mState.mItemsFrames.clear();
            }
            detachAndScrapAttachedViews(recycler);
            return;
        }
        if (state.isPreLayout()) {
            return;
        }
        if (mState != null) {
            mState.mItemsFrames.clear();
        }
        detachAndScrapAttachedViews(recycler);
        mFirstVisiblePosition = 0;
        mLastVisiblePos = 0;
        firstFillCover(recycler, state, 0);
    }


    private void firstFillCover(RecyclerView.Recycler recycler, RecyclerView.State state, int scrollDelta) {
        if (mInitialSelectedPosition < 0 || mInitialSelectedPosition >= getItemCount()) {
            fillCover(recycler, state, scrollDelta);
            return;
        }
        if (mOrientation == HORIZONTAL) {
            firstFillWithHorizontal(recycler, state);
        } else {
            firstFillWithVertical(recycler, state);
        }

        if (DEBUG) {
            Log.d(TAG, "firstFillCover finish:first: " + mFirstVisiblePosition + ",last:" + mLastVisiblePos);
        }

        if (mItemTransformer != null) {
            View child;
            for (int i = 0; i < getChildCount(); i++) {
                child = getChildAt(i);
                mItemTransformer.transformItem(child, getPosition(child), mOrientation, getOrientationHelper(), scrollDelta);
            }
        }
    }

    /**
     * Layout the item view witch position special by {@link GalleryLayoutManager#mInitialSelectedPosition} first and then layout the other
     *
     * @param recycler
     * @param state
     */
    private void firstFillWithHorizontal(RecyclerView.Recycler recycler, RecyclerView.State state) {
        detachAndScrapAttachedViews(recycler);
        int leftEdge = getOrientationHelper().getStartAfterPadding();
        int rightEdge = getOrientationHelper().getEndAfterPadding();
        int startPosition = mInitialSelectedPosition;
        int scrapWidth, scrapHeight;
        Rect scrapRect = new Rect();
        int height = getVerticalSpace();
        int topOffset;
        //layout the init position view
        View scrap = recycler.getViewForPosition(mInitialSelectedPosition);
        addView(scrap);
        measureChildWithMargins(scrap, 0, 0);
        scrapWidth = getDecoratedMeasuredWidth(scrap);
        scrapHeight = getDecoratedMeasuredHeight(scrap);
        topOffset = (int) (getPaddingTop() + (height - scrapHeight) / 2.0f);
        int left = (int) (getPaddingLeft() + (getHorizontalSpace() - scrapWidth) / 2.f);
        scrapRect.set(left, topOffset, left + scrapWidth, topOffset + scrapHeight);
        layoutDecorated(scrap, scrapRect.left, scrapRect.top, scrapRect.right, scrapRect.bottom);
        if (getState().mItemsFrames.get(startPosition) == null) {
            getState().mItemsFrames.put(startPosition, scrapRect);
        } else {
            getState().mItemsFrames.get(startPosition).set(scrapRect);
        }
        mFirstVisiblePosition = mLastVisiblePos = startPosition;
        int leftStartOffset = getDecoratedLeft(scrap);
        int rightStartOffset = getDecoratedRight(scrap);
        //fill left of center
        fillLeft(recycler, mInitialSelectedPosition - 1, leftStartOffset, leftEdge);
        //fill right of center
        fillRight(recycler, mInitialSelectedPosition + 1, rightStartOffset, rightEdge);
    }

    /**
     * Layout the item view witch position special by {@link GalleryLayoutManager#mInitialSelectedPosition} first and then layout the other
     *
     * @param recycler
     * @param state
     */
    private void firstFillWithVertical(RecyclerView.Recycler recycler, RecyclerView.State state) {
        detachAndScrapAttachedViews(recycler);
        int topEdge = getOrientationHelper().getStartAfterPadding();
        int bottomEdge = getOrientationHelper().getEndAfterPadding();
        int startPosition = mInitialSelectedPosition;
        int scrapWidth, scrapHeight;
        Rect scrapRect = new Rect();
        int width = getHorizontalSpace();
        int leftOffset;
        //layout the init position view
        View scrap = recycler.getViewForPosition(mInitialSelectedPosition);
        addView(scrap);
        measureChildWithMargins(scrap, 0, 0);
        scrapWidth = getDecoratedMeasuredWidth(scrap);
        scrapHeight = getDecoratedMeasuredHeight(scrap);
        leftOffset = (int) (getPaddingLeft() + (width - scrapWidth) / 2.0f);
        int top = (int) (getPaddingTop() + (getVerticalSpace() - scrapHeight) / 2.f);
        scrapRect.set(leftOffset, top, leftOffset + scrapWidth, top + scrapHeight);
        layoutDecorated(scrap, scrapRect.left, scrapRect.top, scrapRect.right, scrapRect.bottom);
        if (getState().mItemsFrames.get(startPosition) == null) {
            getState().mItemsFrames.put(startPosition, scrapRect);
        } else {
            getState().mItemsFrames.get(startPosition).set(scrapRect);
        }
        mFirstVisiblePosition = mLastVisiblePos = startPosition;
        int topStartOffset = getDecoratedTop(scrap);
        int bottomStartOffset = getDecoratedBottom(scrap);
        //fill left of center
        fillTop(recycler, mInitialSelectedPosition - 1, topStartOffset, topEdge);
        //fill right of center
        fillBottom(recycler, mInitialSelectedPosition + 1, bottomStartOffset, bottomEdge);
    }

    /**
     * Fill left of the center view
     *
     * @param recycler
     * @param startPosition start position to fill left
     * @param startOffset   layout start offset
     * @param leftEdge
     */
    private void fillLeft(RecyclerView.Recycler recycler, int startPosition, int startOffset, int leftEdge) {
        View scrap;
        int topOffset;
        int scrapWidth, scrapHeight;
        Rect scrapRect = new Rect();
        int height = getVerticalSpace();
        for (int i = startPosition; i > 0 && startOffset > leftEdge; i--) {
            scrap = recycler.getViewForPosition(i);
            addView(scrap);
            measureChildWithMargins(scrap, 0, 0);
            scrapWidth = getDecoratedMeasuredWidth(scrap);
            scrapHeight = getDecoratedMeasuredHeight(scrap);
            topOffset = (int) (getPaddingTop() + (height - scrapHeight) / 2.0f);
            scrapRect.set(startOffset - scrapWidth, topOffset, startOffset, topOffset + scrapHeight);
            layoutDecorated(scrap, scrapRect.left, scrapRect.top, scrapRect.right, scrapRect.bottom);
            startOffset = scrapRect.left;
            mFirstVisiblePosition = i;
            if (getState().mItemsFrames.get(i) == null) {
                getState().mItemsFrames.put(i, scrapRect);
            } else {
                getState().mItemsFrames.get(i).set(scrapRect);
            }
        }
    }

    /**
     * Fill right of the center view
     *
     * @param recycler
     * @param startPosition start position to fill right
     * @param startOffset   layout start offset
     * @param rightEdge
     */
    private void fillRight(RecyclerView.Recycler recycler, int startPosition, int startOffset, int rightEdge) {
        View scrap;
        int topOffset;
        int scrapWidth, scrapHeight;
        Rect scrapRect = new Rect();
        int height = getVerticalSpace();
        for (int i = startPosition; i < getItemCount() && startOffset < rightEdge; i++) {
            scrap = recycler.getViewForPosition(i);
            addView(scrap);
            measureChildWithMargins(scrap, 0, 0);
            scrapWidth = getDecoratedMeasuredWidth(scrap);
            scrapHeight = getDecoratedMeasuredHeight(scrap);
            topOffset = (int) (getPaddingTop() + (height - scrapHeight) / 2.0f);
            scrapRect.set(startOffset, topOffset, startOffset + scrapWidth, topOffset + scrapHeight);
            layoutDecorated(scrap, scrapRect.left, scrapRect.top, scrapRect.right, scrapRect.bottom);
            startOffset = scrapRect.right;
            mLastVisiblePos = i;
            if (getState().mItemsFrames.get(i) == null) {
                getState().mItemsFrames.put(i, scrapRect);
            } else {
                getState().mItemsFrames.get(i).set(scrapRect);
            }
        }
    }

    /**
     * Fill top of the center view
     *
     * @param recycler
     * @param startPosition start position to fill top
     * @param startOffset   layout start offset
     * @param topEdge       top edge of the RecycleView
     */
    private void fillTop(RecyclerView.Recycler recycler, int startPosition, int startOffset, int topEdge) {
        View scrap;
        int leftOffset;
        int scrapWidth, scrapHeight;
        Rect scrapRect = new Rect();
        int width = getHorizontalSpace();
        for (int i = startPosition; i > 0 && startOffset > topEdge; i--) {
            scrap = recycler.getViewForPosition(i);
            addView(scrap);
            measureChildWithMargins(scrap, 0, 0);
            scrapWidth = getDecoratedMeasuredWidth(scrap);
            scrapHeight = getDecoratedMeasuredHeight(scrap);
            leftOffset = (int) (getPaddingLeft() + (width - scrapWidth) / 2.0f);
            scrapRect.set(leftOffset, startOffset - scrapHeight, leftOffset + scrapWidth, startOffset);
            layoutDecorated(scrap, scrapRect.left, scrapRect.top, scrapRect.right, scrapRect.bottom);
            startOffset = scrapRect.top;
            mFirstVisiblePosition = i;
            if (getState().mItemsFrames.get(i) == null) {
                getState().mItemsFrames.put(i, scrapRect);
            } else {
                getState().mItemsFrames.get(i).set(scrapRect);
            }
        }
    }

    /**
     * Fill bottom of the center view
     *
     * @param recycler
     * @param startPosition start position to fill bottom
     * @param startOffset   layout start offset
     * @param bottomEdge    bottom edge of the RecycleView
     */
    private void fillBottom(RecyclerView.Recycler recycler, int startPosition, int startOffset, int bottomEdge) {
        View scrap;
        int leftOffset;
        int scrapWidth, scrapHeight;
        Rect scrapRect = new Rect();
        int width = getHorizontalSpace();
        for (int i = startPosition; i < getItemCount() && startOffset < bottomEdge; i++) {
            scrap = recycler.getViewForPosition(i);
            addView(scrap);
            measureChildWithMargins(scrap, 0, 0);
            scrapWidth = getDecoratedMeasuredWidth(scrap);
            scrapHeight = getDecoratedMeasuredHeight(scrap);
            leftOffset = (int) (getPaddingLeft() + (width - scrapWidth) / 2.0f);
            scrapRect.set(leftOffset, startOffset, leftOffset + scrapWidth, startOffset + scrapHeight);
            layoutDecorated(scrap, scrapRect.left, scrapRect.top, scrapRect.right, scrapRect.bottom);
            startOffset = scrapRect.bottom;
            mLastVisiblePos = i;
            if (getState().mItemsFrames.get(i) == null) {
                getState().mItemsFrames.put(i, scrapRect);
            } else {
                getState().mItemsFrames.get(i).set(scrapRect);
            }
        }
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


        if (mItemTransformer != null) {
            View child;
            for (int i = 0; i < getChildCount(); i++) {
                child = getChildAt(i);
                mItemTransformer.transformItem(child, getPosition(child), mOrientation, getOrientationHelper(), scrollDelta);
            }
        }
    }

    /**
     * @param recycler
     * @param state
     * @param dy
     */
    private void fillWithVertical(RecyclerView.Recycler recycler, RecyclerView.State state, int dy) {
        if (DEBUG) {
            Log.d(TAG, "fillWithVertical: dy:" + dy);
        }
        int topEdge = getOrientationHelper().getStartAfterPadding();
        int bottomEdge = getOrientationHelper().getEndAfterPadding();

        //1.根据滑动方向，回收越界子View
        View child;
        if (getChildCount() > 0) {
            if (dy >= 0) {
                //remove and recycle the top off screen view
                int fixIndex = 0;
                for (int i = 0; i < getChildCount(); i++) {
                    child = getChildAt(i + fixIndex);
                    if (getDecoratedBottom(child) - dy < topEdge) {
                        if (DEBUG) {
                            Log.v(TAG, "fillWithVertical: removeAndRecycleView:" + getPosition(child) + ",bottom:" + getDecoratedBottom(child));
                        }
                        removeAndRecycleView(child, recycler);
                        mFirstVisiblePosition++;
                        fixIndex--;
                    } else {
                        if (DEBUG) {
                            Log.d(TAG, "fillWithVertical: break:" + getPosition(child) + ",bottom:" + getDecoratedBottom(child));
                        }
                        break;
                    }
                }
            } else { //dy<0
                //remove and recycle the bottom off screen view
                for (int i = getChildCount() - 1; i >= 0; i--) {
                    child = getChildAt(i);
                    if (getDecoratedTop(child) - dy > bottomEdge) {
                        if (DEBUG) {
                            Log.v(TAG, "fillWithVertical: removeAndRecycleView:" + getPosition(child));
                        }
                        removeAndRecycleView(child, recycler);
                        mLastVisiblePos--;
                    } else {
                        break;
                    }
                }
            }

        }
        //开始进行布局的位置
        int startPosition = mFirstVisiblePosition;
        int startOffset = -1;
        int scrapWidth, scrapHeight;
        Rect scrapRect;
        int width = getHorizontalSpace();
        int leftOffset;
        View scrap;
        //2.根据不同滑动方向，为空余位置进行布局
        if (dy >= 0) {
            if (getChildCount() != 0) {
                View lastView = getChildAt(getChildCount() - 1);
                startPosition = getPosition(lastView) + 1;
                startOffset = getDecoratedBottom(lastView);
            }
            //考虑边界和数量
            for (int i = startPosition; i < getItemCount() && startOffset < bottomEdge + dy; i++) {
                scrapRect = getState().mItemsFrames.get(i);
                scrap = recycler.getViewForPosition(i);
                addView(scrap);
                if (scrapRect == null) {
                    scrapRect = new Rect();
                    getState().mItemsFrames.put(i, scrapRect);
                    measureChildWithMargins(scrap, 0, 0);
                    scrapWidth = getDecoratedMeasuredWidth(scrap);
                    scrapHeight = getDecoratedMeasuredHeight(scrap);
                } else {
                    scrapWidth = scrapRect.right - scrapRect.left;
                    scrapHeight = scrapRect.bottom - scrapRect.top;
                }
                leftOffset = (int) (getPaddingLeft() + (width - scrapWidth) / 2.0f);
                if (startOffset == -1 && startPosition == 0) {
                    //第0个View，居中处理
                    int top = (int) (getPaddingTop() + (getVerticalSpace() - scrapHeight) / 2.f);
                    scrapRect.set(leftOffset, top, leftOffset + scrapWidth, top + scrapHeight);
                } else {
                    scrapRect.set(leftOffset, startOffset, leftOffset + scrapWidth, startOffset + scrapHeight);
                }
                layoutDecorated(scrap, scrapRect.left, scrapRect.top, scrapRect.right, scrapRect.bottom);
                startOffset = scrapRect.bottom;
                mLastVisiblePos = i;
                if (DEBUG) {
                    Log.d(TAG, "fillWithVertical: add view:" + i + ",startOffset:" + startOffset + ",mLastVisiblePos:" + mLastVisiblePos + ",bottomEdge" + bottomEdge);
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
            for (int i = startPosition; i >= 0 && startOffset > topEdge + dy; i--) {
                scrapRect = getState().mItemsFrames.get(i);
                scrap = recycler.getViewForPosition(i);
                addView(scrap, 0);
                if (scrapRect == null) {
                    scrapRect = new Rect();
                    getState().mItemsFrames.put(i, scrapRect);
                    measureChildWithMargins(scrap, 0, 0);
                    scrapWidth = getDecoratedMeasuredWidth(scrap);
                    scrapHeight = getDecoratedMeasuredHeight(scrap);
                } else {
                    scrapWidth = scrapRect.right - scrapRect.left;
                    scrapHeight = scrapRect.bottom - scrapRect.top;
                }
                leftOffset = (int) (getPaddingLeft() + (width - scrapWidth) / 2.0f);
                scrapRect.set(leftOffset, startOffset - scrapHeight, leftOffset + scrapWidth, startOffset);
                layoutDecorated(scrap, scrapRect.left, scrapRect.top, scrapRect.right, scrapRect.bottom);
                startOffset = scrapRect.top;
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
        if (DEBUG) {
            Log.v(TAG, "fillWithHorizontal() called with: dx = [" + dx + "],leftEdge:" + leftEdge + ",rightEdge:" + rightEdge);
        }
        //1.根据滑动方向，回收越界子View
        View child;
        if (getChildCount() > 0) {
            if (dx >= 0) {
                //remove and recycle the left off screen view
                int fixIndex = 0;
                for (int i = 0; i < getChildCount(); i++) {
                    child = getChildAt(i + fixIndex);
                    if (getDecoratedRight(child) - dx < leftEdge) {
                        removeAndRecycleView(child, recycler);
                        mFirstVisiblePosition++;
                        fixIndex--;
                        if (DEBUG) {
                            Log.v(TAG, "fillWithHorizontal:removeAndRecycleView:" + getPosition(child) + " mFirstVisiblePosition change to:" + mFirstVisiblePosition);
                        }
                    } else {
                        break;
                    }
                }
            } else { //dx<0
                //remove and recycle the right off screen view
                for (int i = getChildCount() - 1; i >= 0; i--) {
                    child = getChildAt(i);
                    if (getDecoratedLeft(child) - dx > rightEdge) {
                        removeAndRecycleView(child, recycler);
                        mLastVisiblePos--;
                        if (DEBUG) {
                            Log.v(TAG, "fillWithHorizontal:removeAndRecycleView:" + getPosition(child) + "mLastVisiblePos change to:" + mLastVisiblePos);
                        }
                    }
                }
            }

        }
        //开始进行布局的位置
        int startPosition = mFirstVisiblePosition;
        int startOffset = -1;
        int scrapWidth, scrapHeight;
        Rect scrapRect;
        int height = getVerticalSpace();
        int topOffset;
        View scrap;
        //2.根据不同滑动方向，为空余位置进行布局
        if (dx >= 0) {
            if (getChildCount() != 0) {
                View lastView = getChildAt(getChildCount() - 1);
                startPosition = getPosition(lastView) + 1; //下一个View的Position
                startOffset = getDecoratedRight(lastView);
                if (DEBUG) {
                    Log.d(TAG, "fillWithHorizontal:to right startPosition:" + startPosition + ",startOffset:" + startOffset + ",rightEdge:" + rightEdge);
                }
            }
            //考虑边界和数量
            for (int i = startPosition; i < getItemCount() && startOffset < rightEdge + dx; i++) {
                scrapRect = getState().mItemsFrames.get(i);
                scrap = recycler.getViewForPosition(i);
                addView(scrap);
                if (scrapRect == null) {
                    scrapRect = new Rect();
                    getState().mItemsFrames.put(i, scrapRect);
                    measureChildWithMargins(scrap, 0, 0);
                    scrapWidth = getDecoratedMeasuredWidth(scrap);
                    scrapHeight = getDecoratedMeasuredHeight(scrap);
                } else {
                    scrapWidth = scrapRect.right - scrapRect.left;
                    scrapHeight = scrapRect.bottom - scrapRect.top;
                }
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
                mLastVisiblePos = i;
                if (DEBUG) {
                    Log.d(TAG, "fillWithHorizontal,layout:mLastVisiblePos: " + mLastVisiblePos);
                }
            }
        } else {
            //dx<0
            if (getChildCount() > 0) {
                View firstView = getChildAt(0);
                startPosition = getPosition(firstView) - 1; //前一个View的position
                startOffset = getDecoratedLeft(firstView);
                if (DEBUG) {
                    Log.d(TAG, "fillWithHorizontal:to left startPosition:" + startPosition + ",startOffset:" + startOffset + ",leftEdge:" + leftEdge + ",child count:" + getChildCount());
                }
            }
            for (int i = startPosition; i >= 0 && startOffset > leftEdge + dx; i--) {
                scrapRect = getState().mItemsFrames.get(i);
                scrap = recycler.getViewForPosition(i);
                //顺序很重要,举个例子，本来添加顺序是【0，1，2，3】没错，但手指右滑后，需要不断在左边空白填充View，不指定index的情况下就可能就会出现这样的情况【3，0，1，2】
                addView(scrap, 0);
                if (scrapRect == null) {
                    scrapRect = new Rect();
                    getState().mItemsFrames.put(i, scrapRect);
                    measureChildWithMargins(scrap, 0, 0);
                    scrapWidth = getDecoratedMeasuredWidth(scrap);
                    scrapHeight = getDecoratedMeasuredHeight(scrap);
                } else {
                    scrapWidth = scrapRect.right - scrapRect.left;
                    scrapHeight = scrapRect.bottom - scrapRect.top;
                }
                topOffset = (int) (getPaddingTop() + (height - scrapHeight) / 2.0f);
                scrapRect.set(startOffset - scrapWidth, topOffset, startOffset, topOffset + scrapHeight);
                layoutDecorated(scrap, scrapRect.left, scrapRect.top, scrapRect.right, scrapRect.bottom);
                startOffset = scrapRect.left;
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

    private int calculateScrollDirectionForPosition(int position) {
        if (getChildCount() == 0) {
            return LAYOUT_START;
        }
        final int firstChildPos = mFirstVisiblePosition;
        return position < firstChildPos ? LAYOUT_START : LAYOUT_END;
    }

    @Override
    public PointF computeScrollVectorForPosition(int targetPosition) {
        final int direction = calculateScrollDirectionForPosition(targetPosition);
        PointF outVector = new PointF();
        if (direction == 0) {
            return null;
        }
        if (mOrientation == HORIZONTAL) {
            outVector.x = direction;
            outVector.y = 0;
        } else {
            outVector.x = 0;
            outVector.y = direction;
        }
        return outVector;
    }

    /**
     * @author chensuilun
     */
    class State {
        /**
         * Record all item view 's last position after last layout
         */
        SparseArray<Rect> mItemsFrames;

        /**
         * RecycleView 's current scroll distance since first layout
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
        if (dx > 0) {
            //If the current last child in RecycleView is the last one of Adapter, fix actual distance scrolled
            if (getPosition(getChildAt(getChildCount() - 1)) == getItemCount() - 1) {
                child = getChildAt(getChildCount() - 1);
                delta = -Math.max(0, Math.min(dx, (child.getRight() - child.getLeft()) / 2 + child.getLeft() - parentCenter));
            }
        } else {
            //If the current first child in RecycleView is the first one of Adapter, fix actual distance scrolled
            if (mFirstVisiblePosition == 0) {
                child = getChildAt(0);
                delta = -Math.min(0, Math.max(dx, ((child.getRight() - child.getLeft()) / 2 + child.getLeft()) - parentCenter));
            }
        }
        if (DEBUG) {
            Log.d(TAG, "scrollHorizontallyBy: dx:" + dx + ",fixed:" + delta);
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
        if (dy > 0) {
            //If the current last child in RecycleView is the last one of Adapter, fix actual distance scrolled
            if (getPosition(getChildAt(getChildCount() - 1)) == getItemCount() - 1) {
                child = getChildAt(getChildCount() - 1);
                delta = -Math.max(0, Math.min(dy, (getDecoratedBottom(child) - getDecoratedTop(child)) / 2 + getDecoratedTop(child) - parentCenter));
            }
        } else {
            //If the current first child in RecycleView is the first one of Adapter, fix actual distance scrolled
            if (mFirstVisiblePosition == 0) {
                child = getChildAt(0);
                delta = -Math.min(0, Math.max(dy, (getDecoratedBottom(child) - getDecoratedTop(child)) / 2 + getDecoratedTop(child) - parentCenter));
            }
        }
        if (DEBUG) {
            Log.d(TAG, "scrollVerticallyBy: dy:" + dy + ",fixed:" + delta);
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

    /**
     * @author chensuilun
     */
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


    private ItemTransformer mItemTransformer;


    public void setItemTransformer(ItemTransformer itemTransformer) {
        mItemTransformer = itemTransformer;
    }

    /**
     * A ItemTransformer is invoked whenever a attached item is scrolled.
     * This offers an opportunity for the application to apply a custom transformation
     * to the item views using animation properties.
     */
    public interface ItemTransformer {
        /**
         * Apply a property transformation to the given item.
         *
         * @param item              Apply the transformation to this item
         * @param position          This item's position in Adapter
         * @param orientation       See {@link GalleryLayoutManager#mOrientation}
         * @param orientationHelper See {@link OrientationHelper}
         * @param pendingOffset     The pending offset that this item will scroll by.
         */
        void transformItem(View item, float position, int orientation, OrientationHelper orientationHelper, int pendingOffset);
    }

    /**
     * Listen for changes to the selected item
     *
     * @author chensuilun
     */
    public interface OnItemSelectedListener {
        /**
         * @param item         The current selected view
         * @param position     The current selected view's position
         * @param recyclerView The RecyclerView which item view belong to.
         */
        void onItemSelected(View item, int position, RecyclerView recyclerView);
    }

    private OnItemSelectedListener mOnItemSelectedListener;

    public void setOnItemSelectedListener(OnItemSelectedListener onItemSelectedListener) {
        mOnItemSelectedListener = onItemSelectedListener;
    }

    public void attach(RecyclerView recyclerView) {
        this.attach(recyclerView, -1);
    }

    /**
     * @param recyclerView
     * @param selectedPosition
     */
    public void attach(RecyclerView recyclerView, int selectedPosition) {
        if (recyclerView == null) {
            throw new IllegalArgumentException("The attach RecycleView must not null!!");
        }
        mInitialSelectedPosition = selectedPosition;
        recyclerView.setLayoutManager(this);
//        mSnapHelper.attachToRecyclerView(recyclerView);
        recyclerView.addOnScrollListener(mInnerScrollListener);
    }


    public void setCallbackInFling(boolean callbackInFling) {
        mCallbackInFling = callbackInFling;
    }

    /**
     * Inner Listener to listen for changes to the selected item
     *
     * @author chensuilun
     */
    private class InnerScrollListener extends RecyclerView.OnScrollListener {
        int mPreSelectedPosition = -1;
        View mPreSelectedView;
        int mState;

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            View snap = mSnapHelper.findSnapView(recyclerView.getLayoutManager());
            if (snap != null) {
                int selectedPosition = recyclerView.getLayoutManager().getPosition(snap);
                if (selectedPosition != mPreSelectedPosition) {
                    if (!mCallbackInFling && mState != SCROLL_STATE_IDLE) {
                        return;
                    }
                    if (mPreSelectedView != null) {
                        mPreSelectedView.setSelected(false);
                    }
                    mPreSelectedView = snap;
                    mPreSelectedView.setSelected(true);
                    mPreSelectedPosition = selectedPosition;
                    if (mOnItemSelectedListener != null) {
                        mOnItemSelectedListener.onItemSelected(snap, mPreSelectedPosition, recyclerView);
                    }
                }
            }
            if (DEBUG) {
                Log.v(TAG, "onScrolled: dx:" + dx + ",dy:" + dy);
            }
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            mState = newState;
            if (DEBUG) {
                Log.v(TAG, "onScrollStateChanged: " + newState);
            }
            if (mState == SCROLL_STATE_IDLE) {
                View snap = mSnapHelper.findSnapView(recyclerView.getLayoutManager());
                if (snap != null) {
                    int selectedPosition = recyclerView.getLayoutManager().getPosition(snap);
                    if (selectedPosition != mPreSelectedPosition) {
                        if (mPreSelectedView != null) {
                            mPreSelectedView.setSelected(false);
                        }
                        mPreSelectedView = snap;
                        mPreSelectedView.setSelected(true);
                        mPreSelectedPosition = selectedPosition;
                        if (mOnItemSelectedListener != null) {
                            mOnItemSelectedListener.onItemSelected(snap, mPreSelectedPosition, recyclerView);
                        }
                    }
                }
            }
        }
    }


    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        GallerySmoothScroller linearSmoothScroller = new GallerySmoothScroller(recyclerView.getContext());
        linearSmoothScroller.setTargetPosition(position);
        startSmoothScroll(linearSmoothScroller);
    }

    /**
     * Implement to support {@link GalleryLayoutManager#smoothScrollToPosition(RecyclerView, RecyclerView.State, int)}
     */
    private class GallerySmoothScroller extends LinearSmoothScroller {

        public GallerySmoothScroller(Context context) {
            super(context);
        }

        /**
         * Calculates the horizontal scroll amount necessary to make the given view in center of the RecycleView
         *
         * @param view The view which we want to make in center of the RecycleView
         * @return The horizontal scroll amount necessary to make the view in center of the RecycleView
         */
        public int calculateDxToMakeCentral(View view) {
            final RecyclerView.LayoutManager layoutManager = getLayoutManager();
            if (layoutManager == null || !layoutManager.canScrollHorizontally()) {
                return 0;
            }
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();
            final int left = layoutManager.getDecoratedLeft(view) - params.leftMargin;
            final int right = layoutManager.getDecoratedRight(view) + params.rightMargin;
            final int start = layoutManager.getPaddingLeft();
            final int end = layoutManager.getWidth() - layoutManager.getPaddingRight();
            final int childCenter = left + (int) ((right - left) / 2.0f);
            final int containerCenter = (int) ((end - start) / 2.f);
            return containerCenter - childCenter;
        }

        /**
         * Calculates the vertical scroll amount necessary to make the given view in center of the RecycleView
         *
         * @param view The view which we want to make in center of the RecycleView
         * @return The vertical scroll amount necessary to make the view in center of the RecycleView
         */
        public int calculateDyToMakeCentral(View view) {
            final RecyclerView.LayoutManager layoutManager = getLayoutManager();
            if (layoutManager == null || !layoutManager.canScrollVertically()) {
                return 0;
            }
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)
                    view.getLayoutParams();
            final int top = layoutManager.getDecoratedTop(view) - params.topMargin;
            final int bottom = layoutManager.getDecoratedBottom(view) + params.bottomMargin;
            final int start = layoutManager.getPaddingTop();
            final int end = layoutManager.getHeight() - layoutManager.getPaddingBottom();
            final int childCenter = top + (int) ((bottom - top) / 2.0f);
            final int containerCenter = (int) ((end - start) / 2.f);
            return containerCenter - childCenter;
        }


        @Override
        protected void onTargetFound(View targetView, RecyclerView.State state, Action action) {
            final int dx = calculateDxToMakeCentral(targetView);
            final int dy = calculateDyToMakeCentral(targetView);
            final int distance = (int) Math.sqrt(dx * dx + dy * dy);
            final int time = calculateTimeForDeceleration(distance);
            if (time > 0) {
                action.update(-dx, -dy, time, mDecelerateInterpolator);
            }
        }
    }
}
