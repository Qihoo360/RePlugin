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

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.text.TextUtils;

import com.qihoo360.i.IPluginManager;
import com.qihoo360.mobilesafe.api.Pref;
import com.qihoo360.replugin.base.IPC;
import com.qihoo360.replugin.component.process.PluginProcessHost;
import com.qihoo360.replugin.helper.HostConfigHelper;
import com.qihoo360.replugin.helper.JSONHelper;
import com.qihoo360.replugin.helper.LogDebug;
import com.qihoo360.replugin.helper.LogRelease;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import static android.content.pm.ActivityInfo.LAUNCH_MULTIPLE;
import static android.content.pm.ActivityInfo.LAUNCH_SINGLE_INSTANCE;
import static android.content.pm.ActivityInfo.LAUNCH_SINGLE_TASK;
import static android.content.pm.ActivityInfo.LAUNCH_SINGLE_TOP;
import static com.qihoo360.loader2.PluginContainers.ActivityState.toName;
import static com.qihoo360.replugin.helper.LogDebug.LOG;
import static com.qihoo360.replugin.helper.LogDebug.PLUGIN_TAG;
import static com.qihoo360.replugin.helper.LogRelease.LOGR;

/**
 * 插件容器管理
 * Plugin Activity Container Manager (PACM)
 *
 * @author RePlugin Team
 */
public class PluginContainers {

    /*
1 请求者：请求分配坑
2 代理模块：依次串联执行如下的 3,4,5 步骤，并返回坑给请求者
3 核心进程：启动宿主进程，返回宿主进程Binder
4 宿主进程：启动时从核心进程加载登记表（坑和目标activity表），减少forward activity和恢复activity时的二次触发
5 宿主进程：查找合适的坑并返回（登记）
6 请求者：发起start Activity请求给系统
7 系统：调度宿主进程
8 宿主进程：查找登记表，加载类
9 宿主进程：触发on Create，校验【Intent的目标和当前activity对象】是否一致，如果一致则确认登记，否则忽略
a 流程完成

登记：
    登记用于明确的请求意图
    并记录到当前宿主进程的登记表，同时让核心进程也记录（用于宿主进程挂掉时恢复用）

例外情况：
1 坑耗尽，强制分配
2 核心进程死亡，信息丢失
3 宿主进程意外死亡后恢复，系统重新发起Activity创建（恢复）

例外环节处理：
5 分配出不稳定的坑
    原因：坑耗尽，复用；由于核心进程和宿主进程挂掉由系统恢复导致的无记录，新分配或复用；
    处理：区分开坑不够还是宿主进程或核心进程意外重启导致的信息丢失；可以先不考虑复用的情况；

8 查找坑失败，登记表没有记录
    原因：宿主进程和核心进程意外重启，由系统恢复Activity，因此此时无记录
    处理：启动forward activity，该activity在on create时获取真正的目标后，立即登记，再次启动Intent的目标Activity

9 检查不合格，Intent的目标和当前activity对象不一致
    原因：宿主进程或核心进程意外重启；或者坑跑飞？
    处理：
    1 如果登记表中找到分配记录，则说明这是一个明确的请求：重新启动分配的Activity
    2 否则，表明是系统恢复Activity动作：立即登记，重新启动Intent的目标Activity

重启目标时：
    增加计数器，当超过一定次数时停止

*/

    private static final String CONTAINER_ACTIVITY_PART = ".loader.a.Activity";

    /**
     *
     */
    private final Object mLock = new Object();

    /**
     * 所有坑的状态集合
     */
    private HashMap<String, ActivityState> mStates = new HashMap<>();

    /**
     * 非默认 TaskAffinity 下，坑位的状态信息。
     */
    private TaskAffinityStates mTaskAffinityStates = new TaskAffinityStates();

    /**
     * 默认 TaskAffinity 下，坑位的状态信息。
     */
    private LaunchModeStates mLaunchModeStates = new LaunchModeStates();

    /**
     * 保存进程和进程中坑位状态的 Map
     */
    private final Map<String, ProcessStates> mProcessStatesMap = new HashMap<>();

