/*
 * Copyright (C) 2005-2017 Qihoo 360 Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed To in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.qihoo360.replugin.sample.webview.views;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.RelativeLayout;

import com.qihoo360.replugin.sample.webview.common.CommonWebChromeClient;
import com.qihoo360.replugin.sample.webview.common.CommonWebViewClient;
import com.qihoo360.replugin.sample.webview.env.Env;

import static android.webkit.WebView.setWebContentsDebuggingEnabled;

/**
 * @author RePlugin Team
 */
public class SimpleWebPage extends RelativeLayout {

    // 使用者提供的Context
    private Context mUserContext;
    private SimpleWebView mWebView;

    public SimpleWebPage(Context context) {
        super(context.getApplicationContext());
        mUserContext = context;
        init();
    }

    public SimpleWebPage(Context context, AttributeSet attrs) {
        super(context.getApplicationContext(), attrs);
        mUserContext = context;
        init();
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void init() {
        if (Env.DEBUG) {
            setWebContentsDebuggingEnabled(true);
        }
        RelativeLayout.LayoutParams rootView = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
        // 将WebView加到布局中
        mWebView = new SimpleWebView(mUserContext);
        // Android 7.0以上的webview不设置背景（默认背景应该是透明的），渲染有问题，有明显的卡顿
        mWebView.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        addView(mWebView, rootView);
        // 辅助处理各种通知、请求事件，如果不设置WebViewClient，请求会跳转系统浏览器
        mWebView.setWebViewClient(new CommonWebViewClient());
        // 辅助处理JavaScript、页喧解析渲染、页面标题、加载进度等等
        mWebView.setWebChromeClient(new CommonWebChromeClient());

        WebSettings ws = mWebView.getSettings();

        // 允许缩放
        ws.setBuiltInZoomControls(true);
        //默认不显示ZoomButton，否则会有windowLeaked警告
        if (Build.VERSION.SDK_INT > 10) {
            ws.setDisplayZoomControls(false);
        }
        // 设置是否允许WebView使用JavaScript
        ws.setJavaScriptEnabled(true);
        // 启动地理定位
        ws.setGeolocationEnabled(true);
        // 开启DomStorage缓存
        ws.setDomStorageEnabled(true);
        ws.setJavaScriptCanOpenWindowsAutomatically(true);
        ws.setAppCacheEnabled(false);
        // 防止WebView跨源攻击
        // 设置是否允许WebView使用File协议，默认值是允许
        // 注意：不允许使用File协议，则不会存在通过file协议的跨源安全威胁，但同时也限制了WebView的功能，使其不能加载本地的HTML文件
        ws.setAllowFileAccess(false);
        if (Build.VERSION.SDK_INT >= 16) {
        // 设置是否允许通过file url加载的Javascript读取其他的本地文件
        ws.setAllowFileAccessFromFileURLs(false);
        // 设置是否允许通过file url加载的Javascript可以访问其他的源，包括其他的文件和http,https等其他的源
        ws.setAllowUniversalAccessFromFileURLs(false);
        }
        // 防止个人敏感数据泄漏
        ws.setSavePassword(false);
        ws.setSaveFormData(false);
        ws.setUserAgentString(mWebView.getUserAgentEx());
        if (Build.VERSION.SDK_INT >= 21) {
            ws.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            CookieManager.getInstance().acceptThirdPartyCookies(mWebView);
        }
        if (mUserContext instanceof Activity) {
            // 此处添加自定义的JS接口
//            mWebView.addJavascriptInterface(...);
        }
    }

    public WebView getWebView() {
        return mWebView;
    }
}
