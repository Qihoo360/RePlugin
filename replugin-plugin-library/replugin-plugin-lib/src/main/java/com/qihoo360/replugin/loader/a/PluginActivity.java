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

package com.qihoo360.replugin.loader.a;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.qihoo360.replugin.RePluginInternal;
import com.qihoo360.replugin.helper.LogRelease;

/**
 * 插件内的BaseActivity，建议插件内所有的Activity都要继承此类
 * 此类通过override方式来完成插件框架的相关工作
 *
 * @author RePlugin Team
 */
public abstract class PluginActivity extends Activity {

    @Override
    protected void attachBaseContext(Context newBase) {
        newBase = RePluginInternal.createActivityContext(this, newBase);
        super.attachBaseContext(newBase);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //
        RePluginInternal.handleActivityCreateBefore(this, savedInstanceState);

        super.onCreate(savedInstanceState);

        //
        RePluginInternal.handleActivityCreate(this, savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        //
        RePluginInternal.handleActivityDestroy(this);

        super.onDestroy();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        //
        RePluginInternal.handleRestoreInstanceState(this, savedInstanceState);

        try {
            super.onRestoreInstanceState(savedInstanceState);
        } catch (Throwable e) {
            // Added by Jiongxuan Zhang
            // Crash Hash: B1F67129BC6A67C882AF2BBE62202BF0
            // java.lang.IllegalArgumentException: Wrong state class异常
            // 原因：恢复现场时，Activity坑位找错了。通常是用于占坑的Activity的层级过深导致
            // 举例：假如我们只有一个坑位可用，A和B分别是清理和通讯录的两个Activity
            //      如果进程重启，系统原本恢复B，却走到了A，从而出现此问题
            // 解决：将其Catch住，这样系统在找ViewState时不会出错。
            // 后遗症：
            // 1、可能无法恢复系统级View的保存的状态；
            // 2、如果自己代码处理不当，可能会出现异常。故自己代码一定要用SecExtraUtils来获取Bundle数据
            if (LogRelease.LOGR) {
                LogRelease.e("PluginActivity", "o r i s: p=" + getPackageCodePath() + "; " + e.getMessage(), e);
            }
        }
    }

    @Override
    public void startActivity(Intent intent) {
        //
        if (RePluginInternal.startActivity(this, intent)) {
            // 这个地方不需要回调startActivityAfter，因为Factory2最终还是会回调回来，最终还是要走super.startActivity()
            return;
        }

        super.startActivity(intent);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        //
        if (RePluginInternal.startActivityForResult(this, intent, requestCode)) {
            // 这个地方不需要回调startActivityAfter，因为Factory2最终还是会回调回来，最终还是要走super.startActivityForResult()
            return;
        }

        super.startActivityForResult(intent, requestCode);

    }
}
