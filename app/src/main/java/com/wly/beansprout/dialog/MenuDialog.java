package com.wly.beansprout.dialog;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wly.beansprout.R;
import com.wly.beansprout.TouchEventManager;
import com.wly.beansprout.adapter.TouchPointAdapter;
import com.wly.beansprout.bean.TouchEvent;
import com.wly.beansprout.bean.TouchPoint;
import com.wly.beansprout.utils.DensityUtil;
import com.wly.beansprout.utils.DialogUtils;
import com.wly.beansprout.utils.GsonUtils;
import com.wly.beansprout.utils.SpUtils;
import com.wly.beansprout.utils.ToastUtil;

import java.util.List;

/**
 * 菜单弹框
 */
public class MenuDialog extends BaseServiceDialog implements View.OnClickListener {

    private Button btReply;
    private Button btStop;
    private RecyclerView rvPoints;

    private AddPointDialog addPointDialog;
    private AutomaticReplyScriptDialog autoReplyDialog;
    private Listener listener;
    private TouchPointAdapter touchPointAdapter;

    // 功能：0其它；1单击；2点赞；3向下滑动；4向上滑动；5向左滑动；6向右滑动；7自动回复；
    private int functionType;

    public MenuDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_menu;
    }

    @Override
    protected int getWidth() {
        return DensityUtil.dip2px(getContext(), 350);
    }

    @Override
    protected int getHeight() {
        return WindowManager.LayoutParams.WRAP_CONTENT;
    }

    @Override
    protected void onInited() {
        setCanceledOnTouchOutside(true);
        findViewById(R.id.bt_exit).setOnClickListener(this);
        findViewById(R.id.bt_add).setOnClickListener(this);

        btReply = findViewById(R.id.bt_reply);
        btReply.setOnClickListener(this);
        btStop = findViewById(R.id.bt_stop);
        btStop.setOnClickListener(this);
        rvPoints = findViewById(R.id.rv);
        touchPointAdapter = new TouchPointAdapter();
        touchPointAdapter.setOnItemClickListener((view, position, touchPoint) -> {
            if (view.getId() == R.id.item_touch_point) {
                // 【开始】功能
                btReply.setVisibility(View.GONE);
                btStop.setVisibility(View.VISIBLE);
                dismiss();
                touchPoint.setFunctionType(functionType);
                // 通知 动作服务，开启点赞功能
                TouchEvent.postStartAction(touchPoint);
                // 特殊处理，关闭所有，然后单独开启已选择的项
                for (TouchPoint info : touchPointAdapter.getTouchPointList()) {
                    info.setStartClick(false);
                }
                touchPointAdapter.getTouchPointList().get(position).setStartClick(true);
                // 重新保存到 轻文件
                SpUtils.setTouchPoints(getContext(), touchPointAdapter.getTouchPointList());
                if (listener != null) {
                    listener.onStartTouch(touchPoint.getX(), touchPoint.getY());
                }
            } else if (view.getId() == R.id.bt_delete) {
                // 【删除】功能
                if (touchPointAdapter != null) {
                    touchPointAdapter.onRemove(getContext(), position);
                }
            }

        });
        rvPoints.setLayoutManager(new LinearLayoutManager(getContext()));
        rvPoints.setAdapter(touchPointAdapter);
        setOnDismissListener(dialog -> {
            // 如果没有设置专属，并且之前为暂停，关闭Dialog时默认继续点赞动作
            if (TextUtils.isEmpty(TouchEventManager.getInstance().getAppPackageName())) {
                if (TouchEventManager.getInstance().isPaused()) {
                    TouchEvent.postContinueAction();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("啊实打实", "onStart");
        // 如果没有设置专属，则打开软件时暂停
        if (TextUtils.isEmpty(TouchEventManager.getInstance().getAppPackageName())) {
            // 如果正在触控，则暂停
            TouchEvent.postPauseAction();
        }
        if (touchPointAdapter != null) {
            List<TouchPoint> touchPoints = SpUtils.getTouchPoints(getContext());
            Log.d("啊实打实", GsonUtils.beanToJson(touchPoints));
            touchPointAdapter.setTouchPointList(touchPoints);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_add:                                                                       // 添加触控点
                DialogUtils.dismiss(addPointDialog);
                addPointDialog = new AddPointDialog(getContext());
                addPointDialog.setOnDismissListener(dialog -> MenuDialog.this.show());
                addPointDialog.show();
                dismiss();
                break;
            case R.id.bt_stop:                                                                      // 停止触控
                // 如果是 “自动回复” ，则显示 话术功能。
                btReply.setVisibility(functionType == 7 ? View.VISIBLE : View.GONE);
                btStop.setVisibility(View.GONE);
                TouchEvent.postStopAction();
                ToastUtil.show("已停止触控");

                // 改变界面
                for (TouchPoint info : touchPointAdapter.getTouchPointList()) {
                    info.setStartClick(false);
                }
                SpUtils.setTouchPoints(getContext(), touchPointAdapter.getTouchPointList());
                touchPointAdapter.notifyDataSetChanged();
                if (listener != null) {
                    listener.onStopTouch();
                }
                break;
            case R.id.bt_reply:                                                                     // 回复话术
                DialogUtils.dismiss(autoReplyDialog);
                autoReplyDialog = new AutomaticReplyScriptDialog(getContext());
                autoReplyDialog.setOnDismissListener(dialog -> MenuDialog.this.show());
                autoReplyDialog.show();
                dismiss();
                break;
            case R.id.bt_exit:                                                                      // 退出助手
                TouchEvent.postStopAction();
                if (listener != null) {
                    listener.onExitService();
                }
                break;

        }
    }

    public void setFunctionType(int functionType) {
        this.functionType = functionType;

        // 如果是 “自动回复” ，则显示 话术功能。
        btReply.setVisibility(functionType == 7 ? View.VISIBLE : View.GONE);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public interface Listener {
        /**
         * 开始动作
         *
         * @param x 目标X
         * @param y 目标Y
         */
        void onStartTouch(int x, int y);

        /**
         * 停止触控
         */
        void onStopTouch();

        /**
         * 关闭辅助
         */
        void onExitService();
    }
}
