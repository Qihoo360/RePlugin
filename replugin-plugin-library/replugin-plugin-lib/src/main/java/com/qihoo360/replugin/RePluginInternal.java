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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import com.qihoo360.replugin.helper.LogDebug;
import com.qihoo360.replugin.i.IPluginManager;
import com.qihoo360.replugin.library.BuildConfig;

import static com.qihoo360.replugin.helper.LogDebug.TAG;

/**
 * 对框架暴露的一些通用的接口。
 * <p>
 * 注意：插件框架内部使用，外界请不要调用。
 *
 * @author RePlugin Team
 */
public class RePluginInternal {

    public static final boolean FOR_DEV = BuildConfig.DEBUG;

    /**
     * @param activity
     * @param newBase
     * @return 为Activity构造一个base Context
     * @hide 内部方法，插件框架使用
     * 插件的Activity创建成功后通过此方法获取其base context
     */
    public static Context createActivityContext(Activity activity, Context newBase) {
        if (!RePluginFramework.mHostInitialized) {
            return newBase;
        }

        try {
            return (Context) ProxyRePluginInternalVar.createActivityContext.call(null, activity, newBase);
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * @param activity
     * @param savedInstanceState
     * @hide 内部方法，插件框架使用
     * 插件的Activity的onCreate调用前调用此方法
     */
    public static void handleActivityCreateBefore(Activity activity, Bundle savedInstanceState) {
        if (!RePluginFramework.mHostInitialized) {
            return;
        }

        ProxyRePluginInternalVar.handleActivityCreateBefore.call(null, activity, savedInstanceState);
    }

    /**
     * @param activity
     * @param savedInstanceState
     * @hide 内部方法，插件框架使用
     * 插件的Activity的onCreate调用后调用此方法
     */
    public static void handleActivityCreate(Activity activity, Bundle savedInstanceState) {
        if (!RePluginFramework.mHostInitialized) {
            return;
        }

        ProxyRePluginInternalVar.handleActivityCreate.call(null, activity, savedInstanceState);
    }

    /**
     * @param activity
     * @hide 内部方法，插件框架使用
     * 插件的Activity的onDestroy调用后调用此方法
     */
    public static void handleActivityDestroy(Activity activity) {
        if (!RePluginFramework.mHostInitialized) {
            return;
        }

        ProxyRePluginInternalVar.handleActivityDestroy.call(null, activity);
    }

    /**
     * @param activity
     * @param savedInstanceState
     * @hide 内部方法，插件框架使用
     * 插件的Activity的onRestoreInstanceState调用后调用此方法
     */
    public static void handleRestoreInstanceState(Activity activity, Bundle savedInstanceState) {
        if (!RePluginFramework.mHostInitialized) {
            return;
        }

        ProxyRePluginInternalVar.handleRestoreInstanceState.call(null, activity, savedInstanceState);
    }

    /**
     * @param activity Activity上下文
     * @param intent
     * @return 插件机制层是否成功，例如没有插件存在、没有合适的Activity坑
     * @hide 内部方法，插件框架使用
     * 启动一个插件中的activity
     * 通过Extra参数IPluginManager.KEY_COMPATIBLE，IPluginManager.KEY_PLUGIN，IPluginManager.KEY_ACTIVITY，IPluginManager.KEY_PROCESS控制
     */
    public static boolean startActivity(Activity activity, Intent intent) {
        if (!RePluginFramework.mHostInitialized) {
            return false;
        }

        try {
            Object obj = ProxyRePluginInternalVar.startActivity.call(null, activity, intent);
            if (obj != null) {
                return (Boolean) obj;
            }
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }

        return false;
    }

    /**
     * @param activity Activity上下文
     * @param intent
     * @return 插件机制层是否成功，例如没有插件存在、没有合适的Activity坑
     * Added by liupo
     * @hide 内部方法，插件框架使用
     * 启动一个插件中的activity
     * 通过Extra参数IPluginManager.KEY_COMPATIBLE，IPluginManager.KEY_PLUGIN，IPluginManager.KEY_ACTIVITY，IPluginManager.KEY_PROCESS控制
     */
    public static boolean startActivityForResult(Activity activity, Intent intent, int requestCode) {
        return startActivityForResult(activity, intent, requestCode, null);
    }

    /**
     * @param activity Activity上下文
     * @param intent
     * @return 插件机制层是否成功，例如没有插件存在、没有合适的Activity坑
     * Added by Jiongxuan Zhang
     * @hide 内部方法，插件框架使用
     * 启动一个插件中的activity
     * 通过Extra参数IPluginManager.KEY_COMPATIBLE，IPluginManager.KEY_PLUGIN，IPluginManager.KEY_ACTIVITY，IPluginManager.KEY_PROCESS控制
     */
    public static boolean startActivityForResult(Activity activity, Intent intent, int requestCode, Bundle options) {
        if (!RePluginFramework.mHostInitialized) {
            return false;
        }

        try {
            Object obj = ProxyRePluginInternalVar.startActivityForResult.call(null, activity, intent, requestCode, options);
            if (obj != null) {
                return (Boolean) obj;
            }
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }

        // replugin-host-lib 版本小于 2.1.3 时
        return startActivityForResultCompat(activity, intent, requestCode, options);
    }

    /**
     * 如果 replugin-host-lib 版本小于 2.1.3，使用此 compat 方法。
     */
    private static boolean startActivityForResultCompat(Activity activity, Intent intent, int requestCode, Bundle options) {
        String plugin = getPluginName(activity, intent);

        if (LogDebug.LOG) {
            LogDebug.d(TAG, "start activity with startActivityForResult: intent=" + intent);
        }

        if (TextUtils.isEmpty(plugin)) {
            return false;
        }

        ComponentName cn = intent.getComponent();
        if (cn == null) {
            return false;
        }
        String name = cn.getClassName();

        ComponentName cnNew = loadPluginActivity(intent, plugin, name, IPluginManager.PROCESS_AUTO);
        if (cnNew == null) {
            return false;
        }

        intent.setComponent(cnNew);

        if (Build.VERSION.SDK_INT >= 16) {
            activity.startActivityForResult(intent, requestCode, options);
        } else {
            activity.startActivityForResult(intent, requestCode);
        }
        return true;
    }

    /**
     * 加载插件Activity，在startActivity之前调用
     *
     * @param intent
     * @param plugin  插件名
     * @param target  目标Service名，如果传null，则取获取到的第一个
     * @param process 是否在指定进程中启动
     * @return
     */
    public static ComponentName loadPluginActivity(Intent intent, String plugin, String target, int process) {
        if (!RePluginFramework.mHostInitialized) {
            return null;
        }

        try {
            return (ComponentName) ProxyRePluginInternalVar.loadPluginActivity.call(null, intent, plugin, target, process);
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * 获取插件名称
     */
    private static String getPluginName(Activity activity, Intent intent) {
        String plugin = "";
        if (intent.getComponent() != null) {
            plugin = intent.getComponent().getPackageName();
        }
        // 如果 plugin 是包名，则说明启动的是本插件。
        if (TextUtils.isEmpty(plugin) || plugin.contains(".")) {
            plugin = RePlugin.fetchPluginNameByClassLoader(activity.getClassLoader());
        }
        // 否则是其它插件
        return plugin;
    }

    static class ProxyRePluginInternalVar {

        private static MethodInvoker createActivityContext;

        private static MethodInvoker handleActivityCreateBefore;

        private static MethodInvoker handleActivityCreate;

        private static MethodInvoker handleActivityDestroy;

        private static MethodInvoker handleRestoreInstanceState;

        private static MethodInvoker startActivity;

        private static MethodInvoker startActivityForResult;

        private static MethodInvoker loadPluginActivity;

        static void initLocked(final ClassLoader classLoader) {

            final String factory2 = "com.qihoo360.i.Factory2";
            final String factory = "com.qihoo360.i.Factory";

            // 初始化Factory2相关方法
            createActivityContext = new MethodInvoker(classLoader, factory2, "createActivityContext", new Class<?>[]{Activity.class, Context.class});
            handleActivityCreateBefore = new MethodInvoker(classLoader, factory2, "handleActivityCreateBefore", new Class<?>[]{Activity.class, Bundle.class});
            handleActivityCreate = new MethodInvoker(classLoader, factory2, "handleActivityCreate", new Class<?>[]{Activity.class, Bundle.class});
            handleActivityDestroy = new MethodInvoker(classLoader, factory2, "handleActivityDestroy", new Class<?>[]{Activity.class});
            handleRestoreInstanceState = new MethodInvoker(classLoader, factory2, "handleRestoreInstanceState", new Class<?>[]{Activity.class, Bundle.class});
            startActivity = new MethodInvoker(classLoader, factory2, "startActivity", new Class<?>[]{Activity.class, Intent.class});
            startActivityForResult = new MethodInvoker(classLoader, factory2, "startActivityForResult", new Class<?>[]{Activity.class, Intent.class, int.class, Bundle.class});

            // 初始化Factory相关方法
            loadPluginActivity = new MethodInvoker(classLoader, factory, "loadPluginActivity", new Class<?>[]{Intent.class, String.class, String.class, int.class});
        }
    }
}
