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

package com.qihoo360.replugin.packages;

import android.os.RemoteException;
import android.text.TextUtils;

import com.qihoo360.loader2.IPluginHost;
import com.qihoo360.loader2.MP;
import com.qihoo360.replugin.base.IPC;
import com.qihoo360.replugin.helper.LogDebug;
import com.qihoo360.replugin.helper.LogRelease;
import com.qihoo360.replugin.model.PluginInfo;

import java.util.List;

import static com.qihoo360.replugin.helper.LogDebug.LOG;
import static com.qihoo360.replugin.helper.LogDebug.PLUGIN_TAG;

/**
 * 用于各进程（包括常驻自己）缓存 PluginManagerServer 的Binder实现
 *
 * @author RePlugin Team
 * @see PluginManagerServer
 */

public class PluginManagerProxy {

    private static final String TAG = "PluginManagerClient";

    private static IPluginManagerServer sRemote;
    private static boolean sRunningSynced;

    private static PluginRunningList sRunningList = new PluginRunningList();
    static {
        sRunningList.setProcessInfo(IPC.getCurrentProcessName(), IPC.getCurrentProcessId());
    }

    /**
     * 连接到常驻进程，并缓存IPluginManagerServer对象
     *
     * @param host IPluginHost对象
     * @throws RemoteException 和常驻进程通讯出现异常
     */
    public static void connectToServer(IPluginHost host) throws RemoteException {
        if (sRemote != null) {
            if (LogDebug.LOG) {
                LogDebug.e(TAG, "connectToServer: Already connected! host=" + sRemote);
            }
            return;
        }

        sRemote = host.fetchManagerServer();
    }

    /**
     * 当常驻进程出现异常时，断开当前进程与其的缓存的链接
     */
    public static void disconnect() {
        sRemote = null;

        // 表示常驻挂掉，下回需同步
        sRunningSynced = false;

        // 不要清除"正在运行插件"的列表，毕竟插件还在该进程中运行着。在下次常驻启动时会自动同步过去
        // sRunningList.clear();
    }

    /**
     * 调用常驻进程的Server端去加载插件列表，方便之后使用
     *
     * @return PluginInfo的列表
     * @throws RemoteException 和常驻进程通讯出现异常
     */
    public static List<PluginInfo> load() throws RemoteException {
        // 不判断sRemote在不在，因为本应该在sRemote获取后就马上调用
        return sRemote.load();
    }

    /**
     * 预安装内置插件到app_p_a目录。因为考虑到内置插件的特殊性和缓存中针对内置插件需要做一些处理，最开始我们需要认为内置插件就已经属于安装了，读取列表应该能读取到才行。
     * 所以需要把插件信息写入到p.l中，以便于后续的加载和更新都可以读到。
     * 注意：只会在首次启动应用的时候做这个事
     * @param builtins 内置插件列表
     * @return 安装完后的插件列表，理论上就是内置插件
     * @throws RemoteException
     */
    public static List<PluginInfo> preInstallBuiltins(List<PluginInfo> builtins) throws RemoteException {
        return sRemote.preInstallBuiltins(builtins);
    }

    /**
     * 调用常驻进程的Server端去更新插件列表
     *
     * @return PluginInfo的列表
     * @throws RemoteException 和常驻进程通讯出现异常
     */
    public static List<PluginInfo> updateAllPlugins() throws RemoteException {
        // 不判断sRemote在不在，因为本应该在sRemote获取后就马上调用
        return sRemote.load();
    }

    /**
     * 去常驻进程更新isUsed状态，并发送到所有进程中更新
     *
     * @param name 插件名字
     * @param path 插件释放的路径
     * @param type 插件状态类型
     * @param used 插件是否已被使用
     */
    public static void updateUsedIfNeeded(String name, String path, int type, boolean used) throws RemoteException {
        PluginInfo pi = MP.getPlugin(name, false);
        if (pi == null) {
            // 不太可能到这里
            return;
        }
        if (pi.isUsed() == used) {
            // 已经改变了？那就不做处理
            if (LOG) {
                LogDebug.i(TAG, "updateUsedIfNeeded: pi.isUsed == used, ignore. used=" + used + "; pn=" + name);
            }
            return;
        }

        if (sRemote == null) {
            // 常驻已挂掉，可以认为无需处理
            if (LogRelease.LOGR) {
                LogRelease.e(PLUGIN_TAG, "pmc.uuin: s=null");
            }
            return;
        }

        // 去常驻进程更新状态
        sRemote.updateUsedNew(name, path, type, used);
    }


    /**
     * 更新插件信息到常驻进程和p.l文件，一般是首次内置插件加载完毕后，需要调用
     *
     * @param plugin 插件名字
     * @param type   插件的类型
     * @param path   插件的路径
     * @throws RemoteException
     */
    public static void updateTP(String plugin, int type, String path) throws RemoteException {
        sRemote.updateTP(plugin, type, path);
    }

