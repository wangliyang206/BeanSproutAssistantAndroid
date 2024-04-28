package com.wly.beansprout.utils;

import android.app.Dialog;

public class DialogUtils {

    public static void dismiss(Dialog dialog) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

}
