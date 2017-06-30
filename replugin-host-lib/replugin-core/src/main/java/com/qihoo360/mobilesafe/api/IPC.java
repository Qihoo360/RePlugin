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

package com.qihoo360.mobilesafe.api;

import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.qihoo360.replugin.RePluginInternal;
import com.qihoo360.replugin.base.AMSUtils;

import java.util.List;

/**
 * 包装类，封装一些跨进程通讯的接口
 *
 * @author RePlugin Team
 */
public final class IPC {

    public static final int getUIProcessPID(Context c) {
        String pkg = c.getApplicationInfo().packageName;
        return getRunningProcessPID(c, pkg);
    }

    public static final int getRunningProcessPID(Context c, String processName) {
        List<RunningAppProcessInfo> processes = AMSUtils.getRunningAppProcessesNoThrows(c);
        if (processes != null) {
            for (RunningAppProcessInfo appInfo : processes) {
                if (TextUtils.equals(appInfo.processName, processName)) {
                    return appInfo.pid;
                }
            }
        }
        return 0;
    }

    /**
     * 判断当前包名是否在“运行进程列表”中
     * @param packageName
     * @return
     */
    public static final boolean isRunningProcess(String packageName) {
        List<RunningAppProcessInfo> processes = AMSUtils.getRunningAppProcessesNoThrows(RePluginInternal.getAppContext());
        if (processes != null) {
            for (RunningAppProcessInfo appInfo : processes) {
                if (TextUtils.equals(appInfo.processName, packageName)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @return
     */
    public static final String getCurrentProcessName() {
        return com.qihoo360.replugin.base.IPC.getCurrentProcessName();
    }

    // 双进程使用, 判断目前进程是否是常驻进程
    public static final boolean isPersistentProcess() {
        return com.qihoo360.replugin.base.IPC.isPersistentProcess();
    }

    /**
     * 双进程使用, 判断目前进程是否是UI进程
     * @return
     */
    public static final boolean isUIProcess() {
        return com.qihoo360.replugin.base.IPC.isUIProcess();
    }

    /**
     * 多进程使用, 将intent送到目标进程，对方将受到Local Broadcast
     * 只有当目标进程存活时才能将数据送达
     * 常驻进程通过Local Broadcast注册处理代码
     * @param c
     * @param target
     * @param intent
     */
    public static final void sendLocalBroadcast2Process(Context c, String target, Intent intent) {
        com.qihoo360.replugin.base.IPC.sendLocalBroadcast2Process(c, target, intent);
    }

    /**
     * 多进程使用, 将intent送到目标插件所在进程，对方将受到Local Broadcast
     * 只有当目标进程存活时才能将数据送达
     * 常驻进程通过Local Broadcast注册处理代码
     * @param c
     * @param target
     * @param intent
     */
    public static final void sendLocalBroadcast2Plugin(Context c, String target, Intent intent) {
        com.qihoo360.replugin.base.IPC.sendLocalBroadcast2Plugin(c, target, intent);
    }

    /**
     * 发送广播到所有卫士进程（底层已实现权限控制，不会被第三方APP污染）
     * 只有当目标进程存活时才能将数据送达
     * 卫士任意进程（非single插件进程）只需要通过{@code LocalBroadcastManager}注册即可收到
     * @param c
     * @param intent
     */
    public static final void sendLocalBroadcast2All(Context c, Intent intent) {
        com.qihoo360.replugin.base.IPC.sendLocalBroadcast2All(c, intent);
    }
}
