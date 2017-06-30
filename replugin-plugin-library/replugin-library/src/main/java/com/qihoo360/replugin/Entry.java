
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

import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;

import com.qihoo360.loader2.IPlugin;

/**
 * @author RePlugin Team
 */
public class Entry {

    /**
     * @param context 插件上下文
     * @param cl      HOST程序的类加载器
     * @param manager 插件管理器
     * @return
     */
    public static final IBinder create(Context context, ClassLoader cl, IBinder manager) {
        // 初始化插件框架
        RePluginFramework.init(cl);
        // 初始化Env
        RePluginEnv.init(context, cl, manager);

        return new IPlugin.Stub() {
            @Override
            public IBinder query(String name) throws RemoteException {
                return RePluginServiceManager.getInstance().getService(name);
            }
        };
    }
}
