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

import android.os.IBinder;
import android.text.TextUtils;

import com.qihoo360.replugin.helper.LogDebug;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 插件内部向外提供服务的管理实现类
 * <p>
 * 这里注册过来的binder服务，是为了外界（其它插件或者Host宿主）通过IPlugin.query() 接口获取到所需要的服务
 * 服务注册时机：建议在Application.onCreate中注册，方便插件加载后便可通过query接口对外提供服务
 *
 * @author RePlugin Team
 */

public class RePluginServiceManager {

    private static final String TAG = "Entry.SM";

    private static RePluginServiceManager sInstance;

    private ConcurrentHashMap<String, IBinder> mServices = new ConcurrentHashMap<String, IBinder>();

    /**
     * 单例
     *
     * @return
     */
    public static RePluginServiceManager getInstance() {
        if (sInstance != null) {
            return sInstance;
        }

        synchronized (RePluginServiceManager.class) {
            if (sInstance == null) {
                sInstance = new RePluginServiceManager();
            }
        }

        return sInstance;
    }

    /**
     * 注册服务，供IPlugin.query使用
     *
     * @param name
     * @param service
     */
    public void addService(final String name, final IBinder service) {
        if (LogDebug.LOG) {
            LogDebug.d(TAG, "add service for IPlugin.query, name = " + name);
        }

        mServices.put(name, service);
    }

    /**
     * 获取已注册的IBinder
     *
     * @param name
     * @return
     */
    public IBinder getService(final String name) {
        if (LogDebug.LOG) {
            LogDebug.d(TAG, "get service for IPlugin.query, name = " + name);
        }

        if (TextUtils.isEmpty(name)) {
            throw new IllegalArgumentException("service name can not value null");
        }

        IBinder ret = mServices.get(name);

        if (ret == null) {
            return null;
        }

        if (!ret.isBinderAlive() || !ret.pingBinder()) {
            mServices.remove(name);
            return null;
        }

        return ret;
    }
}
