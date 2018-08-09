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
import android.os.Build;

import com.qihoo360.replugin.helper.LogDebug;
import com.qihoo360.replugin.model.PluginInfo;
import com.qihoo360.replugin.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import static com.qihoo360.replugin.helper.LogDebug.LOG;
import static com.qihoo360.replugin.helper.LogDebug.PLUGIN_TAG;

/**
 * @author RePlugin Team
 */
public class Builder {

    /**
     * Plugins 信息搜集
     * 插件搜索顺序：1、内置插件；2、V5复合插件；3、V5插件；4、释放出来的插件
     */
    static final class PxAll {

        /**
         * 内置插件
         */
        private final ArrayList<PluginInfo> builtins = new ArrayList<PluginInfo>();

        /**
         * V5单文件插件
         */
        private final ArrayList<PluginInfo> v5 = new ArrayList<PluginInfo>();

        /**
         * 释放出来的普通插件
         */
        private final ArrayList<PluginInfo> normals = new ArrayList<PluginInfo>();

        /**
         * 其它
         */
        private final HashSet<PluginInfo> others = new HashSet<PluginInfo>();

        /**
         * 所有插件
         */
        private final ArrayList<PluginInfo> all = new ArrayList<PluginInfo>();

        /**
         * 确保版本和插件唯一
         * @param array
         * @param info
         * @param replace true表示更新相同的，false表示不更新相同的
         * @return
         */
        private final boolean insert(ArrayList<PluginInfo> array, PluginInfo info, boolean replace) {
            for (int i = 0; i < array.size(); i++) {
                PluginInfo pi = array.get(i);
                // 存在
                if (pi.getName().equals(info.getName())) {
                    // 忽略
                    if (replace) {
                        if (PluginInfo.VERSION_COMPARATOR.compare(pi, info) > 0) {
                            return false;
                        }
                    } else {
                        if (PluginInfo.VERSION_COMPARATOR.compare(pi, info) >= 0) {
                            return false;
                        }
                    }
                    // 更新
                    others.add(array.get(i));
                    array.set(i, info);
                    return true;
                }
            }
            // 不存在，添加
            array.add(info);
            return true;
        }

        private final boolean hasOlder(ArrayList<PluginInfo> array, PluginInfo info) {
            for (PluginInfo pi : array) {
                if (pi.getName().equals(info.getName())) {
                    if (PluginInfo.VERSION_COMPARATOR.compare(pi, info) < 0) {
                        return true;
                    }
                }
            }
            return false;
        }

        /**
         * @param name
         * @return
         */
        private final PluginInfo getBuiltin(String name) {
            for (PluginInfo pi : builtins) {
                if (pi.getName().equals(name)) {
                    return pi;
                }
            }
            return null;
        }

        /**
         * @param name
         * @return
         */
        private final PluginInfo getV5(String name) {
            for (PluginInfo pi : v5) {
                if (pi.getName().equals(name)) {
                    return pi;
                }
            }
            return null;
        }

        /**
         * @return
         */
        final HashSet<PluginInfo> getOthers() {
            return others;
        }

        /**
         * @return
         */
        final ArrayList<PluginInfo> getPlugins() {
            return all;
        }

        /**
         * @param info
         */
        final void addBuiltin(PluginInfo info) {
            insert(builtins, info, false);
            insert(all, info, false);
        }

        /**
         * @param info
         */
        final void addV5(PluginInfo info) {
            if (!insert(all, info, false)) {
                return;
            }
            insert(v5, info, false);
        }

        /**
         * @param info
         * @return
         */
        final void addNormal(PluginInfo info) {
            PluginInfo pi = null;
            // FIXME 用all表
            if ((pi = getBuiltin(info.getName())) != null && pi.getVersionValue() == info.getVersionValue()) {
                if (LOG) {
                    LogDebug.d(PLUGIN_TAG, "builtin plugin: normal=" + info);
                }
            } else if ((pi = getV5(info.getName())) != null && pi.getVersionValue() == info.getVersionValue()) {
                if (LOG) {
                    LogDebug.d(PLUGIN_TAG, "v5 plugin: normal=" + info);
                }
            } else {
                others.add(info);
                return;
            }

            insert(normals, info, false);
        }
    }

