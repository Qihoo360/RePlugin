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

import com.qihoo360.loader2.mgr.IServiceConnection;

/**
 * 用来表示一个BindService的连接
 * 存储了IServiceConnection对象，被其它几个Record引用
 *
 * @author RePlugin Team
 */
class ConnectionBindRecord {
    final ProcessBindRecord binding;    // 谁绑定了这个链接？
    final IServiceConnection conn;      // IServiceConnection(ServiceConnection for Client)对象
    final int flags;                    // 绑定的Flags

    boolean serviceDead;                // 连接即将被unbind，标记此开关是防止重复unbind

    private String stringName;

    ConnectionBindRecord(ProcessBindRecord abr, IServiceConnection sc, int f) {
        binding = abr;
        conn = sc;
        flags = f;
    }

    public String toString() {
        if (stringName != null) {
            return stringName;
        }
        StringBuilder sb = new StringBuilder(128);
        sb.append("ConnectionBindRecord{");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(" p");
        sb.append(binding.client.pid);
        sb.append(' ');
        if ((flags & Context.BIND_AUTO_CREATE) != 0) {
            sb.append("CR ");
        }
        if ((flags & Context.BIND_DEBUG_UNBIND) != 0) {
            sb.append("DBG ");
        }
        if ((flags & Context.BIND_NOT_FOREGROUND) != 0) {
            sb.append("!FG ");
        }
        if ((flags & Context.BIND_ABOVE_CLIENT) != 0) {
            sb.append("ABCLT ");
        }
        if ((flags & Context.BIND_ALLOW_OOM_MANAGEMENT) != 0) {
            sb.append("OOM ");
        }
        if ((flags & Context.BIND_WAIVE_PRIORITY) != 0) {
            sb.append("WPRI ");
        }
        if ((flags & Context.BIND_IMPORTANT) != 0) {
            sb.append("IMP ");
        }
        if ((flags & Context.BIND_ADJUST_WITH_ACTIVITY) != 0) {
            sb.append("WACT ");
        }
        if (serviceDead) {
            sb.append("DEAD ");
        }
        sb.append(binding.service.shortName);
        sb.append(":@");
        sb.append(Integer.toHexString(System.identityHashCode(conn.asBinder())));
        sb.append('}');
        stringName = sb.toString();
        return stringName;
    }
}
