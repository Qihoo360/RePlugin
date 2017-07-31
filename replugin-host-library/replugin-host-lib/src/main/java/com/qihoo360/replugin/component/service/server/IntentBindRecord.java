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

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.qihoo360.replugin.utils.basic.ArrayMap;
import com.qihoo360.replugin.utils.basic.ArraySet;

/**
 * 用来表示每个Intent所代表的绑定信息
 * 一个Intent可以被多个Process（Client）绑定，但只调用一次onBind方法。像系统那样
 *
 * @author RePlugin Team
 */
class IntentBindRecord {
    /**
     * Intent对应的Service信息
     */
    final ServiceRecord service;
    /**
     * Intent信息
     */
    final Intent.FilterComparison intent;
    /**
     * 哪些进程（Client）的Intent，绑定了这些服务
     */
    final ArrayMap<ProcessRecord, ProcessBindRecord> apps = new ArrayMap<>();
    /**
     * onBind返回的方法
     */
    IBinder binder;
    /**
     * 是否已绑定
     */
    boolean hasBound;

    String stringName;

    IntentBindRecord(ServiceRecord service, Intent.FilterComparison intent) {
        this.service = service;
        this.intent = intent;
    }

    int collectFlags() {
        int flags = 0;
        for (int i = apps.size() - 1; i >= 0; i--) {
            final ArraySet<ConnectionBindRecord> connections = apps.valueAt(i).connections;
            for (int j = connections.size() - 1; j >= 0; j--) {
                flags |= connections.valueAt(j).flags;
            }
        }
        return flags;
    }

    @Override
    public String toString() {
        if (stringName != null) {
            return stringName;
        }
        StringBuilder sb = new StringBuilder(128);
        sb.append("IntentBindRecord{");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(' ');
        if ((collectFlags() & Context.BIND_AUTO_CREATE) != 0) {
            sb.append("CR ");
        }
        sb.append(service.shortName);
        sb.append(':');
        if (intent != null) {
            sb.append(intent.getIntent().toString());
        }
        // 添加Process记录信息
        sb.append(':');
        if (apps.size() > 0) {
            sb.append(apps.toString());
        }
        sb.append('}');
        stringName = sb.toString();
        return stringName;
    }
}
