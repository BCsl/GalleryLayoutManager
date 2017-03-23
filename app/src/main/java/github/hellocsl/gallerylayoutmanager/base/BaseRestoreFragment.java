package github.hellocsl.gallerylayoutmanager.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import github.hellocsl.gallerylayoutmanager.BuildConfig;


/**
 * Created by chensuilun on 16-8-9.
 */
public abstract class BaseRestoreFragment extends Fragment {
    public static final String IS_SHOW = "is_show";
    protected View mRootView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (BuildConfig.DEBUG) {
            Log.d(getClass().getSimpleName(), "onCreate:");
        }
        if (savedInstanceState != null && getFragmentManager() != null) {
            boolean isShow = savedInstanceState.getBoolean(IS_SHOW, true);
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            if (BuildConfig.DEBUG) {
                Log.d(this.getClass().getSimpleName(), "restore show:" + isShow);
            }
            if (!isShow) {
                ft.hide(this);
            } else {
                ft.show(this);
            }
            ft.commit();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(IS_SHOW, !isHidden());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) {
            Log.d(getClass().getSimpleName(), "onCreateView:");
        }
        mRootView = onCreateContentView(inflater, container, savedInstanceState);
        return mRootView;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (BuildConfig.DEBUG) {
            Log.v(getClass().getSimpleName(), "onViewCreated: ");
        }
        initView(view, savedInstanceState);
        initData(savedInstanceState);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (BuildConfig.DEBUG) {
            Log.d(getClass().getSimpleName(), "onStop: ");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (BuildConfig.DEBUG) {
            Log.d(getClass().getSimpleName(), "onDestroyView ");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (BuildConfig.DEBUG) {
            Log.d(getClass().getSimpleName(), "onDestroy: ");
        }
    }

    protected abstract View onCreateContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

    protected abstract void initView(View root, Bundle savedInstanceState);

    protected abstract void initData(Bundle savedInstanceState);


}
