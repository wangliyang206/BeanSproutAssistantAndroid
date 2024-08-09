package com.wly.beansprout.dialog;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.wly.beansprout.R;
import com.wly.beansprout.bean.TouchPoint;
import com.wly.beansprout.global.AccountManager;
import com.wly.beansprout.utils.SpUtils;
import com.wly.beansprout.utils.ToastUtil;

/**
 * @ProjectName: BeanSproutAssistantAndroid
 * @Package: com.wly.beansprout.dialog
 * @ClassName: AutomaticReplyScriptDialog
 * @Description: 自动回复的话术配置
 * @Author: WLY
 * @CreateDate: 2024/8/9 15:11
 */
public class AutomaticReplyScriptDialog extends BaseServiceDialog implements View.OnClickListener{
    // 用户输入话术
    private EditText etInput;
    private AccountManager mAccountManager;

    public AutomaticReplyScriptDialog(@NonNull Context context) {
        super(context);
        mAccountManager = new AccountManager(context);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_auto_reply_script;
    }

    @Override
    protected int getWidth() {
        return WindowManager.LayoutParams.MATCH_PARENT;
    }

    @Override
    protected int getHeight() {
        return WindowManager.LayoutParams.MATCH_PARENT;
    }

    @Override
    protected void onInited() {
        etInput = findViewById(R.id.edit_autoreplyscript_input);
        findViewById(R.id.bt_autoreplyscript_commit).setOnClickListener(this);
        findViewById(R.id.bt_autoreplyscript_cancel).setOnClickListener(this);

        etInput.setText(mAccountManager.getAutoReplyScript());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_autoreplyscript_commit:
                String input = etInput.getText().toString();

                if (TextUtils.isEmpty(input)) {
                    ToastUtil.show("请输入自动回复内容！");
                    return;
                }

                mAccountManager.setAutoReplyScript(input);
                dismiss();
                break;
            case R.id.bt_autoreplyscript_cancel:
                dismiss();
                break;
        }
    }
}
