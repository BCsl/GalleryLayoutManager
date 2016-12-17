package github.hellocsl.gallerylayoutmanager;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import github.hellocsl.gallerylayoutmanager.adapter.DemoAdapter;
import github.hellocsl.gallerylayoutmanager.layout.GalleryLayoutManager;
import github.hellocsl.gallerylayoutmanager.layout.impl.CurveTransformer;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.main_recycle1)
    RecyclerView mMainRecycle1;
    @BindView(R.id.main_recycle2)
    RecyclerView mMainRecycle2;
    @BindView(R.id.main_tv_recycle_info_1)
    TextView mMainTv1;
    @BindView(R.id.main_tv_recycle_info_2)
    TextView mMainTv2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        List<String> title = new ArrayList<String>();
        int size = 50;
        for (int i = 0; i < size; i++) {
            title.add("Hello" + i);
        }
        GalleryLayoutManager layoutManager1 = new GalleryLayoutManager(this, GalleryLayoutManager.HORIZONTAL);
        layoutManager1.setItemTransformer(new CurveTransformer());
        mMainRecycle1.setLayoutManager(layoutManager1);
        DemoAdapter demoAdapter1 = new DemoAdapter(title) {
            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                mMainTv1.append("CreateVH:+" + viewType + "\n");
                return super.onCreateViewHolder(parent, viewType);
            }
        };
        demoAdapter1.setOnItemClickListener(new DemoAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Toast.makeText(MainActivity.this, "position:" + position, Toast.LENGTH_SHORT).show();
            }
        });
        mMainRecycle1.setAdapter(demoAdapter1);

        final GalleryLayoutManager layoutManager2 = new GalleryLayoutManager(this, GalleryLayoutManager.VERTICAL);
        layoutManager2.setItemTransformer(new CurveTransformer());
        layoutManager2.attach(mMainRecycle2);
        layoutManager2.setCallbackInFling(false);
        layoutManager2.setOnItemSelectedListener(new GalleryLayoutManager.OnItemSelectedListener() {
            @Override
            public void onItemSelected(View item, int position, RecyclerView recyclerView) {
                mMainTv2.append("selected:" + position + "\n");
            }
        });
        DemoAdapter demoAdapter2 = new DemoAdapter(title, DemoAdapter.VIEW_TYPE_TEXT);
        demoAdapter2.setOnItemClickListener(new DemoAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                layoutManager2.scrollToPosition(position);
            }
        });
        mMainRecycle2.setAdapter(demoAdapter2);


        final LinearSnapHelper snapHelper1 = new LinearSnapHelper();
        snapHelper1.attachToRecyclerView(mMainRecycle1);


    }
}
