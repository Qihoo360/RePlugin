package com.qihoo360.loader2;

import android.app.ActivityManager;
import android.os.IBinder;
import android.text.TextUtils;

import com.qihoo360.i.IPluginManager;
import com.qihoo360.mobilesafe.api.Tasks;
import com.qihoo360.replugin.RePluginInternal;
import com.qihoo360.replugin.base.AMSUtils;
import com.qihoo360.replugin.helper.LogDebug;
import com.qihoo360.replugin.helper.LogRelease;

import java.io.PrintWriter;
import java.util.List;

import static com.qihoo360.replugin.helper.LogDebug.LOG;
import static com.qihoo360.replugin.helper.LogDebug.PLUGIN_TAG;
import static com.qihoo360.replugin.helper.LogRelease.LOGR;
/**
 * @author RePlugin Team
 * dec: 坑位进程管理 buyuntao
 */
public class StubProcessManager {
    /**
     * 坑位进程列表
     */
    static final ProcessRecord STUB_PROCESSES[] = new ProcessRecord[Constant.STUB_PROCESS_COUNT];
    static final int CHECK_STAGE1_DELAY = 17 * 1000;
    private static final int CHECK_STAGE2_DELAY = 11 * 1000;
    private static final int CHECK_STAGE3_DELAY = 3 * 1000;
    private static final Runnable CHECK = new Runnable() {
        @Override
        public void run() {
            doPluginProcessLoop();
        }
    };

    static {
        for (int i = 0; i < Constant.STUB_PROCESS_COUNT; i++) {
            ProcessRecord r = new ProcessRecord(i, StubProcessState.STATE_UNUSED);
            STUB_PROCESSES[i] = r;
        }
    }

