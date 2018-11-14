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

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.qihoo360.i.IPluginManager;
import com.qihoo360.replugin.base.IPC;
import com.qihoo360.replugin.component.process.PluginProcessHost;
import com.qihoo360.replugin.helper.LogDebug;
import com.qihoo360.replugin.helper.LogRelease;
import com.qihoo360.replugin.model.PluginInfo;
import com.qihoo360.replugin.packages.PluginManagerProxy;
import com.qihoo360.replugin.packages.PluginManagerServer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.qihoo360.replugin.helper.LogDebug.LOG;
import static com.qihoo360.replugin.helper.LogDebug.MAIN_TAG;
import static com.qihoo360.replugin.helper.LogDebug.PLUGIN_TAG;
import static com.qihoo360.replugin.helper.LogRelease.LOGR;

/**
 * 进程管理类
 * @author RePlugin Team
 */
public class PluginProcessMain {

    public static final String TAG = PluginProcessMain.class.getSimpleName();
    /**
     * 常驻进程使用，非常驻进程为null buyuntao
     */
    private static IPluginHost sPluginHostLocal;

    /**
     * 非常驻进程使用，常驻进程为null，用于非常驻进程连接常驻进程  buyuntao
     */
    private static IPluginHost sPluginHostRemote;
    /**
     * 提供binder保存功能，无其他逻辑
     */
    static HashMap<String, IBinder> sBinders = new HashMap<String, IBinder>();
    /**
     * 当前运行的所有进程的列表（常驻进程除外）
     */
    private static final Map<String, ProcessClientRecord> ALL = new HashMap<String, ProcessClientRecord>();
    /**
     * ALL的读写锁，用于并发时的性能提升
     */
    private static final ReentrantReadWriteLock PROCESS_CLIENT_LOCK = new ReentrantReadWriteLock();
    private static final Object COOKIE_LOCK = new Object();
    private static boolean sPersisistCookieInitialized;
    /**
     * 常驻进程cookie，用来控制卫士进程组是否需要退出等
     */
    private static long sPersisistCookie;

    /**
     * 进程记录，用于进程及进程列表管理 buyuntao
     */
    private static final class ProcessClientRecord implements IBinder.DeathRecipient {

        String name; //进程名称
        String plugin;
        int pid;
        int index;
        IBinder binder;
        IPluginClient client;
        PluginManagerServer pluginManager; //单个进程的插件管理类

        public ProcessClientRecord(String process, String plugin, int pid, int index, IBinder binder, IPluginClient client, PluginManagerServer pms) {
            this.name = process;
            this.plugin = plugin;
            this.pid = pid;
            this.index = index;
            this.binder = binder;
            this.client = client;
            this.pluginManager = pms;
        }

        @Override
        public void binderDied() {
            handleBinderDied(this);
        }

        @Override
        public String toString() {
            if (LOG) {
                return super.toString() + " {name=" + name + " plugin=" + plugin + " pid=" + pid + " index=" + index + " binder=" + binder + " client=" + client + "}";
            }
            return super.toString();
        }

