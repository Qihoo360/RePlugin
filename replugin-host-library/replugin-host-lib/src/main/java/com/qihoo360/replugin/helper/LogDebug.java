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

package com.qihoo360.replugin.helper;

import android.os.Build;
import android.os.Debug;
import android.util.Log;

import com.qihoo360.replugin.RePluginInternal;
import com.qihoo360.replugin.base.IPC;
import com.qihoo360.replugin.model.PluginInfo;

/**
 * 只在Debug环境下才输出的各种日志，只有当setDebug为true时才会出来
 * <p>
 * 注意：Release版不会输出，而且会被Proguard删除掉
 *
 * @author RePlugin Team
 */

public class LogDebug {
    public static final String TAG = "RePlugin";

    private static final String TAG_PREFIX = TAG + ".";

    /**
     * 是否输出日志？若用的是nolog编译出的AAR，则这里为False
     * 注意：所有使用LogDebug前，必须先用此字段来做判断。这样Release阶段才会被彻底删除掉
     * 如：
     * <code>
     * if (LogDebug.LOG) {
     * LogDebug.v("xxx", "yyy");
     * }
     * </code>
     */
    public static final boolean LOG = RePluginInternal.FOR_DEV;

    /**
     * 允许Dump出一些内容
     */
    public static final boolean DUMP_ENABLED = LOG;

    /**
     * Send a verbose log message.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static int v(String tag, String msg) {
        if (RePluginInternal.FOR_DEV) {
            return Log.v(TAG_PREFIX + tag, msg);
        }
        return -1;
    }

    /**
     * Send a verbose log message and log the exception.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr  An exception to log
     */
    public static int v(String tag, String msg, Throwable tr) {
        if (RePluginInternal.FOR_DEV) {
            return Log.v(TAG_PREFIX + tag, msg, tr);
        }
        return -1;
    }

    /**
     * Send a debug log message.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static int d(String tag, String msg) {
        if (RePluginInternal.FOR_DEV) {
            return Log.d(TAG_PREFIX + tag, msg);
        }
        return -1;
    }

    /**
     * Send a debug log message and log the exception.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr  An exception to log
     */
    public static int d(String tag, String msg, Throwable tr) {
        if (RePluginInternal.FOR_DEV) {
            return Log.d(TAG_PREFIX + tag, msg, tr);
        }
        return -1;
    }

    /**
     * Send an info log message.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static int i(String tag, String msg) {
        if (RePluginInternal.FOR_DEV) {
            return Log.i(TAG_PREFIX + tag, msg);
        }
        return -1;
    }

    /**
     * Send a inifo log message and log the exception.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr  An exception to log
     */
    public static int i(String tag, String msg, Throwable tr) {
        if (RePluginInternal.FOR_DEV) {
            return Log.i(TAG_PREFIX + tag, msg, tr);
        }
        return -1;
    }

    /**
     * Send a warning log message.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static int w(String tag, String msg) {
        if (RePluginInternal.FOR_DEV) {
            return Log.w(TAG_PREFIX + tag, msg);
        }
        return -1;
    }

    /**
     * Send a warning log message and log the exception.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr  An exception to log
     */
    public static int w(String tag, String msg, Throwable tr) {
        if (RePluginInternal.FOR_DEV) {
            return Log.w(TAG_PREFIX + tag, msg, tr);
        }
        return -1;
    }

    /**
     * Send a warning log message and log the exception.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param tr  An exception to log
     */
    public static int w(String tag, Throwable tr) {
        if (RePluginInternal.FOR_DEV) {
            return Log.w(TAG_PREFIX + tag, tr);
        }
        return -1;
    }

    /**
     * Send an error log message.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static int e(String tag, String msg) {
        if (RePluginInternal.FOR_DEV) {
            return Log.e(TAG_PREFIX + tag, msg);
        }
        return -1;
    }

    /**
     * Send a error log message and log the exception.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr  An exception to log
     */
    public static int e(String tag, String msg, Throwable tr) {
        if (RePluginInternal.FOR_DEV) {
            return Log.e(TAG_PREFIX + tag, msg, tr);
        }
        return -1;
    }

    /**
     * 打印当前内存占用日志，方便外界诊断。注意，这会显著消耗性能（约50ms左右）
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static int printMemoryStatus(String tag, String msg) {
        if (RePluginInternal.FOR_DEV) {
            Debug.MemoryInfo mi = new Debug.MemoryInfo();
            Debug.getMemoryInfo(mi);

            String mit = "desc=, memory_v_0_0_1, process=, " + IPC.getCurrentProcessName() +
                    ", totalPss=, " + mi.getTotalPss() +
                    ", dalvikPss=, " + mi.dalvikPss +
                    ", nativeSize=, " + mi.nativePss +
                    ", otherPss=, " + mi.otherPss + ", ";

            return Log.i(tag + "-MEMORY", mit + msg);
        }
        return -1;
    }

    /**
     * 打印当前内存占用日志，方便外界诊断。注意，这会显著消耗性能（约50ms左右）
     *
     * @param pi
     * @param load
     * @return
     */
    public static int printPluginInfo(PluginInfo pi, int load) {
        long apk = pi.getApkFile().length();
        long dex = pi.getDexFile().length();
        return printMemoryStatus(TAG, "act=, loadLocked, flag=, Start, pn=, " + pi.getName() + ", type=, " + load +
                ", apk=, " + apk + ", odex=, " + dex + ", sys_api=, " + Build.VERSION.SDK_INT);
    }

    /**
     * @deprecated 为兼容卫士，以后干掉
     */
    public static final String PLUGIN_TAG = "ws001";

    /**
     * @deprecated 为兼容卫士，以后干掉
     */
    public static final String MAIN_TAG = "ws000";

    /**
     * @deprecated 为兼容卫士，以后干掉
     */
    public static final String MISC_TAG = "ws002";

    /**
     * createClassLoader TAG
     */
    public static final String LOADER_TAG = "createClassLoader";
}
