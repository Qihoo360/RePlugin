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

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.qihoo360.i.Factory;
import com.qihoo360.i.IModule;
import com.qihoo360.i.IPlugin;
import com.qihoo360.i.IPluginManager;
import com.qihoo360.mobilesafe.core.BuildConfig;
import com.qihoo360.mobilesafe.parser.manifest.ManifestParser;
import com.qihoo360.replugin.RePlugin;
import com.qihoo360.replugin.base.IPC;
import com.qihoo360.replugin.component.ComponentList;
import com.qihoo360.replugin.component.process.PluginProcessHost;
import com.qihoo360.replugin.component.receiver.PluginReceiverProxy;
import com.qihoo360.replugin.helper.LogDebug;
import com.qihoo360.replugin.helper.LogRelease;
import com.qihoo360.replugin.model.PluginInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static com.qihoo360.replugin.helper.LogDebug.LOG;
import static com.qihoo360.replugin.helper.LogDebug.PLUGIN_TAG;
import static com.qihoo360.replugin.helper.LogRelease.LOGR;

/**
 * @author RePlugin Team
 */
class Loader {

    private final Context mContext;

    private final String mPluginName;

    final String mPath;

    final Plugin mPluginObj;

    PackageInfo mPackageInfo;

    Resources mPkgResources;

    Context mPkgContext;

    ClassLoader mClassLoader;

    /**
     * 记录所有缓存的Component列表
     */
    ComponentList mComponents;

    Method mCreateMethod;

    Method mCreateMethod2;

    IPlugin mPlugin;

    IPluginHost mPluginHost;

    ProxyPlugin mBinderPlugin;

    /**
     * layout缓存：忽略表
     */
    HashSet<String> mIgnores = new HashSet<String>();

    /**
     * layout缓存：构造器表
     */
    HashMap<String, Constructor<?>> mConstructors = new HashMap<String, Constructor<?>>();

    static class ProxyPlugin implements IPlugin {

        com.qihoo360.loader2.IPlugin mPlugin;

        ProxyPlugin(IBinder plugin) {
            mPlugin = com.qihoo360.loader2.IPlugin.Stub.asInterface(plugin);
        }

        @Override
        public IModule query(Class<? extends IModule> c) {
            IBinder b = null;
            try {
                b = mPlugin.query(c.getName());
            } catch (Throwable e) {
                if (LOGR) {
                    LogRelease.e(PLUGIN_TAG, "query(" + c + ") exception: " + e.getMessage(), e);
                }
            }
            // TODO: return IModule
            return null;
        }
    }

    /**
     * 初始化Loader对象
     *
     * @param p Plugin类的对象
     *          为何会反向依赖plugin对象？因为plugin.mInfo对象会发生变化，
     *          缓存plugin可以实时拿到最新的mInfo对象，防止出现问题
     *          FIXME 有优化空间，但改动量会很大，暂缓
     */
    Loader(Context context, String name, String path, Plugin p) {
        mContext = context;
        mPluginName = name;
        mPath = path;
        mPluginObj = p;
    }

    final boolean isPackageInfoLoaded() {
        return mPackageInfo != null;
    }

    final boolean isResourcesLoaded() {
        return isPackageInfoLoaded() && mPkgResources != null;
    }

    final boolean isDexLoaded() {
        return isResourcesLoaded() && mClassLoader != null;
    }

    final boolean isAppLoaded() {
        return mPlugin != null;
    }

    final Context createBaseContext(Context newBase) {
        return new PluginContext(newBase, android.R.style.Theme, mClassLoader, mPkgResources, mPluginName, this);
    }

