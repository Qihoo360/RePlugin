
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

package com.qihoo360.replugin.plugin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.IBinder;
import android.os.RemoteException;

import com.qihoo360.loader2.IPlugin;

/**
 * @author RePlugin Team
 */
public class Entry {
    @SuppressLint("StaticFieldLeak")
    private static Context sPluginContext;
    @SuppressLint("StaticFieldLeak")
    private static Context sHostContext;
    private static ClassLoader sHostClassLoader;
    private static IBinder sPluginManager;

    /**
     * 加载插件的时候会调用这个函数来初始化插件
     *
     * @param context 插件上下文
     * @param cl      HOST程序的类加载器
     * @param manager 插件管理器
     * @return
     */
    public static IBinder create(Context context, ClassLoader cl, IBinder manager) {
        sPluginContext = context;
        // 确保获取的一定是主程序的Context
        sHostContext = ((ContextWrapper) context).getBaseContext();
        sHostClassLoader = cl;
        // 从宿主传过来的，目前恒为null
        sPluginManager = manager;

        return new IPlugin.Stub() {
            @Override
            public IBinder query(String name) throws RemoteException {
                return RePluginServiceManager.getInstance().getService(name);
            }
        };
    }
    /**
     * 获取宿主部分的Context对象
     * 可用来：获取宿主部分的资源、反射类、Info等信息
     * <p>
     * 注意：此Context对象不能用于插件（如资源、类等）的获取。
     * 若需使用插件的Context对象，可直接调用Activity中的getApplicationContext（7.1.0及以后才支持），或直接使用Activity对象即可
     */
    public static Context getHostContext() {
        return sHostContext;
    }

    /**
     * 获取宿主部分的ClassLoader对象
     * 可用来：获取宿主部分的反射类
     * <p>
     * 注意：同HostContext一样，这里的ClassLoader不能加载插件中的class
     */
    public static ClassLoader getHostCLassLoader() {
        return sHostClassLoader;
    }

    /**
     * 获取该插件的PluginContext
     * @return
     */
    public static Context getPluginContext() {
        return sPluginContext;
    }
}
