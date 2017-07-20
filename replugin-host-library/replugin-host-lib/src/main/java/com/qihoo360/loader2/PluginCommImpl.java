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

package com.qihoo360.loader2;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;

import com.qihoo360.i.IModule;
import com.qihoo360.i.IPluginManager;
import com.qihoo360.mobilesafe.svcmanager.QihooServiceManager;
import com.qihoo360.replugin.IHostBinderFetcher;
import com.qihoo360.replugin.RePlugin;
import com.qihoo360.replugin.base.IPC;
import com.qihoo360.replugin.component.ComponentList;
import com.qihoo360.replugin.component.process.PluginProcessHost;
import com.qihoo360.replugin.component.utils.IntentMatcherHelper;
import com.qihoo360.replugin.component.utils.PluginClientHelper;
import com.qihoo360.replugin.helper.LogDebug;
import com.qihoo360.replugin.helper.LogRelease;
import com.qihoo360.replugin.model.PluginInfo;

import java.util.HashMap;
import java.util.List;

import static com.qihoo360.replugin.helper.LogDebug.LOG;
import static com.qihoo360.replugin.helper.LogDebug.PLUGIN_TAG;
import static com.qihoo360.replugin.helper.LogRelease.LOGR;

/**
 * 负责宿主与插件、插件间的互通，可通过插件的Factory直接调用，也可通过RePlugin来跳转 <p>
 * TODO 原名为PmLocalImpl。新名字也不太好，待重构后会去掉
 *
 * @author RePlugin Team
 */
public class PluginCommImpl {

    private static final String CONTAINER_PROVIDER_AUTHORITY_PART = ".loader.p.pr";
    static final String INTENT_KEY_THEME_ID = "__themeId";

    /**
     *
     */
    Context mContext;

    /**
     *
     */
    PmBase mPluginMgr;

    PluginCommImpl(Context context, PmBase pm) {
        mContext = context;
        mPluginMgr = pm;
    }