    private static final int STATE_NONE = 0;

    private static final int STATE_OCCUPIED = 1;

    private static final int STATE_RESTORED = 2;

    static final class ActivityState {

        final String container;

        int state;

        String plugin;

        String activity;

        long timestamp;

        final ArrayList<WeakReference<Activity>> refs;

        ActivityState(String container) {
            this.container = container;
            this.refs = new ArrayList<WeakReference<Activity>>();
        }

        public ActivityState(ActivityState state) {
            this.container = state.container;
            this.state = state.state;
            this.plugin = state.plugin;
            this.activity = state.activity;
            this.timestamp = state.timestamp;
            this.refs = new ArrayList<WeakReference<Activity>>(state.refs);
        }

        @Override
        public String toString() {
            if (LogDebug.LOG) {
                String s = " state=" + toName(this.state);
                String p = " plugin=" + this.plugin;
                String a = " activity=" + this.activity;
                String sz = " size=" + refs.size();
                return "ActivityState {container=" + this.container + s + p + a + sz + "}";
            }
            return super.toString();
        }

        static final String toName(int state) {
            switch (state) {
                case STATE_NONE:
                    return "none";
                case STATE_OCCUPIED:
                    return "occupied";
                case STATE_RESTORED:
                    return "restored";
            }
            return "unknown";
        }

        private final boolean isTarget(String plugin, String activity) {
            if (TextUtils.equals(this.plugin, plugin) && TextUtils.equals(this.activity, activity)) {
                return true;
            }
            return false;
        }

        private final void occupy(String plugin, String activity) {
            if (TextUtils.isEmpty(plugin) || TextUtils.isEmpty(activity)) {
                if (LOG) {
                    LogDebug.w(PLUGIN_TAG, "PACM: occupy: invalid s=" + toName(this.state) + " plugin=" + plugin + " activity=" + activity);
                }
                return;
            }

            this.state = STATE_OCCUPIED;
            this.plugin = plugin;
            this.activity = activity;
            cleanRefs();
            this.timestamp = System.currentTimeMillis();

            //
            save2Pref(this.plugin, this.activity, this.container);
        }

        private final void restore(String plugin, String activity, long timestamp) {
            if (TextUtils.isEmpty(plugin) || TextUtils.isEmpty(activity)) {
                if (LOG) {
                    LogDebug.w(PLUGIN_TAG, "PACM: restore: invalid s=" + toName(this.state) + " plugin=" + plugin + " activity=" + activity);
                }
                return;
            }
            this.state = STATE_RESTORED;
            this.plugin = plugin;
            this.activity = activity;
            cleanRefs();
            this.timestamp = timestamp;
        }

        private final void recycle() {
            this.state = STATE_NONE;
            this.plugin = null;
            this.activity = null;
            cleanRefs();
            this.timestamp = System.currentTimeMillis();
        }


        private final void create(String plugin, Activity activity) {
            if (this.state == STATE_OCCUPIED || this.state == STATE_RESTORED) { // 当处于restored状态时，表明是系统恢复activity（没有经过register，无显式start activity，核心进程有记录）
                if (!TextUtils.equals(this.plugin, plugin)) {
                    if (LOG) {
                        LogDebug.w(PLUGIN_TAG, "PACM: create: invalid plugin=" + plugin + " this.plugin=" + this.plugin);
                    }
                    return;
                }
                if (!TextUtils.equals(this.activity, activity.getClass().getName())) {
                    if (LOG) {
                        LogDebug.w(PLUGIN_TAG, "PACM: create: invalid a=" + activity.getClass().getName() + " this.a=" + this.activity);
                    }
                    return;
                }
                if (this.state == STATE_RESTORED) {
                    if (LOG) {
                        LogDebug.i(PLUGIN_TAG, "PACM: create: relaunch activity: history: container=" + container + " plugin=" + plugin + " activity=" + activity);
                    }
                }
            } else if (this.state == STATE_NONE) { // 当处于none状态时，表明是系统恢复activity（没有经过register，无显式start activity，核心进程数据丢失）
                if (LOG) {
                    LogDebug.i(PLUGIN_TAG, "PACM: create: relaunch activity: blank");
                }
                return;
            } else { // Never: 当前已经在created状态？一坑多实例？
                if (LOG) {
                    LogDebug.w(PLUGIN_TAG, "PACM: create: invalid s=" + toName(this.state) + " e=registered c=" + this.container);
                }
                return;
            }

            addRef(activity);
            this.timestamp = System.currentTimeMillis();
        }

