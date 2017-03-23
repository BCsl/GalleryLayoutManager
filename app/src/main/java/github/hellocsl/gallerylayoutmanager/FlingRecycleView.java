package github.hellocsl.gallerylayoutmanager;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * Created by chensuilun on 2017/3/23.
 */
public class FlingRecycleView extends RecyclerView {
    private boolean mIsFlingAble = true;

    public void setFlingAble(boolean flingAble) {
        mIsFlingAble = flingAble;
    }

    public FlingRecycleView(Context context) {
        super(context);
    }

    public FlingRecycleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FlingRecycleView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean fling(int velocityX, int velocityY) {
        return mIsFlingAble ? super.fling(velocityX, velocityY) : false;
    }
}
