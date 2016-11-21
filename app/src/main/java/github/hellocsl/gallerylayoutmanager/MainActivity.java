package github.hellocsl.gallerylayoutmanager;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;

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
        DemoAdapter demoAdapter1 = new DemoAdapter(title);
        mMainRecycle1.setAdapter(demoAdapter1);

        GalleryLayoutManager layoutManager2 = new GalleryLayoutManager(this, GalleryLayoutManager.VERTICAL);
        mMainRecycle2.setLayoutManager(layoutManager2);
        DemoAdapter demoAdapter2 = new DemoAdapter(title);
        mMainRecycle2.setAdapter(demoAdapter2);

    }
}