        private final boolean hasRef() {
            for (int i = refs.size() - 1; i >= 0; i--) {
                WeakReference<Activity> ref = refs.get(i);
                if (ref.get() == null) {
                    refs.remove(i);
                }
            }
            return refs.size() > 0;
        }

        private final void cleanRefs() {
            if (LOG) {
                for (WeakReference<Activity> ref : refs) {
                    if (ref.get() != null) {
                        LogDebug.w(PLUGIN_TAG, "PACM: clean refs: exist a=" + ref.get());
                    }
                }
            }
            refs.clear();
        }

        private final void addRef(Activity activity) {
            for (WeakReference<Activity> ref : refs) {
                if (ref.get() == activity) {
                    return;
                }
            }
            refs.add(new WeakReference<Activity>(activity));
        }

        private final void removeRef(Activity activity) {
            for (int i = refs.size() - 1; i >= 0; i--) {
                WeakReference<Activity> ref = refs.get(i);
                if (ref.get() == activity) {
                    refs.remove(i);
                    break;
                }
            }
        }

        private final void finishRefs() {
            for (WeakReference<Activity> ref : refs) {
                Activity a = ref.get();
                if (a != null) {
                    a.finish();
                }
            }
        }

        final void forwardSelf(Activity activity1, Intent intent) {
            try {
                // 补齐参数：附上额外数据，进行校验
                PluginIntent ii = new PluginIntent(intent);
                ii.setPlugin(plugin);
                ii.setActivity(activity);
                ii.setProcess(IPluginManager.PROCESS_AUTO);
                ii.setContainer(container);
                // 直接启动，避免再次进入插件框架
                intent.putExtra(IPluginManager.KEY_COMPATIBLE, true);
                // 设定组件
                intent.setComponent(new ComponentName(IPC.getPackageName(), container));
                //
                activity1.startActivity(intent);
            } catch (Throwable e) {
                if (LOGR) {
                    LogRelease.e(PLUGIN_TAG, "f.a fs: " + e.getMessage(), e);
                }
            }
        }
    }

