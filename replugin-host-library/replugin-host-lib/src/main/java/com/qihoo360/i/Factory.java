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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;

import com.qihoo360.loader2.PluginCommImpl;
import com.qihoo360.mobilesafe.core.BuildConfig;
import com.qihoo360.replugin.RePlugin;
import com.qihoo360.replugin.component.ComponentList;

import java.util.List;

/**
 * Wrapper类，简化使用代码
 *
 * <p>插件文件名规范：barcode-1-10-2.jar<p>
 * <ul>
 * <li>
 * 最小支持版本 ：例如 1<br>
 * 插件可选择host/adapter，低于该版本的host/adapter是无法使用<br>
 * 即插件可限制老的host/adapter不能使用，只能在特定版本以后的新的host/adapter使用
 * </li>
 * <li>
 * 当前接口版本：例如 10<br>
 * host/adapter可选择插件
 * </li>
 * <li>
 * 插件自身版本：例如 2<br>
 * 通常是用来表示是一个bug修复版本
 * </li>
 * </ul>
 *
 * <p>适配器文件名规范：adapter-1-10-3.jar<p>
 * <ul>
 * <li>
 * 支持接口版本 ：例如 1<br>
 * 表明该适配器支持的主程序
 * </li>
 * <li>
 * 当前接口版本：例如 10<br>
 * 当插件的当前接口版本小于或等于该值时才会加载，即适配器可选择特定版本以前的插件<br>
 * 也就是说，限制只能使用较老插件，不支持超出适配器能力之外的插件
 * </li>
 * <li>
 * 适配器自身版本：例如 3<br>
 * 通常是用来表示是一个bug修复版本
 * </li>
 * </ul>
 *
 * @author RePlugin Team
 * @deprecated 慢慢会被废弃掉，只留着旧卫士插件反射用。现阶段先不做优化
 */
public final class Factory {

    /**
     * 插件的入口包名前缀
     * 在插件中，该包名不能混淆
     * 例如，二维码的插件入口类为：com.qihoo360.plugin.barcode.Entry
     * @hide 内部框架使用
     */
    public static final String PLUGIN_ENTRY_PACKAGE_PREFIX = "com.qihoo360.plugin";

    /**
     * 新版SDK（RePlugin-library）插件入口报名前缀
     * 在插件中，该包名不能混淆
     */
    public static final String REPLUGIN_LIBRARY_ENTRY_PACKAGE_PREFIX = "com.qihoo360.replugin";

    /**
     * 插件的入口类
     * 在插件中，该名字不能混淆
     * @hide 内部框架使用
     */
    public static final String PLUGIN_ENTRY_CLASS_NAME = "Entry";

    /**
     * 插件的入口类导出函数
     * 在插件中，该方法名不能混淆
     * 通过该函数创建IPlugin对象
     * @hide 内部框架使用
     */
    public static final String PLUGIN_ENTRY_EXPORT_METHOD_NAME = "create";

    /**
     * 参数1：插件上下文，可通过它获取应用上下文
     * 参数2：
     * @hide 内部框架使用
     */
    public static final Class<?> PLUGIN_ENTRY_EXPORT_METHOD_PARAMS[] = {
        Context.class, IPluginManager.class
    };

    /**
     * 参数1：插件上下文，可通过它获取应用上下文
     * 参数2：HOST的类加载器
     * 参数3：已废弃
     * 返回：插件 IPlugin.aidl
     * @hide 内部框架使用
     */
    public static final Class<?> PLUGIN_ENTRY_EXPORT_METHOD2_PARAMS[] = {
        Context.class, ClassLoader.class, IBinder.class
    };

    /**
     * @hide 内部框架使用
     */
    public static PluginCommImpl sPluginManager;

    /**
     * @deprecated 新插件框架不再用i接口依赖，此接口已废弃
     * @param name 插件名
     * @param c 需要查询的interface的类
     * @return
     */
    @Deprecated
    public static final IModule query(String name, Class<? extends IModule> c) {
        return sPluginManager.query(name, c);
    }

    /**
     * 调用此接口不会在当前进程加载插件
     * @param name 插件名
     * @return
     */
    public static final boolean isPluginLoaded(String name) {
        return sPluginManager.isPluginLoaded(name);
    }

    /**
     * 调用此接口会在当前进程加载插件
     * @param name 插件名
     * @param binder 需要查询的binder的名称（不要用IXXX.class.getName，因为不再建议keep IXXX类，IXXX有可能被混淆）
     * @return
     */
    public static final IBinder query(String name, String binder) {
        return sPluginManager.query(name, binder);
    }

