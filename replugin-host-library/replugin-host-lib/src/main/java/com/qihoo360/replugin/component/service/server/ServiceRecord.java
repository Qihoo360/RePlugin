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
import android.content.pm.ServiceInfo;
import android.os.IBinder;

import com.qihoo360.replugin.utils.basic.ArrayMap;

import java.util.ArrayList;

/**
 * 用来表示一个Service对象的信息
 * 包含绑定此Service的连接信息、Intent绑定信息等
 * <p>
 * NOTE 类似于Android的ServiceRecord
 *
 * @author RePlugin Team
 */
class ServiceRecord {
    // Service的ComponentName
    final ComponentName name;

    // 插件名
    final String plugin;

    // Service类名
    final String className;

    // Intent过滤器，方便直接获取Service对象
    final Intent.FilterComparison intent;

    // 可用来创建Service的ServiceInfo对象
    final ServiceInfo serviceInfo;

    // Service对象
    Service service;

    // 替当前 "插件服务" 在AMS中占坑的组件
    ComponentName pitComponentName;

    // 是否调用过startService且没有停止
    boolean startRequested;

    // 每个Intent对应一个IntentBindRecord缓存
    final ArrayMap<Intent.FilterComparison, IntentBindRecord> bindings = new ArrayMap<>();

    // 每个IBinder（IServiceConnection）对应一个连接信息的缓存
    final ArrayMap<IBinder, ArrayList<ConnectionBindRecord>> connections = new ArrayMap<>();

    final String shortName;

    ServiceRecord(ComponentName cn, Intent.FilterComparison fi, ServiceInfo si) {
        name = cn;
        plugin = cn.getPackageName();
        className = cn.getClassName();
        shortName = name.flattenToShortString();
        intent = fi;
        serviceInfo = si;
    }

    public ProcessBindRecord retrieveAppBindingLocked(Intent intent, ProcessRecord app) {
        Intent.FilterComparison filter = new Intent.FilterComparison(intent);
        IntentBindRecord i = bindings.get(filter);
        if (i == null) {
            i = new IntentBindRecord(this, filter);
            bindings.put(filter, i);
        }
        ProcessBindRecord a = i.apps.get(app);
        if (a != null) {
            return a;
        }
        a = new ProcessBindRecord(this, i, app);
        i.apps.put(app, a);
        return a;
    }

    // 是否有Flag为AUTO_CREATE的绑定链接
    public boolean hasAutoCreateConnections() {
        // XXX should probably keep a count of the number of auto-create
        // connections directly in the service.
        for (int conni = connections.size() - 1; conni >= 0; conni--) {
            ArrayList<ConnectionBindRecord> cr = connections.valueAt(conni);
            for (int i = 0; i < cr.size(); i++) {
                if ((cr.get(i).flags & Context.BIND_AUTO_CREATE) != 0) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "[srv=" + service == null ? "null" : service.getClass().getName() + "; startRequested=" + startRequested + "; bindings=(" + bindings.size() + ") " + bindings + "]";
    }

    public String getPlugin() {
        return plugin;
    }

    public ComponentName getPitComponentName() {
        return pitComponentName;
    }

    public ServiceInfo getServiceInfo() {
        return serviceInfo;
    }
}