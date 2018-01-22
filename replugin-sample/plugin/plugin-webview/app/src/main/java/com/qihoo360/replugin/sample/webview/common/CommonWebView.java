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

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.webkit.JsPromptResult;
import android.webkit.WebView;

import com.qihoo360.replugin.sample.webview.env.Env;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author RePlugin Team
 */
public class CommonWebView extends WebView {

    public CommonWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public CommonWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CommonWebView(Context context) {
        super(context);
        init();
    }

    private void init() {
        removeSearchBoxImpl();
    }

    private boolean removeSearchBoxImpl() {
        try {
            if (Build.VERSION.SDK_INT >= 11 && !(Build.VERSION.SDK_INT >= 17)) {
                invokeMethod("removeJavascriptInterface", "searchBoxJavaBridge_");
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void invokeMethod(String method, String param) {
        Method m;
        try {
            m = WebView.class.getDeclaredMethod(method, String.class);
            m.setAccessible(true);
            m.invoke(this, param);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addJavascriptInterface(Object obj, String interfaceName) {
        if (TextUtils.isEmpty(interfaceName)) {
            return;
        }

        injectJavascriptInterfaces();
    }

    public boolean handleJsInterface(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
        try {
            // 此处添加对message的解析处理逻辑等
            result.cancel();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        result.cancel();
        return false;
    }

    public void injectJavascriptInterfaces() {
        String jsStringCache = genJavascriptInterfacesString();
        try {
            if (!TextUtils.isEmpty(jsStringCache)) {
                this.loadUrl(jsStringCache);
            }
        } catch (Exception e) {
            if (Env.DEBUG) {
                e.printStackTrace();
            }
        }

    }

    private String genJavascriptInterfacesString() {
        // 此处添加自己具体要注入的 JS 代码段
        StringBuilder script = new StringBuilder();
        script.append("javascript:(function JsAddJavascriptInterface_(){");
        // add ...
        script.append("})()");

        return script.toString();
    }
}