    /**
     * 调用此接口会在指定进程加载插件
     * @param name 插件名
     * @param binder 需要查询的binder的名称（不要用IXXX.class.getName，因为不再建议keep IXXX类，IXXX有可能被混淆）
     * @param process 是否在指定进程中启动
     * @return
     */
    public static final IBinder query(String name, String binder, int process) {
        return sPluginManager.query(name, binder, process);
    }

    /**
     * 警告：低层接口
     * 当插件升级之后，通过adapter.jar标准接口，甚至invoke接口都无法完成任务时，可通过此接口反射来完成任务
     * 调用此接口会在当前进程加载插件
     * @param name 插件名
     * @return 插件的context，可通过此context得到插件的ClassLoader
     */
    public static final Context queryPluginContext(String name) {
        return sPluginManager.queryPluginContext(name);
    }

    /**
     * 警告：低层接口
     * 调用此接口会在当前进程加载插件（不加载代码，只加载资源和PackageInfo）
     * @param name 插件名
     * @return 插件的Resources
     */
    public static final Resources queryPluginResouces(String name) {
        return sPluginManager.queryPluginResouces(name);
    }

    /**
     * 警告：低层接口
     * 调用此接口会在当前进程加载插件（不加载代码和资源，只获取PackageInfo）
     * @param name 插件名
     * @return 插件的PackageInfo
     */
    public static final PackageInfo queryPluginPackageInfo(String name) {
        return sPluginManager.queryPluginPackageInfo(name);
    }

    /**
     * 警告：低层接口
     * 调用此接口会在当前进程加载插件（不加载代码和资源，只获取PackageInfo）
     *
     * @param pkgName 插件包名
     * @param flags   Flags
     * @return 插件的PackageInfo
     */
    public static final PackageInfo queryPluginPackageInfo(String pkgName, int flags) {
        return sPluginManager.queryPluginPackageInfo(pkgName, flags);
    }

    /**
     * 警告：低层接口
     * 调用此接口会在当前进程加载插件（不加载代码和资源，只获取ComponentList）
     * @param name 插件名
     * @return 插件的ComponentList
     */
    public static final ComponentList queryPluginComponentList(String name) {
        return sPluginManager.queryPluginComponentList(name);
    }

    /**
     * 警告：低层接口
     * 调用此接口会在当前进程加载插件（不启动App）
     * @param name 插件名
     * @return 插件的ComponentList
     */
    public static final ClassLoader queryPluginClassLoader(String name) {
        return sPluginManager.queryPluginClassLoader(name);
    }

    /**
     * 根据 插件名称 和 Activity 名称 查询 ActivityInfo
     *
     * @param name      插件名称
     * @param className Activity 名称
     * @return Activity 对应的 ActivityInfo
     */
    public static final ActivityInfo queryActivityInfo(String name, String className) {
        ComponentList componentList = sPluginManager.queryPluginComponentList(name);
        if (componentList != null) {
            return componentList.getActivity(className);
        } else {
            return null;
        }
    }

    /**
     * 根据 插件名称 和 Service 名称 查询 ServiceInfo
     *
     * @param name      插件名称
     * @param className Service 名称
     * @return Service 对应的 ServiceInfo
     */
    public static final ServiceInfo queryServiceInfo(String name, String className) {
        ComponentList componentList = sPluginManager.queryPluginComponentList(name);
        if (componentList != null) {
            return componentList.getService(className);
        } else {
            return null;
        }
    }

    /**
     * 根据 activity 和 intent 中的数据获取 ActivityInfo 信息
     * @param plugin 插件名
     * @param activity Activity 名称
     * @param intent 其中可能包含 action
     */
    public static ActivityInfo getActivityInfo(String plugin, String activity, Intent intent) {
        return sPluginManager.getActivityInfo(plugin, activity, intent);
    }

    /**
     * 根据 action 从插件获取 receiver 列表
     *
     * @return 符合 action 的所有 ReceiverInfo
     */
    public static List<ActivityInfo> queryPluginsReceiverList(Intent intent) {
        return sPluginManager.queryPluginsReceiverList(intent);
    }

