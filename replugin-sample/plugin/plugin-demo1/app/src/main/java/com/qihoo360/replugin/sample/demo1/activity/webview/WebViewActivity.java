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

package com.qihoo360.replugin.sample.demo1.activity.webview;

import android.app.Activity;
import android.os.Bundle;

import com.qihoo360.replugin.sample.demo1.R;

/**
 * @author RePlugin Team
 *
 * WebView 示例
 */
public class WebViewActivity extends Activity {

    private RePluginWebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_3);

        mWebView = (RePluginWebView) findViewById(R.id.web);
        mWebView.loadUrl("https://github.com/qihoo360/RePlugin");
    }
}