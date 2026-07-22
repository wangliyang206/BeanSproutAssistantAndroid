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
 * 自定义动作录制弹窗（三步式流程）
 *
 * 1. 选择手势类型
 * 2. 采集坐标
 * 3. 设置间隔并保存
 */
class AddCustomActionDialog(
    context: Context,
    private val sequenceId: Int,
    private val onSave: (TouchPoint) -> Unit
) : Dialog(context, R.style.NoTitleDialog) {

    private var currentStep = 1
    private var selectedGestureType = 0
    private var capturedX = 0
    private var capturedY = 0

    private lateinit var gestureStep: LinearLayout
    private lateinit var captureStep: FrameLayout
    private lateinit var formStep: LinearLayout
    private lateinit var etDelay: EditText
    private lateinit var summaryView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dp = { v: Float -> DensityUtil.dip2px(context, v) }

        val rootLayout = FrameLayout(context).apply {
            setBackgroundColor(0xCC000000.toInt())
            setPadding(dp(20f), dp(20f), dp(20f), dp(20f))
        }

        gestureStep = buildGestureStep(dp)
        rootLayout.addView(gestureStep, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply { gravity = Gravity.CENTER })

        captureStep = buildCaptureStep(dp)
        captureStep.visibility = View.GONE
        rootLayout.addView(captureStep, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ))

        formStep = buildFormStep(dp)
        formStep.visibility = View.GONE
        rootLayout.addView(formStep, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply { gravity = Gravity.CENTER })

        setContentView(rootLayout)
        window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (currentStep == 2 && ev.action == MotionEvent.ACTION_UP) {
            capturedX = ev.rawX.toInt()
            capturedY = ev.rawY.toInt()
            goToStep3()
            return true
        }
        return super.dispatchTouchEvent(ev)
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

    private fun buildGestureStep(dp: (Float) -> Int): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL

            val title = TextView(context).apply {
                text = "选择动作类型"
                setTextColor(Color.WHITE)
                textSize = 24f
                gravity = Gravity.CENTER
            }
            addView(title)

            val gestures = listOf(
                "单击" to TouchPoint.TYPE_SINGLE_CLICK,
                "双击" to TouchPoint.TYPE_DOUBLE_CLICK,
                "向上滑" to TouchPoint.TYPE_SLIDE_UP,
                "向下滑" to TouchPoint.TYPE_SLIDE_DOWN,
                "向左滑" to TouchPoint.TYPE_SLIDE_LEFT,
                "向右滑" to TouchPoint.TYPE_SLIDE_RIGHT
            )

            var row: LinearLayout? = null
            for ((index, pair) in gestures.withIndex()) {
                if (index % 2 == 0) {
                    row = LinearLayout(context).apply {
                        orientation = LinearLayout.HORIZONTAL
                        gravity = Gravity.CENTER
                    }
                    addView(row, LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { topMargin = dp(8f) })
                }

                val btn = Button(context).apply {
                    text = pair.first
                    textSize = 18f
                    setBackgroundColor(0xFF333333.toInt())
                    setTextColor(Color.WHITE)
                }
                btn.setOnClickListener {
                    selectedGestureType = pair.second
                    goToStep2()
                }
                row?.addView(btn, LinearLayout.LayoutParams(dp(140f), dp(50f)).apply {
                    setMargins(dp(4f), 0, dp(4f), 0)
                })
            }

            val btCancel = Button(context).apply {
                text = "取消"
                textSize = 16f
                setBackgroundColor(0xFF999999.toInt())
                setTextColor(Color.WHITE)
            }
            btCancel.setOnClickListener { dismiss() }
            addView(btCancel, LinearLayout.LayoutParams(dp(200f), dp(44f)).apply {
                topMargin = dp(16f)
            })
        }
    }

    private fun buildCaptureStep(dp: (Float) -> Int): FrameLayout {
        return FrameLayout(context).apply {
            val hint = TextView(context).apply {
                text = "点击屏幕任意位置记录坐标"
                setTextColor(Color.WHITE)
                textSize = 28f
                alpha = 0.7f
                gravity = Gravity.CENTER
            }
            addView(hint, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply { gravity = Gravity.CENTER })
        }
    }

    private fun buildFormStep(dp: (Float) -> Int): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL

            val titleView = TextView(context).apply {
                text = "设置动作间隔"
                setTextColor(Color.WHITE)
                textSize = 24f
            }
            addView(titleView)

            summaryView = TextView(context).apply {
                setTextColor(0xFFAAAAAA.toInt())
                textSize = 14f
                gravity = Gravity.CENTER
            }
            addView(summaryView, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(8f) })

            etDelay = EditText(context).apply {
                hint = "间隔时间(毫秒)"
                inputType = InputType.TYPE_CLASS_NUMBER
                textSize = 20f
                setText("500")
            }
            val fieldWidth = dp(280f)
            addView(etDelay, LinearLayout.LayoutParams(fieldWidth, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                topMargin = dp(12f)
            })

            val tipsView = TextView(context).apply {
                text = "建议设置500~800毫秒"
                setTextColor(Color.WHITE)
                textSize = 12f
            }
            addView(tipsView, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(4f) })

            val btnRow = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
            }

            val btSave = Button(context).apply {
                text = "保存"
                textSize = 20f
            }
            val btCancel = Button(context).apply {
                text = "取消"
                textSize = 20f
            }

            btSave.setOnClickListener {
                val msText = etDelay.text.toString().trim()
                val ms = msText.toIntOrNull() ?: 0
                if (ms <= 0) {
                    ToastUtils.showToast(context, "毫秒数必须大于0")
                    return@setOnClickListener
                }
                onSave(TouchPoint(
                    name = "${TouchPoint.getGestureName(selectedGestureType)}(${capturedX},${capturedY})",
                    x = capturedX,
                    y = capturedY,
                    delay = ms,
                    functionType = TouchPoint.TYPE_CUSTOM,
                    sequenceType = selectedGestureType,
                    sequenceId = this@AddCustomActionDialog.sequenceId
                ))
                dismiss()
            }

            btCancel.setOnClickListener { dismiss() }

            val btnParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                topMargin = dp(15f)
            }
            btnRow.addView(btSave, btnParams)
            btnRow.addView(btCancel, btnParams)
            addView(btnRow, LinearLayout.LayoutParams(fieldWidth, LinearLayout.LayoutParams.WRAP_CONTENT))
        }
    }

    private fun goToStep2() {
        currentStep = 2
        gestureStep.visibility = View.GONE
        captureStep.visibility = View.VISIBLE
        formStep.visibility = View.GONE
    }

    private fun goToStep3() {
        currentStep = 3
        gestureStep.visibility = View.GONE
        captureStep.visibility = View.GONE
        formStep.visibility = View.VISIBLE
        summaryView.text = "${TouchPoint.getGestureName(selectedGestureType)}  坐标($capturedX, $capturedY)"
    }
}
