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

package com.qihoo360.replugin.base;

import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.os.RemoteException;
import android.text.TextUtils;

import com.qihoo360.loader.utils.SysUtils;
import com.qihoo360.loader2.PluginProcessMain;
import com.qihoo360.replugin.helper.HostConfigHelper;
import com.qihoo360.replugin.helper.LogDebug;

import static com.qihoo360.replugin.helper.LogDebug.LOG;
import static com.qihoo360.replugin.helper.LogRelease.LOGR;

/**
 * 用于“进程间通信”的类。插件和宿主可使用此类来做一些跨进程发送广播、判断进程等工作。
 *
 * @author RePlugin Team
 */

public class IPC {

    private static final String TAG = "IPC";

    private static String sCurrentProcess;
    private static int sCurrentPid;
    private static String sPackageName;
    private static String sPersistentProcessName;

    private static boolean sIsPersistentProcess;
    private static boolean sIsUIProcess;

    /**
     * [HIDE] 外界请不要调用此方法
     */
    public static void init(Context context) {
        sCurrentProcess = SysUtils.getCurrentProcessName();
        sCurrentPid = Process.myPid();
        sPackageName = context.getApplicationInfo().packageName;

        // 设置最终的常驻进程名
        if (HostConfigHelper.PERSISTENT_ENABLE) {
            String cppn = HostConfigHelper.PERSISTENT_NAME;
            if (!TextUtils.isEmpty(cppn)) {
                if (cppn.startsWith(":")) {
                    sPersistentProcessName = sPackageName + cppn;
                } else {
                    sPersistentProcessName = cppn;
                }
            }
        } else {
            sPersistentProcessName = sPackageName;
        }

        sIsUIProcess = sCurrentProcess.equals(sPackageName);
        sIsPersistentProcess = sCurrentProcess.equals(sPersistentProcessName);
    }

    /**
     * 获取当前进程名称（从缓存中）
     *
     * @return 当前进程名
     */
    public static String getCurrentProcessName() {
        return sCurrentProcess;
    }

    /**
     * 获取当前进程的ID（PID）
     *
     * @return 当前进程的ID
     */
    public static int getCurrentProcessId() {
        return sCurrentPid;
    }

    /**
     * 获取常驻进程名
     *
     * @return 常驻进程名
     */
    public static String getPersistentProcessName() {
        return sPersistentProcessName;
    }

    /**
     * 获取“插件处理逻辑所在进程名”
     * 若为“单进程模型”则返回UI进程，否则返回“常驻进程”名
     *
     * @return 插件处理逻辑所在进程名
     */
    public static String getPluginHostProcessName() {
        return sPersistentProcessName;
    }

    /**
     * 是否为“插件处理逻辑所在进程”？
     * 若为“单进程模型”则判断当前是否在UI进程，否则判断是否在“常驻进程”
     *
     * @return 若为True，则表示当前正处于“插件处理逻辑所在进程”
     */
    public static boolean isPluginHostProcess() {
        // FIXME 这块儿处理逻辑和原来不同，原来是endsWith，这里是Equals，需判断ROM是否做过什么修改
        return TextUtils.equals(getCurrentProcessName(), getPluginHostProcessName());
    }

    /**
     * 当前是否为“UI进程”（主进程）？
     *
     * @return 是否为UI进程
     */
    public static boolean isUIProcess() {
        return sIsUIProcess;
    }

    /**
     * 当前是否为“常驻进程”？
     *
     * @return 是否为常驻进程
     */
    public static boolean isPersistentProcess() {
        return sIsPersistentProcess;
    }

    /**
     * 是否支持在“常驻进程”中处理插件逻辑？若应用有常驻进程，则应将此设为True
     * 若为False，则处理插件的逻辑全部放在UI进程中（单进程）
     *
     * @return 是否支持？
     */
    public static boolean isPersistentEnable() {
        return HostConfigHelper.PERSISTENT_ENABLE;
    }

    /**
     * 通过进程名来获取PID。仅允许获取应用自己的进程
     *
     * @param processName 进程名
     * @return PID，若为-1则表示没有此进程，或获取时出现问题
     */
    public static int getPidByProcessName(String processName) {
        if (TextUtils.isEmpty(processName)) {
            return -1;
        }

        // 拿的是自己？直接返回即可
        if (TextUtils.equals(processName, getCurrentProcessName())) {
            return getCurrentProcessId();
        }

        // 向常驻服务索要
        try {
            return PluginProcessMain.getPluginHost().getPidByProcessName(processName);
        } catch (RemoteException e) {
            if (LOGR) {
                e.printStackTrace();
            }
            return -1;
        }
    }

    /**
     * 通过PID来获取进程名。仅允许获取应用自己的进程
     *
     * @param pid 进程PID
     * @return 进程名，若为null则表示没有此进程，或获取时出现问题
     */
    public static String getProcessNameByPid(int pid) {
        if (pid < 0) {
            return null;
        }

        // 拿的是自己？直接返回即可
        if (pid == IPC.getCurrentProcessId()) {
            return getCurrentProcessName();
        }

        try {
            return PluginProcessMain.getPluginHost().getProcessNameByPid(pid);
        } catch (RemoteException e) {
            if (LOGR) {
                e.printStackTrace();
            }
            return null;
        }
    }

