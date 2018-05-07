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

package com.qihoo360.loader2;

import android.os.IBinder;
import android.os.RemoteException;

import com.qihoo360.loader.utils.ProcessLocker;
import com.qihoo360.replugin.IHostBinderFetcher;
import com.qihoo360.replugin.RePlugin;
import com.qihoo360.replugin.RePluginInternal;
import com.qihoo360.replugin.helper.LogDebug;
import com.qihoo360.replugin.helper.LogRelease;
import com.qihoo360.replugin.model.PluginInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.qihoo360.replugin.helper.LogDebug.LOG;
import static com.qihoo360.replugin.helper.LogDebug.MAIN_TAG;
import static com.qihoo360.replugin.helper.LogDebug.PLUGIN_TAG;
import static com.qihoo360.replugin.helper.LogRelease.LOGR;

/**
 * 对外接口代码
 *
 * @author RePlugin Team
 */
public class MP {

    /**
     * 需要重启常驻进程
     */
    public static final String ACTION_REQUEST_RESTART = "com.qihoo360.loader2.ACTION_REQUEST_RESTART";

    /**
     * 马上重启常驻服务
     */
    public static final String ACTION_QUICK_RESTART = "com.qihoo360.loader2.ACTION_QUICK_RESTART";

    /**
     * 调试用
     */
    static volatile HashMap<String, String> sBinderReasons;

    /**
     * 仿插件对象，用来实现主程序提供binder给其他模块
     *
     * @param name
     * @param p
     */
    public static final void installBuiltinPlugin(String name, IHostBinderFetcher p) {
        PMF.sPluginMgr.installBuiltinPlugin(name, p);
    }

