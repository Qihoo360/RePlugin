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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.res.Resources;
import android.net.Uri;
import android.os.IBinder;

import com.qihoo360.replugin.component.ComponentList;
import com.qihoo360.replugin.model.PluginInfo;

import java.util.List;

/**
 * 负责插件和插件之间的interface互通，可通过插件Entry得到，也可通过wrapper类Factory直接调用
 *
 * @author RePlugin Team
 */
public interface IPluginManager {

    /**
     * 插件Activity上下文通过startActivity启动，用系统默认的启动方法
     * 如果设置该值，总是为boolean值true
     */
    String KEY_COMPATIBLE = "compatible";

    /**
     * 通过Intent的extra参数指出目标插件名
     * 插件Activity上下文通过startActivity启动其它插件Activity时用到
     * 如果不指定，则默认用当前Activity的插件
     * 如果设置了KEY_COMPATIBLE，则忽略此参数
     */
    String KEY_PLUGIN = "plugin";

    /**
     * 通过Intent的extra参数指出目标Activity名
     * 如果不指定，则默认用ComponentName参数的类名来启动
     * 如果设置了KEY_COMPATIBLE，则忽略此参数
     */
    String KEY_ACTIVITY = "activity";

    /**
     * 通过Intent的extra参数指出需要在指定进程中启动
     * 只能启动standard的Activity，不能启动singleTask、singleInstance等
     * 不指定时，自动分配插件进程，即PROCESS_AUTO
     */
    String KEY_PROCESS = "process";

    /**
     * 自动分配插件进程
     */
    int PROCESS_AUTO = Integer.MIN_VALUE;

    /**
     * UI进程
     */
    int PROCESS_UI = -1;

    /**
     * 常驻进程
     */
    int PROCESS_PERSIST = -2;

    /**
     * 此方法调用主程序或特定插件的IPlugin.query，当插件未加载时会尝试加载
     * @param name 插件名
     * @param c 需要查询的interface的类
     * @return
     */
    IModule query(String name, Class<? extends IModule> c);

    /**
     * @param name 插件名
     * @return
     */
    boolean isPluginLoaded(String name);

    /**
     * @param name 插件名
     * @param binder 需要查询的binder的类
     * @return
     */
    IBinder query(String name, String binder);

    /**
     * @param name 插件名
     * @param binder 需要查询的binder的类
     * @param process 是否在指定进程中启动
     * @return
     */
    IBinder query(String name, String binder, int process);

    /**
     * 警告：低层接口
     * 当插件升级之后，通过adapter.jar标准接口，甚至invoke接口都无法完成任务时，可通过此接口反射来完成任务
     * @param name 插件名
     * @return 插件的context，可通过此context得到插件的ClassLoader
     */
    Context queryPluginContext(String name);

    /**
     * 警告：低层接口
     * 调用此接口会在当前进程加载插件（不加载代码，只加载资源）
     * @param name 插件名
     * @return 插件的Resources
     */
    Resources queryPluginResouces(String name);

    /**
     * 警告：低层接口
     * 调用此接口会在当前进程加载插件（不加载代码，只加载资源）
     * @param name 插件名
     * @return 插件的PackageInfo
     */
    PackageInfo queryPluginPackageInfo(String name);

    /**
     * 警告：低层接口
     * 调用此接口会在当前进程加载插件（不加载代码和资源，只获取PackageInfo）
     *
     * @param pkgName 插件包名
     * @param flags   Flags
     * @return 插件的PackageInfo
     */
    PackageInfo queryPluginPackageInfo(String pkgName, int flags);

    /**
     * 警告：低层接口
     * 调用此接口会在当前进程加载插件（不加载代码和资源，只获取ComponentList）
     * @param name 插件名
     * @return 插件的ComponentList
     */
    ComponentList queryPluginComponentList(String name);

    /**
     * 警告：低层接口
     * 调用此接口会在当前进程加载插件（不启动App）
     * @param name 插件名
     * @return 插件的Resources
     */
    ClassLoader queryPluginClassLoader(String name);

    /**
     * 警告：低层接口
     * 调用此接口会“依据PluginInfo中指定的插件信息”，在当前进程加载插件（不启动App）。通常用于“指定路径来直接安装”的情况
     * 注意：调用此接口将不会“通知插件更新”
     * Added by Jiongxuan Zhang
     * @param pi 插件信息
     * @return 插件的Resources
     */
    ClassLoader loadPluginClassLoader(PluginInfo pi);

    /**
     * 警告：低层接口
     * 调用此接口会在当前进程加载插件（不加载代码和资源，只获取Collection<ReceiverInfo>）
     * @return 符合 action 的所有 ReceiverInfo
     */
    List<ActivityInfo> queryPluginsReceiverList(Intent intent);

    /**
     * 启动一个插件中的activity，如果插件不存在会触发下载界面
     * @param context 应用上下文或者Activity上下文
     * @param intent
     * @param plugin 插件名
     * @param activity 待启动的activity类名
     * @param process 是否在指定进程中启动
     * @return 插件机制层是否成功，例如没有插件存在、没有合适的Activity坑
     */
    boolean startActivity(Context context, Intent intent, String plugin, String activity, int process);

    /**
     * 加载插件Activity，在startActivity之前调用
     * @param intent
     * @param plugin 插件名
     * @param target 目标Service名，如果传null，则取获取到的第一个
     * @param process 是否在指定进程中启动
     * @return
     */
    ComponentName loadPluginActivity(Intent intent, String plugin, String target, int process);

    /**
     * 启动插件Service，在startService、bindService之前调用
     * @param plugin 插件名
     * @param target 目标Service名，如果传null，则取获取到的第一个
     * @param process 是否在指定进程中启动
     * @return
     */
    ComponentName loadPluginService(String plugin, String target, int process);

    /**
     * 启动插件的Provider
     * @param plugin 插件名
     * @param target 目标Provider名，如果传null，则取获取到的第一个
     * @param process 是否在指定进程中启动
     * @return
     * @deprecated 已废弃该方法，请使用PluginProviderClient里面的方法
     */
    Uri loadPluginProvider(String plugin, String target, int process);

    /**
     * 通过ClassLoader来获取插件名
     *
     * @param cl ClassLoader对象
     * @return 插件名，若和主程序一致，则返回IModule.PLUGIN_NAME_MAIN（“main”）
     * Added by Jiongxuan Zhang
     */
    String fetchPluginName(ClassLoader cl);

    /**
     * 根据条件，查找 ActivityInfo 对象
     *
     * @param plugin   插件名称
     * @param activity Activity 名称
     * @param intent   调用者传递过来的 Intent
     * @return 插件中 Activity 的 ActivityInfo
     */
    ActivityInfo getActivityInfo(String plugin, String activity, Intent intent);
}
