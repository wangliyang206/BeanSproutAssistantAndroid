package com.wly.beansprout.feature.floating

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wly.beansprout.R
import com.wly.beansprout.core.TouchAction
import com.wly.beansprout.core.TouchEventManager
import com.wly.beansprout.core.utils.DensityUtil
import com.wly.beansprout.core.utils.ToastUtils
import com.wly.beansprout.data.model.TouchPoint
import com.wly.beansprout.data.repository.TouchPointRepository
import com.wly.beansprout.feature.accessibility.AutoTouchService

/**
 * 悬浮窗菜单弹窗
 *
 * 从 FloatingService 弹出，提供触控点的管理与操作入口：
 * - 添加触控点（标记当前功能类型）
 * - 停止触控
 * - 回复话术（自动回复模式下）
 * - 开始抢福袋（福袋模式下专属按钮）
 * - 退出助手
 *
 * 福袋模式（functionType == 8）：
 * - 列表只显示福袋专属触控点
 * - 点击列表项不启动（需点击"开始抢福袋"按钮）
 * - 按钮触发后按顺序循环执行所有福袋坐标
 *
 * 其他模式：
 * - 列表只显示非福袋触控点
 * - 点击列表项直接启动对应功能
 *
 * 使用传统 Dialog 而非 Compose，因为从 Service 上下文弹出。
 */
class FloatingMenuDialog(context: Context) : Dialog(context, R.style.NoTitleDialog) {

    private lateinit var btAdd: Button
    private lateinit var btStop: Button
    private lateinit var btReply: Button
    private lateinit var btExit: Button
    private lateinit var btLuckyBagStart: Button
    private lateinit var recyclerView: RecyclerView

    // 福袋方案按钮（固定三个）
    private lateinit var llSchemeRow: LinearLayout
    private lateinit var btScheme1: Button
    private lateinit var btScheme2: Button
    private lateinit var btScheme3: Button
    private var currentSchemeId: Int = 0

    private val touchPointAdapter = TouchPointAdapter()
    private val repository = TouchPointRepository(context)

    private var functionType: Int = 0
    private var luckybagTime: Int = 0
    private var listener: Listener? = null

    /** 防止快速重复点击 */
    private var lastClickTime: Long = 0L

    interface Listener {
        /** 开始触控动作 */
        fun onStartTouch(x: Int, y: Int)
        /** 停止触控 */
        fun onStopTouch()
        /** 退出助手 */
        fun onExitService()
        /** 添加触控点 — 跳转主 App 添加触点页面 */
        fun onAddTouchPoint()
        /** 编辑自动回复话术 */
        fun onEditReplyScript()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_menu)

        // 设置弹窗尺寸
        window?.setLayout(DensityUtil.dip2px(context, 350f), WindowManager.LayoutParams.WRAP_CONTENT)
        window?.setGravity(Gravity.CENTER)
        setCanceledOnTouchOutside(true)

        // 初始化控件
        btAdd = findViewById(R.id.bt_add)
        btStop = findViewById(R.id.bt_stop)
        btReply = findViewById(R.id.bt_reply)
        btExit = findViewById(R.id.bt_exit)
        btLuckyBagStart = findViewById(R.id.bt_lucky_bag_start)
        recyclerView = findViewById(R.id.rv)

        // 设置 RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = touchPointAdapter

        // ── 初始化福袋方案按钮 ──
        llSchemeRow = findViewById(R.id.ll_scheme_row)
        btScheme1 = findViewById(R.id.bt_scheme_1)
        btScheme2 = findViewById(R.id.bt_scheme_2)
        btScheme3 = findViewById(R.id.bt_scheme_3)

        // 确保至少有三个方案
        ensureThreeSchemes()

        // 恢复上次选中的方案
        currentSchemeId = repository.getCurrentSchemeId()
        TouchEventManager.currentLuckyBagSchemeId = currentSchemeId
        updateSchemeButtons()

        // 方案按钮点击
        btScheme1.setOnClickListener { selectSchemeByIndex(0) }
        btScheme2.setOnClickListener { selectSchemeByIndex(1) }
        btScheme3.setOnClickListener { selectSchemeByIndex(2) }

