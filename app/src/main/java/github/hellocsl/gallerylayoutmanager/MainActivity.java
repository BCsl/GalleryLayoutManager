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
            title.add("Title:" + i);
        }
        GalleryLayoutManager layoutManager1 = new GalleryLayoutManager(this, GalleryLayoutManager.HORIZONTAL);
        mMainRecycle1.setLayoutManager(layoutManager1);
        DemoAdapter demoAdapter1 = new DemoAdapter(title) {
            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                mMainTv1.append("onCreateViewHolder\n");
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

        GalleryLayoutManager layoutManager2 = new GalleryLayoutManager(this, GalleryLayoutManager.VERTICAL);
        mMainRecycle2.setLayoutManager(layoutManager2);
        DemoAdapter demoAdapter2 = new DemoAdapter(title) {
            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                mMainTv2.append("onCreateViewHolder\n");
                return super.onCreateViewHolder(parent, viewType);
            }
        };
        mMainRecycle2.setAdapter(demoAdapter2);


        LinearSnapHelper snapHelper1 = new LinearSnapHelper();
        snapHelper1.attachToRecyclerView(mMainRecycle1);

        LinearSnapHelper snapHelper2 = new LinearSnapHelper();
        snapHelper2.attachToRecyclerView(mMainRecycle2);
    }
}
