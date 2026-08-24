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

    // 自定义序列当前选中的序列 ID
    private var currentSequenceId: Int = 0

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
                    // 自定义模式：点击列表项不启动，提示使用开始按钮
                    if (isCustomMode()) {
                        ToastUtils.showToast(context, "请点击下方「开始执行序列」按钮启动")
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
            if (isCustomMode()) {
                showAddCustomActionDialog()
            } else {
                showAddPointDialog()
            }
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

        // 福袋/自定义序列：开始按钮
        btLuckyBagStart.setOnClickListener {
            if (isDoubleClick()) return@setOnClickListener
            if (isCustomMode()) {
                startCustomSequenceTouch()
            } else {
                startLuckyBagTouch()
            }
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

        // ── 关键：切换模式前先保存当前 adapter 状态，防止数据交叉覆盖 ──
        saveAdapterToRepo()

        // 福袋模式和自定义模式都显示方案选择器行
        llSchemeRow.visibility = if (isLuckyBagMode() || isCustomMode()) View.VISIBLE else View.GONE

        if (isCustomMode()) {
            // 自定义模式：恢复上次选中的序列，更新序列按钮
            currentSequenceId = repository.getCurrentCustomSequenceId()
            ensureThreeSequences()
            updateSequenceButtons()
        } else if (isLuckyBagMode()) {
            // 福袋模式：恢复上次选中的方案，更新方案按钮
            currentSchemeId = repository.getCurrentSchemeId()
            TouchEventManager.currentLuckyBagSchemeId = currentSchemeId
            ensureThreeSchemes()
            updateSchemeButtons()
        }

        // 按功能类型过滤触点列表（从仓库重新加载，确保数据隔离）
        loadFilteredList()
    }

    /**
     * 安全地将 adapter 当前数据保存回仓库
     * 仅在 adapter 有数据且处于需要隔离的模式时才保存，
     * 避免切换模式时旧 adapter 数据覆盖新模式的存储
     */
    private fun saveAdapterToRepo() {
        if (touchPointAdapter.itemCount == 0) return
        try {
            mergeAndSavePoints()
        } catch (_: Exception) {
            // 首次打开时可能还没设 functionType，忽略
        }
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
        if (::btAdd.isInitialized) {
            btAdd.text = if (isCustomMode()) "添加动作" else "添加触控点"
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

    private fun isCustomMode(): Boolean = functionType == TYPE_CUSTOM

    // ─────────────────────────────────────────────────
    //  列表加载（按功能类型过滤）
    // ─────────────────────────────────────────────────

    /** 根据当前功能类型加载过滤后的触点列表 */
    private fun loadFilteredList() {
        val points = when {
            isLuckyBagMode() -> repository.getTouchPointsByScheme(currentSchemeId)
            isCustomMode() -> repository.getTouchPointsBySequence(currentSequenceId)
            else -> repository.getNonLuckyBagTouchPoints()
        }
        touchPointAdapter.setTouchPointList(points)
        updateLuckyBagButtonVisibility()
        updateCustomStartButtonVisibility()
    }

    /** 更新福袋/自定义序列开始按钮的可见性和文字 */
    private fun updateLuckyBagButtonVisibility() {
        if (!::btLuckyBagStart.isInitialized) return
        val isLuckyBag = isLuckyBagMode() && !TouchEventManager.isTouching() && touchPointAdapter.itemCount > 0
        val isCustom = isCustomMode() && !TouchEventManager.isTouching() && touchPointAdapter.itemCount > 0

        if (isLuckyBag) {
            btLuckyBagStart.text = "开始抢福袋"
            btLuckyBagStart.visibility = View.VISIBLE
        } else if (isCustom) {
            btLuckyBagStart.text = "开始执行序列"
            btLuckyBagStart.visibility = View.VISIBLE
        } else {
            btLuckyBagStart.visibility = View.GONE
        }
    }

    /** 自定义序列模式：更新开始按钮可见性 */
    private fun updateCustomStartButtonVisibility() {
        updateLuckyBagButtonVisibility()
    }

    // ─────────────────────────────────────────────────
    //  数据合并保存（过滤列表 ↔ 完整存储）
    // ─────────────────────────────────────────────────

    /**
     * 将适配器中的过滤后列表合并回完整存储：
     * - 福袋模式：保留非当前方案的福袋点 + 非福袋点 + 替换当前方案的福袋点
     * - 自定义模式：保留福袋点 + 其他序列的自定义点 + 非自定义非福袋点 + 替换当前序列的点
     * - 其他模式：保留福袋点 + 自定义点 + 替换非福袋非自定义点
     */
    private fun mergeAndSavePoints() {
        val allPoints = repository.getTouchPoints()
        val mergedList = when {
            isLuckyBagMode() -> {
                val otherPoints = allPoints.filter {
                    it.functionType != TouchPoint.TYPE_LUCKY_BAG || it.schemeId != currentSchemeId
                }
                otherPoints + touchPointAdapter.getTouchPointList()
            }
            isCustomMode() -> {
                val otherPoints = allPoints.filter {
                    it.functionType != TouchPoint.TYPE_CUSTOM || it.sequenceId != currentSequenceId
                }
                otherPoints + touchPointAdapter.getTouchPointList()
            }
            else -> {
                val special = allPoints.filter {
                    it.functionType == TouchPoint.TYPE_LUCKY_BAG || it.functionType == TouchPoint.TYPE_CUSTOM
                }
                touchPointAdapter.getTouchPointList() + special
            }
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

        // 重置自定义序列模式标记
        TouchEventManager.isCustomSequenceMode = false

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
            // 同步更新 adapter，防止 onStart 中 mergeAndSavePoints 用旧列表覆盖新数据
            val currentList = touchPointAdapter.getTouchPointList().toMutableList()
            currentList.add(touchPoint)
            touchPointAdapter.setTouchPointList(currentList)
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

        // 重新绑定点击事件（福袋方案模式），防止自定义模式的监听器残留
        btScheme1.setOnClickListener { selectSchemeByIndex(0) }
        btScheme2.setOnClickListener { selectSchemeByIndex(1) }
        btScheme3.setOnClickListener { selectSchemeByIndex(2) }
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

    // ─────────────────────────────────────────────────
    //  自定义序列管理（三个固定按钮，复用福袋方案按钮行）
    // ─────────────────────────────────────────────────

    /** 确保至少有三个自定义序列存在 */
    private fun ensureThreeSequences() {
        val defaultNames = arrayOf("序列一", "序列二", "序列三")
        val sequences = repository.getCustomSequences().toMutableList()
        while (sequences.size < 3) {
            val newSeq = repository.addCustomSequence(defaultNames[sequences.size])
            sequences.add(newSeq)
        }
    }

    /** 刷新自定义序列按钮的选中状态 */
    private fun updateSequenceButtons() {
        val sequences = repository.getCustomSequences()
        val buttons = listOf(btScheme1, btScheme2, btScheme3)

        for (i in buttons.indices) {
            val btn = buttons[i]
            if (i < sequences.size) {
                btn.text = sequences[i].name
                btn.visibility = View.VISIBLE
                if (sequences[i].id == currentSequenceId) {
                    btn.setBackgroundColor(0xFF008577.toInt())
                    btn.setTypeface(null, Typeface.BOLD)
                } else {
                    btn.setBackgroundColor(0xFF666666.toInt())
                    btn.setTypeface(null, Typeface.NORMAL)
                }
            } else {
                btn.visibility = View.GONE
            }
        }

        // 重新绑定点击事件（自定义序列模式）
        btScheme1.setOnClickListener { selectSequenceByIndex(0) }
        btScheme2.setOnClickListener { selectSequenceByIndex(1) }
        btScheme3.setOnClickListener { selectSequenceByIndex(2) }
    }

    /** 选中指定索引的序列 */
    private fun selectSequenceByIndex(index: Int) {
        if (isDoubleClick()) return
        val sequences = repository.getCustomSequences()
        if (index >= sequences.size) return
        val selectedSeq = sequences[index]
        currentSequenceId = selectedSeq.id
        repository.setCurrentCustomSequenceId(currentSequenceId)
        TouchEventManager.currentCustomSequenceId = currentSequenceId
        updateSequenceButtons()
        loadFilteredList()
    }

    /**
     * 启动自定义序列执行
     */
    private fun startCustomSequenceTouch() {
        val sequencePoints = repository.getTouchPointsBySequence(currentSequenceId)
        if (sequencePoints.isEmpty()) {
            ToastUtils.showToast(context, "请先添加动作")
            return
        }

        btStop.visibility = View.VISIBLE
        btLuckyBagStart.visibility = View.GONE
        dismiss()

        // 设置自定义序列模式标记
        TouchEventManager.isCustomSequenceMode = true
        TouchEventManager.currentCustomSequenceId = currentSequenceId

        // 通知无障碍服务启动自定义序列循环
        AutoTouchService.instance?.handleTouchAction(TouchAction.START)

        // 通知悬浮窗开始跳绳动画
        val firstPoint = sequencePoints.first()
        listener?.onStartTouch(firstPoint.x, firstPoint.y)
    }

    /**
     * 弹出自定义动作录制弹窗（三步：选手势→采坐标→设间隔）
     */
    private fun showAddCustomActionDialog() {
        val addDialog = AddCustomActionDialog(context, currentSequenceId) { touchPoint ->
            val allPoints = repository.getTouchPoints().toMutableList()
            allPoints.add(touchPoint)
            repository.saveTouchPoints(allPoints)
            // 同步更新 adapter，防止 onStart 中 mergeAndSavePoints 用旧列表覆盖新数据
            val currentList = touchPointAdapter.getTouchPointList().toMutableList()
            currentList.add(touchPoint)
            touchPointAdapter.setTouchPointList(currentList)
        }
        addDialog.setOnDismissListener {
            show()
        }
        addDialog.show()
    }

    companion object {
        private const val TYPE_AUTO_REPLY = 7
        private const val TYPE_LUCKY_BAG = 8
        private const val TYPE_CUSTOM = 9
    }
}