        // 设置 item 点击监听
        touchPointAdapter.setOnItemClickListener { view, position, touchPoint ->
            if (isDoubleClick()) return@setOnItemClickListener

            when (view.id) {
                R.id.item_touch_point -> {
                    // 福袋模式：点击列表项不启动，只允许删除
                    if (isLuckyBagMode()) {
                        ToastUtils.showToast(context, "请点击下方「开始抢福袋」按钮启动")
                        return@setOnItemClickListener
                    }

                    // 其他模式：点击即开始触控动作
                    startNonLuckyBagTouch(position)
                }

                R.id.bt_delete -> {
                    // 删除触点
                    touchPointAdapter.onRemove(position)
                    mergeAndSavePoints()
                    updateLuckyBagButtonVisibility()
                }
            }
        }

        // 按钮点击
        btAdd.setOnClickListener {
            if (isDoubleClick()) return@setOnClickListener
            dismiss()
            showAddPointDialog()
        }

        btStop.setOnClickListener {
            if (isDoubleClick()) return@setOnClickListener
            stopAllTouch()
        }

        btReply.setOnClickListener {
            if (isDoubleClick()) return@setOnClickListener
            dismiss()
            showReplyScriptDialog()
        }

        btExit.setOnClickListener {
            if (isDoubleClick()) return@setOnClickListener
            AutoTouchService.instance?.handleTouchAction(TouchAction.STOP)
            listener?.onExitService()
        }

        // 福袋专属：开始按钮
        btLuckyBagStart.setOnClickListener {
            if (isDoubleClick()) return@setOnClickListener
            startLuckyBagTouch()
        }

