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
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.qihoo360.replugin.utils.CloseableUtils;
import com.qihoo360.loader.utils.PackageUtils;
import com.qihoo360.loader.utils.StringUtils;
import com.qihoo360.replugin.utils.basic.SecurityUtil;
import com.qihoo360.replugin.RePlugin;
import com.qihoo360.replugin.RePluginInternal;
import com.qihoo360.replugin.helper.LogDebug;
import com.qihoo360.replugin.helper.LogRelease;
import com.qihoo360.replugin.model.PluginInfo;

import com.qihoo360.replugin.utils.FileUtils;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Locale;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.qihoo360.replugin.helper.LogDebug.LOG;
import static com.qihoo360.replugin.helper.LogDebug.MAIN_TAG;
import static com.qihoo360.replugin.helper.LogDebug.PLUGIN_TAG;
import static com.qihoo360.replugin.helper.LogRelease.LOGR;

/**
 * @author RePlugin Team
 */
public class V5FileInfo {

    /**
     *
     */
    public static final int NONE_PLUGIN = 0;

    /**
     *
     */
    public static final int NORMAL_PLUGIN = 1;

    /**
     *
     */
    public static final int SINGLE_PLUGIN = 2;

    /**
     *
     */
    public static final int INCREMENT_PLUGIN = 3;

    /**
     * V5复合插件
     */
    public static final int MULTI_PLUGIN = 4;

    /**
     * （通过V5下载的）增量模式的插件
     */
    private static final String INCREMENT_PLUGIN_FILE_PATTERN = "^v-plugin-([^.-]+).jar$";

    /**
     * （通过V5下载的）非增量模式的single插件
     */
    private static final String SINGLE_PLUGIN_FILE_PATTERN = "^plugin-s-([^.-]+).jar$";

    /**
     * （通过V5下载的）非增量模式的插件
     */
    private static final String NORMAL_PLUGIN_FILE_PATTERN = "^p-n-([^.-]+).jar$";

    /**
     * （通过V5下载的）非增量模式的复合插件
     */
    private static final String MULTI_PLUGIN_FILE_PATTERN = "^p-m-([^.-]+).jar$";

    private static final String NORMAL_PREFIX = "p-n-";
    private static final String EXTENSION = ".jar";

    /**
     * V5增量文件头
     */
    private static final int V5_FILE_HEADER_SIZE = 16;

    private static final Pattern INCREMENT_REGEX;

    private static final Pattern INCREMENT_SINGLE_REGEX;

    private static final Pattern NORMAL_REGEX;

    private static final Pattern MULTI_REGEX;

    /**
     * 插件名
     */
    String mName;

    /**
     * 原始路径（V5文件路径），需要转换为加载路径
     */
    File mFile;

    /**
     * 插件类型
     */
    int mType;

    static {
        INCREMENT_REGEX = Pattern.compile(INCREMENT_PLUGIN_FILE_PATTERN);
        INCREMENT_SINGLE_REGEX = Pattern.compile(SINGLE_PLUGIN_FILE_PATTERN);
        NORMAL_REGEX = Pattern.compile(NORMAL_PLUGIN_FILE_PATTERN);
        MULTI_REGEX = Pattern.compile(MULTI_PLUGIN_FILE_PATTERN);
    }

    static final String getFileName(String plugin) {
        return NORMAL_PREFIX + plugin + EXTENSION;
    }

