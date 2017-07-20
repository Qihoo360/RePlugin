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

import android.text.TextUtils;

import com.qihoo360.replugin.RePlugin;
import com.qihoo360.replugin.helper.LogDebug;
import com.qihoo360.replugin.model.PluginInfo;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.qihoo360.replugin.helper.LogDebug.LOG;
import static com.qihoo360.replugin.helper.LogDebug.PLUGIN_TAG;

/**
 * @author RePlugin Team
 */
class PluginTable {

    /**
     *
     */
    static final HashMap<String, PluginInfo> PLUGINS = new HashMap<String, PluginInfo>();

    static final void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        if (LogDebug.DUMP_ENABLED) {
            writer.println("--- PluginTable.size = " + PLUGINS.size() + " ---");
            List<PluginInfo> l = MP.getPlugins(false);
            for (PluginInfo r : l) {
                writer.println(r);
            }
            writer.println();
        }
    }

    static final void initPlugins(Map<String, Plugin> plugins) {
        synchronized (PLUGINS) {
            for (Plugin plugin : plugins.values()) {
                putPluginInfo(plugin.mInfo);
            }
        }
    }

    static final void updatePlugin(PluginInfo info) {
        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "update plugin table: info=" + info);
        }
        synchronized (PLUGINS) {

            // 检查插件是否已经被禁用
            if (RePlugin.getConfig().getCallbacks().isPluginBlocked(info)) {
                if (LOG) {
                    LogDebug.d(PLUGIN_TAG, "update plugin table: plugin is blocked, in=" + info);
                }
                return;
            }

            // 此处直接使用该插件，没有考虑是否只采用最新版
            putPluginInfo(info);
        }
    }

    static final void replaceInfo(PluginInfo info) {
        boolean rc = false;
        PluginInfo pi = null;
        synchronized (PLUGINS) {
            pi = PLUGINS.get(info.getName());
            if (pi != null) {
                if (pi.canReplaceForPn(info)) {
                    putPluginInfo(info);
                    rc = true;
                }
            }
        }
        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "replace plugin table: info=" + info + " rc=" + rc);
        }
    }

    static final void removeInfo(PluginInfo info) {
        boolean rc = false;
        PluginInfo pi = null;
        synchronized (PLUGINS) {
            pi = PLUGINS.get(info.getName());
            if (pi != null) {
                removePluginInfo(info);
                rc = true;
            }
        }
        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "removeInfo plugin table: info=" + info + " rc=" + rc);
        }
    }

    static final PluginInfo getPluginInfo(String plugin) {
        synchronized (PLUGINS) {
            return PLUGINS.get(plugin);
        }
    }

    static final List<PluginInfo> buildPlugins() {
        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "build plugins");
        }

        List<PluginInfo> lst = MP.getPlugins(false);
        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "build " + lst.size() + " plugins");
        }
        return lst;
    }

    private static void putPluginInfo(PluginInfo info) {
        // 同时加入PackageName和Alias（如有）
        PLUGINS.put(info.getPackageName(), info);
        if (!TextUtils.isEmpty(info.getAlias())) {
            // 即便Alias和包名相同也可以再Put一次，反正只是覆盖了相同Value而已
            PLUGINS.put(info.getAlias(), info);
        }
    }

    private static void removePluginInfo(PluginInfo info) {
        PLUGINS.remove(info.getPackageName());
        PLUGINS.remove(info.getAlias());
    }

//    /**
//     * @deprecated
//     *
//     * @return
//     */
//    @Deprecated
//    static final Cursor fetchPlugins() {
//        if (LOG) {
//            LogDebug.d(PLUGIN_TAG, "build plugins");
//        }
//
//        MatrixCursor cursor = new MatrixCursor(PluginInfo.QUERY_COLUMNS);
//
//        synchronized (sPlugins) {
//            if (sAdapter != null) {
//                sAdapter.to(cursor);
//            }
//            for (PluginInfo p : sPlugins.values()) {
//                p.to(cursor);
//            }
//            if (LOG) {
//                LogDebug.d(PLUGIN_TAG, "build " + sPlugins.size() + " plugins");
//            }
//        }
//
//        return cursor;
//    }

}