    final void init(int process, HashSet<String> containers) {
        if (process != IPluginManager.PROCESS_UI
                && !PluginProcessHost.isCustomPluginProcess(process)
                && !PluginManager.isPluginProcess()) {
            return;
        }

        String prefix = IPC.getPackageName() + CONTAINER_ACTIVITY_PART;

        // 因为自定义进程可能也会唤起使用 UI 进程的坑，所以这里使用'或'条件
        if (process == IPluginManager.PROCESS_UI || PluginProcessHost.isCustomPluginProcess(process)) {

            /* UI 进程标识为 N1 */
            String suffix = "N1";

            // Standard
            mLaunchModeStates.addStates(mStates, containers, prefix + suffix, LAUNCH_MULTIPLE, true, HostConfigHelper.ACTIVITY_PIT_COUNT_TS_STANDARD);
            mLaunchModeStates.addStates(mStates, containers, prefix + suffix, LAUNCH_MULTIPLE, false, HostConfigHelper.ACTIVITY_PIT_COUNT_NTS_STANDARD);

            // SingleTop
            mLaunchModeStates.addStates(mStates, containers, prefix + suffix, LAUNCH_SINGLE_TOP, true, HostConfigHelper.ACTIVITY_PIT_COUNT_TS_SINGLE_TOP);
            mLaunchModeStates.addStates(mStates, containers, prefix + suffix, LAUNCH_SINGLE_TOP, false, HostConfigHelper.ACTIVITY_PIT_COUNT_NTS_SINGLE_TOP);

            // SingleTask
            mLaunchModeStates.addStates(mStates, containers, prefix + suffix, LAUNCH_SINGLE_TASK, true, HostConfigHelper.ACTIVITY_PIT_COUNT_TS_SINGLE_TASK);
            mLaunchModeStates.addStates(mStates, containers, prefix + suffix, LAUNCH_SINGLE_TASK, false, HostConfigHelper.ACTIVITY_PIT_COUNT_NTS_SINGLE_TASK);

            // SingleInstance
            mLaunchModeStates.addStates(mStates, containers, prefix + suffix, LAUNCH_SINGLE_INSTANCE, true, HostConfigHelper.ACTIVITY_PIT_COUNT_TS_SINGLE_INSTANCE);
            mLaunchModeStates.addStates(mStates, containers, prefix + suffix, LAUNCH_SINGLE_INSTANCE, false, HostConfigHelper.ACTIVITY_PIT_COUNT_NTS_SINGLE_INSTANCE);

            // taskAffinity
            mTaskAffinityStates.init(prefix, suffix, mStates, containers);

            // 因为有可能会在 UI 进程启动自定义进程的 Activity，所以此处也要初始化自定义进程的坑位数据
            for (int i = 0; i < PluginProcessHost.PROCESS_COUNT; i++) {
                ProcessStates processStates = new ProcessStates();
                // [":p1": state("P1"), ":p2": state("P2")]
                mProcessStatesMap.put(PluginProcessHost.PROCESS_PLUGIN_SUFFIX2 + i, processStates);
                init2(prefix, containers, processStates, PluginProcessHost.PROCESS_PLUGIN_SUFFIX + i);
            }

            // 从内存中加载
            loadFromPref();
        }

        // TODO more
    }

    /**
     * 初始化自定义进程坑坑位
     *
     * @param prefix     xxx.xx.loader.a.Activity
     * @param containers 保存所有 Activity 坑名称
     * @param states     当前进程所有坑位的状态
     * @param suffix     p0, p1, p2
     */
    private void init2(String prefix, HashSet<String> containers, ProcessStates states, String suffix) {
        suffix = suffix.toUpperCase();

        // Standard
        states.mLaunchModeStates.addStates(mStates, containers, prefix + suffix, LAUNCH_MULTIPLE, true, HostConfigHelper.ACTIVITY_PIT_COUNT_TS_STANDARD);
        states.mLaunchModeStates.addStates(mStates, containers, prefix + suffix, LAUNCH_MULTIPLE, false, HostConfigHelper.ACTIVITY_PIT_COUNT_NTS_STANDARD);

        // SingleTop
        states.mLaunchModeStates.addStates(mStates, containers, prefix + suffix, LAUNCH_SINGLE_TOP, true, HostConfigHelper.ACTIVITY_PIT_COUNT_TS_SINGLE_TOP);
        states.mLaunchModeStates.addStates(mStates, containers, prefix + suffix, LAUNCH_SINGLE_TOP, false, HostConfigHelper.ACTIVITY_PIT_COUNT_NTS_SINGLE_TOP);

        // SingleTask
        states.mLaunchModeStates.addStates(mStates, containers, prefix + suffix, LAUNCH_SINGLE_TASK, true, HostConfigHelper.ACTIVITY_PIT_COUNT_TS_SINGLE_TASK);
        states.mLaunchModeStates.addStates(mStates, containers, prefix + suffix, LAUNCH_SINGLE_TASK, false, HostConfigHelper.ACTIVITY_PIT_COUNT_NTS_SINGLE_TASK);

        // SingleInstance
        states.mLaunchModeStates.addStates(mStates, containers, prefix + suffix, LAUNCH_SINGLE_INSTANCE, true, HostConfigHelper.ACTIVITY_PIT_COUNT_TS_SINGLE_INSTANCE);
        states.mLaunchModeStates.addStates(mStates, containers, prefix + suffix, LAUNCH_SINGLE_INSTANCE, false, HostConfigHelper.ACTIVITY_PIT_COUNT_NTS_SINGLE_INSTANCE);

        // taskAffinity
        states.mTaskAffinityStates.init(prefix, suffix, mStates, containers);
    }

