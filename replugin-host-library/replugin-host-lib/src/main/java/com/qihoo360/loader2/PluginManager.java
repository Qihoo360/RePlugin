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
import android.text.TextUtils;

import com.qihoo360.i.IPluginManager;
import com.qihoo360.mobilesafe.api.Tasks;
import com.qihoo360.replugin.base.IPC;
import com.qihoo360.replugin.component.process.PluginProcessHost;
import com.qihoo360.replugin.helper.LogDebug;

import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.qihoo360.replugin.helper.LogDebug.LOG;
import static com.qihoo360.replugin.helper.LogDebug.PLUGIN_TAG;

/**
 * @author RePlugin Team
 */
public class PluginManager {

    private static final Pattern PROCESS_NAME_PATTERN = Pattern.compile(Constant.STUB_PROCESS_SUFFIX_PATTERN);

    public static final int PROCESS_AUTO = IPluginManager.PROCESS_AUTO;

    public static final int COUNTER_MAX = 10;

    /**
     * @deprecated 临时实现
     */
    @Deprecated
    static int sUid;

    /**
     * @deprecated 临时实现
     */
    @Deprecated
    static int sPluginProcessIndex = -1;

    /**
     * @return
     */
    public static final boolean isPluginProcess() {
        return sPluginProcessIndex >= 0 && sPluginProcessIndex < Constant.STUB_PROCESS_COUNT;
    }

    public static final boolean isValidActivityProcess(int process) {
        if (process == IPluginManager.PROCESS_UI || process == IPluginManager.PROCESS_AUTO || isPluginProcess(process)) {
            return true;
        }
        return false;
    }

    /**
     * @return
     */
    static final boolean isPluginProcess(int index) {
        return index >= 0 && index < Constant.STUB_PROCESS_COUNT;
    }

    static final int getPluginProcessIndex() {
        return sPluginProcessIndex;
    }

    /**
     * @deprecated 临时实现
     */
    @Deprecated
    static final void init(Context context) {
        // 初始化操作，方便后面执行任务，不必担心Handler为空的情况
        Tasks.init();
        //
        sUid = android.os.Process.myUid();

        //
        sPluginProcessIndex = evalPluginProcess(IPC.getCurrentProcessName());
    }

    static final int evalPluginProcess(String name) {
        int index = IPluginManager.PROCESS_AUTO;

        try {
            if (TextUtils.equals(IPC.getPackageName(), name)) {
                if (LOG) {
                    LogDebug.d(PLUGIN_TAG, "plugin process checker: default, index=" + 0);
                }
                return IPluginManager.PROCESS_UI;
            }

            if (!TextUtils.isEmpty(name)) {
                if (name.contains(PluginProcessHost.PROCESS_PLUGIN_SUFFIX2)) {
                    String tail = PluginProcessHost.processTail(name);
                    return PluginProcessHost.PROCESS_INT_MAP.get(tail);
                }
            }

            Matcher m = PROCESS_NAME_PATTERN.matcher(name);
            if (m == null || !m.matches()) {
                if (LOG) {
                    LogDebug.d(PLUGIN_TAG, "plugin process checker: non plugin process in=" + name);
                }
                return IPluginManager.PROCESS_AUTO;
            }

            MatchResult r = m.toMatchResult();
            if (r == null || r.groupCount() != 2) {
                if (LOG) {
                    LogDebug.d(PLUGIN_TAG, "plugin process checker: no group in=" + name);
                }
                return IPluginManager.PROCESS_AUTO;
            }

            String pr = r.group(1);
            if (!TextUtils.equals(IPC.getPackageName(), pr)) {
                if (LOG) {
                    LogDebug.d(PLUGIN_TAG, "plugin process checker: package name not match in=" + name);
                }
                return IPluginManager.PROCESS_AUTO;
            }

            String str = r.group(2);
            index = Integer.parseInt(str);
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "plugin process checker: index=" + index);
            }
        } catch (Throwable e) {
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, e.getMessage(), e);
            }
        }

        return index;
    }
}
