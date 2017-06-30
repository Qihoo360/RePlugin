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

import org.json.JSONArray;

/**
 * @hide 内部框架使用
 *
 * @author RePlugin Team
 */
public interface IPluginActivityManager {

    /**
     * @hide 内部方法，插件框架使用
     * 插件的Activity创建成功后通过此方法获取其base context
     * @param activity
     * @param newBase
     * @return 为Activity构造一个base Context
     */
    Context createActivityContext(Activity activity, Context newBase);

    /**
     * @hide 内部方法，插件框架使用
     * 插件的Activity的onCreate调用前调用此方法
     * @param activity
     * @param savedInstanceState
     */
    void handleActivityCreateBefore(Activity activity, Bundle savedInstanceState);

    /**
     * @hide 内部方法，插件框架使用
     * 插件的Activity的onCreate调用后调用此方法
     * @param activity
     * @param savedInstanceState
     */
    void handleActivityCreate(Activity activity, Bundle savedInstanceState);

    /**
     * @hide 内部方法，插件框架使用
     * 插件的Activity的onDestroy调用后调用此方法
     * @param activity
     */
    void handleActivityDestroy(Activity activity);

    /**
     * @hide 内部方法，插件框架使用
     * 插件的Activity的onRestoreInstanceState调用后调用此方法
     * @param activity
     * @param savedInstanceState
     */
    void handleRestoreInstanceState(Activity activity, Bundle savedInstanceState);

    /**
     * @hide 内部方法，插件框架使用
     * 插件的Service的onCreate调用后调用此方法
     * @param service
     */
    void handleServiceCreate(Service service);

    /**
     * @hide 内部方法，插件框架使用
     * 插件的Service的onDestroy调用后调用此方法
     * @param service
     */
    void handleServiceDestroy(Service service);

    /**
     * @hide 内部方法，插件框架使用
     * 启动一个插件中的activity
     * 通过Extra参数IPluginManager.KEY_COMPATIBLE，IPluginManager.KEY_PLUGIN，IPluginManager.KEY_ACTIVITY，IPluginManager.KEY_PROCESS控制
     * @param activity Activity上下文
     * @param intent
     * @return 插件机制层是否成功，例如没有插件存在、没有合适的Activity坑
     */
    boolean startActivity(Activity activity, Intent intent);

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
    boolean startActivity(Context context, Intent intent, String plugin, String activity, int process, boolean download);

    /**
     * @hide 内部方法，插件框架使用
     * 返回所有插件的json串，格式见plugins-builtin.json文件
     * @param name 插件名，传null或者空串表示获取全部
     * @return
     */
    JSONArray fetchPlugins(String name);

    /**
     * @hide 内部方法，插件框架使用
     * 登记动态映射的类(6.5.0 later)
     * @param className 壳类名
     * @param plugin 目标插件名
     * @param type 目标类的类型: activity, service, provider
     * @param target 目标类名
     * @return
     */
    boolean registerDynamicClass(String className, String plugin, String type, String target);

    /**
     * @hide 内部方法，插件框架使用
     * 登记动态映射的类(7.7.0 later)
     */
    boolean registerDynamicClass(String className, String plugin, String target, Class defClass);

    /**
     * @hide 内部方法，插件框架使用
     * 查询某个类是否是动态映射的类(7.7.0 later)
     */
    boolean isDynamicClass(String plugin, String className);

    /**
     * @hide 内部方法，插件框架使用
     * 查询某个动态映射的类对应的插件(7.7.0 later)
     */
    String getPluginByDynamicClass(String className);
}