    /**
     * 启动一个插件中的activity，如果插件不存在会触发下载界面
     * @deprecated 只为旧插件而用。请使用RePlugin.startActivity方法
     * @param context 应用上下文或者Activity上下文
     * @param intent Intent对象
     * @param plugin 插件名
     * @param activity 待启动的activity类名
     * @param process 是否在指定进程中启动
     * @return 插件机制层，是否成功，例如没有插件存在、没有合适的Activity坑
     */
    public static final boolean startActivity(Context context, Intent intent, String plugin, String activity, int process) {

        // 此方法“唯一”调用路径是从插件或主程序中调用Factory.startActivity，表示调用方是“要求”打开插件Activity的，排除了要打开宿主Activity的情况
        // 为了和旧插件Factory.startActivity方法兼容，判断当plugin和activity均有值，则自动帮其填入
        // 注意：
        // 1. 仅在此方法上生效，其余方法均不能这么做，防止出现“本想打开宿主，结果定向到了插件”的问题
        // 2. 若以Action打开，则无需（也不能）填写ComponentName
        // 3. plugin/activity会覆盖Intent.ComponentName（为兼容旧插件），毕竟在框架内部，这两个组合也是优先于CN的
        // Added by Jiongxuan Zhang
        if (!TextUtils.isEmpty(plugin) && !TextUtils.isEmpty(activity)) {
            intent.setComponent(RePlugin.createComponentName(plugin, activity));
        }
        return startActivityWithNoInjectCN(context, intent, plugin, activity, process);
    }

    /**
     * 内部接口，仅为Factory2.startActivity(context, intent) 和 RePlugin.startActivity方法而使用
     *
     * @param context  应用上下文或者Activity上下文
     * @param intent   Intent对象
     * @param plugin   插件名
     * @param activity 待启动的activity类名
     * @param process  是否在指定进程中启动
     * @return 插件机制层，是否成功，例如没有插件存在、没有合适的Activity坑
     * Added by Jiongxuan Zhang
     */
    public static final boolean startActivityWithNoInjectCN(Context context, Intent intent, String plugin, String activity, int process) {
        boolean result = sPluginManager.startActivity(context, intent, plugin, activity, process);

        RePlugin.getConfig().getEventCallbacks().onStartActivityCompleted(plugin, activity, result);
        return result;
    }

    /**
     * 加载插件Activity，在startActivity之前调用
     * @param intent
     * @param plugin 插件名
     * @param target 目标Activity名，如果传null，则取获取到的第一个
     * @param process 是否在指定进程中启动
     * @return
     */
    public static final ComponentName loadPluginActivity(Intent intent, String plugin, String target, int process) {
        return sPluginManager.loadPluginActivity(intent, plugin, target, process);
    }

    /**
     * 加载插件Service，在startService、bindService之前调用
     * @param plugin 插件名
     * @param target 目标Service名，如果传null，则取获取到的第一个
     * @param process 是否在指定进程中启动
     * @return
     */
    public static final ComponentName loadPluginService(String plugin, String target, int process) {
        return sPluginManager.loadPluginService(plugin, target, process);
    }

    /**
     * 加载插件的Provider，在使用插件的Provider之前调用
     * @param plugin 插件名
     * @param target 目标Provider名，如果传null，则取获取到的第一个
     * @param process 是否在指定进程中启动
     * @return
     *
     * @deprecated 已废弃，请使用PluginProviderClient里面的方法
     */
    @Deprecated
    public static final Uri loadPluginProvider(String plugin, String target, int process) {
        return sPluginManager.loadPluginProvider(plugin, target, process);
    }

    /**
     * 不要直接使用该方法，否则会抛出异常（Debug）
     * @deprecated 已废弃，请使用PluginProviderClient里面的方法
     */
    public static final Uri makePluginProviderUri(String plugin, Uri uri, int process) {
        // 因目前没有插件要用，所以直接抛出异常即可
        if (BuildConfig.DEBUG) {
            throw new IllegalStateException();
        }
        return uri;
    }

    /**
     * 通过ClassLoader来获取插件名
     *
     * @param cl ClassLoader对象
     * @return 插件名，若和主程序一致，则返回IModule.PLUGIN_NAME_MAIN（“main”）
     * Added by Jiongxuan Zhang
     */
    public static final String fetchPluginName(ClassLoader cl) {
        return sPluginManager.fetchPluginName(cl);
    }

    /**
     * 通过 forResult 方式启动一个插件的 Activity
     *
     * @param activity    源 Activity
     * @param intent      要打开 Activity 的 Intent，其中 ComponentName 的 Key 必须为插件名
     * @param requestCode 请求码
     * @param options     附加的数据
     * @since 2.1.3
     */
    public static boolean startActivityForResult(Activity activity, Intent intent, int requestCode, Bundle options) {
        return sPluginManager.startActivityForResult(activity, intent, requestCode, options);
    }
}
