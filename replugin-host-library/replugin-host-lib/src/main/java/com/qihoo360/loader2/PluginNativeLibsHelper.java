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
import android.util.Log;

import com.qihoo360.replugin.helper.LogRelease;
import com.qihoo360.replugin.utils.CloseableUtils;
import com.qihoo360.mobilesafe.core.BuildConfig;
import com.qihoo360.replugin.helper.LogDebug;

import com.qihoo360.replugin.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * 与插件Native（SO）库有关的方法，都放在此处 <p>
 * 该类主要用于：安装插件的SO库，获取SO库路径等 <p>
 * 【仅框架内部使用】
 *
 * @author RePlugin Team
 */
public class PluginNativeLibsHelper {

    private static final String TAG = "PluginNativeLibsHelper";

    /**
     * 安装Native SO库 <p>
     * 模拟系统安装流程，最终只释放一个最合身的SO库进入Libs目录中
     *
     * @param apkPath   APK文件路径
     * @param nativeDir 要释放的Libs目录，通常从getLibDir中获取
     * @return 安装是否成功
     */
    public static boolean install(String apkPath, File nativeDir) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "install(): Start. apkp=" + apkPath + "; nd=" + nativeDir.getAbsolutePath());
        }

        // TODO 线程同步

        // 为防止加载旧SO，先清空目录
        clear(nativeDir);

        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(apkPath);
            Map<String, ZipEntry> libZipEntries = new HashMap<>();
            Map<String, Set<String>> soList = new HashMap<>();

            // 找到所有的SO库，包括各种版本的，方便findSoPathForAbis中过滤
            injectEntriesAndLibsMap(zipFile, libZipEntries, soList);

            for (String soName : soList.keySet()) {
                Set<String> soPaths = soList.get(soName);
                String soPath = findSoPathForAbis(soPaths, soName);
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "install(): Ready to extract. so=" + soName + "; sop=" + soPath);
                }
                if (soPath == null) {
                    continue;
                }
                File file = new File(nativeDir, soName);
                extractFile(zipFile, libZipEntries.get(soPath), file);
            }
            return true;
        } catch (Throwable e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
            // 清除所有释放的文件，防止释放了一半
            clear(nativeDir);
            return false;
        } finally {
            CloseableUtils.closeQuietly(zipFile);
        }
    }

    /**
     * 删除插件的SO库，通常在插件SO释放失败后，或者已有新插件，需要清除老插件时才会生效
     */
    public static void clear(File nativeDir) {
        if (!nativeDir.exists()) {
            return;
        }
        try {
            FileUtils.forceDelete(nativeDir);
        } catch (IOException e) {
            // IOException：有可能是IO，如权限出现问题等，打出日志
            e.printStackTrace();
        } catch (IllegalArgumentException e2) {
            if (LogRelease.LOGR) {
                e2.printStackTrace();
            }
        }
    }

    private static void injectEntriesAndLibsMap(ZipFile zipFile, Map<String, ZipEntry> libZipEntries, Map<String, Set<String>> soList) {
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            String name = entry.getName();
            if (name.contains("../")) {
                // 过滤，防止被攻击
                continue;
            }
            if (name.startsWith("lib/") && !entry.isDirectory()) {
                libZipEntries.put(name, entry);
                String soName = new File(name).getName();
                Set<String> fs = soList.get(soName);
                if (fs == null) {
                    fs = new TreeSet<>();
                    soList.put(soName, fs);
                }
                fs.add(name);
            }
        }
    }

    private static void extractFile(ZipFile zipFile, ZipEntry ze, File outFile) throws IOException {
        InputStream in = null;
        try {
            in = zipFile.getInputStream(ze);
            FileUtils.copyInputStreamToFile(in, outFile);
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "extractFile(): Success! fn=" + outFile.getName());
            }
        } finally {
            CloseableUtils.closeQuietly(in);
        }
    }

    // 根据Abi来获取需要释放的SO在压缩包中的位置
    private static String findSoPathForAbis(Set<String> soPaths, String soName) {
        if (soPaths == null || soPaths.size() <= 0) {
            return null;
        }
        // 若主程序用的是64位进程，则所属的SO必须只拷贝64位的，否则会出异常。32位也是如此
        // 问：如果用户用的是64位处理器，宿主没有放任何SO，那么插件会如何？
        // 答：宿主在被安装时，系统会标记此为64位App，则之后的SO加载则只认64位的
        // 问：如何让插件支持32位？
        // 答：宿主需被标记为32位才可以。可在宿主App中放入任意32位的SO（如放到libs/armeabi目录下）即可。

        // 获取指令集列表
        boolean is64 = VMRuntimeCompat.is64Bit();
        String[] abis;
        if (is64) {
            abis = BuildCompat.SUPPORTED_64_BIT_ABIS;
        } else {
            abis = BuildCompat.SUPPORTED_32_BIT_ABIS;
        }

        // 开始寻找合适指定指令集的SO路径
        String soPath = findSoPathWithAbiList(soPaths, soName, abis);
        if (LogDebug.LOG) {
            LogDebug.d(TAG, "findSoPathForAbis: Find so path. name=" + soName + "; list=" + soPath +
                    "; Host-is-64bit?=" + is64 + "; abis=" + Arrays.toString(abis));
        }
        return soPath;
    }

    private static String findSoPathWithAbiList(Set<String> soPaths, String soName, String[] supportAbis) {
        Arrays.sort(supportAbis);
        for (String soPath : soPaths) {
            String abi = soPath.replaceFirst("lib/", "");
            abi = abi.replace("/" + soName, "");

            if (!TextUtils.isEmpty(abi) && Arrays.binarySearch(supportAbis, abi) >= 0) {
                return soPath;
            }
        }
        return null;
    }
}
