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

import com.qihoo360.loader.utils.ProcessLocker;
import com.qihoo360.loader2.Builder.PxAll;
import com.qihoo360.replugin.RePlugin;
import com.qihoo360.replugin.RePluginInternal;
import com.qihoo360.replugin.helper.LogDebug;
import com.qihoo360.replugin.model.PluginInfo;

import java.io.File;
import java.util.ArrayList;

import static com.qihoo360.replugin.helper.LogDebug.LOG;
import static com.qihoo360.replugin.helper.LogDebug.PLUGIN_TAG;

/**
 * @author RePlugin Team
 */
public class V5Finder {

    static final void search(Context context, File pluginDir, PxAll all) {
        // 扫描V5下载目录
        ArrayList<V5FileInfo> v5Plugins = new ArrayList<V5FileInfo>();
        {
            File dir = RePlugin.getConfig().getPnInstallDir();
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "search v5 files: dir=" + dir.getAbsolutePath());
            }
            searchV5Plugins(dir, v5Plugins);
        }

        // 同步V5原始插件文件到插件目录
        for (V5FileInfo p : v5Plugins) {

            ProcessLocker lock = new ProcessLocker(RePluginInternal.getAppContext(), p.mFile.getParent(), p.mFile.getName() + ".lock");

            /**
             * 此处逻辑的详细介绍请参照
             *
             * @see com.qihoo360.loader2.MP.pluginDownloaded(String path)
             */
            if (lock.isLocked()) {
                // 插件文件不可用，直接跳过
                continue;
            }

            PluginInfo info = p.updateV5FileTo(context, pluginDir, false, true);
            // 已检查版本
            if (info == null) {
                if (LOG) {
                    LogDebug.d(PLUGIN_TAG, "search: fail to update v5 plugin");
                }
            } else {
                all.addV5(info);
            }
        }
    }

    private static final void searchV5Plugins(File dir, ArrayList<V5FileInfo> plugins) {
        File files[] = dir.listFiles();
        if (files == null) {
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "search v5 plugin: nothing");
            }
            return;
        }
        for (File f : files) {
            if (f.isDirectory()) {
                continue;
            }
            if (f.length() <= 0) {
                continue;
            }
            V5FileInfo p = null;
            p = V5FileInfo.build(f, V5FileInfo.NORMAL_PLUGIN);
            if (p != null) {
                plugins.add(p);
                continue;
            }
            p = V5FileInfo.build(f, V5FileInfo.INCREMENT_PLUGIN);
            if (p != null) {
                plugins.add(p);
                continue;
            }
        }
    }
}
