package com.wly.beansprout.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.annotation.SuppressLint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.RequiresApi;

import com.wly.beansprout.bean.TouchEvent;
import com.wly.beansprout.bean.TouchPoint;
import com.wly.beansprout.global.AccountManager;
import com.wly.beansprout.global.TouchEventManager;
import com.wly.beansprout.utils.CommonUtils;
import com.wly.beansprout.utils.FindTargetNodeUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 无障碍服务-自动点赞
 *
 * @date 2024/4/26 16:23
 */
@RequiresApi(api = Build.VERSION_CODES.N)
public class AutoTouchService extends AccessibilityService {

    private final String TAG = "AutoTouchService+++";
    // 自动点击事件
    private TouchPoint autoTouchPoint;
    // 创建一个Handler，它与当前线程（通常是UI线程）的Looper关联
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(Looper.getMainLooper());
    // 延迟Handler，它与当前线程（通常是UI线程）的Looper关联
    private final Handler delayHandler = new Handler(Looper.getMainLooper());
    // 创建单线程池
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    // 基本数据源
    private AccountManager mAccountManager;

    /**
     * 抢福袋环节：0没有抢福袋；1未识别福袋控件；2超级福袋界面；3已参与抽福袋；4已点击观看直播；
     */
    private int mLuckyBagStep;
    /**
     * 是否允许抢福袋
     */
    public static boolean isAllowed = true;
    /**
     * 福袋控件X，Y 坐标(可以自动获取)
     */
    private int mLuckyBagX, mLuckyBagY;
    /**
     * 设置抢福袋时间
     */
    private int luckyBagTime;
    /**
     * 查找目标节点 对象
     */
    private FindTargetNodeUtil findNode;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        handler = new Handler();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
        mAccountManager = new AccountManager(getApplicationContext());
        findNode = new FindTargetNodeUtil(getApplicationContext());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReciverTouchEvent(TouchEvent event) {
        Log.d(TAG, "###onReciverTouchEvent: " + event.toString());
        TouchEventManager.getInstance().setTouchAction(event.getAction());
        handler.removeCallbacks(autoTouchRunnable);
        switch (event.getAction()) {
            case TouchEvent.ACTION_START:                                                           // 动作开启
                autoTouchPoint = event.getTouchPoint();
                getLuckyBagTime();
                onAutoClick();
                break;
            case TouchEvent.ACTION_CONTINUE:                                                        // 动作继续
                if (autoTouchPoint != null) {
                    onAutoClick();
                }
                break;
            case TouchEvent.ACTION_PAUSE:                                                           // 动作暂停
                handler.removeCallbacks(autoTouchRunnable);
                break;
            case TouchEvent.ACTION_STOP:                                                            // 动作停止
                mLuckyBagStep = 0;
                handler.removeCallbacks(autoTouchRunnable);
                autoTouchPoint = null;
                break;
        }
    }

    /**
     * 获取抢福袋时间
     * 如果是随机功能，则生成随机数
     */
    private void getLuckyBagTime() {
        if (autoTouchPoint != null) {
            // 如果是随机，则在此时生成随机数
            if (autoTouchPoint.getLuckybagTime() == 998) {
                // 5~10分钟随机
                luckyBagTime = CommonUtils.getRandomNum(5, 10);
                Log.d(TAG, "###随机生成时间=" + luckyBagTime + "分钟");
            } else if (autoTouchPoint.getLuckybagTime() == 997) {
                // 0~5分钟随机
                luckyBagTime = CommonUtils.getRandomNum(0, 5);
                Log.d(TAG, "###随机生成时间=" + luckyBagTime + "分钟");
            } else {
                // 不随机 或 不设置
                luckyBagTime = autoTouchPoint.getLuckybagTime();
            }
        } else {
            // 不设置
            luckyBagTime = -1;
        }
    }

    /**
     * 执行自动点击
     */
    private void onAutoClick() {
        if (autoTouchPoint != null) {
            handler.postDelayed(autoTouchRunnable, getDelayTime());
        }
    }