    final boolean loadDex(ClassLoader parent, int load) {
        try {
            PackageManager pm = mContext.getPackageManager();

            mPackageInfo = Plugin.queryCachedPackageInfo(mPath);
            if (mPackageInfo == null) {
                // PackageInfo
                mPackageInfo = pm.getPackageArchiveInfo(mPath,
                        PackageManager.GET_ACTIVITIES | PackageManager.GET_SERVICES | PackageManager.GET_PROVIDERS | PackageManager.GET_RECEIVERS | PackageManager.GET_META_DATA);
                if (mPackageInfo == null || mPackageInfo.applicationInfo == null) {
                    if (LOG) {
                        LogDebug.d(PLUGIN_TAG, "get package archive info null");
                    }
                    mPackageInfo = null;
                    return false;
                }
                if (LOG) {
                    LogDebug.d(PLUGIN_TAG, "get package archive info, pi=" + mPackageInfo);
                }
                mPackageInfo.applicationInfo.sourceDir = mPath;
                mPackageInfo.applicationInfo.publicSourceDir = mPath;

                // 添加针对SO库的加载
                // 此属性最终用于ApplicationLoaders.getClassLoader，在创建PathClassLoader时成为其参数
                // 这样findLibrary可不用覆写，即可直接实现SO的加载
                // Added by Jiongxuan Zhang
                PluginInfo pi = mPluginObj.mInfo;
                File ld = pi.getNativeLibsDir();
                mPackageInfo.applicationInfo.nativeLibraryDir = ld.getAbsolutePath();

//                // 若PluginInfo.getFrameworkVersion为FRAMEWORK_VERSION_UNKNOWN（p-n才会有），则这里需要读取并修改
//                if (pi.getFrameworkVersion() == PluginInfo.FRAMEWORK_VERSION_UNKNOWN) {
//                    pi.setFrameworkVersionByMeta(mPackageInfo.applicationInfo.metaData);
//                }

                // 缓存表: pkgName -> pluginName
                synchronized (Plugin.PKG_NAME_2_PLUGIN_NAME) {
                    Plugin.PKG_NAME_2_PLUGIN_NAME.put(mPackageInfo.packageName, mPluginName);
                }

                // 缓存表: pluginName -> fileName
                synchronized (Plugin.PLUGIN_NAME_2_FILENAME) {
                    Plugin.PLUGIN_NAME_2_FILENAME.put(mPluginName, mPath);
                }

                // 缓存表: fileName -> PackageInfo
                synchronized (Plugin.FILENAME_2_PACKAGE_INFO) {
                    Plugin.FILENAME_2_PACKAGE_INFO.put(mPath, new WeakReference<PackageInfo>(mPackageInfo));
                }
            }

            // TODO preload预加载虽然通知到常驻了(但pluginInfo是通过MP.getPlugin(name, true)完全clone出来的)，本进程的PluginInfo并没有得到更新
            // TODO 因此preload会造成某些插件真正生效时由于cache，造成插件版本号2.0或者以上无法生效。
            // TODO 这里是临时做法，避免发版前出现重大问题，后面可以修过修改preload的流程来优化
            // 若PluginInfo.getFrameworkVersion为FRAMEWORK_VERSION_UNKNOWN（p-n才会有），则这里需要读取并修改
            if (mPluginObj.mInfo.getFrameworkVersion() == PluginInfo.FRAMEWORK_VERSION_UNKNOWN) {
                mPluginObj.mInfo.setFrameworkVersionByMeta(mPackageInfo.applicationInfo.metaData);
                // 只有“P-n”插件才会到这里，故无需调用“纯APK”的保存功能
                // PluginInfoList.save();
            }

            // 创建或获取ComponentList表
            // Added by Jiongxuan Zhang
            mComponents = Plugin.queryCachedComponentList(mPath);
            if (mComponents == null) {
                // ComponentList
                mComponents = new ComponentList(mPackageInfo, mPath, mPluginName);

                // 动态注册插件中声明的 receiver
                regReceivers();

                // 缓存表：ComponentList
                synchronized (Plugin.FILENAME_2_COMPONENT_LIST) {
                    Plugin.FILENAME_2_COMPONENT_LIST.put(mPath, new WeakReference<>(mComponents));
                }

                /* 只调整一次 */
                // 调整插件中组件的进程名称
                adjustPluginProcess(mPackageInfo.applicationInfo);

                // 调整插件中 Activity 的 TaskAffinity
                adjustPluginTaskAffinity(mPluginName, mPackageInfo.applicationInfo);
            }

            if (load == Plugin.LOAD_INFO) {
                return isPackageInfoLoaded();
            }

            mPkgResources = Plugin.queryCachedResources(mPath);
            // LOAD_RESOURCES和LOAD_ALL都会获取资源，但LOAD_INFO不可以（只允许获取PackageInfo）
            if (mPkgResources == null) {
                // Resources
                try {
                    if (BuildConfig.DEBUG) {
                        // 如果是Debug模式的话，防止与Instant Run冲突，资源重新New一个
                        Resources r = pm.getResourcesForApplication(mPackageInfo.applicationInfo);
                        mPkgResources = new Resources(r.getAssets(), r.getDisplayMetrics(), r.getConfiguration());
                    } else {
                        mPkgResources = pm.getResourcesForApplication(mPackageInfo.applicationInfo);
                    }
                } catch (NameNotFoundException e) {
                    if (LOG) {
                        LogDebug.d(PLUGIN_TAG, e.getMessage(), e);
                    }
                    return false;
                }
                if (mPkgResources == null) {
                    if (LOG) {
                        LogDebug.d(PLUGIN_TAG, "get resources null");
                    }
                    return false;
                }
                if (LOG) {
                    LogDebug.d(PLUGIN_TAG, "get resources for app, r=" + mPkgResources);
                }

                // 缓存表: Resources
                synchronized (Plugin.FILENAME_2_RESOURCES) {
                    Plugin.FILENAME_2_RESOURCES.put(mPath, new WeakReference<>(mPkgResources));
                }
            }
            if (load == Plugin.LOAD_RESOURCES) {
                return isResourcesLoaded();
            }

            mClassLoader = Plugin.queryCachedClassLoader(mPath);
            if (mClassLoader == null) {
                // ClassLoader
                String out = mPluginObj.mInfo.getDexParentDir().getPath();
                //changeDexMode(out);

                //
                Log.i("dex", "load " + mPath + " ...");
                if (BuildConfig.DEBUG) {
                    // 因为Instant Run会替换parent为IncrementalClassLoader，所以在DEBUG环境里
                    // 需要替换为BootClassLoader才行
                    // Added by yangchao-xy & Jiongxuan Zhang
                    parent = ClassLoader.getSystemClassLoader();
                } else {
                    // 线上环境保持不变
                    parent = getClass().getClassLoader().getParent(); // TODO: 这里直接用父类加载器
                }
                String soDir = mPackageInfo.applicationInfo.nativeLibraryDir;
                mClassLoader = RePlugin.getConfig().getCallbacks().createPluginClassLoader(mPath, out, soDir, parent);
                Log.i("dex", "load " + mPath + " = " + mClassLoader);

                if (mClassLoader == null) {
                    if (LOG) {
                        LogDebug.d(PLUGIN_TAG, "get dex null");
                    }
                    return false;
                }

                // 缓存表：ClassLoader
                synchronized (Plugin.FILENAME_2_DEX) {
                    Plugin.FILENAME_2_DEX.put(mPath, new WeakReference<>(mClassLoader));
                }
            }
            if (load == Plugin.LOAD_DEX) {
                return isDexLoaded();
            }

            // Context
            mPkgContext = new PluginContext(mContext, android.R.style.Theme, mClassLoader, mPkgResources, mPluginName, this);
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "pkg context=" + mPkgContext);
            }

        } catch (Throwable e) {
            if (LOGR) {
                LogRelease.e(PLUGIN_TAG, "p=" + mPath + " m=" + e.getMessage(), e);
            }
            return false;
        }

        return true;
    }

    /**
     * 动态注册插件中静态声明的 receiver 到常驻进程
     *
     * @throws android.os.RemoteException
     */
    private void regReceivers() throws android.os.RemoteException {
        String plugin = mPluginObj.mInfo.getName();

        if (mPluginHost == null) {
            mPluginHost = getPluginHost();
        }

        if (mPluginHost != null) {
            mPluginHost.regReceiver(plugin, ManifestParser.INS.getReceiverFilterMap(plugin));
        }
    }

    /**
     * 获取 IPluginHost Binder 接口
     */
    private IPluginHost getPluginHost() {
        IBinder binder = PluginProviderStub.proxyFetchHostBinder(mContext);
        if (binder == null) {
            if (LOG) {
                LogDebug.e(PluginReceiverProxy.TAG, "p.p fhb fail");
            }
            return null;
        } else {
            return IPluginHost.Stub.asInterface(binder);
        }
    }

    final boolean loadEntryMethod(boolean log) {
        //
        try {
            String className = Factory.PLUGIN_ENTRY_PACKAGE_PREFIX + "." + mPluginName + "." + Factory.PLUGIN_ENTRY_CLASS_NAME;
            Class<?> c = mClassLoader.loadClass(className);
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "found entry: className=" + className + ", loader=" + c.getClassLoader());
            }
            mCreateMethod = c.getDeclaredMethod(Factory.PLUGIN_ENTRY_EXPORT_METHOD_NAME, Factory.PLUGIN_ENTRY_EXPORT_METHOD_PARAMS);
        } catch (Throwable e) {
            if (log) {
                if (LOGR) {
                    LogRelease.e(PLUGIN_TAG, e.getMessage(), e);
                }
            } else {
                if (LOG) {
                    LogDebug.d(PLUGIN_TAG, "loadEntryMethod exception");
                }
            }
        }
        return mCreateMethod != null;
    }

    final boolean invoke(IPluginManager manager) {
        try {
            mPlugin = (IPlugin) mCreateMethod.invoke(null, mPkgContext, manager);
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "Loader.invoke(): plugin=" + mPath + ", cl=" + (mPlugin != null ? mPlugin.getClass().getClassLoader() : "null"));
            }
        } catch (Throwable e) {
            if (LOGR) {
                LogRelease.e(PLUGIN_TAG, e.getMessage(), e);
            }
            return false;
        }
        return true;
    }

    final boolean loadEntryMethod2() {
        //
        try {
            String className = Factory.PLUGIN_ENTRY_PACKAGE_PREFIX + "." + mPluginName + "." + Factory.PLUGIN_ENTRY_CLASS_NAME;
            Class<?> c = mClassLoader.loadClass(className);
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "found entry: className=" + className + ", loader=" + c.getClassLoader());
            }
            mCreateMethod2 = c.getDeclaredMethod(Factory.PLUGIN_ENTRY_EXPORT_METHOD_NAME, Factory.PLUGIN_ENTRY_EXPORT_METHOD2_PARAMS);
        } catch (Throwable e) {
            if (LOGR) {
                LogRelease.e(PLUGIN_TAG, e.getMessage(), e);
            }
        }
        return mCreateMethod2 != null;
    }

    final boolean loadEntryMethod3() {
        //
        try {
            String className = Factory.REPLUGIN_LIBRARY_ENTRY_PACKAGE_PREFIX + "." + Factory.PLUGIN_ENTRY_CLASS_NAME;
            Class<?> c = mClassLoader.loadClass(className);
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "found entry: className=" + className + ", loader=" + c.getClassLoader());
            }
            mCreateMethod2 = c.getDeclaredMethod(Factory.PLUGIN_ENTRY_EXPORT_METHOD_NAME, Factory.PLUGIN_ENTRY_EXPORT_METHOD2_PARAMS);
        } catch (Throwable e) {
            if (LOGR) {
                LogRelease.e(PLUGIN_TAG, e.getMessage(), e);
            }
        }
        return mCreateMethod2 != null;
    }

    final boolean invoke2(IPluginManager x) {
        try {
            IBinder manager = null; // TODO
            IBinder b = (IBinder) mCreateMethod2.invoke(null, mPkgContext, getClass().getClassLoader(), manager);
            if (b == null) {
                if (LOGR) {
                    LogRelease.e(PLUGIN_TAG, "p.e.r.b n");
                }
                return false;
            }
            mBinderPlugin = new ProxyPlugin(b);
            mPlugin = mBinderPlugin;
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "Loader.invoke2(): plugin=" + mPath + ", plugin.binder.cl=" + b.getClass().getClassLoader());
            }
        } catch (Throwable e) {
            if (LOGR) {
                LogRelease.e(PLUGIN_TAG, e.getMessage(), e);
            }
            return false;
        }
        return true;
    }

    /**
     * 调整插件中组件的进程名称
     */
    private void adjustPluginProcess(ApplicationInfo appInfo) throws Exception {
        if (appInfo == null) {
            return;
        }

        Bundle bdl = appInfo.metaData;
        if (bdl == null || TextUtils.isEmpty(bdl.getString("process_map"))) {
            return;
        }

        HashMap<String, String> processMap = new HashMap<>();
        try {
            String processMapStr = bdl.getString("process_map");
            JSONArray ja = new JSONArray(processMapStr);
            for (int i = 0; i < ja.length(); i++) {
                JSONObject jo = (JSONObject) ja.get(i);
                if (jo != null) {
                    String to = jo.getString("to").toLowerCase();
                    if (to.equals("$ui")) {
                        to = IPC.getPackageName();
                    } else {
                        // 非 UI 进程，且是用户自定义的进程
                        if (to.contains("$" + PluginProcessHost.PROCESS_PLUGIN_SUFFIX)) {
                            to = PluginProcessHost.PROCESS_ADJUST_MAP.get(to);
                        }
                    }
                    processMap.put(jo.getString("from"), to);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (!processMap.isEmpty()) {

            if (LOG) {
                Log.d(PLUGIN_TAG, "--- 调整插件中组件的进程 BEGIN ---");
                for (Map.Entry<String, String> entry : processMap.entrySet()) {
                    Log.d(PLUGIN_TAG, entry.getKey() + " -> " + entry.getValue());
                }
            }

            doAdjust(processMap, mComponents.getActivityMap());
            doAdjust(processMap, mComponents.getServiceMap());
            doAdjust(processMap, mComponents.getReceiverMap());
            doAdjust(processMap, mComponents.getProviderMap());

            if (LOG) {
                Log.d(PLUGIN_TAG, "--- 调整插件中组件的进程 END ---");
            }
        }
    }

    private void doAdjust(HashMap<String, String> processMap, HashMap<String, ? extends ComponentInfo> infos) throws Exception {
        for (HashMap.Entry<String, ? extends ComponentInfo> entry : infos.entrySet()) {
            ComponentInfo info = entry.getValue();
            if (info != null) {
                String targetProcess = processMap.get(info.processName);
                // 如果原始进程名称为空，说明解析插件 apk 时有问题（未解析每个组件的进程名称）。
                // 此处抛出异常。
                if (!TextUtils.isEmpty(targetProcess)) {
                    info.processName = targetProcess;
                }

                if (LOG) {
                    Log.d(PLUGIN_TAG, info.name + ":" + info.processName);
                }
            }
        }
    }

    /**
     * 调整插件中 Activity 的默认 TaskAffinity
     *
     * @param plugin 插件名称
     */
    private void adjustPluginTaskAffinity(String plugin, ApplicationInfo appInfo) {
        if (appInfo == null) {
            return;
        }

        Bundle bdl = appInfo.metaData;
        if (bdl != null) {
            boolean useDefault = bdl.getBoolean("use_default_task_affinity", true);
            if (LOG) {
                LogDebug.d(TaskAffinityStates.TAG, "useDefault = " + useDefault);
            }

            if (!useDefault) {
                if (LOG) {
                    LogDebug.d(TaskAffinityStates.TAG, String.format("替换插件 %s 中默认的 TaskAffinity", plugin));
                }

                String defaultPluginTaskAffinity = appInfo.packageName;
                for (HashMap.Entry<String, ActivityInfo> entry : mComponents.getActivityMap().entrySet()) {
                    ActivityInfo info = entry.getValue();
                    if (LOG) {
                        if (info != null) {
                            LogDebug.d(TaskAffinityStates.TAG, String.format("%s.taskAffinity = %s ", info.name, info.taskAffinity));
                        }
                    }

                    // 如果是默认 TaskAffinity
                    if (info != null && info.taskAffinity.equals(defaultPluginTaskAffinity)) {
                        info.taskAffinity = info.taskAffinity + "." + plugin;
                        if (LOG) {
                            LogDebug.d(TaskAffinityStates.TAG, String.format("修改 %s 的 TaskAffinity 为 %s", info.name, info.taskAffinity));
                        }
                    }
                }
            }
        }
    }
}
