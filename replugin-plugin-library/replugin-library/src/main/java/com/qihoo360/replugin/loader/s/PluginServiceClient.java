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

package com.qihoo360.replugin.loader.s;

import android.app.Service;

import com.qihoo360.replugin.RePluginFramework;
import com.qihoo360.replugin.MethodInvoker;
import com.qihoo360.replugin.helper.LogDebug;

/**
 * 一种能够对【插件】的服务进行：启动、停止、绑定、解绑等功能的类
 * 所有针对插件命令的操作，均从此类开始。
 * <p>
 * 外界可直接使用此类
 *
 * @author RePlugin Team
 */
public class PluginServiceClient {

    /**
     * 在插件服务中停止服务。近似于Service.stopSelf
     * 注意：此方法应在插件服务中被调用
     *
     * @param s 要停止的插件服务
     * @see android.app.Service#stopSelf()
     */
    public static void stopSelf(Service s) {
        if (!RePluginFramework.mHostInitialized) {
            s.stopSelf();
            return;
        }

        try {
            ProxyRePluginServiceClientVar.stopSelf.call(null, s);
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }
    }


    public static class ProxyRePluginServiceClientVar {

        private static MethodInvoker stopSelf;

        public static void initLocked(final ClassLoader classLoader) {
            //
            final String rePluginServiceClient = "com.qihoo360.loader2.mgr.PluginServiceClient";
            stopSelf = new MethodInvoker(classLoader, rePluginServiceClient, "stopSelf", new Class<?>[]{Service.class});
        }
    }
}

