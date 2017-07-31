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

import com.qihoo360.replugin.utils.basic.ArraySet;

/**
 * 用来表示每个进程（Client端）与Service的绑定关系
 *
 * NOTE 类似于Android的AppBindRecord
 *
 * @author RePlugin Team
 */
class ProcessBindRecord {

    // 被绑定的Service信息
    final ServiceRecord service;

    // 绑定此Service所用的Intent信息
    final IntentBindRecord intent;

    // 哪个进程（Client）建立了绑定关系
    final ProcessRecord client;

    // 在此Service、此进程、此Intent下面的所有连接
    final ArraySet<ConnectionBindRecord> connections = new ArraySet<>();

    ProcessBindRecord(ServiceRecord service, IntentBindRecord intent,
                      ProcessRecord client) {
        this.service = service;
        this.intent = intent;
        this.client = client;
    }

    public String toString() {
        return "ProcessBindRecord{"
                + Integer.toHexString(System.identityHashCode(this))
                + " " + service.shortName + ":" + client.pid + "}";
    }
}
