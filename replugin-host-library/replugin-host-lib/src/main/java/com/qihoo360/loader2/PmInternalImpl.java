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
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.qihoo360.i.Factory;
import com.qihoo360.i.Factory2;
import com.qihoo360.i.IPluginActivityManager;
import com.qihoo360.i.IPluginManager;
import com.qihoo360.loader.utils.ReflectUtils;
import com.qihoo360.replugin.RePlugin;
import com.qihoo360.replugin.base.IPC;
import com.qihoo360.replugin.component.activity.ActivityInjector;
import com.qihoo360.replugin.helper.HostConfigHelper;
import com.qihoo360.replugin.helper.LogDebug;
import com.qihoo360.replugin.helper.LogRelease;
import com.qihoo360.replugin.model.PluginInfo;

import com.qihoo360.replugin.ext.lang3.ClassUtils;
import com.qihoo360.replugin.ext.lang3.reflect.FieldUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.Set;

import static com.qihoo360.replugin.helper.LogDebug.LOG;
import static com.qihoo360.replugin.helper.LogDebug.PLUGIN_TAG;
import static com.qihoo360.replugin.helper.LogRelease.LOGR;

/**
 * @author RePlugin Team
 */
class PmInternalImpl implements IPluginActivityManager {

    /**
     *
     */
    PmBase mPluginMgr;

    PmInternalImpl(PmBase pm) {
        mPluginMgr = pm;
    }

