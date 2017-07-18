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

import android.database.Cursor;

import java.io.Closeable;
import java.io.IOException;
import java.util.zip.ZipFile;

/**
 * 和关闭流有关的帮助类
 *
 * @author RePlugin Team
 */

public class CloseableUtils {

    /**
     * 大部分Close关闭流，以及实现Closeable的功能可使用此方法
     *
     * @param c Closeable对象，包括Stream等
     */
    public static void closeQuietly(Closeable c) {
        try {
            if (c != null) {
                c.close();
            }
        } catch (final IOException ioe) {
            // ignore
        }
    }

    /**
     * 允许“一口气”关闭多个Closeable的方法
     *
     * @param closeables 多个Closeable对象
     */
    public static void closeQuietly(final Closeable... closeables) {
        if (closeables == null) {
            return;
        }
        for (final Closeable closeable : closeables) {
            closeQuietly(closeable);
        }
    }

    /**
     * 解决API 15及以下的Cursor都没有实现Closeable接口，因此调用Closeable参数会出现转换异常的问题
     * java.lang.IncompatibleClassChangeError: interface not implemented,
     *
     * @param c Cursor对象
     */
    public static void closeQuietly(Cursor c) {
        try {
            if (c != null) {
                c.close();
            }
        } catch (Exception e) {
            // ignore
        }
    }

    /**
     * 解决API 18及以下的ZipFile都没有实现Closeable接口，因此调用Closeable参数会出现转换异常的问题
     * java.lang.IncompatibleClassChangeError: interface not implemented,
     *
     * @param c Cursor对象
     */
    public static void closeQuietly(ZipFile c) {
        try {
            if (c != null) {
                c.close();
            }
        } catch (Exception e) {
            // ignore
        }
    }
}
