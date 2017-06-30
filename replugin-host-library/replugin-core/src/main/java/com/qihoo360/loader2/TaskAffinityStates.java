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
import android.os.RemoteException;

import com.qihoo360.replugin.helper.HostConfigHelper;

import java.util.HashMap;
import java.util.HashSet;

import static android.content.pm.ActivityInfo.LAUNCH_MULTIPLE;
import static android.content.pm.ActivityInfo.LAUNCH_SINGLE_INSTANCE;
import static android.content.pm.ActivityInfo.LAUNCH_SINGLE_TASK;
import static android.content.pm.ActivityInfo.LAUNCH_SINGLE_TOP;

/**
 * @author RePlugin Team
 */
class TaskAffinityStates {

    public static final String TAG = "task-affinity";

    /**
     * TaskAffinity 的组数
     */
    private static final int GROUP_COUNT = HostConfigHelper.ACTIVITY_PIT_COUNT_TASK;

    /**
     * 保存所有 taskAffinity 的坑位的状态，数组索引为 TaskAffinity 索引。
     */
    private LaunchModeStates[] mLaunchModeStates = new LaunchModeStates[GROUP_COUNT];

    /**
     * 初始化 TaskAffinity 的坑位数据
     *
     * @param prefix     {applicationID}.loader.a.Activity
     * @param suffix     [N1, P0, P1]
     * @param allStates  存储在 PluginContainer 中的所有的坑位的状态
     * @param containers 所有坑位名称
     */
    public void init(String prefix, String suffix, HashMap<String, PluginContainers.ActivityState> allStates,
                     HashSet<String> containers) {

        // 外层循环为组数，内层循环为每组的坑的数量
        for (int i = 0; i < GROUP_COUNT; i++) {
            if (mLaunchModeStates[i] == null) {
                mLaunchModeStates[i] = new LaunchModeStates();
            }

            LaunchModeStates states = mLaunchModeStates[i];
            /* Standard */
            states.addStates(allStates, containers, prefix + suffix + "TA" + i, LAUNCH_MULTIPLE, true, HostConfigHelper.ACTIVITY_PIT_COUNT_TS_STANDARD);
            states.addStates(allStates, containers, prefix + suffix + "TA" + i, LAUNCH_MULTIPLE, false, HostConfigHelper.ACTIVITY_PIT_COUNT_NTS_STANDARD);

            /* SingleTop */
            states.addStates(allStates, containers, prefix + suffix + "TA" + i, LAUNCH_SINGLE_TOP, true, HostConfigHelper.ACTIVITY_PIT_COUNT_TS_SINGLE_TOP);
            states.addStates(allStates, containers, prefix + suffix + "TA" + i, LAUNCH_SINGLE_TOP, false, HostConfigHelper.ACTIVITY_PIT_COUNT_NTS_SINGLE_TOP);

            /* SingleTask */
            states.addStates(allStates, containers, prefix + suffix + "TA" + i, LAUNCH_SINGLE_TASK, true, HostConfigHelper.ACTIVITY_PIT_COUNT_TS_SINGLE_TASK);
            states.addStates(allStates, containers, prefix + suffix + "TA" + i, LAUNCH_SINGLE_TASK, false, HostConfigHelper.ACTIVITY_PIT_COUNT_NTS_SINGLE_TASK);

            /* SingleInstance */
            states.addStates(allStates, containers, prefix + suffix + "TA" + i, LAUNCH_SINGLE_INSTANCE, true, HostConfigHelper.ACTIVITY_PIT_COUNT_TS_SINGLE_INSTANCE);
            states.addStates(allStates, containers, prefix + suffix + "TA" + i, LAUNCH_SINGLE_INSTANCE, false, HostConfigHelper.ACTIVITY_PIT_COUNT_NTS_SINGLE_INSTANCE);
        }
    }

    /**
     * 根据插件 Activity 的信息，找到宿主对应的坑位集合
     */
    HashMap<String, PluginContainers.ActivityState> getStates(ActivityInfo ai) {
        if (ai != null) {

            // 找到应该取第几个 TaskAffinity 中的坑
            int index = 0;
            try {
                index = MP.getTaskAffinityGroupIndex(ai.taskAffinity);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            LaunchModeStates states = mLaunchModeStates[index];
            if (states != null) {
                return states.getStates(ai.launchMode, ai.theme);
            }
        }

        return null;
    }
}