    @Override
    public boolean startActivity(Activity activity, Intent intent) {
        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "start activity: intent=" + intent);
        }

        // 兼容模式，直接使用标准方式启动
        if (intent.getBooleanExtra(IPluginManager.KEY_COMPATIBLE, false)) {
            PmBase.cleanIntentPluginParams(intent);
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "start activity: COMPATIBLE is true, direct start");
            }
            return false;
        }

        // 获取插件名，有两种途径：
        // 1. 从Intent里取。通常是明确知道要打开的插件时会用
        // 2. 根据当前Activity的坑位名来“反查”其插件名。通常是插件内开启自己的Activity时用到
        String plugin = intent.getStringExtra(IPluginManager.KEY_PLUGIN);

        /* 检查是否是动态注册的类 */
        // 如果要启动的 Activity 是动态注册的类，则直接启动，不经过 SDK。
        ComponentName componentName = activity.getComponentName();
        if (LogDebug.LOG) {
            LogDebug.d("loadClass", "isHookingClass(" + plugin + "," + componentName.getClassName() + ") = "
                    + isDynamicClass(plugin, componentName.getClassName()));
        }
        if (isDynamicClass(plugin, componentName.getClassName())) {
            intent.setComponent(new ComponentName(IPC.getPackageName(), componentName.getClassName()));
            activity.startActivity(intent);
            return true;
        }

        if (TextUtils.isEmpty(plugin)) {
            //
            PluginContainers.ActivityState state = null;
            if (activity.getComponentName() != null) {
                state = mPluginMgr.mClient.mACM.lookupByContainer(activity.getComponentName().getClassName());
            }
            if (state != null) {
                plugin = state.plugin;
            }
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "start activity: custom plugin is empty, query plugin=" + plugin);
            }
        }

        // 从 ClassLoader 获取插件名称
        if (TextUtils.isEmpty(plugin)) {
            plugin = Factory.fetchPluginName(activity.getClassLoader());
        }

        // 获取Activity的名字，有两种途径：
        // 1. 从Intent里取。通常是明确知道要打开的插件的Activity时会用
        // 2. 从Intent的ComponentName中获取，同上
        String name = intent.getStringExtra(IPluginManager.KEY_ACTIVITY);
        if (TextUtils.isEmpty(name)) {
            ComponentName cn = intent.getComponent();
            if (cn != null) {
                name = cn.getClassName();
                if (LOG) {
                    LogDebug.d(PLUGIN_TAG, "start activity: custom activity=" + activity);
                }
            }
        }

        // 插件名和Activity名，只要缺少一样，就表示打开的Activity可能是宿主的。直接打开即可
        if (TextUtils.isEmpty(plugin)) {
            PmBase.cleanIntentPluginParams(intent);
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "start activity: plugin and activity is empty, direct start");
            }
            return false;
        }

        // 已经是标准坑了，直接使用标准方式启动
        if (mPluginMgr.isActivity(name)) {
            PmBase.cleanIntentPluginParams(intent);
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "start activity: activity is container, direct start");
            }
            return false;
        }

        // 获取进程值，看目标Activity要打开哪个进程
        int process = intent.getIntExtra(IPluginManager.KEY_PROCESS, Integer.MIN_VALUE);

        PmBase.cleanIntentPluginParams(intent);

        // 调用“特殊版”的startActivity，不让自动填写ComponentName，防止外界再用时出错
        return Factory.startActivityWithNoInjectCN(activity, intent, plugin, name, process);
    }

    // FIXME 建议去掉plugin和activity参数，直接用intent代替
    @Override
    public boolean startActivity(Context context, Intent intent, String plugin, String activity, int process, boolean download) {
        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "start activity: intent=" + intent + " plugin=" + plugin + " activity=" + activity + " process=" + process + " download=" + download);
        }

        // 是否启动下载
        // 若插件不可用（不存在或版本不匹配），则直接弹出“下载插件”对话框
        // 因为已经打开UpdateActivity，故在这里返回True，告诉外界已经打开，无需处理
        if (download) {
            if (PluginTable.getPluginInfo(plugin) == null) {
                if (LOG) {
                    LogDebug.d(PLUGIN_TAG, "plugin=" + plugin + " not found, start download ...");
                }

                // 如果用户在下载即将完成时突然点按“取消”，则有可能出现插件已下载成功，但没有及时加载进来的情况
                // 因此我们会判断这种情况，如果是，则重新加载一次即可，反之则提示用户下载
                // 原因：“取消”会触发Task.release方法，最终调用mDownloadTask.destroy，导致“下载服务”的Receiver被注销，即使文件下载了也没有回调回来
                // NOTE isNeedToDownload方法会调用pluginDownloaded再次尝试加载
                if (isNeedToDownload(context, plugin)) {
                    return RePlugin.getConfig().getCallbacks().onPluginNotExistsForActivity(context, plugin, intent, process);
                }
            }
        }

        // 如果是 activity 是动态注册的类，则直接打开
        if (LOG) {
            LogDebug.d("loadClass", "isHookingClass(" + plugin + " , " + activity + ") = "
                    + Factory2.isDynamicClass(plugin, activity));
        }

        if (Factory2.isDynamicClass(plugin, activity)) {
            intent.setComponent(new ComponentName(IPC.getPackageName(), activity));
            context.startActivity(intent);
            return true;
        }

        // 如果插件状态出现问题，则每次弹此插件的Activity都应提示无法使用，或提示升级（如有新版）
        // Added by Jiongxuan Zhang
        if (PluginStatusController.getStatus(plugin) < PluginStatusController.STATUS_OK) {
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "PmInternalImpl.startActivity(): Plugin Disabled. pn=" + plugin);
            }
            return RePlugin.getConfig().getCallbacks().onPluginNotExistsForActivity(context, plugin, intent, process);
        }

        // 若为首次加载插件，且是“大插件”，则应异步加载，同时弹窗提示“加载中”
        // Added by Jiongxuan Zhang
        if (!RePlugin.isPluginDexExtracted(plugin)) {
            PluginDesc pd = PluginDesc.get(plugin);
            if (pd != null && pd.isLarge()) {
                if (LOG) {
                    LogDebug.d(PLUGIN_TAG, "PM.startActivity(): Large Plugin! p=" + plugin);
                }
                return RePlugin.getConfig().getCallbacks().onLoadLargePluginForActivity(context, plugin, intent, process);
            }
        }

        // WARNING：千万不要修改intent内容，尤其不要修改其ComponentName
        // 因为一旦分配坑位有误（或压根不是插件Activity），则外界还需要原封不动的startActivity到系统中
        // 可防止出现“本来要打开宿主，结果被改成插件”，进而无法打开宿主Activity的问题

        // 缓存打开前的Intent对象，里面将包括Action等内容
        Intent from = new Intent(intent);

        // 帮助填写打开前的Intent的ComponentName信息（如有。没有的情况如直接通过Action打开等）
        if (!TextUtils.isEmpty(plugin) && !TextUtils.isEmpty(activity)) {
            from.setComponent(new ComponentName(plugin, activity));
        }

        ComponentName cn = mPluginMgr.mLocal.loadPluginActivity(intent, plugin, activity, process);
        if (cn == null) {
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "plugin cn not found: intent=" + intent + " plugin=" + plugin + " activity=" + activity + " process=" + process);
            }
            return false;
        }

        // 将Intent指向到“坑位”。这样：
        // from：插件原Intent
        // to：坑位Intent
        intent.setComponent(cn);

        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "start activity: real intent=" + intent);
        }

