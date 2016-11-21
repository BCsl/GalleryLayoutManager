package github.hellocsl.gallerylayoutmanager.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import github.hellocsl.gallerylayoutmanager.R;


/**
 * Created by chensuilun on 2016/11/15.
 */
public class DemoAdapter extends RecyclerView.Adapter<DemoAdapter.ViewHolder> implements View.OnClickListener {

    private List<String> items;
    private OnItemClickListener mOnItemClickListener;

    public DemoAdapter(List<String> items) {
        this.items = items;
    }

    public DemoAdapter setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
        return this;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycle_demo, parent, false);
        v.setOnClickListener(this);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String item = items.get(position);
        holder.text.setText(item);
        holder.itemView.setTag(position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void onClick(final View v) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(v, (int) v.getTag());
        }
    }

    /**
     * @author chensuilun
     */
    protected static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView text;

        public ViewHolder(View itemView) {
            super(itemView);
            text = (TextView) itemView.findViewById(R.id.item_tv_title);
        }
    }

    /**
     * @author chensuilun
     */
    public interface OnItemClickListener {

        void onItemClick(View view, int position);

    }
}