    /**
     * 分配坑位进程 buyuntao(外部调用端已经加锁)
     * @param plugin
     * @return 进程index值
     */
    static final int allocProcess(String plugin) {
        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "alloc plugin process: plugin=" + plugin);
        }
        // 取运行列表
        List<ActivityManager.RunningAppProcessInfo> processes = AMSUtils.getRunningAppProcessesNoThrows(RePluginInternal.getAppContext());
        // 取运行列表失败，则直接返回失败
        if (processes == null || processes.isEmpty()) {
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "alloc plugin process: get running processes is empty");
                LogDebug.i(PLUGIN_TAG, "get list exception p=" + plugin);
            }
            return IPluginManager.PROCESS_AUTO;
        }
        //根据优先级分配坑位进程
        int prevMatchPriority = -1; //临时变量，保存上一个ProcessRecord的进程分配优先级
        ProcessRecord selectRecord = null; //被选中的坑位进程
        for (ProcessRecord r : STUB_PROCESSES) {
            synchronized (r) {
                if (r.calculateMatchPriority(plugin) > prevMatchPriority) {
                    prevMatchPriority = r.calculateMatchPriority(plugin);
                    selectRecord = r;
                } else if (r.calculateMatchPriority(plugin) == prevMatchPriority) {
                    if (r.mobified < selectRecord.mobified) {
                        selectRecord = r;
                    }
                }
            }
        }
        if (selectRecord == null) { //不应该出现
            return IPluginManager.PROCESS_AUTO;
        }
        synchronized (selectRecord){
            //插件已在分配进程中运行，直接返回
            if (selectRecord.calculateMatchPriority(plugin) == Integer.MAX_VALUE && (selectRecord.state == StubProcessState.STATE_ALLOCATED || selectRecord.state == StubProcessState.STATE_RUNNING))
            {
                return selectRecord.index;
            }
            selectRecord.resetAllocate(plugin, processes);
            return selectRecord.index;
        }
    }

    private static final int lookupPluginProcess(List<ActivityManager.RunningAppProcessInfo> processes, int index) {
        for (ActivityManager.RunningAppProcessInfo pi : processes) {
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

    private static final void waitKilled(int pid) {
        for (int i = 0; i < 10; i++) {
            try {
                Thread.sleep(100, 0);
            } catch (Throwable e) {
                //
            }
            //
            List<ActivityManager.RunningAppProcessInfo> processes = AMSUtils.getRunningAppProcessesNoThrows(RePluginInternal.getAppContext());
            if (processes == null || processes.isEmpty()) {
                continue;
            }
            boolean found = false;
            for (ActivityManager.RunningAppProcessInfo info : processes) {
                if (info.pid == pid) {
                    found = true;
                }
            }
            if (!found) {
                return;
            }
        }
    }

    static final void cancelPluginProcessLoop() {
        if (Constant.SIMPLE_QUIT_CONTROLLER) {
            Tasks.cancelThreadTask(CHECK);
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
        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "reg activity: pid=" + pid + " index=" + index + " plugin=" + plugin + " activity=" + activity + " container=" + container);
        }

        if (index < 0 || index >= STUB_PROCESSES.length) {
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "reg activity: invalid index=" + index);
            }
            return false;
        }

        ProcessRecord r = STUB_PROCESSES[index];
        synchronized (r){
            r.activities++;
            r.mobified = System.currentTimeMillis();
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "activities=" + r.activities + " services=" + r.services + " binders=" + r.binders);
            }
        }
        cancelPluginProcessLoop();

        return true;
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
        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "unreg activity: pid=" + pid + " index=" + index + " plugin=" + plugin + " activity=" + activity + " container=" + container);
        }

        if (index < 0 || index >= STUB_PROCESSES.length) {
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "unreg activity: invalid index=" + index);
            }
            return false;
        }

        ProcessRecord r = STUB_PROCESSES[index];
        synchronized (r){
            r.activities--;
            r.mobified = System.currentTimeMillis();
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "activities=" + r.activities + " services=" + r.services + " binders=" + r.binders);
            }
        }
        schedulePluginProcessLoop(CHECK_STAGE2_DELAY);

        return true;
    }

    /**
     * @param pid
     * @param index
     * @param plugin
     * @param service
     * @return
     */
    static final boolean attachService(int pid, int index, String plugin, String service) {
        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "reg service: pid=" + pid + " index=" + index + " plugin=" + plugin + " service=" + service);
        }

        if (index < 0 || index >= STUB_PROCESSES.length) {
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "reg service: invalid index=" + index);
            }
            return false;
        }

        ProcessRecord r = STUB_PROCESSES[index];
        synchronized (r) {
            r.services++;
            r.mobified = System.currentTimeMillis();
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "activities=" + r.activities + " services=" + r.services + " binders=" + r.binders);
            }
        }
        cancelPluginProcessLoop();

        return true;
    }

    /**
     * @param pid
     * @param index
     * @param plugin
     * @param service
     * @return
     */
    static final boolean detachService(int pid, int index, String plugin, String service) {
        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "unreg service: pid=" + pid + " index=" + index + " plugin=" + plugin + " service=" + service);
        }

        if (index < 0 || index >= STUB_PROCESSES.length) {
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "unreg service: invalid index=" + index);
            }
            return false;
        }

        ProcessRecord r = STUB_PROCESSES[index];
        synchronized (r){
            r.services--;
            r.mobified = System.currentTimeMillis();
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "activities=" + r.activities + " services=" + r.services + " binders=" + r.binders);
            }
        }
        schedulePluginProcessLoop(CHECK_STAGE2_DELAY);

        return true;
    }

    static final void attachBinder(int pid, IBinder binder) {
        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "reg binder: pid=" + pid + " binder=" + binder);
        }
        for (ProcessRecord r : STUB_PROCESSES) {
            if (r.pid == pid) {
                synchronized (r) {
                    r.binders++;
                    r.mobified = System.currentTimeMillis();
                    if (LOG) {
                        LogDebug.d(PLUGIN_TAG, "activities=" + r.activities + " services=" + r.services + " binders=" + r.binders);
                    }
                }
                break;
            }
        }

        cancelPluginProcessLoop();
    }

    static final void detachBinder(int pid, IBinder binder) {
        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "unreg binder: pid=" + pid + " binder=" + binder);
        }
        for (ProcessRecord r : STUB_PROCESSES) {
            if (r.pid == pid) {
                synchronized (r){
                    r.binders--;
                    r.mobified = System.currentTimeMillis();
                    if (LOG) {
                        LogDebug.d(PLUGIN_TAG, "activities=" + r.activities + " services=" + r.services + " binders=" + r.binders);
                    }
                }
                break;
            }
        }

        schedulePluginProcessLoop(CHECK_STAGE2_DELAY);
    }

    static final int sumBinders(int index) {
        if (index >=0 && index < STUB_PROCESSES.length){
            ProcessRecord r = STUB_PROCESSES[index];
            synchronized (r) {
                return STUB_PROCESSES[index].binders;
            }
        }
        return -1;
    }

    /**
     * attach坑位进程，设置坑位进程为运行状态，并返回正在使用坑位进程的插件名称 buyuntao
     *
     * @param pid
     * @param index
     * @param binder
     * @param client
     * @param def
     * @return
     */
    static final String attachStubProcess(int pid, int index, IBinder binder, IPluginClient client, String def) {
        // 检测状态是否一致
        ProcessRecord r = STUB_PROCESSES[index];
        synchronized (r) {
            if (!TextUtils.isEmpty(def)) {
                if (LOG) {
                    LogDebug.d(PLUGIN_TAG, "attach process: allocate now");
                }
                r.allocate(def);
            }
            if (r.state != StubProcessState.STATE_ALLOCATED) {
                if (LOG) {
                    LogDebug.d(PLUGIN_TAG, "attach process: state not allocated: state=" + r.state);
                }
                return null;
            }

            r.setRunning(pid);
            r.setClient(binder, client);
            return r.plugin;
        }
    }

    static final void setProcessStop(final IBinder binder) {
        for (ProcessRecord r : STUB_PROCESSES) {
            synchronized (r) {
                if (r.binder == binder) {
                    r.setStoped();
                    break;
                }
            }
        }
    }

    private static final void doPluginProcessLoop() {
        if (Constant.SIMPLE_QUIT_CONTROLLER) {
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "do plugin process quit check");
            }
            for (ProcessRecord r : STUB_PROCESSES) {
                synchronized (r) {
                    if (r.state != StubProcessState.STATE_RUNNING) {
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

    static final void schedulePluginProcessLoop(long delayMillis) {
        if (Constant.SIMPLE_QUIT_CONTROLLER) {
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "schedule plugin process quit check: delay=" + (delayMillis / 1000));
            }
            Tasks.cancelThreadTask(CHECK);
            Tasks.postDelayed2Thread(CHECK, delayMillis);
        }
    }

    static final void dump(PrintWriter writer) {
        writer.println("--- STUB_PROCESSES.length = " + STUB_PROCESSES.length + " ---");
        for (ProcessRecord r : STUB_PROCESSES) {
            synchronized (r){
                writer.println(r);
            }
        }
    }

    /**
     * 坑位进程的状态 buyuntao
     */
    public class StubProcessState {
        public static final int STATE_UNUSED = 0;

        public static final int STATE_ALLOCATED = 1;

        public static final int STATE_RUNNING = 2;

        public static final int STATE_STOPED = 4;
    }

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
            this.state = StubProcessState.STATE_ALLOCATED;
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
            this.state = StubProcessState.STATE_RUNNING;
            this.pid = pid;
        }

        void setClient(IBinder binder, IPluginClient client) {
            this.binder = binder;
            this.client = client;
        }

        void setStoped() {
            this.state = StubProcessState.STATE_STOPED;
            this.pid = 0;
            this.binder = null;
            this.client = null;
        }

        /**
         * 当前坑位的选择优先级（值越大被选中的概率越高）
         *
         * @param newPluginName
         * @return 坑位的选择优先级
         */
        int calculateMatchPriority(String newPluginName) {
            int priority = Integer.MAX_VALUE;
            if (TextUtils.equals(newPluginName, plugin)) { //插件可能用过的进程
                return priority;
            }
            if (state == StubProcessState.STATE_UNUSED) { //空闲的进程
                priority = Integer.MAX_VALUE - 1;
                return priority;
            }
            if (state == StubProcessState.STATE_STOPED) { //已停止的进程
                priority = Integer.MAX_VALUE - 2;
                return priority;
            }
            if ((System.currentTimeMillis() - mobified) > 10 * 1000) { //分配时间超过10秒的
                priority = Integer.MAX_VALUE - 3;
                return priority;
            }
            if ((activities <= 0) && (services <= 0) && (binders <= 0)) { //组件为空的
                priority = Integer.MAX_VALUE - 4;
                return priority;
            }
            priority = 0; //默认值
            return priority;
        }

        void resetAllocate(String plugin, List<ActivityManager.RunningAppProcessInfo> processes) {
            killProcess(processes);
            allocate(plugin);
        }

        private void killProcess(List<ActivityManager.RunningAppProcessInfo> processes) {
            // 确保进程为空
            int pid = lookupPluginProcess(processes, index);
            if (pid > 0) {
                if (LOGR) {
                    LogRelease.i(PLUGIN_TAG, "ppr k i: " + pid);
                }
                android.os.Process.killProcess(pid);
                waitKilled(pid);
            }
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


}
