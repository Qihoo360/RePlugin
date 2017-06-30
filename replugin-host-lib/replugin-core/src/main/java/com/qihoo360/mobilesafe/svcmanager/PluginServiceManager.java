
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

package com.qihoo360.mobilesafe.svcmanager;

import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.qihoo360.loader2.MP;
import com.qihoo360.mobilesafe.core.BuildConfig;
import com.qihoo360.replugin.helper.LogRelease;

import java.util.HashMap;
import java.util.Map;

import static com.qihoo360.replugin.helper.LogDebug.PLUGIN_TAG;
import static com.qihoo360.replugin.helper.LogRelease.LOGR;

/**
 * 负责管理Plugin Service的管理器
 *
 * @author RePlugin Team
 */
class PluginServiceManager {

    private static final boolean DEBUG = BuildConfig.DEBUG;

    private static final String TAG = DEBUG ? "PluginServiceManager" : PluginServiceManager.class.getSimpleName();

    private static Map<String, PluginServiceRecord> sRecordMap = new HashMap<String, PluginServiceRecord>();

    /**
     * 获取一个来自于Plugin的service实例
     *
     * @param pluginName plugin的名称
     * @param serviceName 所要获取的service的名称
     * @param pid 发起请求的进程的pid
     * @param deathMonitor 请求方传过来的IBinder对象，仅用来监视请求进程死亡事件
     * @return 所请求的service对象
     * @throws RemoteException
     */
    static IBinder getPluginService(String pluginName, String serviceName, int pid, IBinder deathMonitor) throws RemoteException {
        PluginServiceRecord pr;
        synchronized (sRecordMap) {
            final String key = generateMapKey(pluginName, serviceName);
            pr = sRecordMap.get(key);
            if (pr != null && !pr.isServiceAlive()) {
                pr = null;
            }
            if (pr == null) {
                pr = new PluginServiceRecord(pluginName, serviceName);
                sRecordMap.put(key, pr);
            }
        }

        return pr.getService(pid, deathMonitor);
    }

    /**
     * 处理之前被请求的一个service对象的引用已被释放
     *
     * @param pluginName plugin的名称
     * @param serviceName 获取的service的名称
     * @param pid 发起请求的进程的pid
     */
    static void onRefReleased(String pluginName, String serviceName, int pid) {
        synchronized (sRecordMap) {
            PluginServiceRecord pr = sRecordMap.get(generateMapKey(pluginName, serviceName));
            if (pr != null) {
                int retCount = pr.decrementProcessRef(pid);

                if (DEBUG) {
                    Log.d(TAG, "[onRefReleased] remaining ref count: " + retCount);
                }

                if (retCount <= 0) {
                    removePluginServiceRecord(pr);
                }
            }
        }
    }

    /**
     * 处理之前有过请求纪录的一个进程死掉的事件
     *
     * @param pluginName plugin的名称
     * @param serviceName 获取的service的名称
     * @param pid 发起请求的进程的pid
     */
    static void onRefProcessDied(String pluginName, String serviceName, int pid) {
        synchronized (sRecordMap) {
            PluginServiceRecord pr = sRecordMap.get(generateMapKey(pluginName, serviceName));
            if (pr != null) {
                int retCount = pr.refProcessDied(pid);

                if (DEBUG) {
                    Log.d(TAG, "[onRefProcessDied] remaining ref count: " + retCount);
                }

                if (retCount <= 0) {
                    removePluginServiceRecord(pr);
                }
            }
        }
    }

    /**
     * 当某个plugin service已经被完全释放，既没有任何引用，从缓存map中移除， 并且通知插件框架可以考虑释放此plugin的进程
     */
    private static void removePluginServiceRecord(PluginServiceRecord pr) {
        if (DEBUG) {
            Log.d(TAG, "[removePluginServiceRecord]: " + pr.mPluginName + ", " + pr.mServiceName);
        }

        synchronized (sRecordMap) {
            final String key = generateMapKey(pr.mPluginName, pr.mServiceName);

            // 通知框架接触对此service的引用
            // binder有可能为空，做个保护
            // Edited by Jiongxuan Zhang
            if (pr.mPluginBinder == null) {
                if (LOGR) {
                    LogRelease.e(PLUGIN_TAG, "psm.rpsr: mpb nil");
                }
                return;
            }
            MP.releasePluginBinder(pr.mPluginBinder);

            sRecordMap.remove(key);
        }
    }

    private static String generateMapKey(String pluginName, String serviceName) {
        return pluginName + "-" + serviceName;
    }
}