        // 弹窗关闭时：如果没有专属包名且之前暂停，则继续触控
        setOnDismissListener {
            if (TouchEventManager.appPackageName.value.isBlank() && TouchEventManager.isPaused()) {
                AutoTouchService.instance?.handleTouchAction(TouchAction.CONTINUE)
            }
        }
    }

    override fun show() {
        // 从 Service 上下文弹出 Dialog 必须设置系统级窗口类型，否则会 BadTokenException
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

    override fun onStart() {
        super.onStart()
        // 打开菜单时暂停触控（如果没有专属包名）
        if (TouchEventManager.appPackageName.value.isBlank()) {
            AutoTouchService.instance?.handleTouchAction(TouchAction.PAUSE)
        }
        // 福袋模式始终显示方案选择器
        llSchemeRow.visibility = if (isLuckyBagMode()) View.VISIBLE else View.GONE
        // 按功能类型过滤触点列表
        loadFilteredList()
    }

    /**
     * 设置功能类型和福袋时间
     */
    fun setFunctionType(functionType: Int, luckybagTime: Int) {
        this.functionType = functionType
        this.luckybagTime = luckybagTime

        if (::btReply.isInitialized) {
            if (TouchEventManager.isTouching()) {
                btReply.visibility = View.GONE
            } else {
                btReply.visibility = if (functionType == TYPE_AUTO_REPLY) View.VISIBLE else View.GONE
            }
        }
        if (::btLuckyBagStart.isInitialized) {
            updateLuckyBagButtonVisibility()
        }
    }

    fun setListener(listener: Listener) {
        this.listener = listener
    }

    // ─────────────────────────────────────────────────
    //  福袋模式判断
    // ─────────────────────────────────────────────────

    private fun isLuckyBagMode(): Boolean = functionType == TYPE_LUCKY_BAG

    // ─────────────────────────────────────────────────
    //  列表加载（按功能类型过滤）
    // ─────────────────────────────────────────────────

    /** 根据当前功能类型加载过滤后的触点列表 */
    private fun loadFilteredList() {
        val points = if (isLuckyBagMode()) {
            repository.getTouchPointsByScheme(currentSchemeId)
        } else {
            repository.getNonLuckyBagTouchPoints()
        }
        touchPointAdapter.setTouchPointList(points)
        updateLuckyBagButtonVisibility()
    }

    /** 更新福袋开始按钮的可见性 */
    private fun updateLuckyBagButtonVisibility() {
        if (!::btLuckyBagStart.isInitialized) return
        val showButton = isLuckyBagMode()
                && !TouchEventManager.isTouching()
                && touchPointAdapter.itemCount > 0
        btLuckyBagStart.visibility = if (showButton) View.VISIBLE else View.GONE
    }

    // ─────────────────────────────────────────────────
    //  数据合并保存（过滤列表 ↔ 完整存储）
    // ─────────────────────────────────────────────────

    /**
     * 将适配器中的过滤后列表合并回完整存储：
     * - 福袋模式：保留非当前方案的福袋点 + 非福袋点 + 替换当前方案的福袋点
     * - 非福袋模式：保留福袋点 + 替换非福袋点
     */
    private fun mergeAndSavePoints() {
        val allPoints = repository.getTouchPoints()
        val mergedList = if (isLuckyBagMode()) {
            // 保留：非福袋点 + 其他方案的福袋点
            val otherPoints = allPoints.filter {
                it.functionType != TouchPoint.TYPE_LUCKY_BAG || it.schemeId != currentSchemeId
            }
            otherPoints + touchPointAdapter.getTouchPointList()
        } else {
            val luckyBag = allPoints.filter { it.functionType == TouchPoint.TYPE_LUCKY_BAG }
            touchPointAdapter.getTouchPointList() + luckyBag
        }
        repository.saveTouchPoints(mergedList)
    }

    // ─────────────────────────────────────────────────
    //  非福袋模式：点击列表项启动
    // ─────────────────────────────────────────────────

    private fun startNonLuckyBagTouch(position: Int) {
        btReply.visibility = View.GONE
        btStop.visibility = View.VISIBLE
        btLuckyBagStart.visibility = View.GONE
        dismiss()

        // 更新触点数据：标记选中项、重置其他项
        val points = touchPointAdapter.getTouchPointList()
        val updatedPoints = points.mapIndexed { index, point ->
            point.copy(
                isStartClick = (index == position),
                functionType = if (index == position) functionType else point.functionType,
                luckyBagTime = if (index == position) luckybagTime else point.luckyBagTime
            )
        }
        touchPointAdapter.setTouchPointList(updatedPoints)
        mergeAndSavePoints()

        val selected = updatedPoints[position]

        // 通知无障碍服务开启触控
        AutoTouchService.instance?.handleTouchAction(TouchAction.START, selected)

        // 通知悬浮窗开始动画
        listener?.onStartTouch(selected.x, selected.y)
    }

    // ─────────────────────────────────────────────────
    //  福袋模式：开始按钮启动
    // ─────────────────────────────────────────────────

    private fun startLuckyBagTouch() {
        val luckyBagPoints = repository.getTouchPointsByScheme(currentSchemeId)
        if (luckyBagPoints.isEmpty()) {
            ToastUtils.showToast(context, "请先添加福袋触控点")
            return
        }

        btStop.visibility = View.VISIBLE
        btLuckyBagStart.visibility = View.GONE
        dismiss()

        // 将当前方案 ID 写入 TouchEventManager，供 AutoTouchService 读取
        TouchEventManager.currentLuckyBagSchemeId = currentSchemeId

        // 通知无障碍服务启动福袋坐标循环模式
        AutoTouchService.instance?.handleTouchAction(TouchAction.START)

        // 通知悬浮窗开始跳绳动画（福袋模式不跑到目标点）
        val firstPoint = luckyBagPoints.first()
        listener?.onStartTouch(firstPoint.x, firstPoint.y)
    }

    // ─────────────────────────────────────────────────
    //  停止触控
    // ─────────────────────────────────────────────────

    private fun stopAllTouch() {
        btReply.visibility = if (functionType == TYPE_AUTO_REPLY) View.VISIBLE else View.GONE
        btStop.visibility = View.GONE

        ToastUtils.showToast(context, "已停止触控")

        // 通知无障碍服务停止触控
        AutoTouchService.instance?.handleTouchAction(TouchAction.STOP)

        // 重置所有触点运行状态
        val updatedPoints = touchPointAdapter.getTouchPointList().map { it.copy(isStartClick = false) }
        touchPointAdapter.setTouchPointList(updatedPoints)
        mergeAndSavePoints()

        updateLuckyBagButtonVisibility()
        listener?.onStopTouch()
    }

    // ─────────────────────────────────────────────────
    //  工具方法
    // ─────────────────────────────────────────────────

    private fun isDoubleClick(): Boolean {
        val now = System.currentTimeMillis()
        val diff = now - lastClickTime
        return if (diff in 1..800L) {
            true
        } else {
            lastClickTime = now
            false
        }
    }

    /**
     * 显示自动回复话术编辑弹窗
     */
    private fun showReplyScriptDialog() {
        val currentScript = repository.getAutoReplyScript()

        val editText = EditText(context).apply {
            setText(currentScript)
            minLines = 8
            maxLines = 10
            gravity = Gravity.TOP or Gravity.START
            setPadding(
                DensityUtil.dip2px(context, 12f),
                DensityUtil.dip2px(context, 8f),
                DensityUtil.dip2px(context, 12f),
                DensityUtil.dip2px(context, 8f)
            )
        }

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                DensityUtil.dip2px(context, 16f),
                DensityUtil.dip2px(context, 8f),
                DensityUtil.dip2px(context, 16f),
                0
            )
        }

        val tipView = android.widget.TextView(context).apply {
            text = "支持多条话术随机回复。多条话术之间用英文';'做分割。最后一组结尾不用填英文';'符号。"
            textSize = 12f
            setTextColor(0xFFE53935.toInt())
        }
        container.addView(tipView)
        container.addView(editText, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = DensityUtil.dip2px(context, 8f) })

        val scrollView = ScrollView(context).apply {
            addView(container)
        }

        val dialog = AlertDialog.Builder(context)
            .setTitle("填入自动回复的话术")
            .setView(scrollView)
            .setPositiveButton("保存") { _, _ ->
                val newScript = editText.text.toString().trim()
                if (newScript.isNotEmpty()) {
                    repository.setAutoReplyScript(newScript)
                    ToastUtils.showToast(context, "话术已保存")
                }
            }
            .setNegativeButton("取消", null)
            .create()

        dialog.window?.let { w ->
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                w.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
            } else {
                @Suppress("DEPRECATION")
                w.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT)
            }
        }
        dialog.show()
    }

    /**
     * 弹出添加触控点覆盖层
     * 新触控点会被标记为当前功能类型（福袋模式 → TYPE_LUCKY_BAG）
     */
    private fun showAddPointDialog() {
        val addDialog = AddPointDialog(context, functionType, currentSchemeId) { touchPoint ->
            val allPoints = repository.getTouchPoints().toMutableList()
            allPoints.add(touchPoint)
            repository.saveTouchPoints(allPoints)
        }
        addDialog.setOnDismissListener {
            show()
        }
        addDialog.show()
    }

    // ─────────────────────────────────────────────────
    //  福袋方案管理（三个固定按钮）
    // ─────────────────────────────────────────────────

    /** 选中指定索引的方案 */
    private fun selectSchemeByIndex(index: Int) {
        if (isDoubleClick()) return
        val schemes = repository.getLuckyBagSchemes()
        if (index >= schemes.size) return
        val selectedScheme = schemes[index]
        currentSchemeId = selectedScheme.id
        repository.setCurrentSchemeId(currentSchemeId)
        TouchEventManager.currentLuckyBagSchemeId = currentSchemeId
        updateSchemeButtons()
        loadFilteredList()
    }

    /** 刷新三个方案按钮的选中状态 */
    private fun updateSchemeButtons() {
        val schemes = repository.getLuckyBagSchemes()
        val buttons = listOf(btScheme1, btScheme2, btScheme3)

        for (i in buttons.indices) {
            val btn = buttons[i]
            if (i < schemes.size) {
                btn.text = schemes[i].name
                btn.visibility = View.VISIBLE
                if (schemes[i].id == currentSchemeId) {
                    // 选中状态：主题色 + 粗体
                    btn.setBackgroundColor(0xFF008577.toInt())
                    btn.setTypeface(null, Typeface.BOLD)
                } else {
                    // 未选中：灰色
                    btn.setBackgroundColor(0xFF666666.toInt())
                    btn.setTypeface(null, Typeface.NORMAL)
                }
            } else {
                btn.visibility = View.GONE
            }
        }
    }

    /** 确保至少有三个方案存在（不足则自动创建） */
    private fun ensureThreeSchemes() {
        val defaultNames = arrayOf("方案一", "方案二", "方案三")
        val schemes = repository.getLuckyBagSchemes().toMutableList()
        while (schemes.size < 3) {
            val newScheme = repository.addScheme(defaultNames[schemes.size])
            schemes.add(newScheme)
        }
    }

    companion object {
        private const val TYPE_AUTO_REPLY = 7
        private const val TYPE_LUCKY_BAG = 8
    }
}
