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

import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;

import com.qihoo360.i.IPluginManager;
import com.qihoo360.mobilesafe.api.Tasks;
import com.qihoo360.replugin.RePluginInternal;
import com.qihoo360.replugin.base.AMSUtils;
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

import static com.qihoo360.replugin.helper.LogDebug.LOG;
import static com.qihoo360.replugin.helper.LogDebug.MAIN_TAG;
import static com.qihoo360.replugin.helper.LogDebug.PLUGIN_TAG;
import static com.qihoo360.replugin.helper.LogRelease.LOGR;

/**
 * @author RePlugin Team
 */
public class PluginProcessMain {

    private static final int STATE_UNUSED = 0;

    private static final int STATE_ALLOCATED = 1;

    private static final int STATE_RUNNING = 2;

//    private static final int STATE_MARKED = 3;

    private static final int STATE_STOPED = 4;

    /**
     *
     */
    private static IPluginHost sPluginHostLocal;

    /**
     *
     */
    private static IPluginHost sPluginHostRemote;

    /**
     *
     */
    static HashMap<String, IBinder> sBinders = new HashMap<String, IBinder>();

    /**
     * TODO 待重构 PROCESSES & ALL
     */
    private static final ProcessRecord PROCESSES[] = new ProcessRecord[Constant.STUB_PROCESS_COUNT];

    /**
     * TODO 待重构 PROCESSES & ALL
     * processName -> ProcessClientRecord
     */
    private static final Map<String, ProcessClientRecord> ALL = new HashMap<String, ProcessClientRecord>();

    static {
        for (int i = 0; i < Constant.STUB_PROCESS_COUNT; i++) {
            ProcessRecord r = new ProcessRecord(i, STATE_UNUSED);
            PROCESSES[i] = r;
        }
    }

    /**
     *
     */
    private static final Object COOKIE_LOCK = new Object();

    /**
     *
     */
    private static boolean sPersisistCookieInitialized;

    /**
     * 常驻进程cookie，用来控制卫士进程组是否需要退出等
     */
    private static long sPersisistCookie;

    static final int CHECK_STAGE1_DELAY = 17 * 1000;

    private static final int CHECK_STAGE2_DELAY = 11 * 1000;

    private static final int CHECK_STAGE3_DELAY = 3 * 1000;

    private static final Runnable CHECK = new Runnable() {

        @Override
        public void run() {
            doPluginProcessLoop();
        }
    };

    private static final class ProcessRecord {

        final int index;

        int state;

        long mobified;

        String plugin;

        int pid;

        IBinder binder;

        IPluginClient client;

        int activities;

        int services;

        int binders;

        ProcessRecord(int index, int state) {
            this.index = index;
            this.state = state;
        }

        void allocate(String plugin) {
            this.state = STATE_ALLOCATED;
            this.mobified = System.currentTimeMillis();
            this.plugin = plugin;
            this.pid = 0;
            this.binder = null;
            this.client = null;
            this.activities = 0;
            this.services = 0;
            this.binders = 0;
        }

        void setRunning(int pid) {
            this.state = STATE_RUNNING;
            this.pid = pid;
        }

        void setClient(IBinder binder, IPluginClient client) {
            this.binder = binder;
            this.client = client;
        }

//        void setMarked() {
//            this.state = STATE_MARKED;
//        }

        void setStoped() {
            this.state = STATE_STOPED;
            this.pid = 0;
            this.binder = null;
            this.client = null;
        }

        @Override
        public String toString() {
            if (LOG) {
                return super.toString() + " {index=" + index + " state=" + state + " mobified=" + mobified + " plugin=" + plugin + " pid=" + pid + " binder=" + binder + " client=" + client
                        + " activities=" + activities + " services=" + services + " binders=" + binders + "}";
            }
            return super.toString();
        }
    }

    /**
     * 常驻进程使用
     */
    private static final class ProcessClientRecord implements IBinder.DeathRecipient {

        String name;

        String plugin;

        int pid;

        int index;

        IBinder binder;

        IPluginClient client;

        private final PluginManagerServer mManagerServer;