    /**
     * 首先检查本地进程是否使用，然后再调用常驻进程的Server端去判断
     *
     * @param pluginName 插件名
     * @return 是否运行
     * @throws RemoteException 和常驻进程通讯出现异常
     */
    public static boolean isPluginRunning(String pluginName) throws RemoteException {
        if (sRunningList.isRunning(pluginName)) {
            // 当前进程就已经运行了，直接返回
            return true;
        }
        if (sRemote == null) {
            // 常驻已挂掉，可以认为先返回False
            if (LogRelease.LOGR) {
                LogRelease.e(PLUGIN_TAG, "pmp.ipr: s=null");
            }
            return false;
        }

        // 去常驻进程查其它进程是否运行
        return sRemote.isPluginRunning(pluginName, null);
    }

    /**
     * 首先检查指定进程是否和本地相同，然后再调用常驻进程的Server端去判断
     *
     * @param pluginName 插件名
     * @param process 指定进程
     * @return 是否运行
     * @throws RemoteException 和常驻进程通讯出现异常
     */
    public static boolean isPluginRunningInProcess(String pluginName, String process) throws RemoteException {
        if (TextUtils.equals(process, IPC.getCurrentProcessName())) {
            // 要查的就是当前所在进程？那直接从当前进程中取表即可
            return sRunningList.isRunning(pluginName);
        } else {
            // 要查的不在当前进程？则通过远端去查
            if (sRemote == null) {
                // 常驻已挂掉，可以认为先返回False
                if (LogRelease.LOGR) {
                    LogRelease.e(PLUGIN_TAG, "pmp.iprip: s=null");
                }
                return false;
            }

            // 去常驻进程查其它进程是否运行
            return sRemote.isPluginRunning(pluginName, process);
        }
    }

    /**
     * 同步当前进程"已加载插件列表"到常驻进程的Server端中
     *
     * @throws RemoteException 和常驻进程通讯出现异常
     */
    public static void syncRunningPlugins() throws RemoteException {
        if (sRunningSynced) {
            // 已经同步过，无需再次同步，直到常驻进程挂掉
            return;
        }
        if (!sRunningList.hasRunning()) {
            // 没有正在运行的插件，无需同步。若该进程被刚刚加载时会出现此情况
            return;
        }
        // 不判断sRemote在不在，因为本应该在sRemote获取后就马上调用
        sRemote.syncRunningPlugins(sRunningList);
        sRunningSynced = true;
    }

    /**
     * 添加插件到"当前进程的正在运行插件列表"，并同步到Server端
     *
     * @param pluginName 插件名
     */
    public static void addToRunningPluginsNoThrows(String pluginName) {
        // 本地先加一份
        sRunningList.add(pluginName);

        // 通知常驻在总表中也加一份
        if (sRemote != null) {
            // 有可能常驻进程已经被干掉，那就等下次调用syncRunningPlugins时才同步。
            // 而当它存在时就直接加入列表即可
            try {
                sRemote.addToRunningPlugins(sRunningList.mProcessName, sRunningList.mPid, pluginName);
            } catch (RemoteException e) {
                // 常驻进程出现问题，先不管，等下次启动时再同步
                if (LogRelease.LOGR) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 调用常驻进程的Server端去拉取正在运行的插件列表。如有异常，则只获取本地即可
     *
     * @return 正在运行的插件列表
     */
    public static PluginRunningList getRunningPluginsNoThrows() {
        PluginRunningList rl = null;

        // 只有常驻进程在时才获取
        if (sRemote != null) {
            try {
                rl = new PluginRunningList(sRemote.getRunningPlugins());
            } catch (RemoteException e) {
                // 常驻进程出现问题
                if (LogRelease.LOGR) {
                    e.printStackTrace();
                }
            }
        }

        // 没有获取到？则获取本地运行插件列表
        if (rl == null) {
            rl = new PluginRunningList(sRunningList);
        }
        return rl;
    }

    /**
     * 获取正在运行此插件的进程名列表。若常驻进程无法使用，则只获取当前进程的运行情况
     *
     * @param pluginName 要查询的插件名
     * @return 正在运行此插件的进程名列表。一定不会为Null
     */
    public static String[] getRunningProcessesByPluginNoThrows(String pluginName) {
        // 只有常驻进程在时才获取
        if (sRemote != null) {
            try {
                return sRemote.getRunningProcessesByPlugin(pluginName);
            } catch (RemoteException e) {
                // 常驻进程出现问题
                if (LogRelease.LOGR) {
                    e.printStackTrace();
                }
            }
        }

        // 没有获取到？则只判断本地是否运行即可
        String[] r;
        if (sRunningList.isRunning(pluginName)) {
            r = new String[]{sRunningList.mProcessName};
        } else {
            r = new String[0];
        }
        return r;
    }
}