    private final Runnable autoTouchRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "###onAutoClick: " + "x=" + autoTouchPoint.getX() + " y=" + autoTouchPoint.getY());

            if (autoTouchPoint.getFunctionType() == 8) {
                // 抢福袋
                if (mLuckyBagStep == 0) {
                    Log.d(TAG, "###执行 抢福袋 功能");
                    grabLuckyBag();
                }

            } else if (autoTouchPoint.getFunctionType() == 7) {
                // 自动回复
                Log.d(TAG, "###执行 自动回复 功能");

                // 获取一次屏幕
                AccessibilityNodeInfo layout = getRootInActiveWindow();

                if (layout.getPackageName().equals(TouchEventManager.getInstance().getAppPackageName())) {
                    // 抖音
                    autoReply("com.ss.android.ugc.aweme:id/gl6", "com.ss.android.ugc.aweme:id/0=v");
                } else {
                    // 极速版
                    autoReply("com.ss.android.ugc.aweme.lite:id/dv_", "m.l.live.plugin:id/tv_send_portrait");
                }

            } else {
                // 获取系统的GestureDescription.Builder对象
                GestureDescription.Builder builder = new GestureDescription.Builder();

                if (autoTouchPoint.getFunctionType() == 1) {
                    // 单击
                    builder.addStroke(onSingleClick());
                } else if (autoTouchPoint.getFunctionType() == 2) {
                    // 点赞
                    builder.addStroke(onSingleClick());
                    builder.addStroke(onDoubleClick());
                } else if (autoTouchPoint.getFunctionType() == 3) {
                    // 向下滑动
                    builder.addStroke(onSlide(1));
                } else if (autoTouchPoint.getFunctionType() == 4) {
                    // 上下滑动
                    builder.addStroke(onSlide(2));
                } else if (autoTouchPoint.getFunctionType() == 5) {
                    // 向左滑动
                    builder.addStroke(onSlide(3));
                } else if (autoTouchPoint.getFunctionType() == 6) {
                    // 向右滑动
                    builder.addStroke(onSlide(4));
                } else {
                    // 其它

                }

                // 创建GestureDescription对象
                GestureDescription gestureDescription = builder.build();
                // 执行模拟的手势
                dispatchGesture(gestureDescription, new AccessibilityService.GestureResultCallback() {
                    @Override
                    public void onCompleted(GestureDescription gestureDescription) {
                        super.onCompleted(gestureDescription);
                        // 完成的回调
                    }

                    @Override
                    public void onCancelled(GestureDescription gestureDescription) {
                        super.onCancelled(gestureDescription);
                        // 取消的回调
                    }
                }, null);
            }

            onAutoClick();
        }
    };

    /**
     * 获取抖音底部评论输入框
     */
    private AccessibilityNodeInfo getCommentLayout(String resouceId) {
        // 根据控件id获取控件。
        AccessibilityNodeInfo info = findViewById(resouceId);
        if (info == null) {
            // 未获取到，只能用笨办法，遍历控件了
            info = findViewByText("说点什么...");
        }
        return info;
    }

    /**
     * 获取抖音【发送】按钮
     *
     * @param sendId 发送id
     */
    private AccessibilityNodeInfo getSendButtonLayout(String sendId) {
        // 根据控件id获取控件。
        AccessibilityNodeInfo info = findViewById(sendId);
        if (info == null) {
            // 未获取到，只能用笨办法，遍历控件了
            info = findViewByText("发送");
        }
        return info;
    }

    /**
     * 自动回复
     */
    private void autoReply(String resouceId, String sendId) {
        // 获取到的 resouce-id 来获取 AccessibilityNodeInfo
        AccessibilityNodeInfo info = getCommentLayout(resouceId);
        if (info != null) {

            // 点击一下输入框
            info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            // 等待1秒
            delayHandler.postDelayed(() -> {

                // 重新取布局文件
                AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
                // 如果没有打开弹幕只默认取index = 1的数值
                AccessibilityNodeInfo input = accessibilityNodeInfo.getChild(1);
                if (!input.getClassName().equals("android.widget.EditText")) {
                    // 打开弹幕后，index 取 2
                    input = accessibilityNodeInfo.getChild(2);
                }

                Log.d(TAG, "###input=" + input.getText());

                String[] mAuto = mAccountManager.getAutoReplyScript().split(";");

                // 设置edit文本内容,具体方法为
                int index = CommonUtils.getRandomNum(mAuto.length);
                CharSequence text = mAuto[index];
                Log.d(TAG, "###自动回复=" + text);
                Bundle arguments = new Bundle();
                arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);

                // 尝试在EditText中设置文本
                boolean success = input.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
                if (success) {
                    Log.d("AccessibilityService", "Text set successfully");
                    // 发送
                    AccessibilityNodeInfo send = getSendButtonLayout(sendId);
                    send.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                } else {
                    Log.d("AccessibilityService", "Failed to set text");
                }
            }, 1000);
        }
    }

    /**
     * 单击 事件
     */
    private GestureDescription.StrokeDescription onSingleClick() {
        // 创建第一次点击的Path
        Path path = new Path();
        path.moveTo(autoTouchPoint.getX(), autoTouchPoint.getY());
        return new GestureDescription.StrokeDescription(path, 0, 1);
    }

    /**
     * 双击(点赞) 事件
     */
    private GestureDescription.StrokeDescription onDoubleClick() {
        Path clickPath2 = new Path();
        clickPath2.moveTo(autoTouchPoint.getX(), autoTouchPoint.getY()); // 点击位置2，通常这里x2,y2与x1,y1有一定的间隔
        return new GestureDescription.StrokeDescription(clickPath2, 100, 1); // 100ms后模拟第二次点击
    }

    /**
     * 滑动
     *
     * @param type 滑动方向：1向下滑动；2向上滑动；3向左滑动；4向右滑动；
     */
    private GestureDescription.StrokeDescription onSlide(int type) {
        // 创建Path对象，用于描述滑动的路径
        Path path = new Path();
        // 设置起点
        path.moveTo(autoTouchPoint.getX(), autoTouchPoint.getY());

        if (type == 1) {
            // 向下滑动
            // 添加滑动的路径
            path.lineTo(autoTouchPoint.getX(), autoTouchPoint.getY() - 1000);
        } else if (type == 2) {
            // 向上滑动
            // 添加滑动的路径
            path.lineTo(autoTouchPoint.getX(), autoTouchPoint.getY() + 1000);
        } else if (type == 3) {
            // 向左滑动
            // 添加滑动的路径
            path.lineTo(autoTouchPoint.getX() - 1000, autoTouchPoint.getY());
        } else if (type == 4) {
            // 向右滑动
            // 添加滑动的路径
            path.lineTo(autoTouchPoint.getX() + 1000, autoTouchPoint.getY());
        }

        // 创建PathStroke对象，设置Path的相关属性
        return new GestureDescription.StrokeDescription(path, 0, 100);
    }

    private long getDelayTime() {
        // 单位：秒
//        return autoTouchPoint.getDelay() * 1000L;
        // 单位：毫秒
        return autoTouchPoint.getDelay();
    }

    /**
     * 有关AccessibilityEvent事件的回调函数，系统通过sendAccessibiliyEvent()不断的发送AccessibilityEvent到此处
     */
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // 获取当前侦听到的包名
        String packageName = event.getPackageName() == null ? "" : event.getPackageName().toString();
