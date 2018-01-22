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

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.qihoo360.replugin.sample.webview.utils.WebViewResourceHelper;

/**
 * @author RePlugin Team
 */
public class SimpleWebView extends WebView {

    private static String sUserAgent;

    public SimpleWebView(Context context) {
        this(context, null);
    }

    public SimpleWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // webview 插件化后对资源的统一处理
        WebViewResourceHelper.addChromeResourceIfNeeded(context);
    }

    public SimpleWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // webview 插件化后对资源的统一处理
        WebViewResourceHelper.addChromeResourceIfNeeded(context);
    }

    public String getUserAgentEx() {
        if (sUserAgent == null) {
            WebSettings ws = getSettings();
            sUserAgent = ws.getUserAgentString();
        }

        // 此处可自定义自己的UserAgent
        return sUserAgent;
    }

    @Override
    public void loadUrl(String url) {
        // 此处可“种植”Cookie（Q&T）
        super.loadUrl(url);
    }
}
