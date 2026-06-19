package com.wly.beansprout.feature.floating

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.wly.beansprout.R
import com.wly.beansprout.core.utils.DensityUtil
import com.wly.beansprout.core.utils.ToastUtils
import com.wly.beansprout.data.model.TouchPoint

/**
 * 添加触控点的系统覆盖层弹窗
 *
 * 与旧项目 AddPointDialog 行为一致：
 * 1. 全屏半透明覆盖层，提示"点击屏幕任意位置记录"
 * 2. 用户点击屏幕后捕获 getRawX/getRawY 绝对坐标
 * 3. 显示输入表单（名称 + 间隔毫秒数）
 * 4. 保存后通过回调返回 TouchPoint
 *
 * 从 Service 上下文弹出，使用 TYPE_APPLICATION_OVERLAY 窗口类型。
 */
class AddPointDialog(
    context: Context,
    private val onSave: (TouchPoint) -> Unit
) : Dialog(context, R.style.NoTitleDialog) {

    private var capturedX = 0
    private var capturedY = 0
    private var isCoordinateCaptured = false

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (!isCoordinateCaptured && ev.action == MotionEvent.ACTION_UP) {
            isCoordinateCaptured = true
            capturedX = ev.rawX.toInt()
            capturedY = ev.rawY.toInt()
            onCoordinateCaptured()
            return true
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dp = { v: Float -> DensityUtil.dip2px(context, v) }

        // ── 根布局：全屏半透明背景 ──
        val root = FrameLayout(context).apply {
            setBackgroundColor(0x77000000)
            setPadding(dp(20f), dp(20f), dp(20f), dp(20f))
        }

        // ── 提示文字（初始可见） ──
        val hintView = TextView(context).apply {
            text = "点击屏幕任意位置记录"
            setTextColor(Color.WHITE)
            textSize = 30f
            alpha = 0.6f
            gravity = Gravity.CENTER
        }
        root.addView(hintView, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply { gravity = Gravity.CENTER })

        // ── 输入表单容器（初始隐藏，垂直居中） ──
        val formContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            visibility = View.GONE
        }

        val titleView = TextView(context).apply {
            text = "添加点击位置"
            setTextColor(Color.WHITE)
            textSize = 30f
        }

        val etName = EditText(context).apply {
            hint = "名称"
            textSize = 20f
        }

        val etTime = EditText(context).apply {
            hint = "点击间隔时间(单位/毫秒)"
            inputType = InputType.TYPE_CLASS_NUMBER
            textSize = 20f
        }

        val tipsView = TextView(context).apply {
            text = "建议设置500~800毫秒，低于500手机会爆炸"
            setTextColor(Color.WHITE)
            textSize = 12f
        }

        val btnRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val btCommit = Button(context).apply {
            text = "保存"
            textSize = 22f
        }
        val btCancel = Button(context).apply {
            text = "取消"
            textSize = 22f
        }

        // 组装表单
        val fieldWidth = dp(300f)
        val fieldParams = LinearLayout.LayoutParams(fieldWidth, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
            topMargin = dp(10f)
        }
        formContainer.addView(titleView)
        formContainer.addView(etName, fieldParams)
        formContainer.addView(etTime, fieldParams)
        formContainer.addView(tipsView, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = dp(4f) })

        val btnParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
            topMargin = dp(15f)
        }
        btnRow.addView(btCommit, btnParams)
        btnRow.addView(btCancel, btnParams)
        formContainer.addView(btnRow, LinearLayout.LayoutParams(fieldWidth, LinearLayout.LayoutParams.WRAP_CONTENT))

        root.addView(formContainer, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply { gravity = Gravity.CENTER })

        setContentView(root)

        // ── 按钮事件 ──
        btCommit.setOnClickListener {
            val name = etName.text.toString().trim()
            val msText = etTime.text.toString().trim()
            val ms = msText.toIntOrNull() ?: 0
            if (name.isEmpty() || ms <= 0) {
                ToastUtils.showToast(context, "名字或毫秒数错误")
                return@setOnClickListener
            }
            onSave(TouchPoint(name = name, x = capturedX, y = capturedY, delay = ms))
            dismiss()
        }

        btCancel.setOnClickListener { dismiss() }

        // ── 全屏窗口配置 ──
        window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
    }

    /**
     * 坐标捕获后：隐藏提示，显示输入表单
     */
    private fun onCoordinateCaptured() {
        val root = findViewById<FrameLayout>(android.R.id.content).getChildAt(0) as FrameLayout
        // hintView 是第一个子 View
        root.getChildAt(0).visibility = View.GONE
        // formContainer 是第二个子 View
        root.getChildAt(1).visibility = View.VISIBLE
    }

    override fun show() {
        window?.let { w ->
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                w.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
            } else {
                @Suppress("DEPRECATION")
                w.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT)
            }
        }
        super.show()
    }
}