        // FIXME 不建议这么传递，但为了在死亡周期中使用，不得已而为之
        public ProcessClientRecord(PluginManagerServer pms) {
            mManagerServer = pms;
        }

        @Override
        public void binderDied() {
            handleBinderDied(this, mManagerServer);
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

    static final void reportStatus() {
        for (ProcessRecord r : PROCESSES) {
            if (r.binder == null) {
                continue;
            }
            if (LOG) {
                LogDebug.i(PLUGIN_TAG, "i=" + r.index + " p=" + r.plugin + " a=" + r.activities + " s=" + r.services + " b=" + r.binders);
            }
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
            writer.println("--- PROCESSES.length = " + PROCESSES.length + " ---");
            for (ProcessRecord r : PROCESSES) {
                writer.println(r);
            }
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

        //
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

        //
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

    // @hide 内部框架使用
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
    static final IPluginClient probePluginClient(String plugin, int process, PluginBinderInfo info) {
        synchronized (PROCESSES) {
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
        }
        return null;
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
    static final IPluginClient probePluginClientByPid(int pid, PluginBinderInfo info) {
        synchronized (PROCESSES) {
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
        }
        return null;
    }

    /**
     * @param target
     * @param intent
     */
    static final void sendIntent2Process(String target, Intent intent, boolean sync) {
        synchronized (PROCESSES) {
            for (ProcessClientRecord r : ALL.values()) {
                if (target == null || target.length() <= 0) {
                    // 所有
                } else if (TextUtils.equals(r.name, target)) {
                    // 特定目标
                } else {
                    continue;
                }
                if (!isBinderAlive(r)) {
                    continue;
                }
                if (LOG) {
                    LogDebug.d(PLUGIN_TAG, "sendIntent2Process name=" + r.name);
                }
                try {
                    if (sync) {
                        r.client.sendIntentSync(intent);
                    } else {
                        r.client.sendIntent(intent);
                    }
                } catch (Throwable e) {
                    if (LOGR) {
                        LogRelease.e(PLUGIN_TAG, "s.i2pr e: n=" + r.name + ": " + e.getMessage(), e);
                    }
                }
            }
        }
    }

    /**
     * @param target
     * @param intent
     */
    static final void sendIntent2Plugin(String target, Intent intent, boolean sync) {
        if (target == null || target.length() <= 0) {
            return;
        }
        synchronized (PROCESSES) {
            for (ProcessClientRecord r : ALL.values()) {
                if (TextUtils.equals(r.plugin, target)) {
                    // 特定目标
                } else {
                    continue;
                }
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
                        LogRelease.e(PLUGIN_TAG, "s.i2pl e: " + e.getMessage(), e);
                    }
                }
            }
        }
    }

    /**
     * @param name
     * @return
     */
    static final boolean isProcessAlive(String name) {
        synchronized (PROCESSES) {
            for (ProcessClientRecord r : ALL.values()) {
                if (!TextUtils.equals(r.name, name)) {
                    continue;
                }
                return isBinderAlive(r);
            }
        }
        return false;
    }

    private static boolean isBinderAlive(ProcessClientRecord r) {
        if (r == null) {
            return false;
        }
        if (r.binder == null || r.client == null) {
            return false;
        }
        if (!r.binder.isBinderAlive()) {
            return false;
        }
        return true;
    }

    static final int sumActivities() {
        int sum = 0;
        synchronized (PROCESSES) {
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
        }
        return sum;
    }

    /**
     * @deprecated 待优化
     * 插件进程调度
     * @param plugin
     * @param process
     * @return
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

        synchronized (PROCESSES) {
            return allocProcessLocked(plugin);
        }
    }

    /**
     * 常驻进程调用
     * @param pid
     * @param process
     * @param index
     * @param binder
     * @param client
     * @return
     */
    static final String attachProcess(int pid, String process, int index, IBinder binder, IPluginClient client, String def, PluginManagerServer pms) {
        synchronized (PROCESSES) {
            String plugin = attachProcessLocked(pid, process, index, binder, client, def);

            ProcessClientRecord pr = new ProcessClientRecord(pms);
            pr.name = process;
            pr.plugin = plugin;
            pr.pid = pid;
            pr.index = index;
            pr.binder = binder;
            pr.client = client;
            ALL.put(process, pr);
            try {
                pr.binder.linkToDeath(pr, 0);
            } catch (Throwable e) {
                if (LOGR) {
                    LogRelease.e(PLUGIN_TAG, "ap l2d: " + e.getMessage(), e);
                }
            }

            return plugin;
        }
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
        synchronized (PROCESSES) {
            return regActivityLocked(pid, index, plugin, activity, container);
        }
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
        synchronized (PROCESSES) {
            return unregActivityLocked(pid, index, plugin, activity, container);
        }
    }

    /**
     * @param pid
     * @param index
     * @param plugin
     * @param service
     * @return
     */
    static final boolean attachService(int pid, int index, String plugin, String service) {
        synchronized (PROCESSES) {
            return regServiceLocked(pid, index, plugin, service);
        }
    }

    /**
     * @param pid
     * @param index
     * @param plugin
     * @param service
     * @return
     */
    static final boolean detachService(int pid, int index, String plugin, String service) {
        synchronized (PROCESSES) {
            return unregServiceLocked(pid, index, plugin, service);
        }
    }

    static final void attachBinder(int pid, IBinder binder) {
        synchronized (PROCESSES) {
            regBinderLocked(pid, binder);
        }
    }

    static final void detachBinder(int pid, IBinder binder) {
        synchronized (PROCESSES) {
            unregBinderLocked(pid, binder);
        }
    }

    static final int sumBinders(int index) {
        synchronized (PROCESSES) {
            return sumBindersLocked(index);
        }
    }

    static final void schedulePluginProcessLoop(long delayMillis) {
        if (Constant.SIMPLE_QUIT_CONTROLLER) {
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "schedule plugin process quit check: delay=" + (delayMillis / 1000));
            }
            Tasks.cancelThreadTask(CHECK);
            Tasks.postDelayed2Thread(CHECK, delayMillis);
        }
    }

