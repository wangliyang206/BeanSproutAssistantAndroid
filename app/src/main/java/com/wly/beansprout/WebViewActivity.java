package com.wly.beansprout;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.wly.beansprout.utils.StatusBarCompatUtils;
import com.wly.beansprout.view.ProgressWebView;

import java.io.File;

import qiu.niorgai.StatusBarCompat;

/**
 * WebView
 */

public class WebViewActivity extends AppCompatActivity implements View.OnClickListener {

    /*----------------控件信息----------------*/
    private View mTopLayout;
    private TextView txviTitle;                                                                             // 标题
    private ProgressWebView mWebView;

    /*----------------业务信息----------------*/
    // 业务区(操作数据库、请求网络)
    private static final String APP_CACAHE_DIRNAME = "/webcache";
    //  标题；当前访问地址
    private String title, URL = "";
    // 是否显示Top
    private boolean isShowTop;
    private final String TAG = "WebViewActivity";
    /**
     * 从哪个界面中跳转过来的(后续回退流程不一样)
     * 4 代表 订单详情 - 支付操作
     */
    private int type = -1;

    @Override
    public void onResume() {
        super.onResume();
        if (mWebView != null)
            mWebView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mWebView != null)
            mWebView.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mWebView != null) {
            mWebView.destroy();
            mWebView = null;
        }
        super.onDestroy();
        clearWebViewCache();
        this.mTopLayout = null;
    }

    /**
     * 获取外界提供的参数
     */
    private void getBundleValues() {

        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            URL = bundle.getString("URL");
            type = bundle.getInt("type", -1);
            isShowTop = bundle.getBoolean("isShowTop", false);
            title = bundle.getString("TITLE");
        }
    }


    /**
     * 设置状态栏
     */
    public void setStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //SDK >= 21时, 取消状态栏的阴影
            StatusBarCompat.translucentStatusBar(this, true);
        } else {
            //透明状态栏
            StatusBarCompat.translucentStatusBar(this);
        }

        StatusBarCompatUtils.cancelLightStatusBar(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview_activity);

        setStatusBar();
        // 初始化控件
        mTopLayout = findViewById(R.id.view_top_layout);
        txviTitle = findViewById(R.id.txvi_title);
        mWebView = findViewById(R.id.pwv_webView);
        findViewById(R.id.imgvi_back).setOnClickListener(this);

        getBundleValues();


        // 是否显示顶部
        mTopLayout.setVisibility(isShowTop ? View.VISIBLE : View.GONE);

        // 设置标题
        txviTitle.setText(title);

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.d(TAG, "###赤槿-shouldOverrideUrlLoading_url(" + url + ")###");

                Bundle bundle = new Bundle();
                bundle.putString("URL", url);

                Intent intent = new Intent(WebViewActivity.this, WebViewActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
                return true;
            }

            @Override
            public void onPageStarted(WebView webView, String s, Bitmap bitmap) {
                super.onPageStarted(webView, s, bitmap);

                Log.d(TAG, "###赤槿-onPageStarted###");
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                Log.d(TAG, "###赤槿-onPageFinished###");
                mWebView.getSettings().setBlockNetworkImage(false);
            }
        });

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                mWebView.onProgressChangedX5(view, newProgress);
                super.onProgressChanged(view, newProgress);
            }

            @Override
            public void onCloseWindow(WebView window) {
                super.onCloseWindow(window);
                onBackPressed();
            }
        });

        mWebView.setOnLongClickListener(v -> true);

        mWebView.setScrollbarFadingEnabled(true);
        mWebView.addJavascriptInterface(this, "Android");
        WebSettings webSetting = mWebView.getSettings();
