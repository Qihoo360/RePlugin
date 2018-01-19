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

package com.qihoo360.replugin.sample.webview;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.LinearLayout;

import com.qihoo360.replugin.sample.webview.views.SimpleWebPage;

/**
 * @author RePlugin Team
 */
public class MainActivity extends Activity {

    private SimpleWebPage mWP;

    static final String testUrl = "https://github.com/Qihoo360/RePlugin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.webview);
        LinearLayout rootView = (LinearLayout) findViewById(R.id.root);

        // 添加webview视图
        mWP = new SimpleWebPage(this);
        rootView.addView(mWP);
        mWP.getWebView().loadUrl(testUrl);
    }

}
