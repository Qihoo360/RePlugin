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
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;

import com.qihoo360.replugin.MethodInvoker;
import com.qihoo360.replugin.RePluginFramework;
import com.qihoo360.replugin.helper.LogDebug;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;

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
            Object obj = ProxyRePluginProviderClientVar.update.call(null, c, uri, values, selection, selectionArgs);
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
     * @see android.content.ContentResolver#getType(Uri)
     */
    public static String getType(Context c, Uri uri) {
        if (c == null) {
            return null;
        }

        if (!RePluginFramework.mHostInitialized) {
            return c.getContentResolver().getType(uri);
        }

        try {
            Object obj = ProxyRePluginProviderClientVar.getType.call(null, c, uri);
            if (obj != null) {
                return (String) obj;
            }
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public static InputStream openInputStream(Context c, Uri uri) {
        if (c == null) {
            return null;
        }

        if (!RePluginFramework.mHostInitialized) {
            try {
                return c.getContentResolver().openInputStream(uri);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        }

        try {
            Object obj = ProxyRePluginProviderClientVar.openInputStream.call(null, c, uri);
            if (obj != null) {
                return (InputStream) obj;
            }
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public static OutputStream openOutputStream(Context c, Uri uri) {
        if (c == null) {
            return null;
        }

        if (!RePluginFramework.mHostInitialized) {
            try {
                return c.getContentResolver().openOutputStream(uri);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        }

        try {
            Object obj = ProxyRePluginProviderClientVar.openOutputStream.call(null, c, uri);
            if (obj != null) {
                return (OutputStream) obj;
            }
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }

        return null;
    }

    @TargetApi(3)
    public static OutputStream openOutputStream(Context c, Uri uri, String mode) {
        if (c == null) {
            return null;
        }

        if (!RePluginFramework.mHostInitialized) {
            try {
                return c.getContentResolver().openOutputStream(uri, mode);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        }

        try {
            Object obj = ProxyRePluginProviderClientVar.openOutputStream2.call(null, c, uri, mode);
            if (obj != null) {
                return (OutputStream) obj;
            }
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }

        return null;
    }


    public static ParcelFileDescriptor openFileDescriptor(Context c, Uri uri, String mode) {
        if (c == null) {
            return null;
        }

        if (!RePluginFramework.mHostInitialized) {
            try {
                return c.getContentResolver().openFileDescriptor(uri, mode);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        }

        try {
            Object obj = ProxyRePluginProviderClientVar.openFileDescriptor.call(null, c, uri, mode);
            if (obj != null) {
                return (ParcelFileDescriptor) obj;
            }
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }

        return null;
    }

    @TargetApi(19)
    public static ParcelFileDescriptor openFileDescriptor(Context c, Uri uri, String mode, CancellationSignal cancellationSignal) {
        if (c == null) {
            return null;
        }

        if (!RePluginFramework.mHostInitialized) {
            try {
                return c.getContentResolver().openFileDescriptor(uri, mode, cancellationSignal);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        }

        try {
            Object obj = ProxyRePluginProviderClientVar.openFileDescriptor2.call(null, c, uri, mode, cancellationSignal);
            if (obj != null) {
                return (ParcelFileDescriptor) obj;
            }
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public static void registerContentObserver(Context c, Uri uri, boolean notifyForDescendents, ContentObserver observer) {
        if (c == null) {
            return;
        }

        if (!RePluginFramework.mHostInitialized) {
            c.getContentResolver().registerContentObserver(uri, notifyForDescendents, observer);
            return;
        }

        try {
            ProxyRePluginProviderClientVar.registerContentObserver.call(null, c, uri, notifyForDescendents, observer);
            return;
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }

        return;
    }


    /**
     * TODO 支持{@link android.content.ContentResolver#acquireContentProviderClient(Uri)}
     *
     * @param c
     * @param name
     * @return
     */
    @TargetApi(Build.VERSION_CODES.ECLAIR)
    public static ContentProviderClient acquireContentProviderClient(Context c, String name) {
        if (c == null) {
            return null;
        }

        if (!RePluginFramework.mHostInitialized) {
            return c.getContentResolver().acquireContentProviderClient(name);
        }

        try {
            Object obj = ProxyRePluginProviderClientVar.acquireContentProviderClient.call(null, c, name);
            if (obj != null) {
                return (ContentProviderClient) obj;
            }
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static void notifyChange(Context c, Uri uri, ContentObserver observer) {
        if (c == null) {
            return;
        }

        if (!RePluginFramework.mHostInitialized) {
            c.getContentResolver().notifyChange(uri, observer);
            return;
        }

        try {
            ProxyRePluginProviderClientVar.notifyChange.call(null, c, uri, observer);
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }
    }
    public static void notifyChange(Context c, Uri uri, ContentObserver observer, boolean b) {
        if (c == null) {
            return;
        }

        if (!RePluginFramework.mHostInitialized) {
            c.getContentResolver().notifyChange(uri, observer, b);
            return;
        }

        try {
            ProxyRePluginProviderClientVar.notifyChange2.call(null, c, uri, observer, b);
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }
    }

    public static Uri toCalledUri(Context c, Uri uri) {
        if (c == null) {
            return null;
        }

        if (!RePluginFramework.mHostInitialized) {
            return uri;
        }

        try {
            Object obj = ProxyRePluginProviderClientVar.toCalledUri.call(null, c, uri);
            if (obj != null) {
                return (Uri) obj;
            }
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public static Uri toCalledUri(Context c, String plugin, Uri uri, int process) {
        if (c == null) {
            return null;
        }

        if (!RePluginFramework.mHostInitialized) {
            return uri;
        }

        try {
            Object obj = ProxyRePluginProviderClientVar.toCalledUri2.call(null, c, plugin, uri, process);
            if (obj != null) {
                return (Uri) obj;
            }
        } catch (Exception e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public static class ProxyRePluginProviderClientVar {

        private static MethodInvoker query;

        private static MethodInvoker query2;

        private static MethodInvoker insert;

        private static MethodInvoker bulkInsert;

        private static MethodInvoker delete;

        private static MethodInvoker update;

        private static MethodInvoker getType;

        private static MethodInvoker openInputStream;

        private static MethodInvoker openOutputStream;

        private static MethodInvoker openOutputStream2;

        private static MethodInvoker openFileDescriptor;

        private static MethodInvoker openFileDescriptor2;

        private static MethodInvoker registerContentObserver;

        private static MethodInvoker acquireContentProviderClient;

        private static MethodInvoker notifyChange;

        private static MethodInvoker notifyChange2;

        private static MethodInvoker toCalledUri;

        private static MethodInvoker toCalledUri2;

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
            // new supported
            getType = new MethodInvoker(classLoader, rePluginProviderClient, "getType", new Class<?>[]{Context.class, Uri.class});
            openInputStream = new MethodInvoker(classLoader, rePluginProviderClient, "openInputStream", new Class<?>[]{Context.class, Uri.class});
            openOutputStream = new MethodInvoker(classLoader, rePluginProviderClient, "openOutputStream", new Class<?>[]{Context.class, Uri.class});
            openOutputStream2 = new MethodInvoker(classLoader, rePluginProviderClient, "openOutputStream", new Class<?>[]{Context.class, Uri.class, String.class});
            openFileDescriptor = new MethodInvoker(classLoader, rePluginProviderClient, "openFileDescriptor", new Class<?>[]{Context.class, Uri.class, String.class});
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                openFileDescriptor2 = new MethodInvoker(classLoader, rePluginProviderClient, "openFileDescriptor", new Class<?>[]{Context.class, Uri.class, String.class, CancellationSignal.class});
            }
            registerContentObserver = new MethodInvoker(classLoader, rePluginProviderClient, "registerContentObserver", new Class<?>[]{Context.class, Uri.class, Boolean.class, ContentObserver.class});
            acquireContentProviderClient = new MethodInvoker(classLoader, rePluginProviderClient, "acquireContentProviderClient", new Class<?>[]{Context.class, String.class});
            notifyChange = new MethodInvoker(classLoader, rePluginProviderClient, "notifyChange", new Class<?>[]{Context.class, Uri.class, ContentObserver.class});
            notifyChange2 = new MethodInvoker(classLoader, rePluginProviderClient, "notifyChange", new Class<?>[]{Context.class, Uri.class, ContentObserver.class, Boolean.class});
            toCalledUri = new MethodInvoker(classLoader, rePluginProviderClient, "toCalledUri", new Class<?>[]{Context.class, Uri.class});
            toCalledUri2 = new MethodInvoker(classLoader, rePluginProviderClient, "toCalledUri", new Class<?>[]{Context.class, String.class, Uri.class, Integer.class});
        }
    }
}