    /**
     * @param name
     * @param binder
     */
    public static final void installBinder(String name, IBinder binder) {
        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "installBinder n=" + name + " b=" + binder);
        }
        try {
            PluginProcessMain.getPluginHost().installBinder(name, binder);
        } catch (Throwable e) {
            if (LOGR) {
                LogRelease.e(PLUGIN_TAG, "mp i.b: " + e.getMessage(), e);
            }
        }
    }

    /**
     * @param name
     * @return
     */
    public static final IBinder fetchBinder(String name) {
        try {
            IBinder binder = PluginProcessMain.getPluginHost().fetchBinder(name);
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "fetchBinder n=" + name + " b=" + binder);
            }
            return binder;
        } catch (Throwable e) {
            if (LOGR) {
                LogRelease.e(PLUGIN_TAG, "mp f.b: " + e.getMessage(), e);
            }
        }
        return null;
    }

    /**
     * 拉起插件（或UI）进程，并查询对应的binder接口对象
     *
     * @param plugin
     * @param process
     * @param binder
     * @return
     */
    public static final PluginBinder fetchPluginBinder(String plugin, int process, String binder) {
        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "MP.fetchPluginBinder ... plugin=" + plugin + " binder.name=" + binder);
        }

        // 若开启了“打印详情”则打印调用栈，便于观察
        if (RePlugin.getConfig().isPrintDetailLog()) {
            String reason = "";
            StackTraceElement elements[] = Thread.currentThread().getStackTrace();
            for (StackTraceElement item : elements) {
                if (item.isNativeMethod()) {
                    continue;
                }
                String cn = item.getClassName();
                String mn = item.getMethodName();
                String filename = item.getFileName();
                int line = item.getLineNumber();
                reason += cn + "." + mn + "(" + filename + ":" + line + ")" + "\n";
            }
            if (sBinderReasons == null) {
                sBinderReasons = new HashMap<String, String>();
            }
            sBinderReasons.put(plugin + ":" + binder, reason);
        }

        PluginBinderInfo info = new PluginBinderInfo(PluginBinderInfo.BINDER_REQUEST);
        IBinder b = null;
        try {
            // 容器选择
            IPluginClient client = MP.startPluginProcess(plugin, process, info);
            if (client == null) {
                if (LOGR) {
                    LogRelease.e(PLUGIN_TAG, "mp.f.p.b: s c fail");
                }
                return null;
            }
            // 远程获取
            b = client.queryBinder(plugin, binder);
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "MP.fetchPluginBinder binder.object=" + b + " pid=" + info.pid);
            }
            // 增加计数器
            if (b != null) {
                PluginProcessMain.getPluginHost().regPluginBinder(info, b);
            }
        } catch (Throwable e) {
            if (LOGR) {
                LogRelease.e(PLUGIN_TAG, "mp.f.p.b: p=" + info.pid, e);
            }
        }

        if (b == null) {
            return null;
        }

        return new PluginBinder(plugin, binder, info.pid, b);
    }

    /**
     * @param binder
     */
    public static final void releasePluginBinder(PluginBinder binder) {
        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "MP.releasePluginBinder ... pid=" + binder.pid + " binder=" + binder.binder);
        }

        // 记录调用栈，便于观察：删除
        if (LOG) {
            if (sBinderReasons != null) {
                sBinderReasons.remove(binder.plugin + ":" + binder.name);
            }
        }

        PluginBinderInfo info = new PluginBinderInfo(PluginBinderInfo.BINDER_REQUEST);
        info.pid = binder.pid;
        try {
            PluginProcessMain.getPluginHost().unregPluginBinder(info, binder.binder);
        } catch (Throwable e) {
            if (LOGR) {
                LogRelease.e(PLUGIN_TAG, "mp.r.p.b: " + e.getMessage(), e);
            }
        }
    }

    /**
     * @param path
     * @return
     */
    public static final PluginInfo pluginDownloaded(String path) {
        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "MP.pluginDownloaded ... path=" + path);
        }

        /**
         * 问题描述：
         *
         * 对于正在生效的插件，如果当前时机pluginHost没有存活，那么这里会先启动pluginHost，然后再调用它的PluginHost进程的pluginDownloaded接口
         *
         * 这里的问题是：pluginHost进程在启动过程会通过扫描文件的方式将当前即将生效的插件识别到，
         * 而在进程ready后，再去调用pluginDownloaded接口的时候会认为它不是新插件，从而不会通过NEW_PLUGIN广播来周知所有进程新插件生效了
         * 因此，当前进程也不会同步新插件生效的逻辑。
         * so，问题就来了，当前进程新下载的插件由于pluginHost的逻辑无法正常生效。
         * 当然该问题只针对p-n格式的插件，而纯APK格式的插件不再使用进程启动的时候通过扫描文件目录的方式来来识别所有准备好的插件
         *
         * 解决办法：
         * 对于处于该流程的插件文件（p-n插件）加上lock文件，以表示当前插件正在生效，不需要plugHost进程再次扫描生效了，也就不存在新插件在新进程中成为了老插件
         */

        ProcessLocker lock = null;

        try {
            if (path != null) {
                File f = new File(path);
                String fileName = f.getName();
                String fileDir = f.getParent();
                if (fileName.startsWith("p-n-")) {
                    lock = new ProcessLocker(RePluginInternal.getAppContext(), fileDir, fileName + ".lock");
                }
            }

            if (lock != null && !lock.tryLock()) {
                // 加锁
                if (LOG) {
                    LogDebug.d(PLUGIN_TAG, "MP.pluginDownloaded ... lock file + " + path + " failed! ");
                }
            }

            PluginInfo info = PluginProcessMain.getPluginHost().pluginDownloaded(path);
            if (info != null) {
                RePlugin.getConfig().getEventCallbacks().onInstallPluginSucceed(info);
            }
            return info;
        } catch (Throwable e) {
            if (LOGR) {
                LogRelease.e(PLUGIN_TAG, "mp.pded: " + e.getMessage(), e);
            }
        } finally {
            // 去锁
            if (lock != null) {
                lock.unlock();
            }
        }
        return null;
    }

    /**
     * 插件卸载
     * 判断插件是否已安装：插件未安装，不做处理
     * 插件已安装，正在运行，则记录“卸载状态”，推迟到到主程序进程重启的时执行卸载
     * 插件已安装，未在运行，则直接删除Dex、Native库等资源
     *
     * @param pluginName
     * @return 插件卸载成功与否
     */
    public static final boolean pluginUninstall(String pluginName) {
        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "MP.pluginUninstall ... pluginName=" + pluginName);
        }
        PluginInfo pi = getPlugin(pluginName, true);

        // 插件未安装
        if (pi == null) {
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "Not installed. pluginName=" + pluginName);
            }
            return true;
        }

        try {
            return PluginProcessMain.getPluginHost().pluginUninstalled(pi);
        } catch (Throwable e) {
            if (LOGR) {
                LogRelease.e(PLUGIN_TAG, "uninstall. error: " + e.getMessage(), e);
            }
        }

        return false;
    }

    /**
     * @param path
     * @return
     */
    public static final boolean pluginExtracted(String path) {
        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "MP.pluginExtracted ... path=" + path);
        }
        try {
            return PluginProcessMain.getPluginHost().pluginExtracted(path);
        } catch (Throwable e) {
            if (LOGR) {
                LogRelease.e(PLUGIN_TAG, "mp.peed: " + e.getMessage(), e);
            }
        }
        return false;
    }

    /**
     * 获取当前所有插件信息快照。内部框架使用
     *
     * @param clone true：深拷贝 false：浅拷贝
     * @return
     */
    public static final List<PluginInfo> getPlugins(boolean clone) {
        ArrayList<PluginInfo> array = new ArrayList<>();
        Set<String> pathSet = new HashSet<>();

        synchronized (PluginTable.PLUGINS) {
            for (PluginInfo info : PluginTable.PLUGINS.values()) {
                String path = info.getPath();

                // 避免加了两次，毕竟包名和别名都会加进来
                if (!pathSet.contains(path)) {
                    pathSet.add(path);

                    PluginInfo addTo;
                    if (clone) {
                        addTo = (PluginInfo) info.clone();
                    } else {
                        addTo = info;
                    }

                    array.add(addTo);
                }
            }
        }
        return array;
    }

    /**
     * 获取某个插件信息快照。内部框架使用
     * @return
     */
    public static final PluginInfo getPlugin(String name, boolean clone) {
        synchronized (PluginTable.PLUGINS) {
            PluginInfo info = PluginTable.PLUGINS.get(name);
            if (clone && info != null) {
                // 防止外界可以修改PluginTable表中的元素，故对外必须Clone一份
                return (PluginInfo) info.clone();
            } else {
                return info;
            }
        }
    }

    /**
     * 注：内部接口
     *
     * @return
     */
    public static final int sumActivities() {
        int rc = 0;
        rc = PluginProcessMain.sumActivities();
        if (LOG) {
            LogDebug.d(MAIN_TAG, "MP.sumActivities = " + rc);
        }
        return rc;
    }

    /**
     * 注：内部接口
     *
     * @return
     */
    public static final int sumBinders() {
        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "MP.sumBinders ... index=" + PluginManager.sPluginProcessIndex);
        }
        try {
            return PluginProcessMain.getPluginHost().sumBinders(PluginManager.sPluginProcessIndex);
        } catch (Throwable e) {
            if (LOGR) {
                LogRelease.e(PLUGIN_TAG, "mp.s.b: " + e.getMessage(), e);
            }
        }
        return -2;
    }

    /**
     * 根据Activity坑的名称，返回对应的Activity对象
     *
     * @param container Activity坑名称
     * @return 插件名 + 插件Activity名 + 启动时间
     */
    public static final String[] resolvePluginActivity(String container) {
        return PluginContainers.resolvePluginActivity(container);
    }

    /**
     * 检测卫士进程是否正在运行
     *
     * @param name 进程名（全名）
     * @return
     */
    public static final boolean isMsProcessAlive(String name) {
        try {
            return PluginProcessMain.getPluginHost().isProcessAlive(name);
        } catch (Throwable e) {
            if (LOGR) {
                LogRelease.e(PLUGIN_TAG, "mp.i.p.a: " + e.getMessage(), e);
            }
        }
        return false;
    }

    /**
     * 注：内部接口
     *
     * @param plugin
     * @param process
     * @param info
     * @return
     * @throws RemoteException
     * @hide 内部框架使用
     */
    public static final IPluginClient startPluginProcess(String plugin, int process, PluginBinderInfo info) throws RemoteException {
        return PluginProcessMain.getPluginHost().startPluginProcess(plugin, process, info);
    }

    /**
     * 根据 taskAffinity，判断应该取第几组 TaskAffinity
     * 由于 taskAffinity 是跨进程的属性，所以这里要将 taskAffinityGroup 的数据保存在常驻进程中
     * @param taskAffinity
     * @return 索引值
     */
    public static int getTaskAffinityGroupIndex(String taskAffinity) throws RemoteException {
        return PluginProcessMain.getPluginHost().getTaskAffinityGroupIndex(taskAffinity);
    }

    public static final class PluginBinder {

        public final String plugin;

        public final String name;

        public final int pid;

        public final IBinder binder;

        PluginBinder(String plugin, String name, int pid, IBinder binder) {
            this.plugin = plugin;
            this.name = name;
            this.binder = binder;
            this.pid = pid;
        }
    }
}