//        if (RePluginInternal.FOR_DEV) {
//            try {
//                String str = cn.getPackageName() + "/" + cn.getClassName();
//                if (LOG) {
//                    LogDebug.d(PLUGIN_TAG, "str=" + str);
//                }
//                new ProcessBuilder().command("am", "start", "-D", "--user", "0", "-n", str).start();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        } else {

        context.startActivity(intent);

        // 通知外界，已准备好要打开Activity了
        // 其中：from为要打开的插件的Intent，to为坑位Intent
        RePlugin.getConfig().getCallbacks().onPrepareStartPitActivity(context, from, intent);

        return true;
    }

    private boolean isNeedToDownload(Context context, String plugin) {
        // 以下两种情况需要下载插件：
        // 1、V5文件不存在（常见）；
        // 2、V5文件非法（加载失败）
        String n = V5FileInfo.getFileName(plugin);
        File f = new File(RePlugin.getConfig().getPnInstallDir(), n);
        if (!f.exists()) {
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "isNeedToDownload(): V5 file not exists. Plugin = " + plugin);
            }
            return true;
        }

        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "isNeedToDownload(): V5 file exists. Extracting... Plugin = " + plugin);
        }

        PluginInfo i = MP.pluginDownloaded(f.getAbsolutePath());
        if (i == null) {
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "isNeedToDownload(): V5 file is invalid. Plugin = " + plugin);
            }
            return true;
        }

        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "isNeedToDownload(): V5 file is Okay. Loading... Plugin = " + plugin);
        }
        return false;
    }

    @Override
    public Context createActivityContext(Activity activity, Context newBase) {
//        PluginContainers.ActivityState state = mPluginMgr.mClient.mACM.lookupLastLoading(activity.getClass().getName());
//        if (state == null) {
//            if (LOG) {
//                LogDebug.w(PLUGIN_TAG, "PACM: createActivityContext: can't found plugin activity: activity=" + activity.getClass().getName());
//            }
//            return null;
//        }
//        Plugin plugin = mPluginMgr.loadAppPlugin(state.mCN.getPackageName());

        // 此时插件必须被加载，因此通过class loader一定能找到对应的PLUGIN对象
        Plugin plugin = mPluginMgr.lookupPlugin(activity.getClass().getClassLoader());
        if (plugin == null) {
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "PACM: createActivityContext: can't found plugin object for activity=" + activity.getClass().getName());
            }
            return null;
        }

        return plugin.mLoader.createBaseContext(newBase);
    }

    @Override
    public void handleActivityCreateBefore(Activity activity, Bundle savedInstanceState) {
        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "activity create before: " + activity.getClass().getName() + " this=" + activity.hashCode() + " taskid=" + activity.getTaskId());
        }

        // 对FragmentActivity做特殊处理
        if (savedInstanceState != null) {
            //
            savedInstanceState.setClassLoader(activity.getClassLoader());
            //
            try {
                savedInstanceState.remove("android:support:fragments");
            } catch (Throwable e) {
                if (LOGR) {
                    LogRelease.e(PLUGIN_TAG, "a.c.b1: " + e.getMessage(), e);
                }
            }
        }

        // 对FragmentActivity做特殊处理
        Intent intent = activity.getIntent();
        if (intent != null) {
            intent.setExtrasClassLoader(activity.getClassLoader());
            activity.setTheme(getThemeId(activity, intent));
        }
    }

    @Override
    public void handleActivityCreate(Activity activity, Bundle savedInstanceState) {
        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "activity create: " + activity.getClass().getName() + " this=" + activity.hashCode() + " taskid=" + activity.getTaskId());
        }

        if (activity.getIntent() != null) {
            try {
                Intent intent = new Intent(activity.getIntent());
//                String pluginName = intent.getStringExtra(PluginManager.EXTRA_PLUGIN);
//                String activityName = intent.getStringExtra(PluginManager.EXTRA_ACTIVITY);
//                int process = intent.getIntExtra(PluginManager.EXTRA_PROCESS, PluginManager.PROCESS_AUTO);
//                String container = intent.getStringExtra(PluginManager.EXTRA_CONTAINER);
//                int counter = intent.getIntExtra(PluginManager.EXTRA_COUNTER, 0);
                PluginIntent ii = new PluginIntent(intent);
                String pluginName = ii.getPlugin();
                String activityName = ii.getActivity();
                int process = ii.getProcess();
                String container = ii.getContainer();
                int counter = ii.getCounter();
                if (LOG) {
                    LogDebug.d(PLUGIN_TAG, "activity create: name=" + pluginName + " activity=" + activityName + " process=" + process + " container=" + container + " counter=" + counter);
                }
                // activity跑飞
                if (!TextUtils.equals(activityName, activity.getClass().getName())) {
                    // activity=, l=
                    if (LOGR) {
                        LogRelease.w(PLUGIN_TAG, "a.c.1: a=" + activityName + " l=" + activity.getClass().getName());
                    }
                    PMF.forward(activity, intent);
                    return;
                }
                if (LOG) {
                    LogDebug.i(PLUGIN_TAG, "perfect: container=" + container + " plugin=" + pluginName + " activity=" + activityName);
                }
            } catch (Throwable e) {
                if (LOGR) {
                    LogRelease.e(PLUGIN_TAG, "a.c.2: exception: " + e.getMessage(), e);
                }
            }
        }

        //
        PluginContainers.ActivityState state = null;
        if (activity.getComponentName() != null) {
            state = mPluginMgr.mClient.mACM.lookupByContainer(activity.getComponentName().getClassName());
        }
        if (state == null) {
            // PACM: handleActivityCreate: can't found PLUGIN activity: loaded=
            if (LOGR) {
                LogRelease.e(PLUGIN_TAG, "a.c1: l=" + activity.getClass().getName());
            }
            return;
        }

        // 记录坑
        mPluginMgr.mClient.mACM.handleCreate(state.plugin, activity, state.container);

        // 插件进程信息登记，用于插件进程管理（例如可能用于插件进程分配/回收）
        try {
            PluginProcessMain.getPluginHost().regActivity(PluginManager.sPluginProcessIndex, state.plugin, state.container, activity.getClass().getName());
        } catch (Throwable e) {
            if (LOGR) {
                LogRelease.e(PLUGIN_TAG, "a.c2: " + e.getMessage(), e);
            }
        }

        //
        if (savedInstanceState != null) {
            savedInstanceState.setClassLoader(activity.getClassLoader());
        }

        //
        Intent intent = activity.getIntent();
        if (intent != null) {
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "set activity intent cl=" + activity.getClassLoader());
            }
            intent.setExtrasClassLoader(activity.getClassLoader());
        }

        // 开始填充一些必要的属性给Activity对象
        // Added by Jiongxuan Zhang
        ActivityInjector.inject(activity, state.plugin, state.activity);
    }

    @Override
    public void handleRestoreInstanceState(Activity activity, Bundle savedInstanceState) {
        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "activity restore instance state: " + activity.getClass().getName());
        }

        //
        if (savedInstanceState != null) {
            savedInstanceState.setClassLoader(activity.getClassLoader());
            // 二级修正
            Set<String> set = savedInstanceState.keySet();
            if (set != null) {
                for (String key : set) {
                    Object obj = savedInstanceState.get(key);
                    if (obj instanceof Bundle) {
                        ((Bundle) obj).setClassLoader(activity.getClassLoader());
                    }
                }
            }
        }
    }

    @Override
    public void handleActivityDestroy(Activity activity) {
        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "activity destroy: " + activity.getClass().getName() + " this=" + activity.hashCode() + " taskid=" + activity.getTaskId());
        }

        // 回收坑
        mPluginMgr.mClient.mACM.handleDestroy(activity);

        //
        PluginContainers.ActivityState state = null;
        if (activity.getComponentName() != null) {
            state = mPluginMgr.mClient.mACM.lookupByContainer(activity.getComponentName().getClassName());
        }
        if (state == null) {
            // PACM: handleActivityDestroy: can't found plugin activity: activity=
            if (LOGR) {
                LogRelease.e(PLUGIN_TAG, "p a h a d c f p a " + activity.getClass().getName());
            }
            return;
        }

        // 插件进程信息登记，用于插件进程管理（例如可能用于插件进程分配/回收）