    static final void cancelPluginProcessLoop() {
        if (Constant.SIMPLE_QUIT_CONTROLLER) {
            Tasks.cancelThreadTask(CHECK);
        }
    }

    // Added by Jiongxuan Zhang
    static final int getPidByProcessName(String processName) {
        // 获取的是常驻进程自己？直接返回
        if (TextUtils.equals(processName, IPC.getCurrentProcessName())) {
            return IPC.getCurrentProcessId();
        }

        // 在“进程列表”中寻找“线索”
        synchronized (PROCESSES) {
            for (ProcessClientRecord r : ALL.values()) {
                if (!TextUtils.equals(r.name, processName)) {
                    continue;
                }
                if (!isBinderAlive(r)) {
                    continue;
                }
                return r.pid;
            }
        }
        return -1;
    }

    // Added by Jiongxuan Zhang
    static final String getProcessNameByPid(int pid) {
        // 获取的是常驻进程自己？直接返回
        if (pid == IPC.getCurrentProcessId()) {
            return IPC.getCurrentProcessName();
        }

        synchronized (PROCESSES) {
            for (ProcessClientRecord r : ALL.values()) {
                if (r.pid != pid) {
                    continue;
                }
                if (!isBinderAlive(r)) {
                    continue;
                }
                return r.name;
            }
        }
        return null;
    }

