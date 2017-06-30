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

package com.qihoo360.loader2.mgr;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

/**
 * （该类仅为兼容360手机卫士的旧插件而存在，因涉及到反射而保留此类）
 *
 * @deprecated 请使用新类
 * @see com.qihoo360.replugin.component.service.PluginServiceClient
 * @author RePlugin Team
 */
public class PluginServiceClient {

    /**
     * @deprecated
     */
    public static ComponentName startService(Context context, Intent intent) {
        return com.qihoo360.replugin.component.service.PluginServiceClient.startService(context, intent);
    }

    /**
     * @deprecated
     */
    public static boolean stopService(Context context, Intent intent) {
        return com.qihoo360.replugin.component.service.PluginServiceClient.stopService(context, intent);
    }

    /**
     * @deprecated
     */
    public static boolean bindService(Context context, Intent intent, ServiceConnection sc, int flags) {
        return com.qihoo360.replugin.component.service.PluginServiceClient.bindService(context, intent, sc, flags);
    }

    /**
     * @deprecated
     */
    public static boolean unbindService(Context context, ServiceConnection sc) {
        return com.qihoo360.replugin.component.service.PluginServiceClient.unbindService(context, sc);
    }

    /**
     * @deprecated
     */
    public static void stopSelf(Service s) {
        com.qihoo360.replugin.component.service.PluginServiceClient.stopSelf(s);
    }
}

