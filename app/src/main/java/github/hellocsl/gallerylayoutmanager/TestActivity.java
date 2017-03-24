package github.hellocsl.gallerylayoutmanager;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import github.hellocsl.gallerylayoutmanager.adapter.DemoAdapter;
import github.hellocsl.gallerylayoutmanager.layout.impl.ScaleTransformer;
import github.hellocsl.layoutmanager.gallery.GalleryLayoutManager;

/**
 * Created by chensuilun on 2017/3/24.
 */

public class TestActivity extends AppCompatActivity {
    private static final String TAG = "TestActivity";
    @BindView(R.id.main_recycle1)
    RecyclerView mMainRecycle1;
    @BindView(R.id.main_tv_recycle_info_1)
    TextView mMainTvRecycleInfo1;
    @BindView(R.id.main_recycle2)
    RecyclerView mMainRecycle2;
    @BindView(R.id.main_tv_recycle_info_2)
    TextView mMainTvRecycleInfo2;
    @BindView(R.id.main_tv_recycle_info_3)
    TextView mMainTvRecycleInfo3;
    @BindView(R.id.main_btn_random)
    Button mMainBtnRandom;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_test);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        final List<String> title = new ArrayList<String>();
        int size = 50;
        for (int i = 0; i < size; i++) {
            title.add("Hello" + i);
        }
        GalleryLayoutManager layoutManager1 = new GalleryLayoutManager(GalleryLayoutManager.HORIZONTAL);
        layoutManager1.attach(mMainRecycle1, 30);
        layoutManager1.setItemTransformer(new ScaleTransformer());
        DemoAdapter demoAdapter1 = new DemoAdapter(title) {
            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                mMainTvRecycleInfo1.append("Create VH type:+" + viewType + "\n");
                return super.onCreateViewHolder(parent, viewType);
            }
        };
        demoAdapter1.setOnItemClickListener(new DemoAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                mMainRecycle1.smoothScrollToPosition(position);
            }
        });
        mMainRecycle1.setAdapter(demoAdapter1);

        final GalleryLayoutManager layoutManager2 = new GalleryLayoutManager(GalleryLayoutManager.VERTICAL);
        layoutManager2.attach(mMainRecycle2, 20);
        layoutManager2.setCallbackInFling(true);
        layoutManager2.setOnItemSelectedListener(new GalleryLayoutManager.OnItemSelectedListener() {
            @Override
            public void onItemSelected(RecyclerView recyclerView, View item, int position) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "onItemSelected: " + item.isSelected());
                }
                mMainTvRecycleInfo2.setText("selected:" + position + "\n");
            }
        });
        DemoAdapter demoAdapter2 = new DemoAdapter(title, DemoAdapter.VIEW_TYPE_TEXT);
        demoAdapter2.setOnItemClickListener(new DemoAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                mMainRecycle2.smoothScrollToPosition(position);
            }
        });
        mMainRecycle2.setAdapter(demoAdapter2);
    }

    private final Random mRandom = new Random();

    @OnClick(R.id.main_btn_random)
    public void onClick() {
        int selectPosition = mRandom.nextInt(50);
        mMainRecycle1.smoothScrollToPosition(selectPosition);
        mMainRecycle2.smoothScrollToPosition(selectPosition);
    }

}