    /**
     * 通过文件名和文件类型，构建V5FileInfo对象
     *
     * @param f
     * @param type
     * @return
     */
    static final V5FileInfo build(File f, int type) {
        Matcher m = null;
        String fullname = f.getName();
        if (type == INCREMENT_PLUGIN) {
            m = INCREMENT_REGEX.matcher(fullname);
        } else if (type == SINGLE_PLUGIN) {
            m = INCREMENT_SINGLE_REGEX.matcher(fullname);
        } else if (type == MULTI_PLUGIN) {
            m = MULTI_REGEX.matcher(fullname);
        } else {
            m = NORMAL_REGEX.matcher(fullname);
        }
        if (m == null || !m.matches()) {
            if (Constant.LOG_V5_FILE_SEARCH) {
                if (LOG) {
                    LogDebug.d(PLUGIN_TAG, "V5FileInfo.build: skip, no match1, type=" + type + " file=" + f.getAbsolutePath());
                }
            }
            return null;
        }
        MatchResult r = m.toMatchResult();
        if (r == null || r.groupCount() != 1) {
            if (Constant.LOG_V5_FILE_SEARCH) {
                if (LOG) {
                    LogDebug.d(PLUGIN_TAG, "V5FileInfo.build: skip, no match2, type=" + type + " file=" + f.getAbsolutePath());
                }
            }
            return null;
        }
        if (!f.exists() || !f.isFile()) {
            if (Constant.LOG_V5_FILE_SEARCH) {
                if (LOG) {
                    LogDebug.d(PLUGIN_TAG, "V5FileInfo.build: nor exist or file, file=" + f.getAbsolutePath());
                }
            }
            return null;
        }
        V5FileInfo p = new V5FileInfo();
        p.mName = r.group(1);
        p.mFile = f;
        p.mType = type;
        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "V5FileInfo.build: found plugin, name=" + p.mName + " file=" + f.getAbsolutePath());
        }
        return p;
    }

    /**
     * 根据文件名得到插件名
     *
     * @param fullname
     * @param type
     * @return
     */
    public static final String parseName(String fullname, int type) {
        Matcher m = null;
        if (type == INCREMENT_PLUGIN) {
            m = INCREMENT_REGEX.matcher(fullname);
        } else if (type == SINGLE_PLUGIN) {
            m = INCREMENT_SINGLE_REGEX.matcher(fullname);
        } else if (type == MULTI_PLUGIN) {
            m = MULTI_REGEX.matcher(fullname);
        } else {
            m = NORMAL_REGEX.matcher(fullname);
        }
        if (m == null || !m.matches()) {
            return null;
        }
        MatchResult r = m.toMatchResult();
        if (r == null || r.groupCount() != 1) {
            return null;
        }
        return r.group(1);
    }

    /**
     * 获取插件的名称
     *
     * @return
     */
    public String getName() {
        return mName;
    }

    final PluginInfo updateV5FileTo(Context context, File dir, boolean updateNow, boolean verifyCert) {
        return updateV5FileTo(context, dir, true, updateNow, verifyCert);
    }

    final PluginInfo updateV5FileTo(Context context, File dir, boolean checkOverride, boolean updateNow, boolean verifyCert) {
        FileInputStream is = null;
        FileOutputStream os = null;
        DataInputStream dis = null;
        try {
            is = new FileInputStream(mFile);
            dis = new DataInputStream(is);
            int pos = 0;
            // V5头：跳过V5文件头的n个字节
            if (mType == INCREMENT_PLUGIN) {
                dis.skip(V5_FILE_HEADER_SIZE);
                pos += V5_FILE_HEADER_SIZE;
            }
            // 插件基础字段
            int low = dis.readInt();
            pos += 4;
            int high = dis.readInt();
            pos += 4;
            int ver = dis.readInt();
            pos += 4;
            String md5 = dis.readUTF();
            if (md5.length() != 32) {
                if (LOG) {
                    LogDebug.d(PLUGIN_TAG, "update v5 plugin: invalid md5 length: length=" + md5.length() + " name=" + mName);
                }
                return null;
            }
            pos += 2 + md5.length();
            // 扩展字段
            int custom = dis.readInt();
            pos += 4;
            dis.skip(custom);
            pos += custom;
            // 文件长度
            int length = dis.readInt();
            pos += 4;
            //
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "update v5 plugin: low=" + low + " high=" + high + " ver=" + ver + " md5=" + md5 + " custom=" + custom + " length=" + length + " name=" + mName);
            }
            // 校验文件长度如果不一致则返回，则返回
            if (pos + length != mFile.length()) {
                if (LOG) {
                    LogDebug.d(PLUGIN_TAG, "update v5 plugin: invalid length: calc.length=" + (mFile.length() - pos) + " name=" + mName);
                }
                return null;
            }
            // 如果插件版本太低，则返回
            if (low < Constant.ADAPTER_COMPATIBLE_VERSION) {
                if (LOG) {
                    LogDebug.d(PLUGIN_TAG, "update v5 plugin: not supported plugin.low=" + low + " host.compatible.ver=" + Constant.ADAPTER_COMPATIBLE_VERSION + " name=" + mName);
                }
                return null;
            }
            if (high < low || (high - low) > 1024) {
                if (LOG) {
                    LogDebug.d(PLUGIN_TAG, "update v5 plugin: invalid plugin.high=" + high + " plugin.low=" + low);
                }
                return null;
            }

            PluginInfo pluginInfo = PluginInfo.build(mName, low, high, ver);
            if (checkOverride && RePlugin.getConfig().getCallbacks().isPluginBlocked(pluginInfo)) {
                if (LOG) {
                    LogDebug.d(PLUGIN_TAG, "update v5 plugin: failed, plugin is blocked, name=" + mName + ",low=" + low + ",high=" + high + ",ver=" + ver);
                }
                return null;
            }

            // 如果不是立即更新，则延迟再释放
            if (!updateNow) {
                if (LOG) {
                    LogDebug.d(PLUGIN_TAG, "update v5 plugin: delay extract f=" + mFile);
                }
                return PluginInfo.buildV5(mName, low, high, ver, mType, mFile.getAbsolutePath(), -1, -1, -1, null);
            }
            // 目标文件名
            File target = new File(dir, PluginInfo.format(mName, low, high, ver) + ".jar");
            // 如果目标文件存在且校验MD5一致，表示目标文件已是最新，则跳过
            if (target.exists() && target.length() == length) {
                if (LOG) {
                    LogDebug.d(PLUGIN_TAG, "update v5 plugin: checking target ... " + " name=" + mName);
                }
                byte rc[] = SecurityUtil.MD5(target);
                String tmpMD5 = rc != null ? StringUtils.toHexString(rc) : "";
                tmpMD5 = tmpMD5.toLowerCase(Locale.ENGLISH);
                if (md5.equals(tmpMD5)) {
                    if (LOG) {
                        LogDebug.d(PLUGIN_TAG, "update v5 plugin: target match" + " name=" + mName);
                    }
                    return PluginInfo.build(target);
                }
            }
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "update v5 plugin: extract ..." + " name=" + mName);
            }
            //
            File tmpfile = new File(dir, String.format("%s_plugin.tmp", mName));
            FileUtils.copyInputStreamToFile(dis, tmpfile);

            // 检查结果，如果不成功就删除该文件
            boolean deleted = false;

            // 长度校验
            if (tmpfile.length() != length) {
                if (LOG) {
                    LogDebug.d(PLUGIN_TAG, "update v5 plugin: extract file length=" + tmpfile.length() + " expected=" + length);
                }
                deleted = true;
            }

            // MD5校验
            if (!deleted) {
                byte rc[] = SecurityUtil.MD5(tmpfile);
                String tmpMD5 = rc != null ? StringUtils.toHexString(rc) : "";
                tmpMD5 = tmpMD5.toLowerCase(Locale.ENGLISH);
                if (!md5.equals(tmpMD5)) {
                    if (LOG) {
                        LogDebug.d(PLUGIN_TAG, "update v5 plugin: extract=" + tmpMD5 + " orig=" + md5 + ", delete tmpfile" + " name=" + mName);
                    }
                    deleted = true;
                }
            }

            // 证书校验
            if (!deleted) {
                PackageManager pm = context.getPackageManager();
                PackageInfo info = null;
                try {
                    info = PackageUtils.getPackageArchiveInfo(pm, tmpfile.getAbsolutePath(), PackageManager.GET_SIGNATURES);
                } catch (Throwable e) {
                    if (LOG) {
                        LogDebug.d(PLUGIN_TAG, e.getMessage(), e);
                    }
                }
                if (info == null) {
                    if (LOG) {
                        LogDebug.d(PLUGIN_TAG, "update v5 plugin: can't fetch package info: " + " name=" + mName);
                    }
                    deleted = true;
                }
                if (verifyCert) {
                    // 无论Debug还是Release都做下签名校验。只不过Debug下不会删除"校验失败"的文件
                    if (!CertUtils.isPluginSignatures(info)) {
                        if (LOG) {
                            LogDebug.d(PLUGIN_TAG, "update v5 plugin: invalid cert: " + " name=" + mName);
                        }
                        if (LOGR) {
                            LogRelease.e(PLUGIN_TAG, "uv5p ic n=" + mName);
                        }
                        // 只有Release环境才会删除"校验失败"的文件
                        if (!RePluginInternal.FOR_DEV) {
                            deleted = true;
                        }
                    }
                }
                if (LOG) {
                    LogDebug.d(PLUGIN_TAG, "update v5 plugin: package=" + (info != null ? info.packageName : "") + " delete=" + (deleted ? "true" : "false") + " name=" + mName);
                }
            }
            // 释放Native（SO）库文件
            // Added by Jiongxuan Zhang

            // Jar和Dex都释放完了，到这一步，基本上可以创建PluginInfo对象了
            PluginInfo pi = PluginInfo.build(target);
            if (pi == null) {
                // 不太可能走到这里，因为target已经被验证过了
                deleted = true;
            }
            if (!deleted) {
                File libDir = pi.getNativeLibsDir();
                if (!PluginNativeLibsHelper.install(tmpfile.getAbsolutePath(), libDir)) {
                    // 释放失败，删除插件文件
                    deleted = true;
                }
            }

            if (deleted) {
                FileUtils.forceDelete(tmpfile);
                return null;
            }
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "update v5 plugin: extract success" + " name=" + mName);
            }

            if (target.exists()) {
                FileUtils.forceDelete(target);
            }
            
            // 更名
            FileUtils.moveFile(tmpfile, target);
            return pi;
        } catch (Throwable e) {
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, e.getMessage(), e);
            }
        } finally {
            CloseableUtils.closeQuietly(is);
            CloseableUtils.closeQuietly(dis);
        }

        return null;
    }

    /**
     * 查看是否有相应的V5插件存在（不管是否生效）
     * <p>
     * 供V5升级新逻辑使用
     *
     * @param context
     * @param pName
     * @return
     */
    public static PluginInfo fetchPluginInfo(Context context, String pName) {
        File f = new File(context.getFilesDir(), "p-n-" + pName + ".jar");

        if (LOG) {
            LogDebug.d(MAIN_TAG, "needUpdate(): local file =  " + f.getAbsolutePath());
        }

        if (!f.exists()) {

            if (LOG) {
                LogDebug.d(MAIN_TAG, "needUpdate(): file is not exists, file =  " + f.getAbsolutePath());
            }
            return null;
        }

        V5FileInfo p = V5FileInfo.build(f, V5FileInfo.NORMAL_PLUGIN);

        if (p == null) {
            p = V5FileInfo.build(f, V5FileInfo.INCREMENT_PLUGIN);
        }

        if (p == null) {
            p = V5FileInfo.build(f, V5FileInfo.MULTI_PLUGIN);
        }

        if (LOG) {
            LogDebug.d(MAIN_TAG, "needUpdate(): localFileInfo =  " + p);
        }

        if (p != null) {
            return p.updateV5FileTo(context, context.getDir(Constant.LOCAL_PLUGIN_SUB_DIR, 0), false, false, false);
        }

        return null;
    }
}
