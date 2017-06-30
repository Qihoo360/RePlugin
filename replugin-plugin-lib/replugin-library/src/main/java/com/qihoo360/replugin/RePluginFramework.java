
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

import android.util.Log;

import com.qihoo360.replugin.loader.b.PluginLocalBroadcastManager;
import com.qihoo360.replugin.loader.p.PluginProviderClient;
import com.qihoo360.replugin.loader.s.PluginServiceClient;
import com.qihoo360.replugin.base.IPC;
import com.qihoo360.replugin.helper.LogRelease;

/**
 * @author RePlugin Team
 */
public class RePluginFramework {

    private static final String TAG = "RePluginFramework";

    private static final byte[] LOCK = new byte[0];

    private static volatile boolean mInitialized;

    /**
     * 注：内部框架使用
     */
    public static volatile boolean mHostInitialized;

    /**
     * 插件需要用此初始化代码
     *
     * @param cl
     * @return 返回true表示运行在HOST中，返回false表示运行的是独立APK
     */
    static boolean init(ClassLoader cl) {
        synchronized (LOCK) {
            return initLocked(cl);
        }
    }

    /**
     * @return 返回true表示运行在HOST中，返回false表示可能是非HOST环境
     */
    public static boolean isHostInitialized() {
        return mHostInitialized;
    }

    private static boolean initLocked(ClassLoader cl) {
        if (mInitialized) {
            return mHostInitialized;
        }
        mInitialized = true;

        try {
            //
            RePluginInternal.ProxyRePluginInternalVar.initLocked(cl);
            RePlugin.ProxyRePluginVar.initLocked(cl);
            PluginLocalBroadcastManager.ProxyLocalBroadcastManagerVar.initLocked(cl);
            PluginProviderClient.ProxyRePluginProviderClientVar.initLocked(cl);
            PluginServiceClient.ProxyRePluginServiceClientVar.initLocked(cl);
            IPC.ProxyIPCVar.initLocked(cl);

            mHostInitialized = true;

        } catch (final Throwable e) {
            if (LogRelease.LOGR) {
                Log.e(TAG, e.getMessage(), e);
            }
        }

        return mHostInitialized;
    }
}