//        webSetting.setAllowFileAccess(true);
        webSetting.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        webSetting.setSupportZoom(true);
        webSetting.setBuiltInZoomControls(false);                                                   // 设置内置的缩放控件。若为false，则该WebView不可缩放
        webSetting.setUseWideViewPort(true);                                                        // 将图片调整到适合webview的大小
        webSetting.setSupportMultipleWindows(false);                                                // 设置允许开启多窗口
        webSetting.setLoadWithOverviewMode(true);                                                   // 缩放至屏幕的大小
        webSetting.setDatabaseEnabled(true);                                                        // 开启 database storage API 功能
        webSetting.setDomStorageEnabled(true);                                                      // 开启 DOM storage API 功能
        webSetting.setJavaScriptEnabled(true);                                                      // 如果访问的页面中要与Javascript交互，则webview必须设置支持Javascript
        webSetting.setGeolocationEnabled(true);
//        webSetting.setAppCacheMaxSize(Long.MAX_VALUE);
        webSetting.setCacheMode(WebSettings.LOAD_DEFAULT);                                          // 缓存模式(不使用缓存 LOAD_CACHE_ELSE_NETWORK)

        String cacheDirPath = getFilesDir().getAbsolutePath() + APP_CACAHE_DIRNAME;
        webSetting.setDatabasePath(cacheDirPath);                                                   // 设置数据库缓存路径
//        webSetting.setGeolocationDatabasePath(cacheDirPath);
        // webSetting.setPageCacheCapacity(IX5WebSettings.DEFAULT_CACHE_CAPACITY);
//        webSetting.setPluginState(WebSettings.PluginState.ON_DEMAND);
        webSetting.setRenderPriority(WebSettings.RenderPriority.HIGH);
        // webSetting.setPreFectch(true);
        webSetting.setBlockNetworkImage(true);

        mWebView.loadUrl(URL);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgvi_back:                                                                   // 返回按钮
                onBackPressed();
                break;
        }
    }


    /**
     * 关闭当前页
     */
    @JavascriptInterface
    public void onClose() {
        runOnUiThread(() -> onBackPressed());

    }


    /**
     * 清除WebView缓存
     */
    public void clearWebViewCache() {
        Log.d(TAG, "MainActivity:1");
        // 清理Webview缓存数据库
        try {
            Log.d(TAG, "MainActivity:2");
            deleteDatabase("webview.db");
            deleteDatabase("webviewCache.db");
            Log.d(TAG, "MainActivity:3");
        } catch (Exception e) {
            Log.e(TAG, "删除数据异常" + e.getMessage());
        }

        // WebView 缓存文件
        File appCacheDir = new File(getFilesDir().getAbsolutePath() + APP_CACAHE_DIRNAME);

        File webviewCacheDir = new File(getCacheDir().getAbsolutePath() + "/webviewCache");

        // 删除webview 缓存目录
        if (webviewCacheDir.exists()) {
            Log.d(TAG, "MainActivity:4");
            deleteFile(webviewCacheDir);
        }
        // 删除webview 缓存 缓存目录
        if (appCacheDir.exists()) {
            Log.d(TAG, "MainActivity:5");
            deleteFile(appCacheDir);
        }
    }

    /**
     * 递归删除 文件/文件夹
     *
     * @param file
     */
    public void deleteFile(File file) {

        if (file.exists()) {
            if (file.isFile()) {
                file.delete();
            } else if (file.isDirectory()) {
                File files[] = file.listFiles();
                for (int i = 0; i < files.length; i++) {
                    deleteFile(files[i]);
                }
            }
            file.delete();
        } else {
        }
    }

    /**
     * Back监听
     */
    @Override
    public void onBackPressed() {
        clearAllCache();
        this.setResult(RESULT_OK);
        this.finish();
    }

    public void clearAllCache() {
        clearWebViewCache();
        clearCacheFolder(this.getCacheDir(), System.currentTimeMillis());
    }


    // clear the cache before time numDays
    private int clearCacheFolder(File dir, long numDays) {
        int deletedFiles = 0;
        if (dir != null && dir.isDirectory()) {
            try {
                for (File child : dir.listFiles()) {
                    if (child.isDirectory()) {
                        deletedFiles += clearCacheFolder(child, numDays);
                    }
                    if (child.lastModified() < numDays) {
                        Log.d(TAG, "deleted file:" + child.getAbsolutePath() + child.getName());
                        if (child.delete()) {
                            deletedFiles++;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return deletedFiles;
    }
}