    private final void loadFromPref() {
        try {
            Map<String, ?> a = Pref.ipcGetAll();
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "PACM: restore table: size=" + a.size());
            }
            for (Entry<String, ?> i : a.entrySet()) {
                String k = i.getKey();
                Object v = i.getValue();
                ActivityState state = mStates.get(k);
                String item[] = v.toString().split(":");
                if (state != null && item != null && item.length == 3) {
                    String plugin = item[0];
                    String activity = item[1];
                    long timestamp = Long.parseLong(item[2]);
                    if (LOG) {
                        LogDebug.d(PLUGIN_TAG, "PACM: restore table: " + " container=" + k + " plugin=" + plugin + " activity=" + activity);
                    }
                    if (!TextUtils.isEmpty(plugin) && !TextUtils.isEmpty(activity)) {
                        state.restore(plugin, activity, timestamp);
                    }
                } else {
                    if (LOG) {
                        LogDebug.w(PLUGIN_TAG, "PACM: invalid table: k=" + k + " v=" + v);
                    }
                }
            }
        } catch (Throwable e) {
            if (LOGR) {
                LogRelease.e(PLUGIN_TAG, "PACM: init e=" + e.getMessage(), e);
            }
        }
    }

    private static final void save2Pref(String plugin, String activity, String container) {
        String v = plugin + ":" + activity + ":" + System.currentTimeMillis();
        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "PACM: save 2 pref: k=" + container + " v=" + v);
        }
        Pref.ipcSet(container, v);
    }

    static final String[] resolvePluginActivity(String container) {
        String v = Pref.ipcGet(container, "");
        //String v = plugin + ":" + activity + ":" + System.currentTimeMillis();
        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "PACM: load special pref: k=" + container + " v=" + v);
        }
        //
        if (TextUtils.isEmpty(v)) {
            return null;
        }
        return v.split(":");
    }

    final void forwardIntent(Activity activity, Intent intent, String original, String container, String plugin, String target, int process) {
        // 找到容器
        ActivityState so = null;
        ActivityState state = null;
        synchronized (mLock) {
            HashMap<String, ActivityState> map = mStates;
            so = map.get(original);
            state = map.get(container);
        }
        if (so == null) {
            if (LOGR) {
                LogRelease.e(PLUGIN_TAG, "f.a fi: cc: inv c.c=" + original);
            }
            return;
        }
        if (state == null) {
            if (LOGR) {
                LogRelease.e(PLUGIN_TAG, "f.a fi: cc: inv t.c=" + container);
            }
            return;
        }
        // 检查
        if (state.state == STATE_NONE) {
            if (LOGR) {
                LogRelease.e(PLUGIN_TAG, "f.a fi: cc: ok, t.c empty, t.c=" + container);
            }
            // 重新登记
            state.occupy(plugin, target);
        } else if (!state.isTarget(plugin, target)) {
            if (LOGR) {
                LogRelease.e(PLUGIN_TAG, "f.a fi: cc: fly, force, t.c=" + container);
            }
            // 如果已经有实例存在了，只能打打日志
            if (state.hasRef()) {
                if (LOGR) {
                    LogRelease.e(PLUGIN_TAG, "f.a fi: cc: exists instances");
                }
            }
            // 重新登记
            state.occupy(plugin, target);
        } else {
            if (LOGR) {
                LogRelease.e(PLUGIN_TAG, "f.a fi: cc: same, t.c=" + container);
            }
        }
        if (so != state) {
            if (LOGR) {
                LogRelease.e(PLUGIN_TAG, "f.a fi: t t.c=" + container);
            }
            if (LOG) {
                LogDebug.i(PLUGIN_TAG, "forward fly: container=" + container + " plugin=" + plugin + " activity=" + target);
            }
            so.recycle();
        } else {
            if (LOGR) {
                LogRelease.i(PLUGIN_TAG, "f.a fi: same t.c=" + container);
            }
            if (LOG) {
                LogDebug.i(PLUGIN_TAG, "forward registered: container=" + container + " plugin=" + plugin + " activity=" + target);
            }
        }
        // 启动目标activity
        state.forwardSelf(activity, intent);
    }

    /**
     * 登记坑和activity的映射
     * 当前进程：宿主进程
     *
     * @param ai
     * @param plugin
     * @param activity
     * @param process
     * @param intent
     * @return
     */
    final String alloc(ActivityInfo ai, String plugin, String activity, int process, Intent intent) {
        ActivityState state;

        String defaultPluginTaskAffinity = ai.applicationInfo.packageName;

        if (LOG) {
            LogDebug.d(TaskAffinityStates.TAG, "originTaskAffinity is " + ai.taskAffinity);
        }

        /* SingleInstance 优先级最高 */
        if (ai.launchMode == LAUNCH_SINGLE_INSTANCE) {
            synchronized (mLock) {
                state = allocLocked(ai, mLaunchModeStates.getStates(ai.launchMode, ai.theme), plugin, activity, intent);
            }

        /* TaskAffinity */
        } else if (!defaultPluginTaskAffinity.equals(ai.taskAffinity)) { // 非默认 taskAffinity
            synchronized (mLock) {
                state = allocLocked(ai, mTaskAffinityStates.getStates(ai), plugin, activity, intent);
            }

        /* SingleTask, SingleTop, Standard */
        } else {
            synchronized (mLock) {
                state = allocLocked(ai, mLaunchModeStates.getStates(ai.launchMode, ai.theme), plugin, activity, intent);
            }
        }

        if (state != null) {
            return state.container;
        }

        return null;
    }

    /**
     * @param ai
     * @param map
     * @param plugin
     * @param activity
     * @param intent
     * @return
     */
    private final ActivityState allocLocked(ActivityInfo ai, HashMap<String, ActivityState> map,
                                            String plugin, String activity, Intent intent) {
        // 坑和状态的 map 为空
        if (map == null) {
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "PACM: alloc fail, map is null");
            }
            return null;
        }

        // 首先找上一个活的，或者已经注册的，避免多个坑到同一个activity的映射
        for (ActivityState state : map.values()) {
            if (state.isTarget(plugin, activity)) {
                if (LOG) {
                    LogDebug.d(PLUGIN_TAG, "PACM: alloc registered container=" + state.container);
                }
                return state;
            }
        }

        // 新分配：找空白的，第一个
        for (ActivityState state : map.values()) {
            if (state.state == STATE_NONE) {
                if (LOG) {
                    LogDebug.d(PLUGIN_TAG, "PACM: alloc empty container=" + state.container);
                }
                state.occupy(plugin, activity);
                return state;
            }
        }

        ActivityState found;

        // 重用：则找最老的那个
        found = null;
        for (ActivityState state : map.values()) {
            if (!state.hasRef()) {
                if (found == null) {
                    found = state;
                } else if (state.timestamp < found.timestamp) {
                    found = state;
                }
            }
        }
        if (found != null) {
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "PACM: alloc recycled container=" + found.container);
            }
            found.occupy(plugin, activity);
            return found;
        }

        // 强挤：最后一招，挤掉：最老的那个
        found = null;
        for (ActivityState state : map.values()) {
            if (found == null) {
                found = state;
            } else if (state.timestamp < found.timestamp) {
                found = state;
            }
        }
        if (found != null) {
            if (LOG) {
                LogDebug.w(PLUGIN_TAG, "PACM: force alloc container=" + found.container);
            }
            found.finishRefs();
            found.occupy(plugin, activity);
            return found;
        }

        if (LOG) {
            LogDebug.w(PLUGIN_TAG, "PACM: alloc failed: plugin=" + plugin + " activity=" + activity);
        }

        // never reach here
        return null;
    }

    String alloc2(ActivityInfo ai, String plugin, String activity, int process, Intent intent, String processTail) {
        // 根据进程名称，取得该进程对应的 PluginContainerStates
        ProcessStates states = mProcessStatesMap.get(processTail);

        ActivityState state;

        String defaultPluginTaskAffinity = ai.applicationInfo.packageName;
        if (LOG) {
            LogDebug.d(TaskAffinityStates.TAG, String.format("插件 %s 默认 TaskAffinity 为 %s", plugin, defaultPluginTaskAffinity));
            LogDebug.d(TaskAffinityStates.TAG, String.format("%s 的 TaskAffinity 为 %s", activity, ai.taskAffinity));
        }

        /* SingleInstance */
        if (ai.launchMode == LAUNCH_SINGLE_INSTANCE) {
            synchronized (mLock) {
                state = allocLocked(ai, states.mLaunchModeStates.getStates(ai.launchMode, ai.theme), plugin, activity, intent);
            }

        /* TaskAffinity */
        } else if (!defaultPluginTaskAffinity.equals(ai.taskAffinity)) { // 非默认 taskAffinity
            synchronized (mLock) {
                state = allocLocked(ai, states.mTaskAffinityStates.getStates(ai), plugin, activity, intent);
            }

        /* other mode */
        } else {
            synchronized (mLock) {
                state = allocLocked(ai, states.mLaunchModeStates.getStates(ai.launchMode, ai.theme), plugin, activity, intent);
            }
        }

        if (state != null) {
            return state.container;
        }
        return null;
    }

    final void handleCreate(String plugin, Activity activity, String container) {
        ComponentName cn = activity.getComponentName();
        if (cn != null) {
            container = cn.getClassName();
        }
        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "PACM: activity created h=" + activity.hashCode() + " class=" + activity.getClass().getName() + " container=" + container);
        }
        synchronized (mLock) {
            HashMap<String, ActivityState> map = mStates;
            ActivityState state = map.get(container);
            if (state != null) {
                state.create(plugin, activity);
            }
        }
    }

    final void handleDestroy(Activity activity) {
        String container = null;
        //
        ComponentName cn = activity.getComponentName();
        if (cn != null) {
            container = cn.getClassName();
        }
        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "PACM: activity destroy h=" + activity.hashCode() + " class=" + activity.getClass().getName() + " container=" + container);
        }
        if (container == null) {
            return;
        }
        synchronized (mLock) {
            HashMap<String, ActivityState> map = mStates;
            ActivityState state = map.get(container);
            if (state != null) {
                state.removeRef(activity);
            }
        }
    }

    /**
     * 容器对应的类
     *
     * @param container
     * @return 注意，对于返回值：可能是登记的，可能是activity退出的残留，也可能是进程恢复后上次的记录
     */
    final ActivityState lookupByContainer(String container) {
        if (container == null) {
            return null;
        }

        synchronized (mLock) {
            HashMap<String, ActivityState> map = mStates;
            ActivityState state = map.get(container);
            if (state != null && state.state != STATE_NONE) {
                if (LOG) {
                    LogDebug.d(PLUGIN_TAG, "found: " + state);
                }
                return new ActivityState(state);
            }
        }

        // PC: lookupByContainer
        if (LOGR) {
            long s = mStates.size();
            LogRelease.e(PLUGIN_TAG, "not found:" + " c=" + container + " pool=" + s);
        }

        return null;
    }

    final String dump() {

        JSONArray activityArr = new JSONArray();
        JSONObject activityObj;

        for (Map.Entry<String, ActivityState> entry : mStates.entrySet()) {
            String container = entry.getKey();
            ActivityState state = entry.getValue();

            if (!TextUtils.isEmpty(state.plugin) && !TextUtils.isEmpty(state.activity)) {
                activityObj = new JSONObject();
                JSONHelper.putNoThrows(activityObj, "process", IPC.getCurrentProcessName());
                JSONHelper.putNoThrows(activityObj, "className", container);
                JSONHelper.putNoThrows(activityObj, "plugin", state.plugin);
                JSONHelper.putNoThrows(activityObj, "realClassName", state.activity);
                JSONHelper.putNoThrows(activityObj, "state", toName(state.state));
                JSONHelper.putNoThrows(activityObj, "refs", state.refs != null ? state.refs.size() : 0);
                activityArr.put(activityObj);
            }
        }

        return activityArr.toString();
    }
}