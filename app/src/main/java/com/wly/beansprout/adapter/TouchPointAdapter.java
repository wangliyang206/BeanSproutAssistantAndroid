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
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.wly.beansprout.R;
import com.wly.beansprout.bean.TouchPoint;
import com.wly.beansprout.utils.SpUtils;

import java.util.ArrayList;
import java.util.List;

public class TouchPointAdapter extends RecyclerView.Adapter<TouchPointAdapter.TouchPointHolder> implements View.OnClickListener {

    private final AsyncListDiffer<TouchPoint> differ = new AsyncListDiffer<>(this, new TouchPointDiffCallback());
    private OnItemClickListener onItemClickListener;

    /**
     * 对外提供数据源
     */
    public List<TouchPoint> getTouchPointList() {
        return differ.getCurrentList();
    }

    /**
     * 设置数据源
     */
    public void setTouchPointList(List<TouchPoint> touchPointList) {
        differ.submitList(touchPointList); // 替换全局刷新
    }

    /**
     * 删除一条数据
     */
    public void onRemove(Context context, int position) {
        List<TouchPoint> newList = new ArrayList<>(differ.getCurrentList());
        newList.remove(position);
        SpUtils.delTouchPoints(context, position);
        differ.submitList(newList); // 触发智能局部刷新
    }

    /**
     * 关闭所有开始事件
     */
    public void closeStartClick(Context context) {
        List<TouchPoint> newList = onDeepCopy();
        for (TouchPoint touchPoint : newList) {
            touchPoint.setStartClick(false);
        }
        differ.submitList(newList); // 触发智能局部刷新

        // 重新保存到 轻文件
        SpUtils.setTouchPoints(context, newList);
    }

    /**
     * 设置开始点击事件
     */
    public void startItemClick(Context context, int position) {
        List<TouchPoint> newList = onDeepCopy();
        for (TouchPoint touchPoint : newList) {
            touchPoint.setStartClick(false);
        }
        newList.get(position).setStartClick(true);

        // 重新保存到 轻文件
        SpUtils.setTouchPoints(context, newList);
    }


    /**
     * 深度拷贝当前数据源
     */
    public List<TouchPoint> onDeepCopy() {
        List<TouchPoint> newList = new ArrayList<>();
        for (TouchPoint touchPoint : differ.getCurrentList()) {
            newList.add(touchPoint.copy());
        }
        return newList;
    }

    @NonNull
    @Override
    public TouchPointHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_touch_point, parent, false);
        return new TouchPointHolder(view, onItemClickListener);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull TouchPointHolder holder, int position) {
        TouchPoint touchPoint = getItem(position);

        holder.tvName.setText(touchPoint.getName());
        holder.tvOffset.setText("间隔(" + touchPoint.getDelay() + "ms)");
        holder.btDel.setVisibility(touchPoint.isStartClick() ? View.GONE : View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return differ.getCurrentList().size();
    }

    public TouchPoint getItem(int position) {
        return differ.getCurrentList().get(position);
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

    public class TouchPointHolder extends RecyclerView.ViewHolder {
        LinearLayout layout;
        TextView tvName, tvOffset;
        Button btDel;

        public TouchPointHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            layout = itemView.findViewById(R.id.item_touch_point);
            tvName = itemView.findViewById(R.id.tv_name);
            tvOffset = itemView.findViewById(R.id.tv_offset);
            btDel = itemView.findViewById(R.id.bt_delete);

            // 改用动态获取position的方式
            layout.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(v, position, getItem(position));
                }
            });

            btDel.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(v, position, getItem(position));
                }
            });
        }
    }

    // 新增DiffUtil对比逻辑
    private static class TouchPointDiffCallback extends DiffUtil.ItemCallback<TouchPoint> {
        @Override
        public boolean areItemsTheSame(@NonNull TouchPoint oldItem, @NonNull TouchPoint newItem) {
            // 根据坐标判断是否为同一项目
            return oldItem.getX() == newItem.getX() && oldItem.getY() == newItem.getY();
        }

        @Override
        public boolean areContentsTheSame(@NonNull TouchPoint oldItem, @NonNull TouchPoint newItem) {
            // 检查关键属性是否变化
            return oldItem.isStartClick() == newItem.isStartClick()
                    && oldItem.getFunctionType() == newItem.getFunctionType()
                    && oldItem.getDelay() == newItem.getDelay()
                    && oldItem.getName().equals(newItem.getName());
        }
    }
}