    private static final void handleBinderDied(ProcessClientRecord p, PluginManagerServer pms) {
        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "plugin process has died: plugin=" + p.plugin + " index=" + p.index + " pid=" + p.pid);
        }
        synchronized (PROCESSES) {
            handleBinderDiedLocked(p, pms);
        }
    }

    /**
     * @deprecated 待优化
     * @param plugin
     * @return
     */
    @Deprecated
    private static final int allocProcessLocked(String plugin) {
        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "alloc plugin process: plugin=" + plugin);
        }

        // 取运行列表
        List<RunningAppProcessInfo> processes = AMSUtils.getRunningAppProcessesNoThrows(RePluginInternal.getAppContext());

        // 取运行列表失败，则直接返回失败
        if (processes == null || processes.isEmpty()) {
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "alloc plugin process: get running processes is empty");
                LogDebug.i(PLUGIN_TAG, "get list exception p=" + plugin);
            }
            return IPluginManager.PROCESS_AUTO;
        }

        updateListLocked(processes);

        // 找一个插件可能用过的进程
        for (ProcessRecord r : PROCESSES) {
            if (TextUtils.equals(plugin, r.plugin)) {
                if (LOG) {
                    LogDebug.d(PLUGIN_TAG, "alloc plugin process: found saved plugin process: index=" + r.index + " p=" + plugin);
                }
                // 标记为分配状态
                if (r.state == STATE_UNUSED || r.state == STATE_STOPED) {
                    r.allocate(plugin);
                    // 确保进程为空
                    int pid = lookupPluginProcess(processes, r.index);
                    if (pid > 0) {
                        if (LOGR) {
                            LogRelease.i(PLUGIN_TAG, "ppr k i: " + pid);
                        }
                        android.os.Process.killProcess(pid);
                        waitKilled(pid);
                    }
                }
                if (LOG) {
                    LogDebug.i(PLUGIN_TAG, "used st=" + r.state + " i=" + r.index + " p=" + plugin);
                }
                return r.index;
            }
        }

        // 没有找到，则找一个空闲的
        for (ProcessRecord r : PROCESSES) {
            if (r.state == STATE_UNUSED) {
                if (LOG) {
                    LogDebug.d(PLUGIN_TAG, "alloc plugin process: found unused plugin process: index=" + r.index);
                    LogDebug.i(PLUGIN_TAG, "free st=" + r.state + " i=" + r.index + " p=" + plugin + " orig.p=" + r.plugin);
                }
                // 标记为分配状态
                r.allocate(plugin);
                // 确保进程为空
                int pid = lookupPluginProcess(processes, r.index);
                if (pid > 0) {
                    if (LOGR) {
                        LogRelease.i(PLUGIN_TAG, "ppr k i: " + pid);
                    }
                    android.os.Process.killProcess(pid);
                    waitKilled(pid);
                }
                return r.index;
            }
        }

        // 没有找到，则找一个停止的
        for (ProcessRecord r : PROCESSES) {
            if (r.state == STATE_STOPED) {
                if (LOG) {
                    LogDebug.d(PLUGIN_TAG, "alloc plugin process: found stoped plugin process: index=" + r.index);
                    LogDebug.i(PLUGIN_TAG, "stoped st=" + r.state + " i=" + r.index + " orig.p=" + r.plugin);
                }
               // 标记为分配状态
                r.allocate(plugin);
                // 确保进程为空
                int pid = lookupPluginProcess(processes, r.index);
                if (pid > 0) {
                    if (LOGR) {
                        LogRelease.i(PLUGIN_TAG, "ppr k i: " + pid);
                    }
                    android.os.Process.killProcess(pid);
                    waitKilled(pid);
                }
                return r.index;
            }
        }

        // 分配之后，一定时间如果没被使用，则需要回收
        // 没有找到，则找一个最早分配中的，且分配了很久的
        {
            int i = -1;
            long mod = Long.MAX_VALUE;
            for (ProcessRecord r : PROCESSES) {
                if (r.state != STATE_ALLOCATED) {
                    continue;
                }
                if (r.mobified < mod) {
                    i = r.index;
                    mod = r.mobified;
                }
            }
            if (i >= 0 && (System.currentTimeMillis() - mod > 10 * 1000)) {
                ProcessRecord r = PROCESSES[i];
                if (LOG) {
                    LogDebug.d(PLUGIN_TAG, "alloc plugin process: plugin processes maybe busy, reuse process which allocating and expired: index=" + r.index);
                    LogDebug.i(PLUGIN_TAG, "force maybe st=" + r.state + " i=" + r.index + " orig.p=" + r.plugin);
                }
                //
                r.setStoped();
                //
                r.allocate(plugin);
                // 确保进程为空
                int pid = lookupPluginProcess(processes, r.index);
                if (pid > 0) {
                    if (LOGR) {
                        LogRelease.i(PLUGIN_TAG, "ppr k i: " + pid);
                    }
                    android.os.Process.killProcess(pid);
                    waitKilled(pid);
                }
                return r.index;
            }
        }

        // 没有找到，则找一个最先分配的组件为空的
        {
            int i = -1;
            long mod = Long.MAX_VALUE;
            for (ProcessRecord r : PROCESSES) {
                if (r.activities > 0) {
                    continue;
                }
                if (r.services > 0) {
                    continue;
                }
                if (r.binders > 0) {
                    continue;
                }
                if (r.mobified < mod) {
                    i = r.index;
                    mod = r.mobified;
                }
            }
            if (i >= 0) {
                ProcessRecord r = PROCESSES[i];
                if (LOG) {
                    LogDebug.d(PLUGIN_TAG, "alloc plugin process: plugin processes busy, reuse process which components is empty: index=" + r.index);
                }
                if (LOGR) {
                    LogRelease.e(PLUGIN_TAG, "ppr r & k i: " + r.pid);
                    LogRelease.i(PLUGIN_TAG, "force empty st=" + r.state + " i=" + r.index + " orig.p=" + r.plugin);
                }
                //
                android.os.Process.killProcess(r.pid);
                waitKilled(r.pid);
                //
                r.setStoped();
                //
                r.allocate(plugin);
                return r.index;
            }
        }

        // 还没有找到，则强制终止一个最先分配的
        {
            int i = 0;
            long mod = Long.MAX_VALUE;
            for (ProcessRecord r : PROCESSES) {
                if (r.mobified < mod) {
                    i = r.index;
                    mod = r.mobified;
                }
            }
            //
            ProcessRecord r = PROCESSES[i];
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "alloc plugin process: plugin processes busy, reuse process which earliest allocated: index=" + r.index);
            }
            if (LOGR) {
                LogRelease.e(PLUGIN_TAG, "ppr r & k i: " + r.pid);
                LogRelease.i(PLUGIN_TAG, "force earliest st=" + r.state + " i=" + r.index + " orig.p=" + r.plugin);
            }
            //
            android.os.Process.killProcess(r.pid);
            waitKilled(r.pid);
            //
            r.setStoped();
            //
            r.allocate(plugin);
            return r.index;
        }
    }

    private static final String attachProcessLocked(int pid, String process, int index, IBinder binder, IPluginClient client, String def) {
        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "attach process: pid=" + pid + " index=" + index + " binder=" + client);
        }

        if (index == IPluginManager.PROCESS_UI) {
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "attach process: ui");
            }
            return Constant.PLUGIN_NAME_UI;
        }

        /* 是否是用户自定义进程 */
        if (PluginProcessHost.isCustomPluginProcess(index)) {
            return getProcessStringByIndex(index);
        }

        if (!PluginManager.isPluginProcess(index)) {
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "attach process: invalid index=" + index);
            }
            return null;
        }

        // 检测状态是否一致
        ProcessRecord r = PROCESSES[index];
        if (!TextUtils.isEmpty(def)) {
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "attach process: allocate now");
            }
            r.allocate(def);
        }

        if (r.state != STATE_ALLOCATED) {
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "attach process: state not allocated: state=" + r.state);
            }
            return null;
        }

        r.setRunning(pid);
        r.setClient(binder, client);

        return r.plugin;
    }

    private static final boolean regActivityLocked(int pid, int index, String plugin, String activity, String container) {
        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "reg activity: pid=" + pid + " index=" + index + " plugin=" + plugin + " activity=" + activity + " container=" + container);
        }

        if (index < 0 || index >= PROCESSES.length) {
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "reg activity: invalid index=" + index);
            }
            return false;
        }

        ProcessRecord r = PROCESSES[index];
        r.activities++;
        r.mobified = System.currentTimeMillis();
        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "activities=" + r.activities + " services=" + r.services + " binders=" + r.binders);
        }

        cancelPluginProcessLoop();

        return true;
    }

    private static final boolean unregActivityLocked(int pid, int index, String plugin, String activity, String container) {
        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "unreg activity: pid=" + pid + " index=" + index + " plugin=" + plugin + " activity=" + activity + " container=" + container);
        }

        if (index < 0 || index >= PROCESSES.length) {
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "unreg activity: invalid index=" + index);
            }
            return false;
        }

        ProcessRecord r = PROCESSES[index];
        r.activities--;
        r.mobified = System.currentTimeMillis();
        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "activities=" + r.activities + " services=" + r.services + " binders=" + r.binders);
        }

        schedulePluginProcessLoop(CHECK_STAGE2_DELAY);

        return true;
    }

    private static final boolean regServiceLocked(int pid, int index, String plugin, String service) {
        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "reg service: pid=" + pid + " index=" + index + " plugin=" + plugin + " service=" + service);
        }

        if (index < 0 || index >= PROCESSES.length) {
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "reg service: invalid index=" + index);
            }
            return false;
        }

        ProcessRecord r = PROCESSES[index];
        r.services++;
        r.mobified = System.currentTimeMillis();
        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "activities=" + r.activities + " services=" + r.services + " binders=" + r.binders);
        }

        cancelPluginProcessLoop();

        return true;
    }

    private static final boolean unregServiceLocked(int pid, int index, String plugin, String service) {
        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "unreg service: pid=" + pid + " index=" + index + " plugin=" + plugin + " service=" + service);
        }

        if (index < 0 || index >= PROCESSES.length) {
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "unreg service: invalid index=" + index);
            }
            return false;
        }

        ProcessRecord r = PROCESSES[index];
        r.services--;
        r.mobified = System.currentTimeMillis();
        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "activities=" + r.activities + " services=" + r.services + " binders=" + r.binders);
        }

        schedulePluginProcessLoop(CHECK_STAGE2_DELAY);

        return true;
    }

    private static final boolean regBinderLocked(int pid, IBinder binder) {
        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "reg binder: pid=" + pid + " binder=" + binder);
        }

