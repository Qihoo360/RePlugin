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

package com.qihoo360.loader.utils;

import android.text.TextUtils;

import com.qihoo360.replugin.helper.LogRelease;

import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.InputStream;

import static com.qihoo360.replugin.helper.LogDebug.LOG;
import static com.qihoo360.replugin.helper.LogRelease.LOGR;

/**
 * @author RePlugin Team
 */
public final class SysUtils {

    private static final String TAG = "Plugin.SysUtils";

    /**
     * 返回当前的进程名
     * @return
     */
    public static String getCurrentProcessName() {
        FileInputStream in = null;
        try {
            String fn = "/proc/self/cmdline";
            in = new FileInputStream(fn);
            byte[] buffer = new byte[256];
            int len = 0;
            int b;
            while ((b = in.read()) > 0 && len < buffer.length) {
                buffer[len++] = (byte) b;
            }
            if (len > 0) {
                String s = new String(buffer, 0, len, "UTF-8");
                return s;
            }
        } catch (Throwable e) {
            if (LOGR) {
                LogRelease.e(TAG, e.getMessage(), e);
            }
        } finally {
            IOUtils.closeQuietly(in);
        }
        return null;
    }

    /**
     * 检测是否模拟器（Release版本不做任何处理，总是返回false）
     * @return
     */
    public static boolean isEmulator() {
        if (LOG) {
            try {
                Process pr = Runtime.getRuntime().exec("getprop ro.hardware");
                int code = pr.waitFor();
                if (code == 0) {
                    InputStream in = pr.getInputStream();
                    byte buffer[] = new byte[1024];
                    try {
                        int rc = IOUtils.read(in, buffer, 0, buffer.length);
                        String str = new String(buffer, 0, rc, "UTF-8");
                        if (!TextUtils.isEmpty(str) && str.contains("goldfish")) {
                            return true;
                        }
                    } catch (Throwable e) {
                        if (LOGR) {
                            LogRelease.e(TAG, e.getMessage(), e);
                        }
                    }
                    IOUtils.closeQuietly(in);
                }
                pr.destroy();
            } catch (Throwable e) {
                if (LOGR) {
                    LogRelease.e(TAG, e.getMessage(), e);
                }
            }
        }
        return false;
    }
}
