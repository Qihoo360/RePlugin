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

package com.qihoo360.i;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.qihoo360.loader2.PluginLibraryInternalProxy;

import org.json.JSONArray;

/**
 * plugin-library中，通过“反射”调用的内部逻辑（如PluginActivity类的调用等）均在此处 <p>
 * 注意：务必要Keep住此类，否则插件调用将失败
 *
 * @author RePlugin Team
 */
public final class Factory2 {

    /**
     * @hide 内部框架使用
     */
    public static PluginLibraryInternalProxy sPLProxy;

    /**
     * @hide 内部方法，插件框架使用
     * 插件的Activity创建成功后通过此方法获取其base context
     * @param activity
     * @param newBase
     * @return 为Activity构造一个base Context
     */
    public static final Context createActivityContext(Activity activity, Context newBase) {
        return sPLProxy.createActivityContext(activity, newBase);
    }

    /**
     * @hide 内部方法，插件框架使用
     * 插件的Activity的onCreate调用前调用此方法
     * @param activity
     * @param savedInstanceState
     */
    public static final void handleActivityCreateBefore(Activity activity, Bundle savedInstanceState) {
        sPLProxy.handleActivityCreateBefore(activity, savedInstanceState);
    }

    /**
     * @hide 内部方法，插件框架使用
     * 插件的Activity的onCreate调用后调用此方法
     * @param activity
     * @param savedInstanceState
     */
    public static final void handleActivityCreate(Activity activity, Bundle savedInstanceState) {
        sPLProxy.handleActivityCreate(activity, savedInstanceState);
    }

    /**
     * @hide 内部方法，插件框架使用
     * 插件的Activity的onDestroy调用后调用此方法
     * @param activity
     */
    public static final void handleActivityDestroy(Activity activity) {
        sPLProxy.handleActivityDestroy(activity);
    }

    /**
     * @hide 内部方法，插件框架使用
     * 插件的Activity的onRestoreInstanceState调用后调用此方法
     * @param activity
     * @param savedInstanceState
     */
    public static final void handleRestoreInstanceState(Activity activity, Bundle savedInstanceState) {
        sPLProxy.handleRestoreInstanceState(activity, savedInstanceState);
    }

    /**
     * @hide 内部方法，插件框架使用
     * 插件的Service的onCreate调用后调用此方法
     * @param service
     */
    public static final void handleServiceCreate(Service service) {
        sPLProxy.handleServiceCreate(service);
    }

    /**
     * @hide 内部方法，插件框架使用
     * 插件的Service的onDestroy调用后调用此方法
     * @param service
     */
    public static final void handleServiceDestroy(Service service) {
        sPLProxy.handleServiceDestroy(service);
    }

    /**
     * @hide 内部方法，插件框架使用
     * 启动一个插件中的activity
     * 通过Extra参数IPluginManager.KEY_COMPATIBLE，IPluginManager.KEY_PLUGIN，IPluginManager.KEY_ACTIVITY，IPluginManager.KEY_PROCESS控制
     * @param context Context上下文
     * @param intent
     * @return 插件机制层是否成功，例如没有插件存在、没有合适的Activity坑
     */
    public static final boolean startActivity(Context context, Intent intent) {
        return sPLProxy.startActivity(context, intent);
    }

    /**
     * @hide 内部方法，插件框架使用
     * 启动一个插件中的activity
     * 通过Extra参数IPluginManager.KEY_COMPATIBLE，IPluginManager.KEY_PLUGIN，IPluginManager.KEY_ACTIVITY，IPluginManager.KEY_PROCESS控制
     * @param activity Activity上下文
     * @param intent
     * @return 插件机制层是否成功，例如没有插件存在、没有合适的Activity坑
     */
    public static final boolean startActivity(Activity activity, Intent intent) {
        return sPLProxy.startActivity(activity, intent);
    }

    /**
     * @hide 内部方法，插件框架使用
     * 启动一个插件中的activity，如果插件不存在会触发下载界面
     * @param context 应用上下文或者Activity上下文
     * @param intent
     * @param plugin 插件名
     * @param activity 待启动的activity类名
     * @param process 是否在指定进程中启动
     * @param download 下载
     * @return 插件机制层是否成功，例如没有插件存在、没有合适的Activity坑
     */
    public static final boolean startActivity(Context context, Intent intent, String plugin, String activity, int process, boolean download) {
        return sPLProxy.startActivity(context, intent, plugin, activity, process, download);
    }

    /**
     * 通过 forResult 方式启动一个插件的 Activity
     *
     * @param activity    源 Activity
     * @param intent      要打开 Activity 的 Intent，其中 ComponentName 的 Key 必须为插件名
     * @param requestCode 请求码
     * @param options     附加的数据
     * @see #startActivityForResult(Activity, Intent, int, Bundle)
     */
    public static final boolean startActivityForResult(Activity activity, Intent intent, int requestCode, Bundle options) {
        return sPLProxy.startActivityForResult(activity, intent, requestCode, options);
    }

    /**
     * @hide 内部方法，插件框架使用
     * 返回所有插件的json串，格式见plugins-builtin.json文件
     * @param name 插件名，传null或者空串表示获取全部
     * @return
     */
    public static final JSONArray fetchPlugins(String name) {
        return sPLProxy.fetchPlugins(name);
    }

    /**
     * @hide 内部方法，插件框架使用
     * 登记动态映射的类
     * @param className 壳类名
     * @param plugin 目标插件名
     * @param type 目标类的类型: activity, service, provider
     * @param target 目标类名
     * @return
     */
    public static final boolean registerDynamicClass(String className, String plugin, String type, String target) {
        return sPLProxy.registerDynamicClass(className, plugin, type, target);
    }

    /**
     * @hide 内部方法，插件框架使用
     * 登记动态映射的类
     * @param className 壳类名
     * @param plugin 目标插件名
     * @param target 目标类名
     * @return
     */
    public static final boolean registerDynamicClass(String className, String plugin, String target, Class defClass) {
        return sPLProxy.registerDynamicClass(className, plugin, target, defClass);
    }

    /**
     * @hide 内部方法，插件框架使用
     * 查询动态映射的类
     * @param className 壳类名
     * @param plugin 目标插件名
     * @return
     */
    public static final boolean isDynamicClass(String plugin, String className) {
        return sPLProxy.isDynamicClass(plugin, className);
    }

    public static void unregisterDynamicClass(String source) {
        sPLProxy.unregisterDynamicClass(source);
    }

    /**
     * @hide 内部方法，插件框架调用
     * 根据动态注册的类，反查此类对应的插件名称
     *
     * @param className 动态类名称
     * @return 插件名称
     */
    public static final String getPluginByDynamicClass(String className) {
        return sPLProxy.getPluginByDynamicClass(className);
    }
}
