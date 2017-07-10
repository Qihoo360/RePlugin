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

package com.qihoo360.replugin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.qihoo360.replugin.model.PluginInfo;

/**
 * 插件化框架对外事件回调接口集
 * <p>
 * 宿主需继承此类，并复写相应的方法来自定义插件框架的事件处理机制
 *
 * @author RePlugin Team
 */
public class RePluginEventCallbacks {

    protected final Context mContext;

    public RePluginEventCallbacks(Context context) {
        mContext = context;
    }

    /**
     * 安装插件失败
     *
     * @param path 插件路径
     * @param code 安装失败的原因
     */
    public void onInstallPluginFailed(String path, InstallResult code) {
        // Nothing
    }

    /**
     * 安装插件成功
     *
     * @param info 插件信息
     */
    public void onInstallPluginSucceed(PluginInfo info) {
        // Nothing
    }

    /**
     * 启动 Activity 完成
     *
     * @param plugin   插件名称
     * @param activity 插件 Activity
     * @param result   启动是否成功
     */
    public void onStartActivityCompleted(String plugin, String activity, boolean result) {
        // Nothing
    }

    /**
     * 当插件Activity准备分配坑位时执行
     *
     * @param intent 要打开的插件的Activity
     */
    public void onPrepareAllocPitActivity(Intent intent) {
        // Nothing
    }

    /**
     * 当插件Activity即将被打开时执行，在onActivityPitAllocated之后被执行
     *
     * @param context      要打开的Activity所在的Context
     * @param intent       原来要打开的插件的Activity
     * @param pittedIntent 目标坑位的Activity
     */
    public void onPrepareStartPitActivity(Context context, Intent intent, Intent pittedIntent) {
        // Nothing
    }

    /**
     * 当插件Activity所在的坑位被执行“销毁”时被执行
     *
     * @param activity 要销毁的Activity对象，通常是插件里的Activity
     */
    public void onActivityDestroyed(Activity activity) {
        // Nothing
    }

    /**
     * 当插件Service的Binder被释放时被执行
     */
    public void onBinderReleased() {
        // Nothing
    }

    /**
     * 插件安装结果值
     */
    public enum InstallResult {
        SUCCEED,
        V5_FILE_BUILD_FAIL,
        V5_FILE_UPDATE_FAIL,
        READ_PKG_INFO_FAIL,
        VERIFY_SIGN_FAIL,
        VERIFY_VER_FAIL,
        COPY_APK_FAIL
    }
}