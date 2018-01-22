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

package com.qihoo360.replugin.loader.p;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.CancellationSignal;

import com.qihoo360.replugin.MethodInvoker;
import com.qihoo360.replugin.RePluginFramework;
import com.qihoo360.replugin.helper.LogDebug;

/**
 * 一种能够对【插件】的Provider做增加、删除、改变、查询的接口。
 * 就像使用ContentResolver一样
 *
 * @author RePlugin Team
 */
public class PluginProviderClient {

    /**
     * 调用插件里的Provider
     *
     * @see android.content.ContentResolver#query(Uri, String[], String, String[], String)
     */
    public static Cursor query(Context c, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (c == null) {
            return null;
        }

        if (!RePluginFramework.mHostInitialized) {
            return c.getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);
        }

        try {
            return (Cursor) ProxyRePluginProviderClientVar.query.call(null, c, uri, projection, selection, selectionArgs, sortOrder);
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * 调用插件里的Provider
     *
     * @see android.content.ContentResolver#query(Uri, String[], String, String[], String, CancellationSignal)
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static Cursor query(Context c, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder, CancellationSignal cancellationSignal) {
        if (c == null) {
            return null;
        }

        if (!RePluginFramework.mHostInitialized) {
            return c.getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder, cancellationSignal);
        }

        try {
            return (Cursor) ProxyRePluginProviderClientVar.query2.call(null, c, uri, projection, selection, selectionArgs, sortOrder, cancellationSignal);
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * 调用插件里的Provider
     *
     * @see android.content.ContentResolver#insert(Uri, ContentValues)
     */
    public static Uri insert(Context c, Uri uri, ContentValues values) {
        if (c == null) {
            return null;
        }

        if (!RePluginFramework.mHostInitialized) {
            return c.getContentResolver().insert(uri, values);
        }

        try {
            return (Uri) ProxyRePluginProviderClientVar.insert.call(null, c, uri, values);
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * 调用插件里的Provider
     *
     * @see android.content.ContentResolver#bulkInsert(Uri, ContentValues[])
     */
    public static int bulkInsert(Context c, Uri uri, ContentValues[] values) {
        if (c == null) {
            return 0;
        }

        if (!RePluginFramework.mHostInitialized) {
            return c.getContentResolver().bulkInsert(uri, values);
        }

        try {
            Object obj = ProxyRePluginProviderClientVar.bulkInsert.call(null, c, uri, values);
            if (obj != null) {
                return (Integer) obj;
            }
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }

        return -1;
    }

    /**
     * 调用插件里的Provider
     *
     * @see android.content.ContentResolver#delete(Uri, String, String[])
     */
    public static int delete(Context c, Uri uri, String selection, String[] selectionArgs) {
        if (c == null) {
            return 0;
        }

        if (!RePluginFramework.mHostInitialized) {
            return c.getContentResolver().delete(uri, selection, selectionArgs);
        }

        try {
            Object obj = ProxyRePluginProviderClientVar.delete.call(null, c, uri, selection, selectionArgs);
            if (obj != null) {
                return (Integer) obj;
            }
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }

        return -1;
    }

    /**
     * 调用插件里的Provider
     *
     * @see android.content.ContentResolver#update(Uri, ContentValues, String, String[])
     */
    public static int update(Context c, Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (c == null) {
            return 0;
        }

        if (!RePluginFramework.mHostInitialized) {
            return c.getContentResolver().update(uri, values, selection, selectionArgs);
        }

        try {
            Object obj = ProxyRePluginProviderClientVar.update.call(null, c, uri);
            if (obj != null) {
                return (Integer) obj;
            }
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }

        return -1;
    }

    public static class ProxyRePluginProviderClientVar {

        private static MethodInvoker query;

        private static MethodInvoker query2;

        private static MethodInvoker insert;

        private static MethodInvoker bulkInsert;

        private static MethodInvoker delete;

        private static MethodInvoker update;

        public static void initLocked(final ClassLoader classLoader) {
            //
            String rePluginProviderClient = "com.qihoo360.loader2.mgr.PluginProviderClient";
            query = new MethodInvoker(classLoader, rePluginProviderClient, "query", new Class<?>[]{Context.class, Uri.class, String[].class, String.class, String[].class, String.class});

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                query2 = new MethodInvoker(classLoader, rePluginProviderClient, "query", new Class<?>[]{Context.class, Uri.class, String[].class, String.class, String[].class, String.class, CancellationSignal.class});
            }

            insert = new MethodInvoker(classLoader, rePluginProviderClient, "insert", new Class<?>[]{Context.class, Uri.class, ContentValues.class});
            bulkInsert = new MethodInvoker(classLoader, rePluginProviderClient, "bulkInsert", new Class<?>[]{Context.class, Uri.class, ContentValues[].class});
            delete = new MethodInvoker(classLoader, rePluginProviderClient, "delete", new Class<?>[]{Context.class, Uri.class, String.class, String[].class});
            update = new MethodInvoker(classLoader, rePluginProviderClient, "update", new Class<?>[]{Context.class, Uri.class, ContentValues.class, String.class, String[].class});
        }
    }
}

