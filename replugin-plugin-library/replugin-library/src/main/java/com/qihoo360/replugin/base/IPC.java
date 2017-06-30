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
import android.text.TextUtils;

import com.qihoo360.replugin.MethodInvoker;
import com.qihoo360.replugin.RePluginFramework;
import com.qihoo360.replugin.helper.LogDebug;

/**
 * 用于“进程间通信”的类。插件和宿主可使用此类来做一些跨进程发送广播、判断进程等工作。
 *
 * @author RePlugin Team
 */

public class IPC {
    private static final String TAG = "IPC";

    /**
     * 获取当前进程名称（从缓存中）
     *
     * @return 当前进程名
     */
    public static String getCurrentProcessName() {
        if (!RePluginFramework.isHostInitialized()) {
            return null;
        }

        try {
            return (String) ProxyIPCVar.getCurrentProcessName.call(null);
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * 获取当前进程的ID（PID）
     *
     * @return 当前进程的ID
     */
    public static int getCurrentProcessId() {
        if (!RePluginFramework.isHostInitialized()) {
            return -1;
        }

        try {
            Object obj = ProxyIPCVar.getCurrentProcessId.call(null);
            if (obj != null) {
                return (Integer) obj;
            }
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }

        return -1;
    }

    /**
     * 获取常驻进程名
     *
     * @return 常驻进程名
     */
    public static String getPersistentProcessName() {
        if (!RePluginFramework.isHostInitialized()) {
            return null;
        }

        try {
            return (String) ProxyIPCVar.getPersistentProcessName.call(null);
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * 获取“插件处理逻辑所在进程名”
     * 若为“单进程模型”则返回UI进程，否则返回“常驻进程”名
     *
     * @return 插件处理逻辑所在进程名
     */
    public static String getPluginHostProcessName() {
        if (!RePluginFramework.isHostInitialized()) {
            return null;
        }

        try {
            return (String) ProxyIPCVar.getPluginHostProcessName.call(null);
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * 是否为“插件处理逻辑所在进程”？
     * 若为“单进程模型”则判断当前是否在UI进程，否则判断是否在“常驻进程”
     *
     * @return 若为True，则表示当前正处于“插件处理逻辑所在进程”
     */
    public static boolean isPluginHostProcess() {
        if (!RePluginFramework.isHostInitialized()) {
            return false;
        }

        try {
            Object obj = ProxyIPCVar.isPluginHostProcess.call(null);
            if (obj != null) {
                return (Boolean) obj;
            }
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }

        return false;
    }

    /**
     * 当前是否为“UI进程”（主进程）？
     *
     * @return 是否为UI进程
     */
    public static boolean isUIProcess() {
        if (!RePluginFramework.isHostInitialized()) {
            return false;
        }

        try {
            Object obj = ProxyIPCVar.isUIProcess.call(null);
            if (obj != null) {
                return (Boolean) obj;
            }
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }

        return false;
    }

    /**
     * 当前是否为“常驻进程”？
     *
     * @return 是否为常驻进程
     */
    public static boolean isPersistentProcess() {
        if (!RePluginFramework.isHostInitialized()) {
            return false;
        }

        try {
            Object obj = ProxyIPCVar.isPersistentProcess.call(null);
            if (obj != null) {
                return (Boolean) obj;
            }
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }

        return false;
    }

    /**
     * 是否支持在“常驻进程”中处理插件逻辑？若应用有常驻进程，则应将此设为True
     * 若为False，则处理插件的逻辑全部放在UI进程中（单进程）
     *
     * @return 是否支持？
     */
    public static boolean isPersistentEnable() {
        if (!RePluginFramework.isHostInitialized()) {
            return false;
        }

        try {
            Object obj = ProxyIPCVar.isPersistentEnable.call(null);
            if (obj != null) {
                return (Boolean) obj;
            }
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }

        return false;
    }

    /**
     * 通过进程名来获取PID。仅允许获取应用自己的进程
     *
     * @param processName 进程名
     * @return PID，若为-1则表示没有此进程，或获取时出现问题
     */
    public static int getPidByProcessName(String processName) {
        if (!RePluginFramework.isHostInitialized()) {
            return -1;
        }

        try {
            Object obj = ProxyIPCVar.getPidByProcessName.call(null, processName);
            if (obj != null) {
                return (Integer) obj;
            }
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }

        return -1;
    }

    /**
     * 通过PID来获取进程名。仅允许获取应用自己的进程
     *
     * @param pid 进程PID
     * @return 进程名，若为null则表示没有此进程，或获取时出现问题
     */
    public static String getProcessNameByPid(int pid) {
        if (!RePluginFramework.isHostInitialized()) {
            return null;
        }

        try {
            return (String) ProxyIPCVar.getProcessNameByPid.call(null, pid);
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * 获取当前宿主的包名（从缓存中）
     *
     * @return 宿主的包名
     */
    public static String getPackageName() {
        if (!RePluginFramework.isHostInitialized()) {
            return null;
        }

        try {
            return (String) ProxyIPCVar.getPackageName.call(null);
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }

        return null;
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
        if (LogDebug.LOG) {
            LogDebug.d(TAG, "sendLocalBroadcast2Plugin: target=" + target + " intent=" + intent);
        }

        if (TextUtils.isEmpty(target)) {
            return false;
        }

        if (!RePluginFramework.isHostInitialized()) {
            return false;
        }

        try {
            Object obj = ProxyIPCVar.sendLocalBroadcast2Plugin.call(null, c, target, intent);
            if (obj != null) {
                return (Boolean) obj;
            }
        } catch (Exception e) {
            if (LogDebug.LOG) {
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
        if (LogDebug.LOG) {
            LogDebug.d(TAG, "sendLocalBroadcast2Process: target=" + target + " intent=" + intent);
        }
        if (TextUtils.isEmpty(target)) {
            return false;
        }

        if (!RePluginFramework.isHostInitialized()) {
            return false;
        }

        try {
            Object obj = ProxyIPCVar.sendLocalBroadcast2Process.call(null, c, target, intent);
            if (obj != null) {
                return (Boolean) obj;
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        if (LogDebug.LOG) {
            LogDebug.d(TAG, "sendLocalBroadcast2All: intent=" + intent);
        }

        if (!RePluginFramework.isHostInitialized()) {
            return false;
        }

        try {
            Object obj = ProxyIPCVar.sendLocalBroadcast2All.call(null, c, intent);
            if (obj != null) {
                return (Boolean) obj;
            }
        } catch (Exception e) {
            if (LogDebug.LOG) {
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
        if (LogDebug.LOG) {
            LogDebug.d(TAG, "sendLocalBroadcast2PluginSync: target=" + target + " intent=" + intent);
        }
        if (TextUtils.isEmpty(target)) {
            return false;
        }
        if (!RePluginFramework.isHostInitialized()) {
            return false;
        }

        try {
            Object obj = ProxyIPCVar.sendLocalBroadcast2PluginSync.call(null, c, target, intent);
            if (obj != null) {
                return (Boolean) obj;
            }
        } catch (Exception e) {
            if (LogDebug.LOG) {
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
        if (LogDebug.LOG) {
            LogDebug.d(TAG, "sendLocalBroadcast2ProcessSync: target=" + target + " intent=" + intent);
        }

        if (TextUtils.isEmpty(target)) {
            return false;
        }

        if (!RePluginFramework.isHostInitialized()) {
            return false;
        }

        try {
            Object obj = ProxyIPCVar.sendLocalBroadcast2ProcessSync.call(null, c, target, intent);
            if (obj != null) {
                return (Boolean) obj;
            }
        } catch (Exception e) {
            if (LogDebug.LOG) {
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
        if (LogDebug.LOG) {
            LogDebug.d(TAG, "sendLocalBroadcast2AllSync: intent=" + intent);
        }

        if (!RePluginFramework.isHostInitialized()) {
            return false;
        }

        try {
            Object obj = ProxyIPCVar.sendLocalBroadcast2AllSync.call(null, c, intent);
            if (obj != null) {
                return (Boolean) obj;
            }
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }

        return false;
    }

    public static class ProxyIPCVar {

        private static MethodInvoker getCurrentProcessName;

        private static MethodInvoker getCurrentProcessId;

        private static MethodInvoker getPersistentProcessName;

        private static MethodInvoker getPluginHostProcessName;

        private static MethodInvoker isPluginHostProcess;

        private static MethodInvoker isUIProcess;

        private static MethodInvoker isPersistentProcess;

        private static MethodInvoker isPersistentEnable;

        private static MethodInvoker getPidByProcessName;

        private static MethodInvoker getProcessNameByPid;

        private static MethodInvoker getPackageName;

        private static MethodInvoker sendLocalBroadcast2Plugin;

        private static MethodInvoker sendLocalBroadcast2Process;

        private static MethodInvoker sendLocalBroadcast2All;

        private static MethodInvoker sendLocalBroadcast2PluginSync;

        private static MethodInvoker sendLocalBroadcast2ProcessSync;

        private static MethodInvoker sendLocalBroadcast2AllSync;

        public static void initLocked(final ClassLoader classLoader) {
            //
            final String IPC = "com.qihoo360.replugin.base.IPC";
            getCurrentProcessName = new MethodInvoker(classLoader, IPC, "getCurrentProcessName", new Class<?>[]{});
            getCurrentProcessId = new MethodInvoker(classLoader, IPC, "getCurrentProcessId", new Class<?>[]{});
            getPersistentProcessName = new MethodInvoker(classLoader, IPC, "getPersistentProcessName", new Class<?>[]{});
            getPluginHostProcessName = new MethodInvoker(classLoader, IPC, "getPluginHostProcessName", new Class<?>[]{});
            isPluginHostProcess = new MethodInvoker(classLoader, IPC, "isPluginHostProcess", new Class<?>[]{});
            isUIProcess = new MethodInvoker(classLoader, IPC, "isUIProcess", new Class<?>[]{});
            isPersistentProcess = new MethodInvoker(classLoader, IPC, "isPersistentProcess", new Class<?>[]{});
            isPersistentEnable = new MethodInvoker(classLoader, IPC, "isPersistentEnable", new Class<?>[]{});
            getPidByProcessName = new MethodInvoker(classLoader, IPC, "getPidByProcessName", new Class<?>[]{String.class});
            getProcessNameByPid = new MethodInvoker(classLoader, IPC, "getProcessNameByPid", new Class<?>[]{int.class});
            getPackageName = new MethodInvoker(classLoader, IPC, "getPackageName", new Class<?>[]{});
            sendLocalBroadcast2Plugin = new MethodInvoker(classLoader, IPC, "sendLocalBroadcast2Plugin", new Class<?>[]{Context.class, String.class, Intent.class});
            sendLocalBroadcast2Process = new MethodInvoker(classLoader, IPC, "sendLocalBroadcast2Process", new Class<?>[]{Context.class, String.class, Intent.class});
            sendLocalBroadcast2All = new MethodInvoker(classLoader, IPC, "sendLocalBroadcast2All", new Class<?>[]{Context.class, Intent.class});
            sendLocalBroadcast2PluginSync = new MethodInvoker(classLoader, IPC, "sendLocalBroadcast2PluginSync", new Class<?>[]{Context.class, String.class, Intent.class});
            sendLocalBroadcast2ProcessSync = new MethodInvoker(classLoader, IPC, "sendLocalBroadcast2ProcessSync", new Class<?>[]{Context.class, String.class, Intent.class});
            sendLocalBroadcast2AllSync = new MethodInvoker(classLoader, IPC, "sendLocalBroadcast2AllSync", new Class<?>[]{Context.class, Intent.class});
        }
    }
}
