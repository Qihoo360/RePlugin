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

package com.qihoo360.replugin.sample.webview.env;

import android.os.Build;
import android.webkit.WebView;

import java.lang.reflect.Method;

/**
 * @author RePlugin Team
 */
public class Env {
    public static final boolean DEBUG = true;
    public static final String TAG = "webview_demo";

    private static void setWebContentsDebuggingEnabled(boolean b) {
        if (Build.VERSION.SDK_INT < 19) {
            return;
        }
        try {
            Method m = WebView.class.getMethod("setWebContentsDebuggingEnabled", boolean.class);
            m.invoke(null, b);
        } catch (Exception e) {
            if (Env.DEBUG) {
                e.printStackTrace();
            }
        }
    }
}