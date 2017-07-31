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

package com.qihoo360.replugin.utils;

import android.content.Context;
import android.util.Log;

import com.qihoo360.loader2.PluginNativeLibsHelper;
import com.qihoo360.replugin.helper.LogDebug;
import com.qihoo360.replugin.helper.LogRelease;
import com.qihoo360.replugin.model.PluginInfo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static com.qihoo360.replugin.helper.LogDebug.LOG;
import static com.qihoo360.replugin.helper.LogRelease.LOGR;

/**
 * @author RePlugin Team
 */
public class AssetsUtils {

    private static final String TAG = LogDebug.PLUGIN_TAG;

    /**
     * 提取文件到目标位置
     * @param context
     * @param name asset名称（asset的相对路径，可包含子路径）
     * @param dir 目标文件夹（asset的输出目录）
     * @return
     */
    public static final boolean extractTo(Context context, final String name, final String dir, final String dstName) {
        File file = new File(dir + "/" + dstName);
        InputStream is = FileUtils.openInputStreamFromAssetsQuietly(context, name);
        if (is == null) {
            if (LOG) {
                LogDebug.e(TAG, "extractTo: Fail to open " + name + "from Assets");
            }
            return false;
        }

        try {
            FileUtils.copyInputStreamToFile(is, file);
            return true;
        } catch (IOException e) {
            if (LOGR) {
                e.printStackTrace();
            }
        } finally {
            CloseableUtils.closeQuietly(is);
        }

        return false;
    }

    /**
     * 提取文件到目标位置，并处理文件夹是否存在，是否校验，是否强制覆盖，是否需要释放SO库
     * @param context
     * @param info PluginInfo对象（asset的相对路径，可包含子路径）
     * @param dir 目标文件夹（asset的输出目录）
     * @param dexOutputDir 成功提取该文件时，是否删除同名的DEX文件
     * @return
     */
    public static final boolean quickExtractTo(Context context, final PluginInfo info, final String dir, final String dstName, String dexOutputDir) {
        QuickExtractResult result = quickExtractTo(context, info.getPath(), dir, dstName, dexOutputDir);
        // 释放失败 || 被释放的文件已经存在
        switch (result) {
            case FAIL:
                return false;
            case EXISTED:
                return true;
            default:
                // 释放插件里的Native（SO）库文件
                // Added by Jiongxuan Zhang
                File file = new File(dir + "/" + dstName);
                File libDir = info.getNativeLibsDir();
                boolean rc = PluginNativeLibsHelper.install(file.getAbsolutePath(), libDir);
                if (!rc) {
                    if (LOGR) {
                        LogRelease.e(TAG, "a u e rc f so " + file.getPath());
                    }
                    return rc;
                }
                return true;
        }
    }

    /**
     * 提取文件到目标位置，并处理文件夹是否存在，是否校验，是否强制覆盖。不会释放SO库
     * @param context
     * @param name asset名称（asset的相对路径，可包含子路径）
     * @param dir 目标文件夹（asset的输出目录）
     * @param dexOutputDir 成功提取该文件时，是否删除同名的DEX文件
     * @return 释放文件的结果
     */
    public static final QuickExtractResult quickExtractTo(Context context, final String name, final String dir, final String dstName, String dexOutputDir) {
        File file = new File(dir + "/" + dstName);

        // 建立子目录
        File d = file.getParentFile();
        if (!d.exists()) {
            if (!d.mkdirs()) {
                if (LOGR) {
                    Log.e(TAG, "can't create: " + d.getPath());
                }
                return QuickExtractResult.FAIL;
            }
        }

        // 检查创建是否成功
        if (!d.exists() || !d.isDirectory()) {
            if (LOGR) {
                Log.e(TAG, "can't create dir: " + d.getPath());
            }
            return QuickExtractResult.FAIL;
        }

        // 文件已存在，直接返回
        if (file.exists()) {
            return QuickExtractResult.EXISTED;
        }

        boolean rc = extractTo(context, name, dir, dstName);
        if (LOG) {
            Log.d(TAG, "create new: " + file.getPath() + (rc ? " ok" : " error"));
        }
        if (!rc) {
            if (LOGR) {
                Log.e(TAG, "a u e rc f " + file.getPath());
            }
            return QuickExtractResult.FAIL;
        }
        return QuickExtractResult.SUCCESS;
   }

    /**
     * 调用 QuickExtract 的结果值
     */
   public enum QuickExtractResult {
       SUCCESS, // 释放文件成功
       FAIL,    // 释放文件失败
       EXISTED  // 文件已存在（之前释放过了）
   }
}
