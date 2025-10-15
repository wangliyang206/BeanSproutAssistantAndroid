package com.wly.beansprout.view;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;

import com.google.android.flexbox.FlexboxLayout;

/**
 * RadioGroup自适应流式布局
 */
public class FlexboxRadioGroup extends FlexboxLayout {
    // 记录当前选中的RadioButton
    private RadioButton mCheckedRadioButton;
    // 选中状态变化监听器
    private OnCheckedChangeListener mOnCheckedChangeListener;

    public FlexboxRadioGroup(Context context) {
        super(context);
        init();
    }

    public FlexboxRadioGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FlexboxRadioGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // 设置默认的Flexbox属性
//        setFlexDirection(FLEX_DIRECTION_ROW);
//        setFlexWrap(FLEX_WRAP_WRAP);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);

        // 如果添加的是RadioButton，设置其点击事件
        if (child instanceof RadioButton) {
            final RadioButton radioButton = (RadioButton) child;

            // 如果这个RadioButton默认是选中的
            if (radioButton.isChecked()) {
                mCheckedRadioButton = radioButton;
            }

            // 设置选中状态变化监听
            radioButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    // 记录之前选中的RadioButton
                    RadioButton previousChecked = mCheckedRadioButton;

                    // 如果点击的RadioButton被选中，取消之前选中的RadioButton
                    if (mCheckedRadioButton != null && mCheckedRadioButton != radioButton) {
                        mCheckedRadioButton.setChecked(false);
                    }
                    mCheckedRadioButton = radioButton;

                    // 通知监听器选中状态发生变化
                    if (mOnCheckedChangeListener != null && previousChecked != radioButton) {
                        mOnCheckedChangeListener.onCheckedChanged(FlexboxRadioGroup.this, radioButton.getId());
                    }
                } else {
                    // 如果取消选中，重置当前选中的RadioButton
                    if (mCheckedRadioButton == radioButton) {
                        mCheckedRadioButton = null;

                        // 通知监听器选中状态变为未选中
                        if (mOnCheckedChangeListener != null) {
                            mOnCheckedChangeListener.onCheckedChanged(FlexboxRadioGroup.this, -1);
                        }
                    }
                }
            });
        }
    }

    // 选中状态变化监听器接口
    public interface OnCheckedChangeListener {
        /**
         * 当选中的RadioButton发生变化时调用
         *
         * @param group     当前的FlexboxRadioGroup
         * @param checkedId 选中的RadioButton的ID，如果没有选中则为-1
         */
        void onCheckedChanged(FlexboxRadioGroup group, int checkedId);
    }

    // 设置选中状态变化监听器
    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        mOnCheckedChangeListener = listener;
    }

    // 获取当前选中的RadioButton
    public RadioButton getCheckedRadioButton() {
        return mCheckedRadioButton;
    }

    // 获取当前选中的RadioButton的ID
    public int getCheckedRadioButtonId() {
        return mCheckedRadioButton != null ? mCheckedRadioButton.getId() : -1;
    }

    // 设置选中指定ID的RadioButton
    public void check(int id) {
        View child = findViewById(id);
        if (child instanceof RadioButton) {
            ((RadioButton) child).setChecked(true);
        }
    }
}

