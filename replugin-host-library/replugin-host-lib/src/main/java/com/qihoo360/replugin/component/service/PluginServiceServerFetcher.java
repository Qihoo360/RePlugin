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

package com.qihoo360.replugin.component.service;

import android.os.IBinder;

import com.qihoo360.i.IPluginManager;
import com.qihoo360.loader2.IPluginClient;
import com.qihoo360.loader2.IPluginHost;
import com.qihoo360.loader2.MP;
import com.qihoo360.loader2.PluginBinderInfo;
import com.qihoo360.loader2.PluginProcessMain;
import com.qihoo360.replugin.utils.basic.ArrayMap;
import com.qihoo360.replugin.component.service.server.IPluginServiceServer;
import com.qihoo360.replugin.helper.LogDebug;
import com.qihoo360.replugin.helper.LogRelease;

import static com.qihoo360.replugin.helper.LogDebug.LOG;
import static com.qihoo360.replugin.helper.LogDebug.PLUGIN_TAG;
import static com.qihoo360.replugin.helper.LogRelease.LOGR;

/**
 * 用来获取PluginServiceServer服务的类
 *
 * @author RePlugin Team
 */
public class PluginServiceServerFetcher {
    private ArrayMap<Integer, IPluginServiceServer> mServiceManagerByProcessMap = new ArrayMap<>();
    private static final byte[] PSS_LOCKER = new byte[0];

    public IPluginServiceServer fetchByProcess(int process) {
        if (process == PluginServiceClient.PROCESS_UNKNOWN) {
            return null;
        }
        // 取之前的缓存
        IPluginServiceServer pss;
        synchronized (PSS_LOCKER) {
            pss = mServiceManagerByProcessMap.get(process);
            if (pss != null) {
                if (LOG) {
                    LogDebug.d(PLUGIN_TAG, "PluginServiceClient.fsmbp(): Exists! p=" + process);
                }
                return pss;
            }
        }

        // 缓存没有？则去目标进程获取新的
        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "PluginServiceClient.fsmbp(): Create a new one! p=" + process);
        }
        try {
            if (process == IPluginManager.PROCESS_PERSIST) {
                IPluginHost ph = PluginProcessMain.getPluginHost();
                pss = ph.fetchServiceServer();
            } else {
                PluginBinderInfo pbi = new PluginBinderInfo(PluginBinderInfo.NONE_REQUEST);
                IPluginClient pc = MP.startPluginProcess(null, process, pbi);
                pss = pc.fetchServiceServer();
            }

            // 挂死亡周期，如果出问题了就置空重来，防止外界调用psm出现DeadObject问题
            pss.asBinder().linkToDeath(new PSSDeathMonitor(process, pss.asBinder()), 0);
        } catch (Throwable e) {
            if (LOGR) {
                LogRelease.e(PLUGIN_TAG, "psc.fsm: e", e);
            }
        }
        if (pss != null) {
            synchronized (PSS_LOCKER) {
                mServiceManagerByProcessMap.put(process, pss);
            }
        }
        return pss;
    }

    // Plugin Service Server（目标进程）的死亡周期监听
    private final class PSSDeathMonitor implements IBinder.DeathRecipient {
        PSSDeathMonitor(int process, IBinder service) {
            mProcess = process;
            mService = service;
        }

        public void binderDied() {
            if (LOGR) {
                LogRelease.e(PLUGIN_TAG, "psc.dm: d, rm p " + mProcess);
            }
            synchronized (PSS_LOCKER) {
                mServiceManagerByProcessMap.remove(mProcess);
            }
        }

        final int mProcess;
        final IBinder mService;
    }
}
