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

package com.qihoo360.replugin.packages;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.qihoo360.replugin.RePlugin;
import com.qihoo360.replugin.helper.LogDebug;
import com.qihoo360.replugin.model.PluginInfo;

import com.qihoo360.replugin.utils.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * 和插件安装有关的入口类
 *
 * @author RePlugin Team
 */

public class RePluginInstaller {

    private static final String TAG = "RePluginInstaller";

    // 将插件从APK -> p-n-xxx.jar，并放入files目录下
    public static File covertToPnFile(Context context, String path) {
        File filesDir = RePlugin.getConfig().getPnInstallDir();

        // 已经是p-n开头的了？直接返回
        File f = new File(path);
        if (f.getName().startsWith("p-n-")) {
            return copyPnToInstallPathIfNeeded(f, filesDir);
        }

        // 1. 读取APK内容
        PackageInfo pi = context.getPackageManager().getPackageArchiveInfo(path, PackageManager.GET_META_DATA);
        if (pi == null) {
            if (LogDebug.LOG) {
                LogDebug.e(TAG, "covertToPnFile: Not a valid apk. path=" + path);
            }
            return null;
        }

        // 2. 解析出名字和三元组
        PluginInfo pli = PluginInfo.parseFromPackageInfo(pi, path);
        if (pli == null) {
            if (LogDebug.LOG) {
                LogDebug.e(TAG, "covertToPnFile: MetaData Invalid! Are you define com.qihoo360.plugin.name and others? path=" + path);
            }
            return null;
        }

        // 3. 转化为p-n-xxx.jar文件，并写入到Files目录下
        File publishFile = new File(filesDir, "p-n-" + pli.getName() + ".jar");
        boolean r = PluginPublishFileGenerator.write(path, publishFile.getAbsolutePath(),
                pli.getLowInterfaceApi(), pli.getHighInterfaceApi(), pli.getVersion());
        if (!r) {
            if (LogDebug.LOG) {
                LogDebug.e(TAG, "covertToPnFile: Write to publish file error! path=" + path + "; publish=" + publishFile.getAbsolutePath());
            }
            return null;
        }

        // 返回给外界，开始直接安装这个Files目录下的文件
        return publishFile;
    }

    private static File copyPnToInstallPathIfNeeded(File f, File filesDir) {
        if (f.getParentFile().equals(filesDir)) {
            // 该p-n已在安装目录上？直接忽略，返回即可
            if (LogDebug.LOG) {
                LogDebug.i(TAG, "copyPnToInstallPathIfNeeded: Already p-n file in install path. Ignore. path=" + f.getAbsolutePath());
            }
            return f;
        } else {
            // 将p-n文件复制到安装目录下，然后再返回
            File df = new File(filesDir, f.getName());
            if (LogDebug.LOG) {
                LogDebug.i(TAG, "copyPnToInstallPathIfNeeded: Already p-n file, copy to install path. src=" + f.getAbsolutePath() + "; dest=" + df.getAbsolutePath());
            }

            try {
                FileUtils.copyFile(f, df);
            } catch (IOException e) {
                if (LogDebug.LOG) {
                    LogDebug.e(TAG, "copyPnToInstallPathIfNeeded: Copy fail!", e);
                }
                return null;
            }
            return df;
        }
    }
}