    /**
     * @param name 插件名
     * @return
     */
    public boolean isPluginLoaded(String name) {
        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "isPluginLoaded: name=" + name);
        }
        Plugin plugin = mPluginMgr.getPlugin(name);
        if (plugin == null) {
            return false;
        }
        return plugin.isLoaded();
    }

    /**
     * 此方法调用主程序或特定插件的IPlugin.query，当插件未加载时会尝试加载
     * @param name 插件名
     * @param c 需要查询的interface的类
     * @return
     */
    public IModule query(String name, Class<? extends IModule> c) {
        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "query: name=" + name + " class=" + c.getName());
        }

        HashMap<String, IModule> modules = mPluginMgr.getBuiltinModules(name);
        if (modules != null) {
            return modules.get(c.getName());
        }

        Plugin p = mPluginMgr.loadAppPlugin(name);
        if (p == null) {
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "query: not found plugin,  name=" + name + " class=" + c.getName());
            }
            return null;
        }

        return p.query(c);
    }

    /**
     * @param name 插件名
     * @param binder 需要查询的binder的类
     * @return
     */
    public IBinder query(String name, String binder) {
        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "query: name=" + name + " binder=" + binder);
        }

        // 先用仿插件对象判断
        {
            IHostBinderFetcher p = mPluginMgr.getBuiltinPlugin(name);
            if (p != null) {
                if (LOG) {
                    LogDebug.d(PLUGIN_TAG, "use buildin plugin");
                }
                return p.query(binder);
            }
        }

        Plugin p = mPluginMgr.loadAppPlugin(name);
        if (p == null) {
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "query: not found plugin,  name=" + name + " binder=" + binder);
            }
            return null;
        }

        return p.query(binder);
    }

    /**
     * @param name 插件名
     * @param binder 需要查询的binder的类
     * @param process 是否在指定进程中启动
     * @return
     */
    public IBinder query(String name, String binder, int process) {
        // 自己进程
        if (IPC.isPersistentProcess() && process == IPluginManager.PROCESS_PERSIST) {
            return query(name, binder);
        }
        // 自己进程
        if (IPC.isUIProcess() && process == IPluginManager.PROCESS_UI) {
            return query(name, binder);
        }
        // 自己进程(自定义进程)
        String processTail = PluginProcessHost.processTail(IPC.getCurrentProcessName());
        if (PluginProcessHost.PROCESS_INT_MAP.containsKey(processTail)
                && process == PluginProcessHost.PROCESS_INT_MAP.get(processTail)) {
            return query(name, binder);
        }

        // 需要在常驻里启动
        if (process == IPluginManager.PROCESS_PERSIST) {
            try {
                return PluginProcessMain.getPluginHost().queryPluginBinder(name, binder);
            } catch (Throwable e) {
                if (LOGR) {
                    LogRelease.e(PLUGIN_TAG, "q.p.b: " + e.getMessage(), e);
                }
            }
            return null;
        }
        // 剩下的交给SvcManager去干
        return QihooServiceManager.getPluginService(mContext, name, binder);
    }

    /**
     * 警告：低层接口
     * 当插件升级之后，通过adapter.jar标准接口，甚至invoke接口都无法完成任务时，可通过此接口反射来完成任务
     * @param name 插件名
     * @return 插件的context，可通过此context得到插件的ClassLoader
     */
    public Context queryPluginContext(String name) {
        Plugin p = mPluginMgr.loadAppPlugin(name);
        if (p != null) {
            return p.mLoader.mPkgContext;
        }

        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "not found plugin=" + name);
        }

        return null;
    }

    /**
     * 警告：低层接口
     * 调用此接口会在当前进程加载插件（不加载代码，只加载资源）
     * @param name 插件名
     * @return 插件的Resources
     */
    public Resources queryPluginResouces(String name) {
        // 先从缓存获取
        Resources resources = Plugin.queryCachedResources(Plugin.queryCachedFilename(name));
        if (resources != null) {
            return resources;
        }

        Plugin p = mPluginMgr.loadResourcePlugin(name, this);
        if (p != null) {
            return p.mLoader.mPkgResources;
        }

        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "not found plugin=" + name);
        }

        return null;
    }

    /**
     * 警告：低层接口
     * 调用此接口会在当前进程加载插件（不加载代码，只加载资源）
     * @param name 插件名
     * @return 插件的PackageInfo
     */
    public PackageInfo queryPluginPackageInfo(String name) {
        // 先从缓存获取
        PackageInfo packageInfo = Plugin.queryCachedPackageInfo(Plugin.queryCachedFilename(name));
        if (packageInfo != null) {
            return packageInfo;
        }

        Plugin p = mPluginMgr.loadPackageInfoPlugin(name, this);
        if (p != null) {
            return p.mLoader.mPackageInfo;
        }

        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "not found plugin=" + name);
        }

        return null;
    }

    /**
     * 警告：低层接口
     * 调用此接口会在当前进程加载插件（不加载代码和资源，只获取PackageInfo）
     *
     * @param pkgName 插件包名
     * @param flags   Flags
     * @return 插件的PackageInfo
     */
    public PackageInfo queryPluginPackageInfo(String pkgName, int flags) {
        // 根据 pkgName 取得 pluginName
        String pluginName = Plugin.queryPluginNameByPkgName(pkgName);
        if (!TextUtils.isEmpty(pluginName)) {
            return queryPluginPackageInfo(pluginName);
        }
        return null;
    }

    /**
     * 警告：低层接口
     * 调用此接口会在当前进程加载插件（不加载代码和资源，只获取ComponentList）
     * @param name 插件名
     * @return 插件的ComponentList
     */
    public ComponentList queryPluginComponentList(String name) {
        // 先从缓存获取
        ComponentList cl = Plugin.queryCachedComponentList(Plugin.queryCachedFilename(name));
        if (cl != null) {
            return cl;
        }

        Plugin p = mPluginMgr.loadPackageInfoPlugin(name, this);
        if (p != null) {
            return p.mLoader.mComponents;
        }

        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "not found plugin=" + name);
        }

        return null;
    }

    /**
     * 警告：低层接口
     * 调用此接口会在当前进程加载插件（不启动App）
     * @param name 插件名
     * @return 插件的Resources
     */
    public ClassLoader queryPluginClassLoader(String name) {
        // 先从缓存获取
        ClassLoader cl = Plugin.queryCachedClassLoader(Plugin.queryCachedFilename(name));
        if (cl != null) {
            return cl;
        }

        Plugin p = mPluginMgr.loadDexPlugin(name, this);
        if (p != null) {
            return p.mLoader.mClassLoader;
        }

        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "not found plugin=" + name);
        }

        return null;
    }

    /**
     * 警告：低层接口
     * 调用此接口会“依据PluginInfo中指定的插件信息”，在当前进程加载插件（不启动App）。通常用于“指定路径来直接安装”的情况
     * 注意：调用此接口将不会“通知插件更新”
     * Added by Jiongxuan Zhang
     * @param pi 插件信息
     * @return 插件的Resources
     */
    public ClassLoader loadPluginClassLoader(PluginInfo pi) {
        // 不从缓存中获取，而是直接初始化ClassLoader
        Plugin p = mPluginMgr.loadPlugin(pi, this, Plugin.LOAD_DEX, false);
        if (p != null) {
            return p.mLoader.mClassLoader;
        }

        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "not found plugin=" + pi.getName());
        }

        return null;
    }

    /**
     * 警告：低层接口
     * 调用此接口会在当前进程加载插件（不加载代码和资源，只获取Collection<ReceiverInfo>）
     * @return 符合 action 的所有 ReceiverInfo
     */
    public List<ActivityInfo> queryPluginsReceiverList(Intent intent) {
        IPluginHost pluginHost = PluginProcessMain.getPluginHost();
        if (pluginHost != null) {
            try {
                return pluginHost.queryPluginsReceiverList(intent);
            } catch (Throwable e) {
                if (LOG) {
                    LogDebug.e(PLUGIN_TAG, "Query PluginsReceiverList fail:" + e.toString());
                }
            }
        }
        return null;
    }

    /**
     * 启动一个插件中的activity，如果插件不存在会触发下载界面
     * @param context 应用上下文或者Activity上下文
     * @param intent
     * @param plugin 插件名
     * @param activity 待启动的activity类名
     * @param process 是否在指定进程中启动
     * @return 插件机制层是否成功，例如没有插件存在、没有合适的Activity坑
     */
    public boolean startActivity(Context context, Intent intent, String plugin, String activity, int process) {
        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "start activity: intent=" + intent + " plugin=" + plugin + " activity=" + activity + " process=" + process);
        }

        return mPluginMgr.mInternal.startActivity(context, intent, plugin, activity, process, true);
    }

    /**
     * 启动一个插件中的 activity 'forResult'
     * @return 插件机制层是否成功，例如没有插件存在、没有合适的Activity坑
     */
    public boolean startActivityForResult(Activity activity, Intent intent, int requestCode, Bundle options) {
        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "startActivityForResult: intent=" + intent + " requestCode=" + requestCode+ " options=" + options);
        }

        return mPluginMgr.mInternal.startActivityForResult(activity, intent, requestCode, options);
    }

    /**
     * 加载插件Activity，在startActivity之前调用
     * @param intent
     * @param plugin 插件名
     * @param target 目标Service名，如果传null，则取获取到的第一个
     * @param process 是否在指定进程中启动
     * @return
     */
    public ComponentName loadPluginActivity(Intent intent, String plugin, String activity, int process) {

        ActivityInfo ai = null;
        String container = null;
        PluginBinderInfo info = new PluginBinderInfo(PluginBinderInfo.ACTIVITY_REQUEST);

        try {
            // 获取 ActivityInfo(可能是其它插件的 Activity，所以这里使用 pair 将 pluginName 也返回)
            ai = getActivityInfo(plugin, activity, intent);
            if (ai == null) {
                if (LOG) {
                    LogDebug.d(PLUGIN_TAG, "PACM: bindActivity: activity not found");
                }
                return null;
            }

            // 存储此 Activity 在插件 Manifest 中声明主题到 Intent
            intent.putExtra(INTENT_KEY_THEME_ID, ai.theme);
            if (LOG) {
                LogDebug.d("theme", String.format("intent.putExtra(%s, %s);", ai.name, ai.theme));
            }

            // 根据 activity 的 processName，选择进程 ID 标识
            if (ai.processName != null) {
                process = PluginClientHelper.getProcessInt(ai.processName);
            }

            // 容器选择（启动目标进程）
            IPluginClient client = MP.startPluginProcess(plugin, process, info);
            if (client == null) {
                return null;
            }

            // 远程分配坑位
            container = client.allocActivityContainer(plugin, process, ai.name, intent);
            if (LOG) {
                LogDebug.i(PLUGIN_TAG, "alloc success: container=" + container + " plugin=" + plugin + " activity=" + activity);
            }
        } catch (Throwable e) {
            if (LOGR) {
                LogRelease.e(PLUGIN_TAG, "l.p.a spp|aac: " + e.getMessage(), e);
            }
        }

        // 分配失败
        if (TextUtils.isEmpty(container)) {
            return null;
        }

        PmBase.cleanIntentPluginParams(intent);

        // TODO 是否重复
        // 附上额外数据，进行校验
//        intent.putExtra(PluginManager.EXTRA_PLUGIN, plugin);
//        intent.putExtra(PluginManager.EXTRA_ACTIVITY, activity);
//        intent.putExtra(PluginManager.EXTRA_PROCESS, process);
//        intent.putExtra(PluginManager.EXTRA_CONTAINER, container);

        PluginIntent ii = new PluginIntent(intent);
        ii.setPlugin(plugin);
        ii.setActivity(ai.name);
        ii.setProcess(IPluginManager.PROCESS_AUTO);
        ii.setContainer(container);
        ii.setCounter(0);
        return new ComponentName(IPC.getPackageName(), container);
    }

    /**
     * 启动插件Service，在startService、bindService之前调用
     * @param plugin 插件名
     * @param target 目标Service名，如果传null，则取获取到的第一个
     * @param process 是否在指定进程中启动
     * @return
     */
    public ComponentName loadPluginService(String plugin, String target, int process) {
        String container = null;

        PluginBinderInfo info = new PluginBinderInfo(PluginBinderInfo.SERVICE_REQUEST);
        try {
            // 容器选择
            IPluginClient client = MP.startPluginProcess(plugin, process, info);
            if (client == null) {
                return null;
            }

            // 直接分配
            container = IPC.getPackageName() + PmBase.CONTAINER_SERVICE_PART + info.index;

            return new ComponentName(IPC.getPackageName(), container);
        } catch (Throwable e) {
            if (LOGR) {
                LogRelease.e(PLUGIN_TAG, "l.p.p spp: " + e.getMessage(), e);
            }
        }

        return null;
    }

    /**
     * 启动插件的Provider
     * @param plugin 插件名
     * @param target 目标Provider名，如果传null，则取获取到的第一个
     * @param process 是否在指定进程中启动
     * @return
     * @deprecated 已废弃该方法，请使用PluginProviderClient里面的方法
     */
    public Uri loadPluginProvider(String plugin, String target, int process) {
        PluginBinderInfo info = new PluginBinderInfo(PluginBinderInfo.PROVIDER_REQUEST);
        try {
            // 容器选择
            IPluginClient client = MP.startPluginProcess(plugin, process, info);
            if (client == null) {
                return null;
            }

            String auth = IPC.getPackageName() + CONTAINER_PROVIDER_AUTHORITY_PART + info.index;

            // 直接分配
            return new Uri.Builder().scheme("content").encodedAuthority(auth).encodedPath("main").build();
        } catch (Throwable e) {
            if (LOGR) {
                LogRelease.e(PLUGIN_TAG, "l.p.p spp: " + e.getMessage(), e);
            }
        }

        return null;
    }

    /**
     * 通过ClassLoader来获取插件名
     *
     * @param cl ClassLoader对象
     * @return 插件名，若和主程序一致，则返回IModule.PLUGIN_NAME_MAIN（“main”）
     * Added by Jiongxuan Zhang
     */
    public String fetchPluginName(ClassLoader cl) {
        if (cl == mContext.getClassLoader()) {
            // Main工程的ClassLoader
            return RePlugin.PLUGIN_NAME_MAIN;
        }
        Plugin p = mPluginMgr.lookupPlugin(cl);
        if (p == null) {
            // 没有拿到插件的
            return null;
        }
        return p.mInfo.getName();
    }

    /**
     * 根据条件，查找 ActivityInfo 对象
     *
     * @param plugin   插件名称
     * @param activity Activity 名称
     * @param intent   调用者传递过来的 Intent
     * @return 插件中 Activity 的 ActivityInfo
     */
    public ActivityInfo getActivityInfo(String plugin, String activity, Intent intent) {
        // 获取插件对象
        Plugin p = mPluginMgr.loadAppPlugin(plugin);
        if (p == null) {
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "PACM: bindActivity: may be invalid plugin name or load plugin failed: plugin=" + p);
            }
            return null;
        }

        ActivityInfo ai = null;

        // activity 不为空时，从插件声明的 Activity 集合中查找
        if (!TextUtils.isEmpty(activity)) {
            ai = p.mLoader.mComponents.getActivity(activity);
        } else {
            // activity 为空时，根据 Intent 匹配
            ai = IntentMatcherHelper.getActivityInfo(mContext, plugin, intent);
        }
        return ai;
    }
}
