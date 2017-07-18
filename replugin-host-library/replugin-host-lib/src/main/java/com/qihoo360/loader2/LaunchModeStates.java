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

import android.content.pm.ActivityInfo;

import com.qihoo360.loader2.PluginContainers.ActivityState;
import com.qihoo360.replugin.RePlugin;
import com.qihoo360.replugin.helper.LogDebug;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static com.qihoo360.replugin.helper.LogDebug.LOG;

/**
 * 存储 LaunchMode + Theme -> 此种组合下的 ActivityState 状态集合
 *
 * @author RePlugin Team
 */
class LaunchModeStates {

    public static final String TAG = "launchMode";

    /**
     * 目前的策略是，针对每一种 launchMode 分配两种坑位（透明主题(TS)和不透明主题(NTS)）
     * <p>
     * 例：透明主题
     * 　　　　　　　<N1NRTS0, ActivityState>
     * NR + TS  - > <N1NRTS1, ActivityState>
     * 　　　　　　　<N1NRTS2, ActivityState>
     * <p>
     * 例：不透明主题
     * 　　　　　　　<N1NRNTS0, ActivityState>
     * NR + NTS - > <N1NRNTS1, ActivityState>
     * 　　　　　　　<N1NRNTS2, ActivityState>
     * <p>
     * 其中：N1 表示当前为 UI 进程，NR 表示 launchMode 为 Standard，NTS 表示坑的 theme 为 Not Translucent。
     */
    private Map<String, HashMap<String, ActivityState>> mStates = new HashMap<>();

    /**
     * 初始化 LaunchMode 和 Theme 对应的坑位
     *
     * @param containers  保存所有 activity 坑位的引用
     * @param prefix      坑位前缀
     * @param launchMode  launchMode
     * @param translucent 是否是透明的坑
     * @param count       坑位数
     */
    void addStates(Map<String, ActivityState> allStates, HashSet<String> containers, String prefix, int launchMode, boolean translucent, int count) {
        String infix = getInfix(launchMode, translucent);
        HashMap<String, ActivityState> states = mStates.get(infix);
        if (states == null) {
            states = new HashMap<>();
            mStates.put(infix, states);
        }

        for (int i = 0; i < count; i++) {
            String key = prefix + infix + i;

            // 只有开启“详细日志”时才输出每一个坑位的信息，防止刷屏
            if (RePlugin.getConfig().isPrintDetailLog()) {
                LogDebug.d(TAG, "LaunchModeStates.add(" + key + ")");
            }

            ActivityState state = new ActivityState(key);
            states.put(key, state);
            allStates.put(key, state);
            containers.add(key);
        }
    }

    /**
     * 根据 launchMode 和 theme 获取对应的坑位集合
     */
    HashMap<String, ActivityState> getStates(int launchMode, int theme) {
        String infix = getInfix(launchMode, isTranslucentTheme(theme));
        return mStates.get(infix);
    }

    /**
     * 根据 launchMode 和 '是否透明' 获取中缀符
     *
     * @return 如果是透明主题，返回 'launchMode'_TS_，否则返回 'launchMode'_NOT_TS_
     */
    private static String getInfix(int launchMode, boolean translucent) {
        String launchModeInfix = getLaunchModeInfix(launchMode);
        return translucent ? launchModeInfix + "TS" : launchModeInfix + "NTS";
    }


    /**
     * 手动判断主题是否是透明主题
     */
    public static boolean isTranslucentTheme(int theme) {
        return theme == android.R.style.Theme_Translucent
                || theme == android.R.style.Theme_Dialog
                || theme == android.R.style.Theme_Translucent_NoTitleBar
                || theme == android.R.style.Theme_Translucent_NoTitleBar_Fullscreen;
    }

    /**
     * 获取 launchMode 对应的前缀
     */
    private static String getLaunchModeInfix(int launchMode) {
        switch (launchMode) {
            case ActivityInfo.LAUNCH_SINGLE_TOP:
                return "STP";
            case ActivityInfo.LAUNCH_SINGLE_TASK:
                return "ST";
            case ActivityInfo.LAUNCH_SINGLE_INSTANCE:
                return "SI";
            default:
                return "NR";
        }
    }
}
