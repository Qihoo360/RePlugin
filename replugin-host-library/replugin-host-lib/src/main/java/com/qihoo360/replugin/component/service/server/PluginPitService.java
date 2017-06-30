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

package com.qihoo360.replugin.component.service.server;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.qihoo360.i.IPluginManager;

import java.lang.ref.WeakReference;

/**
 * 表示一个“坑位”服务。一个进程放着一个坑位，且不做什么实际意义的事情 <p>
 * 注意： <p>
 * 1、不能直接在AndroidManifest.xml中注册，而是通过RePluginClassLoader做中转 <p>
 * 2、仅为防止进程杀掉，以及获取共用Token而设计。其余均在PluginServiceServer中实现
 *
 * @author RePlugin Team
 */

public class PluginPitService extends Service {

    private static WeakReference<PluginPitService> sService;

    public PluginPitService() {
        sService = new WeakReference<>(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static PluginPitService get() {
        return sService.get();
    }

    /**
     * 创建一个可供使用的ComponentName
     *
     * @param process Process代号（注意，不是ProcessID）
     * @return 一个新的CN对象
     */
    public static ComponentName makeComponentName(Context c, int process) {
        String key = c.getPackageName();
        String prefix = PluginPitService.class.getName();
        String value;
        if (process == IPluginManager.PROCESS_UI) {
            value = prefix + "UI";
        } else if (process == IPluginManager.PROCESS_PERSIST) {
            value = prefix + "Guard";
        } else {
            value = prefix + "P" + (100 + process); // TODO 因为process可能是负数，所以这里需要+100，将来会优化这里
        }
        return new ComponentName(key, value);
    }
}
