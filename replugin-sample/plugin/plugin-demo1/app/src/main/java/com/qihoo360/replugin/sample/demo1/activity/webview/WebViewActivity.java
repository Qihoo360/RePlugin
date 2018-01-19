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
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

import com.qihoo360.replugin.sample.demo1.R;
import com.qihoo360.replugin.sample.demo1.webview.WebPageProxy;

/**
 * @author RePlugin Team
 *
 * WebView 示例，具体业务逻辑由webView插件来实现
 */
public class WebViewActivity extends Activity {

    static final String testUrl = "https://github.com/Qihoo360/RePlugin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 从WebView插件获取WebrViewPage的代理
        WebPageProxy viewProxy = WebPageProxy.create(this);
        if (viewProxy == null) {
            Toast.makeText(this, " WebPageProxy create error!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        View contentView = viewProxy.getView();
        if (contentView == null) {
            Toast.makeText(this, " WebPageProxy get View error!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setContentView(contentView);

        WebView simpleWebPage = viewProxy.getWebView();
        simpleWebPage.loadUrl(testUrl);

    }
}