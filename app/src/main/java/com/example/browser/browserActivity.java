package com.example.browser;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebBackForwardList;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public class browserActivity extends AppCompatActivity implements View.OnClickListener {

    private WebView webVu;
    private ProgressBar progressbar;
    private Map<String, String> urlHeaders = new HashMap<String, String>();
    private TextView pageTitle;
    private long openTime = System.currentTimeMillis();
    private long initTime = System.currentTimeMillis();
    private boolean loadFinished = false;



    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface", "JavascriptInterface"})
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("webviewinit",initTime + "");
        setContentView(R.layout.webview);
        webVu = findViewById(R.id.webview);
        progressbar = findViewById(R.id.wbprogress);
        ImageView ivBack = findViewById(R.id.wbback);
        pageTitle = findViewById(R.id.wbtitle);

        // 事件绑定
        ivBack.setOnClickListener(this);

        //webView setting
        WebSettings webVuSetting = webVu.getSettings();
        webVuSetting.setUseWideViewPort(true);
        webVuSetting.setLoadWithOverviewMode(true);
        webVuSetting.setSupportZoom(true);
        webVuSetting.setJavaScriptEnabled(true);
        webVuSetting.setDomStorageEnabled(true);
        webVuSetting.setJavaScriptCanOpenWindowsAutomatically(true); // support window.open()

        webVu.setBackgroundColor(getResources().getColor(R.color.colorWhite));
        webVu.setWebContentsDebuggingEnabled(true);
        webVu.setWebChromeClient(new WebChromeClient());
        webVu.setWebViewClient(new CusWebViewClient());
        webVu.addJavascriptInterface(new jsBridge(), "getChannelId");

        init();

    }

    public void init() {
        progressbar.setVisibility(View.VISIBLE);
        progressbar.setProgress(5); // 去掉

        Intent intent = getIntent();

        String loadUrl = intent.getStringExtra("loadUrl");
        try {
            String url = URLDecoder.decode(loadUrl, "utf-8");
            loadUrl = url;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (!TextUtils.isEmpty(webVu.getUrl()) && webVu.getUrl() == loadUrl) {
            webVu.reload();
            return;
        }

        putHeaders();
        webVu.loadUrl(loadUrl, urlHeaders);

    }

    public void putHeaders() {
        urlHeaders.put("cid", "234234"); //安装唯一标志id
        urlHeaders.put("client", "android"); // 当前客户端类型，android / ios
        urlHeaders.put("token", "asdfasf");
        urlHeaders.put("versionCode", "520000");// 当前 appversion
        urlHeaders.put("versionName", "5.20");// 当前版本名称
        urlHeaders.put("lnCode", getResources().getConfiguration().locale.getLanguage());// 当前app语言
        urlHeaders.put("deviceId", "asdfasf");// deviceId
        urlHeaders.put("network", "2G");// 网络类型 wifi，2G, 3G, 4G
        urlHeaders.put("operator", "");// 手机卡运营商
        urlHeaders.put("phoneModel", Build.MODEL);// 手机型号
        urlHeaders.put("startTime", openTime+"");//系统时间
    }

    @Override
    public void onClick(View v) {
        @Override
        public void onDestroy() {
            super.onDestroy();
            webVu.destroy();
        }        switch (v.getId()) {
            case R.id.wbback:
                goBack();
                break;
            default:
                break;
        }
    }



    protected void goBack() {
        WebBackForwardList wf = webVu.copyBackForwardList();

        String s = wf.getCurrentItem().getUrl();
        long timeDiff = System.currentTimeMillis() - openTime;
        long timeDiff2 = System.currentTimeMillis() - initTime;
        Log.i("back",timeDiff + " "+ s + "/" + timeDiff2);
        if (webVu.canGoBack()) {
            webVu.goBack();
        } else {
            finish();
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (webVu.canGoBack()) {
                goBack();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    // class webchrome client
    public class WebChromeClient extends android.webkit.WebChromeClient {

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            String url = webVu.getUrl();
            pageTitle.setText(url);
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            Log.i("progress",newProgress +"");
            if (newProgress == 100) {
                progressbar.setVisibility(View.GONE);
            } else {
                progressbar.setVisibility(View.VISIBLE);
                progressbar.setProgress(newProgress);
            }
        }

    }


    // class  js appInterface
    // Android 4.2之后提供了@JavascriptInterface对象注解的方式建立Javascript对象和android原生对象的绑定,提供给JavaScript调用的方法必须带有@JavascriptInterface。
    public class jsBridge {

        @JavascriptInterface
        public void closeWindow() {
            finish();
        }

        @JavascriptInterface
        public void clearCache() {
            webVu.clearHistory();
        }

        @JavascriptInterface
        public String jsGetHeadInfo() {
            JSONObject jsonObject = new JSONObject(urlHeaders);
            return jsonObject.toString();
        }

    }

    class CusWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url_) {
            // 点击页面上的链接时会触发，其他不会触发
            Log.i("should", url_);
            if (url_.indexOf("m.startimestv.com") >= 0) {
                // webVu.loadUrl(url_.replaceAll("m.startimestv.com","baidu.com"));
                return false;
            }else if(url_.indexOf("favicon.ico") >= 0){
                return true;
            } else {
                return false;
            }
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            // 加载任何资源时触发，包括页面
            super.onLoadResource(view,url);
            // Log.i("resource", url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.i("onPageStarted", url + " " + (System.currentTimeMillis()-openTime));
            openTime = System.currentTimeMillis();
            // https://juejin.im/entry/591969c9a0bb9f005ff7bac3
            //boolean res = checkUrl(url);
            //根据对URL的检查结果，进行不同的处理，
            //例如，当检查的URL不符合要求时，
            //可以加载本地安全页面，提示用户退出
            //if (!res) {
            //停止加载原页面
            //view.stopLoading();
            //加载安全页面
            //view.loadUrl(LOCAL_SAFE_URL);
            //}
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            loadFinished = true;
            super.onPageFinished(view, url);
            if (view.getProgress() == 100) { // 301的地址不会到100%
                Log.i("onPageFinished", view.getUrl() + " " + (System.currentTimeMillis()-openTime));
            }
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            Log.i("onReceivedError", failingUrl);
            super.onReceivedError(view, errorCode, description, failingUrl);
        }

        // api 23才有支持
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            Log.i("onReceivedHttpError", String.valueOf(errorResponse.getReasonPhrase()));
            super.onReceivedHttpError(view, request, errorResponse);
        }

    }

}
