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

package com.qihoo360.replugin.component.app;

import android.app.Application;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.text.TextUtils;

import com.qihoo360.mobilesafe.core.BuildConfig;
import com.qihoo360.replugin.utils.basic.ArrayMap;
import com.qihoo360.replugin.RePluginInternal;
import com.qihoo360.replugin.component.ComponentList;
import com.qihoo360.replugin.helper.LogDebug;
import com.qihoo360.replugin.model.PluginInfo;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.qihoo360.replugin.helper.LogDebug.LOG;
import static com.qihoo360.replugin.helper.LogDebug.PLUGIN_TAG;

/**
 * 一种能处理【插件】的Application的类
 *
 * @author RePlugin Team
 */
public class PluginApplicationClient {

    private static volatile boolean sInited;
    private static final byte[] LOCKER = new byte[0];
    private static Method sAttachBaseContextMethod;

    private final ClassLoader mPlgClassLoader;
    private final ApplicationInfo mApplicationInfo;

    private Constructor mApplicationConstructor;

    private Application mApplication;

    private static ArrayMap<String, WeakReference<PluginApplicationClient>> sRunningClients = new ArrayMap<>();

    /**
     * 根据插件里的框架版本、Application等情况来创建PluginApplicationClient对象
     * 若已经存在，则返回之前创建的ApplicationClient对象（此时Application不一定被加载进来）
     * 若不符合条件（如插件加载失败、版本不正确等），则会返回null
     *
     * @param pn 插件名
     * @param plgCL 插件的ClassLoader
     * @param cl 插件的ComponentList
     * @param pi 插件的信息
     */
    public static PluginApplicationClient getOrCreate(String pn, ClassLoader plgCL, ComponentList cl, PluginInfo pi) {
        if (pi.getFrameworkVersion() <= 1) {
            // 仅框架版本为2及以上的，才支持Application的加载
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "PAC.create(): FrameworkVer less than 1. cl=" + plgCL);
            }
            return null;
        }
        PluginApplicationClient pac = getRunning(pn);
        if (pac != null) {
            // 已经初始化过Application？直接返回
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "PAC.create(): Already Loaded." + plgCL);
            }
            return pac;
        }

        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "PAC.create(): Create and load Application. cl=" + plgCL);
        }

        // 初始化所有需要反射的方法
        try {
            initMethods();
        } catch (Throwable e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
            return null;
        }

        final PluginApplicationClient pacNew = new PluginApplicationClient(plgCL, cl, pi);
        if (pacNew.isValid()) {
            sRunningClients.put(pn, new WeakReference<>(pacNew));
            if (Build.VERSION.SDK_INT >= 14) {
                RePluginInternal.getAppContext().registerComponentCallbacks(new ComponentCallbacks2() {
                    @Override
                    public void onTrimMemory(int level) {
                        pacNew.callOnTrimMemory(level);
                    }

                    @Override
                    public void onConfigurationChanged(Configuration newConfig) {
                        pacNew.callOnConfigurationChanged(newConfig);
                    }

                    @Override
                    public void onLowMemory() {
                        pacNew.callOnLowMemory();
                    }
                });
            }
            return pacNew;
        } else {
            // Application对象没有初始化出来，则直接按失败处理
            return null;
        }
    }

    public static void notifyOnLowMemory() {
        for (WeakReference<PluginApplicationClient> pacw : sRunningClients.values()) {
            PluginApplicationClient pac = pacw.get();
            if (pac == null) {
                continue;
            }
            pac.callOnLowMemory();
        }
    }

    public static void notifyOnTrimMemory(int level) {
        for (WeakReference<PluginApplicationClient> pacw : sRunningClients.values()) {
            PluginApplicationClient pac = pacw.get();
            if (pac == null) {
                continue;
            }
            pac.callOnTrimMemory(level);
        }
    }

    public static void notifyOnConfigurationChanged(Configuration newConfig) {
        for (WeakReference<PluginApplicationClient> pacw : sRunningClients.values()) {
            PluginApplicationClient pac = pacw.get();
            if (pac == null) {
                continue;
            }
            pac.callOnConfigurationChanged(newConfig);
        }
    }

    public static PluginApplicationClient getRunning(String pn) {
        WeakReference<PluginApplicationClient> w = sRunningClients.get(pn);
        if (w == null) {
            return null;
        }
        return w.get();
    }

    private static void initMethods() throws NoSuchMethodException {
        if (sInited) {
            return;
        }
        synchronized (LOCKER) {
            if (sInited) {
                return;
            }
            // NOTE getDeclaredMethod只能获取当前类声明的方法，无法获取继承到的，而getMethod虽可以获取继承方法，但又不能获取非Public的方法
            // NOTE 权衡利弊，还是仅构造函数用反射类，其余用它明确声明的类来做
            sAttachBaseContextMethod = Application.class.getDeclaredMethod("attach", Context.class);
            sAttachBaseContextMethod.setAccessible(true);   // Protected 修饰
            sInited = true;
        }
    }

    private PluginApplicationClient(ClassLoader plgCL, ComponentList cl, PluginInfo pi) {
        mPlgClassLoader = plgCL;
        mApplicationInfo = cl.getApplication();
        try {
            // 尝试使用自定义Application（如有）
            if (mApplicationInfo != null && !TextUtils.isEmpty(mApplicationInfo.className)) {
                initCustom();
            }
            // 若自定义有误（或没有)，且框架版本为3及以上的，则可以创建空Application对象，方便插件getApplicationContext到自己
            if (!isValid() && pi.getFrameworkVersion() >= 3) {
                mApplication = new Application();
            }
        } catch (Throwable e) {
            // 出现异常，表示Application有问题
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
            mApplication = new Application();
        }
    }

    public void callAttachBaseContext(Context c) {
        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "PAC.callAttachBaseContext(): Call attachBaseContext(), cl=" + mPlgClassLoader);
        }
        try {
            sAttachBaseContextMethod.setAccessible(true);   // Protected 修饰
            sAttachBaseContextMethod.invoke(mApplication, c);
        } catch (Throwable e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }
    }

    public void callOnCreate() {
        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "PAC.callOnCreate(): Call onCreate(), cl=" + mPlgClassLoader);
        }
        mApplication.onCreate();
    }

    public void callOnLowMemory() {
        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "PAC.callOnLowMemory(): Call onLowMemory(), cl=" + mPlgClassLoader);
        }
        mApplication.onLowMemory();
    }

    public void callOnTrimMemory(int level) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            return;
        }

        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "PAC.callOnLowMemory(): Call onTrimMemory(), cl=" + mPlgClassLoader + "; lv=" + level);
        }
        mApplication.onTrimMemory(level);
    }

    public void callOnConfigurationChanged(Configuration newConfig) {
        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "PAC.callOnLowMemory(): Call onConfigurationChanged(), cl=" + mPlgClassLoader + "; nc=" + newConfig);
        }
        mApplication.onConfigurationChanged(newConfig);
    }

    public Application getObj() {
        return mApplication;
    }

    private boolean initCustom() {
        try {
            initCustomConstructor();
            initCustomObject();

            // 看mApplication是否被初始化成功
            return mApplication != null;
        } catch (Throwable e) {
            // 出现异常，表示自定义Application有问题
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private void initCustomConstructor() throws ClassNotFoundException, NoSuchMethodException {
        String aic = mApplicationInfo.className;
        Class<?> psc = mPlgClassLoader.loadClass(aic);
        mApplicationConstructor = psc.getConstructor();
    }

    private void initCustomObject() throws IllegalAccessException, InvocationTargetException, InstantiationException {
        Object appObj = mApplicationConstructor.newInstance();
        if (appObj instanceof Application) {
            mApplication = (Application) appObj;
        }
    }

    private boolean isValid() {
        return mApplication != null;
    }
}