//        // TODO 优化
//        for (ProcessClientRecord r : ALL.values()) {
////            if (r.xx == xx) {
////                break;
////            }
//        }

        // TODO 优化
        for (ProcessRecord r : PROCESSES) {
            if (r.pid == pid) {
                r.binders++;
                r.mobified = System.currentTimeMillis();
                if (LOG) {
                    LogDebug.d(PLUGIN_TAG, "activities=" + r.activities + " services=" + r.services + " binders=" + r.binders);
                }
                break;
            }
        }

        cancelPluginProcessLoop();

        return true;
    }

    private static final boolean unregBinderLocked(int pid, IBinder binder) {
        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "unreg binder: pid=" + pid + " binder=" + binder);
        }

//        // TODO 优化
//        for (ProcessClientRecord r : ALL.values()) {
////            if (r.xx == xx) {
////                break;
////            }
//        }

        // TODO 优化
        for (ProcessRecord r : PROCESSES) {
            if (r.pid == pid) {
                r.binders--;
                r.mobified = System.currentTimeMillis();
                if (LOG) {
                    LogDebug.d(PLUGIN_TAG, "activities=" + r.activities + " services=" + r.services + " binders=" + r.binders);
                }
                break;
            }
        }

        schedulePluginProcessLoop(CHECK_STAGE2_DELAY);

        return true;
    }

    private static final int sumBindersLocked(int index) {
        for (ProcessRecord r : PROCESSES) {
            if (r.index == index) {
                return r.binders;
            }
        }
        return -1;
    }

    private static final void doPluginProcessLoop() {
        if (Constant.SIMPLE_QUIT_CONTROLLER) {
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "do plugin process quit check");
            }
            synchronized (PROCESSES) {
                for (ProcessRecord r : PROCESSES) {
                    if (r.state != STATE_RUNNING) {
                        continue;
                    }
                    if (r.activities > 0) {
                        continue;
                    }
                    if (r.services > 0) {
                        continue;
                    }
                    if (r.binders > 0) {
                        continue;
                    }
                    if (LOGR) {
                        // terminate empty process
                        LogRelease.i(PLUGIN_TAG, "t e p " + r.pid);
                    }
                    //
                    android.os.Process.killProcess(r.pid);
                    waitKilled(r.pid);
                    r.setStoped();
                    //
                    schedulePluginProcessLoop(CHECK_STAGE3_DELAY);
                    return;
                }
            }
        }
    }

    private static final void updateListLocked(List<RunningAppProcessInfo> processes) {
        /* TODO 待完善
        // 标记所有
        for (ProcessRecord r : PROCESSES) {
            if (r.state == STATE_RUNNING) {
                r.setMarked();
            }
        }
        for (ProcessPluginInfo r : USED_PLUGINS.values()) {
            if (r.state == STATE_RUNNING) {
                r.setMarked();
            }
        }

        // 遍历设置状态
        for (RunningAppProcessInfo info : processes) {
            if (info.uid != PluginManager.sUid) {
                continue;
            }
            //
            ProcessPluginInfo pi = USED_PLUGINS.get(info.processName);
            if (pi != null && pi.pid == info.pid) {
                pi.setRunning();
            }
            //
            int index = PluginManager.evalPluginProcess(info.processName);
            if (!PluginManager.isPluginProcess(index)) {
                continue;
            }
            //
            ProcessRecord r = PROCESSES[index];
            // 状态分析
            if (r.state == STATE_ALLOCATED) {
                if (LOG) {
                    LogDebug.d(PLUGIN_TAG, "update plugin process list: plugin process started, index=" + index);
                }
                r.setRunning(info.pid);
            } else if (r.state == STATE_MARKED) {
                if (info.pid == r.pid) {
                    if (LOG) {
                        LogDebug.d(PLUGIN_TAG, "update plugin process list: plugin process running index=" + index);
                    }
                    r.setRunning(info.pid);
                }
            } else {
                if (LOG) {
                    LogDebug.d(PLUGIN_TAG, "update plugin process list: plugin process unknown: state=" + r.state + " index=" + index);
                }
            }
        }

        // 标记所有
        for (ProcessRecord r : PROCESSES) {
            if (r.state == STATE_MARKED) {
                if (LOG) {
                    LogDebug.d(PLUGIN_TAG, "update plugin process list: plugin process died, index=" + r.index);
                }
                r.setStoped();
            }
        }
        for (ProcessPluginInfo r : USED_PLUGINS.values()) {
            if (r.state == STATE_MARKED) {
                if (LOG) {
                    LogDebug.d(PLUGIN_TAG, "update plugin process list: used plugin process died, processName=" + r.processName);
                }
                r.setStoped();
            }
        } */
    }

    private static final void waitKilled(int pid) {
        for (int i = 0; i < 10; i++) {
            try {
                Thread.sleep(100, 0);
            } catch (Throwable e) {
                //
            }
            //
            List<RunningAppProcessInfo> processes = AMSUtils.getRunningAppProcessesNoThrows(RePluginInternal.getAppContext());
            if (processes == null || processes.isEmpty()) {
                continue;
            }
            boolean found = false;
            for (RunningAppProcessInfo info : processes) {
                if (info.pid == pid) {
                    found = true;
                }
            }
            if (!found) {
                return;
            }
        }
    }

    private static final int lookupPluginProcess(List<RunningAppProcessInfo> processes, int index) {
        for (RunningAppProcessInfo pi : processes) {
            if (pi.uid != PluginManager.sUid) {
                continue;
            }
            int i = PluginManager.evalPluginProcess(pi.processName);
            if (i == index) {
                return pi.pid;
            }
        }
        return -1;
    }

    private static final void handleBinderDiedLocked(ProcessClientRecord p, PluginManagerServer pms) {
        // TODO 优化
        for (ProcessClientRecord r : ALL.values()) {
            if (r == p) {
                ALL.remove(r.name);
                break;
            }
        }

        // TODO 优化
        for (ProcessRecord r : PROCESSES) {
            if (r.binder == p.binder) {
                r.setStoped();
                break;
            }
        }

        // 通知 PluginManagerServer 客户端进程链接已断开
        pms.onClientProcessKilled(p.name);
    }
}