    /**
     * 获取当前宿主的包名（从缓存中）
     * @return 宿主的包名
     */
    public static String getPackageName() {
        return sPackageName;
    }

    /**
     * 多进程使用, 将intent送到目标插件所在进程，对方将受到Local Broadcast
     * 只有当目标进程存活时才能将数据送达
     * 常驻进程通过Local Broadcast注册处理代码
     *
     * @param target 插件名
     * @param intent Intent对象
     */
    public static boolean sendLocalBroadcast2Plugin(Context c, String target, Intent intent) {
        if (LOG) {
            LogDebug.d(TAG, "sendLocalBroadcast2Plugin: target=" + target + " intent=" + intent);
        }
        if (TextUtils.isEmpty(target)) {
            return false;
        }
        try {
            PluginProcessMain.getPluginHost().sendIntent2Plugin(target, intent);
            return true;
        } catch (RemoteException e) {
            if (LOGR) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 多进程使用, 将intent送到目标进程，对方将收到Local Broadcast广播
     * <p>
     * 只有当目标进程存活时才能将数据送达
     * <p>
     * 常驻进程通过Local Broadcast注册处理代码
     *
     * @param target 目标进程名
     * @param intent Intent对象
     */
    public static boolean sendLocalBroadcast2Process(Context c, String target, Intent intent) {
        if (LOG) {
            LogDebug.d(TAG, "sendLocalBroadcast2Process: target=" + target + " intent=" + intent);
        }
        if (TextUtils.isEmpty(target)) {
            return false;
        }
        try {
            PluginProcessMain.getPluginHost().sendIntent2Process(target, intent);
            return true;
        } catch (RemoteException e) {
            if (LOGR) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 发送广播到所有卫士进程（底层已实现权限控制，不会被第三方APP污染）
     * <p>
     * 只有当目标进程存活时才能将数据送达
     * <p>
     * 卫士任意进程（非single插件进程）只需要通过{@code LocalBroadcastManager}注册即可收到
     *
     * @param intent Intent对象
     */
    public static boolean sendLocalBroadcast2All(Context c, Intent intent) {
        if (LOG) {
            LogDebug.d(TAG, "sendLocalBroadcast2All: intent=" + intent);
        }
        try {
            PluginProcessMain.getPluginHost().sendIntent2Process(null, intent);
            return true;
        } catch (RemoteException e) {
            if (LOGR) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 多进程使用, 将intent送到目标插件所在进程，对方将收到Local Broadcast广播
     * <p>
     * 只有当目标进程存活时才能将数据送达
     * <p>
     * 常驻进程通过Local Broadcast注册处理代码
     * <p>
     * 会【阻塞】直到所有消息处理完成后才能继续
     *
     * @param target 插件名
     * @param intent Intent对象
     */
    public static boolean sendLocalBroadcast2PluginSync(Context c, String target, Intent intent) {
        if (LOG) {
            LogDebug.d(TAG, "sendLocalBroadcast2PluginSync: target=" + target + " intent=" + intent);
        }
        if (TextUtils.isEmpty(target)) {
            return false;
        }
        try {
            PluginProcessMain.getPluginHost().sendIntent2PluginSync(target, intent);
            return true;
        } catch (RemoteException e) {
            if (LOGR) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 多进程使用, 将intent送到目标进程，对方将收到Local Broadcast广播
     * <p>
     * 只有当目标进程存活时才能将数据送达
     * <p>
     * 常驻进程通过Local Broadcast注册处理代码
     * <p>
     * 会【阻塞】直到所有消息处理完成后才能继续
     *
     * @param target 目标进程名
     * @param intent Intent对象
     */
    public static boolean sendLocalBroadcast2ProcessSync(Context c, String target, Intent intent) {
        if (LOG) {
            LogDebug.d(TAG, "sendLocalBroadcast2ProcessSync: target=" + target + " intent=" + intent);
        }
        if (TextUtils.isEmpty(target)) {
            return false;
        }
        try {
            PluginProcessMain.getPluginHost().sendIntent2ProcessSync(target, intent);
            return true;
        } catch (RemoteException e) {
            if (LOGR) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 发送广播到所有卫士进程（底层已实现权限控制，不会被第三方APP污染）
     * <p>
     * 只有当目标进程存活时才能将数据送达
     * <p>
     * 卫士任意进程（非single插件进程）只需要通过{@code LocalBroadcastManager}注册即可收到
     * <p>
     * 会【阻塞】直到所有消息处理完成后才能继续
     *
     * @param intent Intent对象
     */
    public static boolean sendLocalBroadcast2AllSync(Context c, Intent intent) {
        if (LOG) {
            LogDebug.d(TAG, "sendLocalBroadcast2AllSync: intent=" + intent);
        }
        try {
            PluginProcessMain.getPluginHost().sendIntent2ProcessSync(null, intent);
            return true;
        } catch (RemoteException e) {
            if (LOGR) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
