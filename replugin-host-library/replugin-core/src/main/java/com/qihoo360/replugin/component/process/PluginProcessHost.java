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

package com.qihoo360.replugin.component.process;

import android.util.SparseArray;

import com.qihoo360.replugin.base.IPC;
import com.qihoo360.replugin.component.provider.PluginPitProviderBase;

import java.util.HashMap;
import java.util.Map;

/**
 * 用于在宿主中，处理插件自定义多进程相关业务
 *
 * @author RePlugin Team
 */
public class PluginProcessHost {

    /**
     * 自定义插件的数量，暂时只支持3个自定义进程
     */
    public static final int PROCESS_COUNT = 3;

    /**
     * 自定义进程，int 标识，从 -100 开始，每次加 1；
     * 目前只支持 3 个进程，即到 -98
     */
    public static final int PROCESS_INIT = -100;

    /**
     * 插件中，进程名称后缀，
     * 进程名称类似 xxx.xx:p0, xxx.xx:p1, xxx.xx:p2
     */
    public static final String PROCESS_PLUGIN_SUFFIX = "p";

    /**
     * 插件中，进程名称后缀（带冒号）
     */
    public static final String PROCESS_PLUGIN_SUFFIX2 = ":" + PROCESS_PLUGIN_SUFFIX;

    /**
     * 保存进程后缀和其 Int 值
     * 如：{":p1":-99, ":p2":-98}
     */
    public static final Map<String, Integer> PROCESS_INT_MAP = new HashMap<>();

    /**
     * 保存进程映射时，符号和实际进程名称的关系
     * 如：{"$p1":"com.xx.xxx:p1", "$p2":"com.xx.xxx:p2"}
     */
    public static final Map<String, String> PROCESS_ADJUST_MAP = new HashMap<>();

    /**
     * 保存进程 Int 值与对应 Provider 的 Authority 的关系
     * 如：{-99:"com.qihoo360.mobilesafe.Plugin.NP.1", -98:"com.qihoo360.mobilesafe.Plugin.NP.2"}
     */
    public static final SparseArray<String> PROCESS_AUTHORITY_MAP = new SparseArray<>();

    static {
        for (int i = 0; i < PROCESS_COUNT; i++) {
            PROCESS_INT_MAP.put(PROCESS_PLUGIN_SUFFIX2 + i, PROCESS_INIT + i);
            PROCESS_ADJUST_MAP.put("$" + PROCESS_PLUGIN_SUFFIX + i, IPC.getPackageName() + ":" + PROCESS_PLUGIN_SUFFIX + i);
            PROCESS_AUTHORITY_MAP.put(PROCESS_INIT + i, PluginPitProviderBase.AUTHORITY_PREFIX + i);
        }
    }

    /**
     * 取进程名称中，冒号及后面的部分，如果进程名中无冒号，则返回原值。
     *
     * @param processName 进程名称
     */
    public static String processTail(String processName) {
        int indexOfColon = processName.indexOf(':');
        if (indexOfColon >= 0) {
            processName = processName.toLowerCase();
            return processName.substring(indexOfColon);
        } else {
            return processName;
        }
    }

    /**
     * 是否是用户自定义的进程
     */
    public static boolean isCustomPluginProcess(int index) {
        return index >= PROCESS_INIT && index < PROCESS_INIT + PROCESS_COUNT;
    }

}
