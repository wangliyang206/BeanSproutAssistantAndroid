package com.wly.beansprout.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wly.beansprout.R;
import com.wly.beansprout.bean.TouchPoint;
import com.wly.beansprout.utils.SpUtils;

import java.util.List;

public class TouchPointAdapter extends RecyclerView.Adapter<TouchPointAdapter.TouchPointHolder> implements View.OnClickListener {

    private List<TouchPoint> touchPointList;
    private OnItemClickListener onItemClickListener;

    public TouchPointAdapter() {
    }

    public List<TouchPoint> getTouchPointList() {
        return touchPointList;
    }

    /**
     * 加载数据
     */
    public void setTouchPointList(List<TouchPoint> touchPointList) {
        this.touchPointList = touchPointList;
        notifyDataSetChanged();
    }

    /**
     * 删除一条数据
     */
    public void onRemove(Context context, int position) {
        // 删除文件中的数据
        SpUtils.delTouchPoints(context, position);
        // 删除列表
        touchPointList.remove(position);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TouchPointHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_touch_point, parent, false);
        view.setOnClickListener(this);
        return new TouchPointHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull TouchPointHolder holder, int position) {
        TouchPoint touchPoint = getItem(position);

        holder.tvName.setText(touchPoint.getName());
        holder.tvOffset.setText("间隔(" + touchPoint.getDelay() + "ms)");
        holder.btDel.setVisibility(touchPoint.isStartClick() ? View.GONE : View.VISIBLE);

        holder.layout.setTag(position);
        holder.btDel.setTag(position);
        holder.layout.setOnClickListener(this);
        holder.btDel.setOnClickListener(this);
    }

    @Override
    public int getItemCount() {
        return touchPointList == null ? 0 : touchPointList.size();
    }

    public TouchPoint getItem(int position) {
        return touchPointList.get(position);
    }

    @Override
    public void onClick(View v) {
        if (onItemClickListener != null) {
            int postion = (int) v.getTag();
            TouchPoint touchPoint = getItem(postion);
            onItemClickListener.onItemClick(v, postion, touchPoint);
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position, TouchPoint touchPoint);
    }

    public static class TouchPointHolder extends RecyclerView.ViewHolder {
        LinearLayout layout;
        TextView tvName, tvOffset;
        Button btDel;

        public TouchPointHolder(@NonNull View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.item_touch_point);
            tvName = itemView.findViewById(R.id.tv_name);
            tvOffset = itemView.findViewById(R.id.tv_offset);
            btDel = itemView.findViewById(R.id.bt_delete);
        }
    }

}