//        int pid = Process.myPid();
        String plugin = state.plugin;
        String container = state.container;
        try {
            PluginProcessMain.getPluginHost().unregActivity(PluginManager.sPluginProcessIndex, plugin, container, activity.getClass().getName());
        } catch (Throwable e) {
            if (LOGR) {
                LogRelease.e(PLUGIN_TAG, "ur.a: " + e.getMessage(), e);
            }
        }

        // 触发退出检测
        RePlugin.getConfig().getCallbacks().onActivityDestroyed(activity);
    }

    @Override
    public void handleServiceCreate(Service service) {
        mPluginMgr.handleServiceCreated(service);
    }

    @Override
    public void handleServiceDestroy(Service service) {
        mPluginMgr.handleServiceDestroyed(service);
    }

    @Override
    public JSONArray fetchPlugins(String name) {
        JSONArray ja = new JSONArray();
        synchronized (PluginTable.PLUGINS) {
            for (PluginInfo info : PluginTable.PLUGINS.values()) {
                if (TextUtils.isEmpty(name) || TextUtils.equals(info.getName(), name)) {
                    JSONObject jo = info.getJSON();
                    ja.put(jo);
                }
            }
        }
        return ja;
    }

    @Override
    public boolean registerDynamicClass(String className, String plugin, String type, String target) {
        return mPluginMgr.addDynamicClass(className, plugin, type, target, null);
    }

    @Override
    public boolean registerDynamicClass(String className, String plugin, String target, Class defClass) {
        return mPluginMgr.addDynamicClass(className, plugin, "", target, defClass);
    }

    @Override
    public boolean isDynamicClass(String plugin, String className) {
        return mPluginMgr.isDynamicClass(plugin, className);
    }

    @Override
    public String getPluginByDynamicClass(String className) {
        return mPluginMgr.getPluginByDynamicClass(className);
    }

    /**
     * 获取插件中使用代码设置的主题 id
     */
    private int getDynamicThemeId(Activity activity) {
        int dynamicThemeId = -1;
        try {
            dynamicThemeId = (int) ReflectUtils.invokeMethod(activity.getClassLoader(),
                    "android.view.ContextThemeWrapper", "getThemeResId", activity, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dynamicThemeId;
    }

    /**
     * 获取坑位应该使用的主题
     */
    private int getThemeId(Activity activity, Intent intent) {

        // 通过反射获取主题（可能获取到坑的主题，或者程序员通过代码设置的主题）
        int dynamicThemeId = getDynamicThemeId(activity);

        // 插件 manifest 中设置的 ThemeId
        int manifestThemeId = intent.getIntExtra(PmLocalImpl.INTENT_KEY_THEME_ID, 0);
        //如果插件上没有主题则使用Application节点的Theme
        if (manifestThemeId == 0) {
            manifestThemeId = activity.getApplicationInfo().theme;
        }

        // 根据 manifest 中声明主题是否透明，获取默认主题
        int defaultThemeId = getDefaultThemeId();
        if (LaunchModeStates.isTranslucentTheme(manifestThemeId)) {
            defaultThemeId = android.R.style.Theme_Translucent_NoTitleBar;
        }

        int themeId;

        if (LOG) {
            LogDebug.d("theme", "defaultThemeId = " + defaultThemeId);
            LogDebug.d("theme", "dynamicThemeId = " + dynamicThemeId);
            LogDebug.d("theme", "manifestThemeId = " + manifestThemeId);
        }

        // 通过反射获取主题成功
        if (dynamicThemeId != -1) {
            // 如果动态主题是默认主题，说明插件未通过代码设置主题，此时应该使用 AndroidManifest 中设置的主题。
            if (dynamicThemeId == defaultThemeId) {
                // AndroidManifest 中有声明主题
                if (manifestThemeId != 0) {
                    themeId = manifestThemeId;
                } else {
                    themeId = defaultThemeId;
                }

            } else {
                // 动态主题不是默认主题，说明主题是插件通过代码设置的，使用此代码设置的主题。
                themeId = dynamicThemeId;
            }

            // 反射失败，检查 AndroidManifest 是否有声明主题
        } else {
            if (manifestThemeId != 0) {
                themeId = manifestThemeId;
            } else {
                themeId = defaultThemeId;
            }
        }

        if (LOG) {
            LogDebug.d("theme", "themeId = " + themeId);
        }

        return themeId;
    }

    /**
     * 获取默认 ThemeID
     * 如果 Host 配置了使用 AppCompat，则此处通过反射调用 AppCompat 主题。
     * <p>
     * 注：Host 必须配置 AppCompat 依赖，否则反射调用会失败，导致宿主编译不过。
     */
    private static int getDefaultThemeId() {
        if (HostConfigHelper.ACTIVITY_PIT_USE_APPCOMPAT) {
            try {
                Class clazz = ClassUtils.getClass("android.support.v7.appcompat.R$style");
                return (int) FieldUtils.readStaticField(clazz, "Theme_AppCompat", true);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return android.R.style.Theme_NoTitleBar;
    }
}