//        Log.d("onAccessibilityEvent", "###包名：" + packageName + "，事件类型=" + event.getEventType());
        switch (event.getEventType()) {
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                // 类型窗口内容已更改
                onWindowContentChanged(packageName, autoTouchPoint == null ? 0 : autoTouchPoint.getFunctionType());
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                // 类型窗口状态已更改
                break;
            case AccessibilityEvent.TYPE_WINDOWS_CHANGED:
                // 类型窗口已更改
                break;
        }
    }

    /**
     * 窗口内容发生变化(判断是不是切出目标窗口，如果切出，则需要停止点赞动作)
     */
    private void onWindowContentChanged(String packageName, int type) {
        // 包名不为空，说明外界已经设置了专属。
        if (!TextUtils.isEmpty(TouchEventManager.getInstance().getAppPackageName()) && type != 7 && type != 8) {
            // 如果活动APP不是目标APP，则不响应
            if (packageName.contains(TouchEventManager.getInstance().getAppPackageName())) {
                // 如果是动作暂停状态，则自动开启继续点赞
                if (TouchEventManager.getInstance().isPaused()) {
                    Log.d("onWindowContentChanged", "###执行了 继续动作 事件");
                    TouchEvent.postContinueAction();
                }
            } else {
                // 如果是动作开启状态，则自动停止点赞动作
                if (TouchEventManager.getInstance().isTouching()) {
                    Log.d("onWindowContentChanged", "###执行了 动作暂停 事件");
                    TouchEvent.postPauseAction();
                }
            }
        }

        // 福袋相关逻辑，如果检测到 “没有抽中福袋” 界面，则关闭界面。
        // 必须满足以下条件：1、当前包名是【抖音】包名；2、当前为抢福袋功能；3、已经参与过抽过福袋了。
        if (packageName.contains(TouchEventManager.getInstance().getAppPackageName()) && type == 8 && mLuckyBagStep >= 2) {
            // 提交任务到线程池
            mExecutor.execute(() -> {
                AccessibilityNodeInfo foundNode = findNode.findTargetNode(getRootInActiveWindow(), "com.lynx.tasm.behavior.ui.view.UIView", "我知道了", -1);
                Log.d(TAG, "###" + Thread.currentThread().getName() + "----------foundNode=" + (foundNode != null));
                if (foundNode != null) {
                    Log.d(TAG, "###界面监听到【没有抽中福袋】界面");
                    // 点击我知道了
                    foundNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);

                    // 使用精确手势点击
                    // 初始化点击事件
                    GestureDescription.Builder builder = new GestureDescription.Builder();

                    // 获取屏幕坐标
                    Rect bounds = new Rect();
                    foundNode.getBoundsInScreen(bounds);
                    Path path = new Path();
                    path.moveTo(bounds.centerX(), bounds.centerY());

                    // 50ms点击更接近真实操作
                    builder.addStroke(new GestureDescription.StrokeDescription(path, 0, 50));
                    dispatchGesture(builder.build(), new GestureResultCallback() {
                        @Override
                        public void onCompleted(GestureDescription gestureDescription) {
                            super.onCompleted(gestureDescription);
                            // 完成的回调
                            Log.d(TAG, "###已点击，我知道了");

                            // 重新生成一个抢福袋时间
                            getLuckyBagTime();
                            mLuckyBagStep = 0;
                            isAllowed = true;
                        }

                        @Override
                        public void onCancelled(GestureDescription gestureDescription) {
                            super.onCancelled(gestureDescription);
                            // 取消的回调
                        }
                    }, null);

                    foundNode.recycle(); // 必须回收节点
                }
            });
        }
    }

    /**
     * 查找对应文本的View，无论该node能不能点击
     */
    public AccessibilityNodeInfo findViewByText(String text) {
        AccessibilityNodeInfo viewByText = findViewByText(text, true);
        if (viewByText == null) {
            viewByText = findViewByText(text, false);
        }
        return viewByText;
    }

    /**
     * 查找对应文本的view
     *
     * @param text      文本
     * @param clickable 该View是否可以点击
     * @return view
     */
    public AccessibilityNodeInfo findViewByText(String text, boolean clickable) {
        AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
        if (accessibilityNodeInfo == null) {
            return null;
        }

        List<AccessibilityNodeInfo> nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByText(text);
        if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
            for (AccessibilityNodeInfo nodeInfo : nodeInfoList) {
                if (nodeInfo != null && (nodeInfo.isClickable() == clickable)) {
                    return nodeInfo;
                }
            }
        }

        return null;
    }

    /**
     * 查找对应id的View
     *
     * @param id id
     * @return view
     */
    public AccessibilityNodeInfo findViewById(String id) {
        AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
        if (accessibilityNodeInfo == null) {
            return null;
        }

        List<AccessibilityNodeInfo> nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(id);
        if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
            for (AccessibilityNodeInfo nodeInfo : nodeInfoList) {
                if (nodeInfo != null) {
                    return nodeInfo;
                }
            }
        }

        return null;
    }

    /**
     * 抢福袋
     */
    private void grabLuckyBag() {
        mLuckyBagStep = 1;
        findNode.findNode("com.lynx.tasm.behavior.ui.LynxFlattenUI", "超级福袋", luckyBagTime, getRootInActiveWindow(), luckyBagNode -> {
            if (luckyBagNode != null) {
                // 检查到抢福袋控件，则模拟点击抢福袋
                Log.d(TAG, "###找到 福袋 控件");

                if (!isAllowed) {
                    Log.d(TAG, "###您设置了福袋卡点时间：" + luckyBagTime + "分钟；目前还没有到开抢时间。");
                    mLuckyBagStep = 0;
                    return;
                }
                // 点击抢福袋控件
                luckyBagNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);

                // 获取屏幕坐标
                Rect rect = new Rect();
                luckyBagNode.getBoundsInScreen(rect);
                mLuckyBagX = rect.left;
                mLuckyBagY = rect.top;
//                    Log.d(TAG, "###抢福袋坐标：" + mLuckyBagX + "," + mLuckyBagY + "," + rect.right + "," + rect.bottom);
                // 初始化点击事件
                GestureDescription.Builder builder = new GestureDescription.Builder();
                Path p = new Path();
                p.moveTo(mLuckyBagX, mLuckyBagY);
                builder.addStroke(new GestureDescription.StrokeDescription(p, 0L, 500L));
                GestureDescription gesture = builder.build();
                dispatchGesture(gesture, new GestureResultCallback() {
                    @Override
                    public void onCompleted(GestureDescription gestureDescription) {
                        super.onCompleted(gestureDescription);
                        // 完成的回调
                        mLuckyBagStep = 2;

                        Log.d(TAG, "###已点击，福袋控件");
                        // 等待2秒
                        delayHandler.postDelayed(() -> {
                            findNode.findNode("com.lynx.tasm.behavior.ui.view.UIView", "一键发表评论", -1, getRootInActiveWindow(), senCommentNode -> {
                                if (senCommentNode != null) {

                                    // 初始化点击事件
                                    GestureDescription.Builder builder = new GestureDescription.Builder();
                                    Path p = new Path();
                                    p.moveTo(autoTouchPoint.getX(), autoTouchPoint.getY());
                                    builder.addStroke(new GestureDescription.StrokeDescription(p, 0L, 500L));
                                    GestureDescription gesture = builder.build();
                                    dispatchGesture(gesture, new GestureResultCallback() {
                                        @Override
                                        public void onCompleted(GestureDescription gestureDescription) {
                                            super.onCompleted(gestureDescription);
                                            // 完成的回调
                                            Log.d(TAG, "###已点击，一键发表评论");
                                            mLuckyBagStep = 3;

                                            delayHandler.postDelayed(() -> {
                                                findNode.findNode("com.lynx.tasm.behavior.ui.view.UIView", "开始观看直播任务", -1, getRootInActiveWindow(), startTaskNode -> {
                                                    if (startTaskNode != null) {
                                                        startWatch();
                                                    }
                                                });
                                            }, 2000);
                                        }

                                        @Override
                                        public void onCancelled(GestureDescription gestureDescription) {
                                            super.onCancelled(gestureDescription);
                                            // 取消的回调
                                        }
                                    }, null);
                                } else {
                                    // 没有找到控件或者已参与成功
                                    Log.d(TAG, "###没有找到【一键发表评论】");

                                    // 参与抽奖
                                    findNode.findNode("com.lynx.tasm.behavior.ui.view.UIView", "参与抽奖", -1, getRootInActiveWindow(), luckDrawNode -> {
                                        if (luckDrawNode != null) {

                                            // 初始化点击事件
                                            GestureDescription.Builder builder = new GestureDescription.Builder();
                                            Path p = new Path();
                                            p.moveTo(autoTouchPoint.getX(), autoTouchPoint.getY());
                                            builder.addStroke(new GestureDescription.StrokeDescription(p, 0L, 500L));
                                            GestureDescription gesture = builder.build();
                                            dispatchGesture(gesture, new GestureResultCallback() {
                                                @Override
                                                public void onCompleted(GestureDescription gestureDescription) {
                                                    super.onCompleted(gestureDescription);
                                                    // 完成的回调
                                                    Log.d(TAG, "###已点击，参与抽奖");
                                                    mLuckyBagStep = 3;

                                                    delayHandler.postDelayed(() -> {
                                                        findNode.findNode("com.lynx.tasm.behavior.ui.view.UIView", "开始观看直播任务", -1, getRootInActiveWindow(), nodeInfo -> {
                                                            if (nodeInfo != null) {
                                                                startWatch();
                                                            }
                                                        });
                                                    }, 2000);
                                                }

                                                @Override
                                                public void onCancelled(GestureDescription gestureDescription) {
                                                    super.onCancelled(gestureDescription);
                                                    // 取消的回调
                                                }
                                            }, null);
                                        } else {
                                            // 观看直播
                                            findNode.findNode("com.lynx.tasm.behavior.ui.view.UIView", "开始观看直播任务", -1, getRootInActiveWindow(), nodeInfo -> {
                                                if (nodeInfo != null) {
                                                    // 开始观看直播任务
                                                    startWatch();
                                                } else {
                                                    // 参与成功 等待开奖、即将开奖 无法参与、活动已结束等，直接关闭界面
                                                    emptyWindowClick();
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }, 2000);
                    }

                    @Override
                    public void onCancelled(GestureDescription gestureDescription) {
                        super.onCancelled(gestureDescription);
                        // 取消的回调
                        mLuckyBagStep = 0;
                    }
                }, null);
            } else {
                Log.d(TAG, "###没有找到 福袋 控件");
                mLuckyBagStep = 0;
            }
        });

    }

    /**
     * 当前是“开始观看直播任务”，执行关闭
     */
    private void startWatch() {
        // 初始化点击事件
        GestureDescription.Builder builder = new GestureDescription.Builder();
        Path p = new Path();
        p.moveTo(autoTouchPoint.getX(), autoTouchPoint.getY());
        builder.addStroke(new GestureDescription.StrokeDescription(p, 0L, 500L));
        GestureDescription gesture = builder.build();
        dispatchGesture(gesture, new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
                // 完成的回调
                Log.d(TAG, "###已点击，开始观看直播任务");
                mLuckyBagStep = 4;

                // 关闭弹窗
                emptyWindowClick();
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                super.onCancelled(gestureDescription);
                // 取消的回调
            }
        }, null);
    }

    /**
     * 点击空白界面
     */
    private void emptyWindowClick() {
        // 等待2秒
        delayHandler.postDelayed(() -> {
            GestureDescription.Builder builder = new GestureDescription.Builder();
            Path p = new Path();
            p.moveTo(mLuckyBagX, mLuckyBagY);
            builder.addStroke(new GestureDescription.StrokeDescription(p, 0L, 500L));
            GestureDescription gesture = builder.build();
            dispatchGesture(gesture, new GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gestureDescription) {
                    super.onCompleted(gestureDescription);
                    // 完成的回调
                    Log.d(TAG, "###已点击，空白界面");
                }

                @Override
                public void onCancelled(GestureDescription gestureDescription) {
                    super.onCancelled(gestureDescription);
                    // 取消的回调
                }
            }, null);
        }, 2000);
    }

    @Override
    public void onInterrupt() {
        // 服务中断时关闭线程池
        shutdownExecutor();
    }

    /**
     * 关闭执行器(线程池)
     */
    private void shutdownExecutor() {
        if (!mExecutor.isShutdown()) {
            mExecutor.shutdownNow();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 服务销毁时关闭线程池
        shutdownExecutor();
        findNode.onDestroy();
        EventBus.getDefault().removeAllStickyEvents();
        this.handler.removeCallbacksAndMessages(null);
        this.delayHandler.removeCallbacksAndMessages(null);
        this.mAccountManager = null;
    }
}