        public IPluginClient getClient() {
            return client;
        }
    }
    static final String dump() {

        // 1.dump Activity映射表, service列表
        JSONArray activityArr = new JSONArray();
        JSONArray serviceArr = new JSONArray();

        for (ProcessClientRecord clientRecord : ALL.values()) {
            try {
                IPluginClient pluginClient = clientRecord.getClient();
                if (pluginClient == null) {
                    continue;
                }

                String activityDumpInfo = pluginClient.dumpActivities();
                if (!TextUtils.isEmpty(activityDumpInfo)) {
                    JSONArray activityList = new JSONArray(activityDumpInfo);
                    int activityCount = activityList.length();
                    if (activityCount > 0) {
                        for (int i = 0; i < activityCount; i++) {
                            activityArr.put(activityList.getJSONObject(i));
                        }
                    }
                }

                String serviceDumpInfo = pluginClient.dumpServices();
                if (!TextUtils.isEmpty(serviceDumpInfo)) {
                    JSONArray serviceList = new JSONArray(serviceDumpInfo);
                    int serviceCount = serviceList.length();
                    if (serviceCount > 0) {
                        for (int i = 0; i < serviceCount; i++) {
                            serviceArr.put(serviceList.getJSONObject(i));
                        }
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        // 2.dump 插件信息表
        JSONArray pluginArr = new JSONArray();
        List<PluginInfo> pluginList = MP.getPlugins(false);
        if (pluginList != null) {
            JSONObject pluginObj;
            for (PluginInfo pluginInfo : pluginList) {
                try {
                    pluginObj = new JSONObject();
                    pluginObj.put(pluginInfo.getName(), pluginInfo.toString());
                    pluginArr.put(pluginObj);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        JSONObject detailObj = new JSONObject();
        try {
            detailObj.put("activity", activityArr);
            detailObj.put("service", serviceArr);
            detailObj.put("plugin", pluginArr);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return detailObj.toString();
    }

    static final void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        if (LogDebug.DUMP_ENABLED) {
            writer.println("--- ALL.length = " + ALL.size() + " ---");
            for (ProcessClientRecord r : ALL.values()) {
                writer.println(r);
            }
            writer.println();
            StubProcessManager.dump(writer);
            writer.println();
//            writer.println("--- USED_PLUGINS.size = " + USED_PLUGINS.size() + " ---");
//            for (ProcessPluginInfo r : USED_PLUGINS.values()) {
//                writer.println(r);
//            }
            writer.println();
            PluginTable.dump(fd, writer, args);
        }
    }

    /**
     * 常驻进程调用，缓存自己的 IPluginHost
     */
    static final void installHost(IPluginHost host) {
        sPluginHostLocal = host;
        // 连接到插件化管理器的服务端
        // Added by Jiongxuan Zhang
        try {
            PluginManagerProxy.connectToServer(sPluginHostLocal);
        } catch (RemoteException e) {
            // 基本不太可能到这里，直接打出日志
            if (LOGR) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 非常驻进程调用，获取常驻进程的 IPluginHost
     */
    static final void connectToHostSvc() {
        Context context = PMF.getApplicationContext();
        IBinder binder = PluginProviderStub.proxyFetchHostBinder(context);
        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "host binder = " + binder);
        }
        if (binder == null) {
            // 无法连接到常驻进程，当前进程自杀
            if (LOGR) {
                LogRelease.e(PLUGIN_TAG, "p.p fhb fail");
            }
            System.exit(1);
        }
        try {
            binder.linkToDeath(new IBinder.DeathRecipient() {
                @Override
                public void binderDied() {
                    if (LOGR) {
                        LogRelease.i(PLUGIN_TAG, "p.p d, p.h s n");
                    }
                    // 检测到常驻进程退出，插件进程自杀
                    if (PluginManager.isPluginProcess()) {
                        if (LOGR) {
                            // persistent process exception, PLUGIN process quit now
                            LogRelease.i(MAIN_TAG, "p p e, pp q n");
                        }
                        System.exit(0);
                    }
                    sPluginHostRemote = null;

                    // 断开和插件化管理器服务端的连接，因为已经失效
                    PluginManagerProxy.disconnect();
                }
            }, 0);
        } catch (RemoteException e) {
            // 无法连接到常驻进程，当前进程自杀
            if (LOGR) {
                LogRelease.e(PLUGIN_TAG, "p.p p.h l2a: " + e.getMessage(), e);
            }
            System.exit(1);
        }

        //
        sPluginHostRemote = IPluginHost.Stub.asInterface(binder);
        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "host binder.i = " + PluginProcessMain.sPluginHostRemote);
        }

        // 连接到插件化管理器的服务端
        // Added by Jiongxuan Zhang
        try {
            PluginManagerProxy.connectToServer(sPluginHostRemote);

            // 将当前进程的"正在运行"列表和常驻做同步
            // TODO 若常驻进程重启，则应在启动时发送广播，各存活着的进程调用该方法来同步
            PluginManagerProxy.syncRunningPlugins();
        } catch (RemoteException e) {
            // 获取PluginManagerServer时出现问题，可能常驻进程突然挂掉等，当前进程自杀
            if (LOGR) {
                LogRelease.e(PLUGIN_TAG, "p.p p.h l3a: " + e.getMessage(), e);
            }
            System.exit(1);
        }

        // 注册该进程信息到“插件管理进程”中
        PMF.sPluginMgr.attach();
    }
    /**
     * sPluginHostLocal 常驻进程使用，非常驻进程为null buyuntao
     * sPluginHostRemote 非常驻进程使用，常驻进程为null，用于非常驻进程连接常驻进程  buyuntao
     * @hide 内部框架使用
     */
    public static final IPluginHost getPluginHost() {
        if (sPluginHostLocal != null) {
            return sPluginHostLocal;
        }
        // 可能是第一次，或者常驻进程退出了
        if (sPluginHostRemote == null) {
            if (LogDebug.LOG) {
                if (IPC.isPersistentProcess()) {
                    LogDebug.e(PLUGIN_TAG, "插件框架未正常初始化");
                    throw new RuntimeException("插件框架未正常初始化");
                }
            }
            // 再次唤起常驻进程
            connectToHostSvc();
        }
        return sPluginHostRemote;
    }

    static final long getPersistentCookie() {
        synchronized (COOKIE_LOCK) {
            if (!sPersisistCookieInitialized) {
                sPersisistCookieInitialized = true;
                if (IPC.isPersistentProcess()) {
                    sPersisistCookie = System.currentTimeMillis();
                    if (LOG) {
                        LogDebug.d(PLUGIN_TAG, "generate cookie: " + sPersisistCookie);
                    }
                }
            }
            return sPersisistCookie;
        }
    }

    /**
     * @param plugin
     * @param process
     * @param info
     * @return
     */
    static final IPluginClient probePluginClient(final String plugin, final int process, final PluginBinderInfo info) {
        return readProcessClientLock(new Action<IPluginClient>() {
            @Override
            public IPluginClient call() {
                for (ProcessClientRecord r : ALL.values()) {
                    if (process == IPluginManager.PROCESS_UI) {
                        if (!TextUtils.equals(r.plugin, Constant.PLUGIN_NAME_UI)) {
                            continue;
                        }

                        /* 是否是用户自定义进程 */
                    } else if (PluginProcessHost.isCustomPluginProcess(process)) {
                        if (!TextUtils.equals(r.plugin, getProcessStringByIndex(process))) {
                            continue;
                        }
                    } else {
                        if (!TextUtils.equals(r.plugin, plugin)) {
                            continue;
                        }
                    }
                    if (!isBinderAlive(r)) {
                        return null;
                    }
                    if (!r.binder.pingBinder()) {
                        return null;
                    }
                    info.pid = r.pid;
                    info.index = r.index;
                    return r.client;
                }
                return null;
            }
        });
    }

    /**
     * 根据进程索引，取进程名称标识
     *
     * @param index -99
     * @return :p1
     */
    private static String getProcessStringByIndex(int index) {
        return PluginProcessHost.PROCESS_PLUGIN_SUFFIX2 + (index - PluginProcessHost.PROCESS_INIT);
    }

    /**
     * @param pid
     * @param info
     * @return
     */
    static final IPluginClient probePluginClientByPid(final int pid, final PluginBinderInfo info) {
        return readProcessClientLock(new Action<IPluginClient>() {
            @Override
            public IPluginClient call() {
                for (ProcessClientRecord r : ALL.values()) {
                    if (r.pid != pid) {
                        continue;
                    }
                    if (!isBinderAlive(r)) {
                        return null;
                    }
                    if (!r.binder.pingBinder()) {
                        return null;
                    }
                    info.pid = r.pid;
                    info.index = r.index;
                    return r.client;
                }
                return null;
            }
        });
    }

    /**
     * 发送intent给进程 buyuntao
     * @param target
     * @param intent
     */
    static final void sendIntent2Process(final String target, Intent intent, boolean sync) {
        final Map<String, ProcessClientRecord> map = readProcessClientLock(new Action<Map<String, ProcessClientRecord>>() {
            @Override
            public Map<String, ProcessClientRecord> call() {
                Map<String, ProcessClientRecord> map = new HashMap<>();
                for (ProcessClientRecord r : ALL.values()) {
                    if (TextUtils.isEmpty(target)) {
                        // 所有
                    } else if (TextUtils.equals(r.name, target)) {
                        // 特定目标
                    } else {
                        continue;
                    }
                    map.put(r.name, r);
                }
                return map;
            }
        });
        sendIntent2Client(map, intent, sync);
    }

    /**
     * 发送intent给指定插件 buyuntao
     * @param target
     * @param intent
     */
    static final void sendIntent2Plugin(final String target, Intent intent, boolean sync) {
        if (TextUtils.isEmpty(target)) {
            return;
        }
        final Map<String, ProcessClientRecord> map = readProcessClientLock(new Action<Map<String, ProcessClientRecord>>() {
            @Override
            public Map<String, ProcessClientRecord> call() {
                final Map<String, ProcessClientRecord> map = new HashMap<>(1 << 4);
                for (ProcessClientRecord r : ALL.values()) {
                    if (TextUtils.equals(r.plugin, target)) {
                        // 特定目标
                    } else {
                        continue;
                    }
                    map.put(r.name, r);
                }
                return map;
            }
        });
        sendIntent2Client(map, intent, sync);
    }
    /**
     * 发送intent给进程Client buyuntao
     * @param map
     * @param intent
     */
    private static void sendIntent2Client(Map<String, ProcessClientRecord> map, Intent intent, boolean sync){
        for (ProcessClientRecord r : map.values()) {
            if (!isBinderAlive(r)) {
                continue;
            }
            try {
                if (sync) {
                    r.client.sendIntentSync(intent);
                } else {
                    r.client.sendIntent(intent);
                }
            } catch (Throwable e) {
                if (LOGR) {
                    LogRelease.e(PLUGIN_TAG, "p.p sic e: " + e.getMessage(), e);
                }
            }
        }
    }

    /**
     * 判断进程是否存活 buyuntao
     * @param name
     * @return
     */
    static final boolean isProcessAlive(final String name) {
        if (TextUtils.isEmpty(name)){
            return false;
        }
        return readProcessClientLock(new Action<Boolean>() {
            @Override
            public Boolean call() {
                ProcessClientRecord r = ALL.get(name);
                return isBinderAlive(r);
            }
        });
    }

    private static boolean isBinderAlive(ProcessClientRecord r) {
        return r != null && r.binder != null && r.client != null && r.binder.isBinderAlive();
    }

    static final int sumActivities() {
        return readProcessClientLock(new Action<Integer>() {
            @Override
            public Integer call() {
                int sum = 0;
                for (ProcessClientRecord r : ALL.values()) {
                    if (!isBinderAlive(r)) {
                        continue;
                    }
                    int rc = 0;
                    try {
                        rc = r.client.sumActivities();
                        if (rc == -1) {
                            return -1;
                        }
                        sum += rc;
                    } catch (Throwable e) {
                        if (LOGR) {
                            LogRelease.e(PLUGIN_TAG, "ppm.sa e: " + e.getMessage(), e);
                        }
                    }
                }
                return sum;
            }
        });
    }

    /**
     * @param plugin
     * @param process
     * @return
     * @deprecated 待优化
     * 插件进程调度
     */
    @Deprecated
    static final int allocProcess(String plugin, int process) {
        if (Constant.PLUGIN_NAME_UI.equals(plugin) || process == IPluginManager.PROCESS_UI) {
            return IPluginManager.PROCESS_UI;
        }

        if (PluginProcessHost.isCustomPluginProcess(process)) {
            return process;
        }

        PluginInfo info = PluginTable.getPluginInfo(plugin);
        if (info == null) {
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "alloc process: plugin not found: name=" + plugin);
            }
            return IPluginManager.PROCESS_AUTO;
        }
        return StubProcessManager.allocProcess(plugin);

    }

    /**
     * 常驻进程调用,添加进程信息到进程管理列表
     * @param pid
     * @param process
     * @param index
     * @param binder
     * @param client
     * @return 进程的默认插件名称（非框架内的进程返回null）
     */
    static final String attachProcess(int pid, String process, int index, IBinder binder, IPluginClient client, String def, PluginManagerServer pms) {
        final String plugin = getDefaultPluginName(pid, index, binder, client, def);
        final ProcessClientRecord pr = new ProcessClientRecord(process, plugin, pid, index, binder, client, pms);
        try {
            pr.binder.linkToDeath(pr, 0);
        } catch (Throwable e) {
            if (LOGR) {
                LogRelease.e(PLUGIN_TAG, "ap l2d: " + e.getMessage(), e);
            }
        }
        writeProcessClientLock(new Action<Void>() {
            @Override
            public Void call() {
                ALL.put(pr.name, pr);
                return null;
            }
        });
        return plugin;
    }

    /**
     * @param pid
     * @param index
     * @param plugin
     * @param activity
     * @param container
     * @return
     */
    static final boolean attachActivity(int pid, int index, String plugin, String activity, String container) {
        return StubProcessManager.attachActivity(pid, index, plugin, activity, container);
    }

    /**
     * @param pid
     * @param index
     * @param plugin
     * @param activity
     * @param container
     * @return
     */
    static final boolean detachActivity(int pid, int index, String plugin, String activity, String container) {
        return StubProcessManager.detachActivity(pid, index, plugin, activity, container);
    }

    /**
     * @param pid
     * @param index
     * @param plugin
     * @param service
     * @return
     */
    static final boolean attachService(int pid, int index, String plugin, String service) {
        return StubProcessManager.attachService(pid, index, plugin, service);
    }

    /**
     * @param pid
     * @param index
     * @param plugin
     * @param service
     * @return
     */
    static final boolean detachService(int pid, int index, String plugin, String service) {
        return StubProcessManager.detachService(pid, index, plugin, service);
    }

    static final void attachBinder(int pid, IBinder binder) {
        StubProcessManager.attachBinder(pid, binder);
    }

    static final void detachBinder(int pid, IBinder binder) {
        StubProcessManager.detachBinder(pid, binder);
    }

    static final int sumBinders(int index) {
        return StubProcessManager.sumBinders(index);
    }

    //change by buyuntao
    static final int getPidByProcessName(final String processName) {
        if (TextUtils.isEmpty(processName)){
            return -1;
        }
        // 获取的是常驻进程自己？直接返回
        if (TextUtils.equals(processName, IPC.getCurrentProcessName())) {
            return IPC.getCurrentProcessId();
        }
        // 在“进程列表”中寻找“线索”
        return readProcessClientLock(new Action<Integer>() {
            @Override
            public Integer call() {
                ProcessClientRecord r = ALL.get(processName);
                if (r != null && isBinderAlive(r)) {
                    return r.pid;
                }
                return -1;
            }
        });
    }

    // Added by Jiongxuan Zhang
    static final String getProcessNameByPid(final int pid) {
        // 获取的是常驻进程自己？直接返回
        if (pid == IPC.getCurrentProcessId()) {
            return IPC.getCurrentProcessName();
        }
        return readProcessClientLock(new Action<String>() {
            @Override
            public String call() {
                for (ProcessClientRecord r : ALL.values()) {
                    if (r.pid != pid) {
                        continue;
                    }
                    if (!isBinderAlive(r)) {
                        continue;
                    }
                    return r.name;
                }
                return null;
            }
        });
    }

    private static final void handleBinderDied(ProcessClientRecord p) {
        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "plugin process has died: plugin=" + p.plugin + " index=" + p.index + " pid=" + p.pid);
        }
        handleBinderDiedLocked(p);
    }

    /**
     * 获取进程的默认插件名
     * @param pid
     * @param index
     * @param binder
     * @param client
     * @param def
     * @return
     */
    private static final String getDefaultPluginName(int pid, int index, IBinder binder, IPluginClient client, String def) {
        if (index == IPluginManager.PROCESS_UI) {
            return Constant.PLUGIN_NAME_UI;
        }
        /* 是否是用户自定义进程 */
        if (PluginProcessHost.isCustomPluginProcess(index)) {
            return getProcessStringByIndex(index);
        }
        if (PluginManager.isPluginProcess(index)) {
            return StubProcessManager.attachStubProcess(pid, index, binder, client, def);
        }
        return null;
    }


    /**
     * 进程结束后执行操作 buyuntao
     * @param p
     */
    private static final void handleBinderDiedLocked(final ProcessClientRecord p) {
        if (p == null){
            return;
        }
        writeProcessClientLock(new Action<Void>() {
            @Override
            public Void call() {
                ProcessClientRecord r = ALL.get(p.name);
                if (r == p){ //防止进程重启误判
                    ALL.remove(r.name);
                }
                return null;
            }
        });

        StubProcessManager.setProcessStop(p.binder);

        // 通知 PluginManagerServer 客户端进程链接已断开
        p.pluginManager.onClientProcessKilled(p.name);
    }

    ///

    private static <T> T writeProcessClientLock(@NonNull final Action<T> action) {
        final long start = System.currentTimeMillis();
//        final String stack = OptUtil.stack2Str(Thread.currentThread().getStackTrace()[3]);
        try {
            PROCESS_CLIENT_LOCK.writeLock().lock();
            if (LogDebug.LOG) {
                Log.d(TAG, String.format("%s(%sms@%s) WRITING", Thread.currentThread().getStackTrace()[3], System.currentTimeMillis() - start, Thread.currentThread()));
            }
            return action.call();
        } finally {
            PROCESS_CLIENT_LOCK.writeLock().unlock();
            if (LogDebug.LOG) {
                Log.d(TAG, String.format("%s(%sms@%s) WRITING DONE", Thread.currentThread().getStackTrace()[3], System.currentTimeMillis() - start, Thread.currentThread()));
            }
        }
    }

    private static <T> T readProcessClientLock(@NonNull final Action<T> action) {
        final long start = System.currentTimeMillis();
//        final String stack = OptUtil.stack2Str(Thread.currentThread().getStackTrace()[3]);
        try {
            PROCESS_CLIENT_LOCK.readLock().lock();
            if (LogDebug.LOG) {
                Log.d(TAG, String.format("%s(%sms@%s) READING", Thread.currentThread().getStackTrace()[3], System.currentTimeMillis() - start, Thread.currentThread()));
            }
            return action.call();
        } finally {
            PROCESS_CLIENT_LOCK.readLock().unlock();
            if (LogDebug.LOG) {
                Log.d(TAG, String.format("%s(%sms@%s) READING DONE", Thread.currentThread().getStackTrace()[3], System.currentTimeMillis() - start, Thread.currentThread()));
            }
        }
    }

    private interface Action<T> {
        T call();
    }
}