    static final void builder(Context context, PxAll all) {
        // 搜索所有本地插件和V5插件
        Finder.search(context, all);

        // 删除不适配的PLUGINs
        for (PluginInfo p : all.getOthers()) {
            // TODO 如果已存在built-in和V5则不删除
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "delete obsolote plugin=" + p);
            }
            boolean rc = p.deleteObsolote(context);
            if (!rc) {
                if (LOG) {
                    LogDebug.d(PLUGIN_TAG, "can't delete obsolote plugin=" + p);
                }
            }
        }

        // 删除所有和PLUGINs不一致的DEX文件
        deleteUnknownDexs(context, all);

        // 删除所有和PLUGINs不一致的SO库目录
        // Added by Jiongxuan Zhang
        deleteUnknownLibs(context, all);

        // 构建数据
    }

    private static File getDexDir(Context context) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
            return new File(context.getDir(Constant.LOCAL_PLUGIN_SUB_DIR, 0) + File.separator + "oat" + File.separator + VMRuntimeCompat.getArtOatCpuType());
        } else {
            return context.getDir(Constant.LOCAL_PLUGIN_ODEX_SUB_DIR, 0);
        }
    }

    private static void deleteUnknownDexs(Context context, PxAll all) {
        HashSet<String> names = new HashSet<>();
        for (PluginInfo p : all.getPlugins()) {
            names.add(p.getDexFile().getName());

            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "dexFile:" + p.getDexFile().getName());
            }

            // add vdex for Android O
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
                String fileNameWithoutExt = FileUtils.getFileNameWithoutExt(p.getDexFile().getAbsolutePath());

                if (LOG) {
                    LogDebug.d(PLUGIN_TAG, "vdexFile:" + (fileNameWithoutExt + ".vdex"));
                }

                names.add(fileNameWithoutExt + ".vdex");
            }
        }

        File dexDir = getDexDir(context);

        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "to delete dex dir:" + dexDir);
        }

        File files[] = dexDir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (names.contains(f.getName())) {
                    if (LOG) {
                        LogDebug.d(PLUGIN_TAG, "no need delete " + f.getAbsolutePath());
                    }
                    continue;
                }
                if (LOG) {
                    LogDebug.d(PLUGIN_TAG, "delete unknown dex=" + f.getAbsolutePath());
                }
                try {
                    FileUtils.forceDelete(f);
                } catch (IOException e) {
                    if (LOG) {
                        LogDebug.d(PLUGIN_TAG, "can't delete unknown dex=" + f.getAbsolutePath(), e);
                    }
                } catch (IllegalArgumentException e2) {
                    if (LOG) {
                        e2.printStackTrace();
                    }
                }
            }
        }
    }

    private static void deleteUnknownLibs(Context context, PxAll all) {
        HashSet<String> names = new HashSet<>();
        for (PluginInfo p : all.getPlugins()) {
            names.add(p.getNativeLibsDir().getName());
        }

        File dir = context.getDir(Constant.LOCAL_PLUGIN_DATA_LIB_DIR, 0);
        File files[] = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (names.contains(f.getName())) {
                    continue;
                }
                if (LOG) {
                    LogDebug.d(PLUGIN_TAG, "delete unknown libs=" + f.getAbsolutePath());
                }
                try {
                    FileUtils.forceDelete(f);
                } catch (IOException e) {
                    if (LOG) {
                        LogDebug.d(PLUGIN_TAG, "can't delete unknown libs=" + f.getAbsolutePath(), e);
                    }
                } catch (IllegalArgumentException e2) {
                    if (LOG) {
                        e2.printStackTrace();
                    }
                }
            }
        }
    }
}
