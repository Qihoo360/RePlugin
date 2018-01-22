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

package com.qihoo360.replugin.sample.webview.common;

import android.graphics.Bitmap;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * @author RePlugin Team
 */
public class CommonWebViewClient extends WebViewClient {

    @Override
    public void onLoadResource(WebView view, String url) {
        try {
            if (view instanceof CommonWebView) {
                CommonWebView webview = (CommonWebView) view;
                webview.injectJavascriptInterfaces();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onLoadResource(view, url);
    }

    @Override
    public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
        try {
            if (view instanceof CommonWebView) {
                CommonWebView webview = (CommonWebView) view;
                webview.injectJavascriptInterfaces();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.doUpdateVisitedHistory(view, url, isReload);
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        try {
            if (view instanceof CommonWebView) {
                CommonWebView webview = (CommonWebView) view;
                webview.injectJavascriptInterfaces();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onPageStarted(view, url, favicon);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        try {
            if (view instanceof CommonWebView) {
                CommonWebView webview = (CommonWebView) view;
                webview.injectJavascriptInterfaces();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onPageFinished(view, url);
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        try {
            if (view instanceof CommonWebView) {
                CommonWebView webview = (CommonWebView) view;
                webview.injectJavascriptInterfaces();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onReceivedError(view, errorCode, description, failingUrl);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        try {
            if (view instanceof CommonWebView) {
                CommonWebView webview = (CommonWebView) view;
                webview.injectJavascriptInterfaces();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.shouldOverrideUrlLoading(view, url);
        // 默认不调用第三方浏览器
        return false;
    }

}
