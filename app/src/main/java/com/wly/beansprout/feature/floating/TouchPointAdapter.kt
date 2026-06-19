package com.wly.beansprout.feature.floating

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wly.beansprout.R
import com.wly.beansprout.data.model.TouchPoint

/**
 * 悬浮窗菜单弹窗中的触点列表适配器
 */
class TouchPointAdapter : RecyclerView.Adapter<TouchPointAdapter.ViewHolder>() {

    private var touchPoints: MutableList<TouchPoint> = mutableListOf()
    private var onItemClickListener: ((View, Int, TouchPoint) -> Unit)? = null

    fun setTouchPointList(list: List<TouchPoint>) {
        touchPoints.clear()
        touchPoints.addAll(list)
        notifyDataSetChanged()
    }

    fun getTouchPointList(): MutableList<TouchPoint> = touchPoints

    fun onRemove(position: Int) {
        if (position in touchPoints.indices) {
            touchPoints.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, touchPoints.size)
        }
    }

    fun setOnItemClickListener(listener: (View, Int, TouchPoint) -> Unit) {
        onItemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_touch_point, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val point = touchPoints[position]

        holder.tvName.text = point.name
        holder.tvInfo.text = "坐标(${point.x}, ${point.y})  间隔${point.delay}ms"
        holder.tvStatus.text = if (point.isStartClick) "运行中" else ""
        holder.tvStatus.setTextColor(
            if (point.isStartClick) 0xFF4CAF50.toInt() else 0xFF999999.toInt()
        )

        // 点击整个 item → 开始触控
        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(holder.itemView, position, point)
        }

        // 点击删除按钮
        holder.btDelete.setOnClickListener {
            onItemClickListener?.invoke(it, position, point)
        }
    }

    override fun getItemCount(): Int = touchPoints.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tv_name)
        val tvInfo: TextView = itemView.findViewById(R.id.tv_info)
        val tvStatus: TextView = itemView.findViewById(R.id.tv_status)
        val btDelete: Button = itemView.findViewById(R.id.bt_delete)
    }
}
