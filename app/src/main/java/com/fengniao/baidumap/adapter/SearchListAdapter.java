package com.fengniao.baidumap.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.baidu.mapapi.search.core.PoiInfo;
import com.fengniao.baidumap.R;

import java.util.List;


public class SearchListAdapter extends RecyclerView.Adapter<SearchListAdapter.MyViewHoler> implements View.OnClickListener {
    private Context context;
    private List<PoiInfo> list;
    private OnItemClickListener mOnItemClickListener;

    public void setmOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    public SearchListAdapter(Context context, List<PoiInfo> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public MyViewHoler onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_list_search, parent, false);
        MyViewHoler viewHoler = new MyViewHoler(view);
        view.setOnClickListener(this);
        return viewHoler;
    }

    @Override
    public void onBindViewHolder(MyViewHoler holder, int position) {
        holder.itemView.setTag(position);
        holder.name.setText(list.get(position).name);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public void onClick(View view) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(view, (Integer) view.getTag());
        }
    }

    public class MyViewHoler extends RecyclerView.ViewHolder {
        TextView name;

        public MyViewHoler(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.name);
        }
    }


    public interface OnItemClickListener {
        void onItemClick(View view, int positon);
}
}
