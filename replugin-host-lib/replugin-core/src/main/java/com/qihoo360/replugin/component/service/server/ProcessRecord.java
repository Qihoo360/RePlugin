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

import android.os.Messenger;

import java.util.ArrayList;

/**
 * 表示一个Client信息。每个Client是一个进程
 *
 * NOTE 类似于Android的ProcessRecord
 *
 * @author RePlugin Team
 */
class ProcessRecord {

    // 该Client进程的PID
    final int pid;

    // 该Client传递而来的IBinder（Messenger）对象
    final Messenger client;

    // Client进程绑定到此（Server）进程的所有连接信息
    final ArrayList<ConnectionBindRecord> connections = new ArrayList<>();

    private String stringName;

    ProcessRecord(int p, Messenger c) {
        pid = p;
        client = c;
    }

    public String toString() {
        if (stringName != null) {
            return stringName;
        }
        StringBuilder sb = new StringBuilder(128);
        sb.append("ProcessRecord{");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(" p");
        sb.append(pid);
        sb.append('}');
        stringName = sb.toString();
        return stringName;
    }
}
