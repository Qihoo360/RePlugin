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

package com.qihoo360.replugin.component.provider;

import android.annotation.TargetApi;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ProviderInfo;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.Log;

import com.qihoo360.i.Factory;
import com.qihoo360.i.IPluginManager;
import com.qihoo360.replugin.RePlugin;
import com.qihoo360.replugin.component.ComponentList;
import com.qihoo360.replugin.component.process.PluginProcessHost;
import com.qihoo360.replugin.component.utils.PluginClientHelper;
import com.qihoo360.replugin.helper.LogDebug;
import com.qihoo360.replugin.helper.LogRelease;

import java.io.InputStream;
import java.io.OutputStream;

import static com.qihoo360.replugin.helper.LogDebug.LOG;
import static com.qihoo360.replugin.helper.LogDebug.PLUGIN_TAG;
import static com.qihoo360.replugin.helper.LogRelease.LOGR;

/**
 * 一种能够对【插件】的Provider做增加、删除、改变、查询的接口。
 * 就像使用ContentResolver一样
 *
 * @author RePlugin Team
 */
public class PluginProviderClient {
    private static final String TAG = "PluginProviderClient";
    private static final int PROCESS_UNKNOWN = Integer.MAX_VALUE;

    /**
     * 调用插件里的Provider
     * @see android.content.ContentResolver#acquireContentProviderClient(String)
     */
    public static ContentProviderClient acquireContentProviderClient(Context c, String name) {
        // fixme 如何判断应该使用哪个进程的 provider 呢？
        return c.getContentResolver().acquireContentProviderClient(PluginPitProviderP0.AUTHORITY);
    }
    /**
     * 调用插件里的Provider
     * @see android.content.ContentResolver#query(Uri, String[], String, String[], String)
     */
    public static Cursor query(Context c, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Uri turi = toCalledUri(c, uri);
        return c.getContentResolver().query(turi, projection, selection, selectionArgs, sortOrder);
    }

    /**
     * 调用插件里的Provider
     * @see android.content.ContentResolver#query(Uri, String[], String, String[], String, CancellationSignal)
     */
    @TargetApi(16)
    public static Cursor query(Context c, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder, CancellationSignal cancellationSignal) {
        Uri turi = toCalledUri(c, uri);
        return c.getContentResolver().query(turi, projection, selection, selectionArgs, sortOrder, cancellationSignal);
    }

    /**
     * 调用插件里的Provider
     * @see android.content.ContentResolver#getType(Uri)
     */
    public static String getType(Context c, Uri uri) {
        Uri turi = toCalledUri(c, uri);
        return c.getContentResolver().getType(turi);
    }

    /**
     * 调用插件里的Provider
     * @see android.content.ContentResolver#insert(Uri, ContentValues)
     */
    public static Uri insert(Context c, Uri uri, ContentValues values) {
        Uri turi = toCalledUri(c, uri);
        return c.getContentResolver().insert(turi, values);
    }

    /**
     * 调用插件里的Provider
     * @see android.content.ContentResolver#bulkInsert(Uri, ContentValues[])
     */
    public static int bulkInsert(Context c, Uri uri, ContentValues[] values) {
        Uri turi = toCalledUri(c, uri);
        return c.getContentResolver().bulkInsert(turi, values);
    }

    /**
     * 调用插件里的Provider
     * @see android.content.ContentResolver#delete(Uri, String, String[])
     */
    public static int delete(Context c, Uri uri, String selection, String[] selectionArgs) {
        Uri turi = toCalledUri(c, uri);
        return c.getContentResolver().delete(turi, selection, selectionArgs);
    }

    /**
     * 调用插件里的Provider
     * @see android.content.ContentResolver#update(Uri, ContentValues, String, String[])
     */
    public static int update(Context c, Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Uri turi = toCalledUri(c, uri);
        return c.getContentResolver().update(turi, values, selection, selectionArgs);
    }

    /**
     * 调用插件里的Provider
     *
     * @see android.content.ContentResolver#openInputStream(Uri)
     */
    public static InputStream openInputStream(Context c, Uri uri) {
        try {
            Uri turi = toCalledUri(c, uri);
            return c.getContentResolver().openInputStream(turi);
        } catch (Throwable e) {
            if (LOGR) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 调用插件里的Provider
     *
     * @see android.content.ContentResolver#openOutputStream(Uri)
     */
    public static OutputStream openOutputStream(Context c, Uri uri) {
        try {
            Uri turi = toCalledUri(c, uri);
            return c.getContentResolver().openOutputStream(turi);
        } catch (Throwable e) {
            if (LOGR) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 调用插件里的Provider
     *
     * @see android.content.ContentResolver#openOutputStream(Uri, String)
     */
    public static OutputStream openOutputStream(Context c, Uri uri, String mode) {
        try {
            Uri turi = toCalledUri(c, uri);
            return c.getContentResolver().openOutputStream(turi, mode);
        } catch (Throwable e) {
            if (LOGR) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 调用插件里的Provider
     *
     * @see android.content.ContentResolver#openFileDescriptor(Uri, String)
     */
    public static ParcelFileDescriptor openFileDescriptor(Context c, Uri uri, String mode) {
        try {
            Uri turi = toCalledUri(c, uri);
            return c.getContentResolver().openFileDescriptor(turi, mode);
        } catch (Throwable e) {
            if (LOGR) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 调用插件里的Provider
     *
     * @see android.content.ContentResolver#openFileDescriptor(Uri, String, CancellationSignal)
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static ParcelFileDescriptor openFileDescriptor(Context c, Uri uri, String mode, CancellationSignal cancellationSignal) {
        try {
            Uri turi = toCalledUri(c, uri);
            return c.getContentResolver().openFileDescriptor(turi, mode, cancellationSignal);
        } catch (Throwable e) {
            if (LOGR) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 调用插件里的Provider
     *
     * @see android.content.ContentResolver#registerContentObserver(Uri uri, boolean notifyForDescendents, ContentObserver observer)
     */
    public static void registerContentObserver(Context c, Uri uri, boolean notifyForDescendents, ContentObserver observer) {
        try {
            Uri turi = toCalledUri(c, uri);
            c.getContentResolver().registerContentObserver(turi, notifyForDescendents, observer);
        } catch (Throwable e) {
            if (LOGR) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 调用插件里的Provider
     *
     * @see android.content.ContentResolver#notifyChange(Uri, ContentObserver)
     */
    public static void notifyChange(Context c, Uri uri, ContentObserver observer) {
        Uri turi = toCalledUri(c, uri);
        c.getContentResolver().notifyChange(turi, observer);
    }

    /**
     * 调用插件里的Provider
     *
     * @see android.content.ContentResolver#notifyChange(Uri, ContentObserver, boolean)
     */
    public static void notifyChange(Context c, Uri uri, ContentObserver observer, boolean b) {
        Uri turi = toCalledUri(c, uri);
        c.getContentResolver().notifyChange(turi, observer, b);
    }

    /**
     * 将从【当前】插件或主程序里的URI转化成系统传过来的URI，且由插件Manifest来指定进程。例如：
     * Before:  content://com.qihoo360.contacts.abc/people （Contacts插件，UI）
     * After:   content://com.qihoo360.mobilesafe.PluginUIP/contacts/com.qihoo360.mobilesafe.contacts.abc/people
     *
     * @param c   当前的Context对象。若传递主程序的Context，则直接返回Uri，不作处理。否则就做Uri转换
     * @param uri URI对象
     * @return 转换后可直接在ContentResolver使用的URI
     */
    public static Uri toCalledUri(Context c, Uri uri) {
        String pn = fetchPluginByContext(c, uri);
        if (pn == null) {
            return uri;
        }
        return toCalledUri(c, pn, uri, IPluginManager.PROCESS_AUTO);
    }

    /**
     * 将从插件里的URI转化成系统传过来的URI。可自由指定在哪个进程启动。例如：
     * Before:  content://com.qihoo360.contacts.abc/people （Contacts插件，UI）
     * After:   content://com.qihoo360.mobilesafe.PluginUIP/contacts/com.qihoo360.mobilesafe.contacts.abc/people
     *
     * @param context 当前的Context对象，目前暂无用
     * @param plugin  要使用的插件
     * @param uri     URI对象
     * @param process 进程信息，若为PROCESS_AUTO，则根据插件Manifest来指定进程
     * @return 转换后可直接在ContentResolver使用的URI
     */
    public static Uri toCalledUri(Context context, String plugin, Uri uri, int process) {
        if (TextUtils.isEmpty(plugin)) {
            throw new IllegalArgumentException();
        }
        if (uri == null) {
            throw new IllegalArgumentException();
        }

        if (uri.getAuthority().startsWith(PluginPitProviderBase.AUTHORITY_PREFIX)) {
            // 自己已填好了要使用的插件名（以PluginUIProvider及其它为开头），这里不做处理
            return uri;
        }

        // content://com.qihoo360.mobilesafe.PluginUIP
        if (process == IPluginManager.PROCESS_AUTO) {
            // 直接从插件的Manifest中获取
            process = getProcessByAuthority(plugin, uri.getAuthority());
            if (process == PROCESS_UNKNOWN) {
                // 可能不是插件里的，而是主程序的，直接返回Uri即可
                return uri;
            }
        }

        String au;
        if (process == IPluginManager.PROCESS_PERSIST) {
            au = PluginPitProviderPersist.AUTHORITY;
        } else if (PluginProcessHost.isCustomPluginProcess(process)) {
            au = PluginProcessHost.PROCESS_AUTHORITY_MAP.get(process);
        } else {
            au = PluginPitProviderUI.AUTHORITY;
        }

        // from => content://                                                  com.qihoo360.contacts.abc/people?id=9
        // to   => content://com.qihoo360.mobilesafe.Plugin.NP.UIP/plugin_name/com.qihoo360.contacts.abc/people?id=9
        String newUri = String.format("content://%s/%s/%s", au, plugin, uri.toString().replace("content://", ""));
        return Uri.parse(newUri);
    }

    // 根据Context所带的插件信息，来获取插件名。若获取不到，或者为主程序，则返回Null
    private static String fetchPluginByContext(Context c, Uri uri) {
        // 根据Context的ClassLoader来看到底属于哪个插件，还是只是主程序
        ClassLoader cl = c.getClassLoader();
        String pn = Factory.fetchPluginName(cl);
        if (TextUtils.isEmpty(pn)) {
            // 获得了无效的插件信息，这种情况很少见，故打出错误信息，什么也不做
            if (LOGR) {
                LogRelease.e(PLUGIN_TAG, "ppc.fubc: pn is n. u=" + uri);
            }
            return null;
        } else if (TextUtils.equals(pn, RePlugin.PLUGIN_NAME_MAIN)) {
            // 此Context属于主工程，则也什么都不做。稍后会直接走“主程序的Context”来做处理
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "PluginProviderClient.fubc(): Call Main! u=" + uri);
            }
            return null;
        } else {
            // 返回这个Plugin名字
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "PluginProviderClient.fubc(): Call Plugin! u=" + uri);
            }
            return pn;
        }
    }

    private static int getProcessByAuthority(String pn, String authority) {
        // 开始尝试获取插件的ServiceInfo
        ComponentList col = Factory.queryPluginComponentList(pn);
        if (col == null) {
            if (LogDebug.LOG) {
                Log.e(TAG, "getProcessByAuthority(): Fetch Component List Error! pn=" + pn + "; au=" + authority);
            }
            return PROCESS_UNKNOWN;
        }
        ProviderInfo si = col.getProviderByAuthority(authority);
        if (si == null) {
            if (LogDebug.LOG) {
                Log.e(TAG, "getProcessByAuthority(): Not register! pn=" + pn + "; au=" + authority);
            }
            return PROCESS_UNKNOWN;
        }
        int p = PluginClientHelper.getProcessInt(si.processName);
        if (LogDebug.LOG) {
            Log.d(TAG, "getProcessByAuthority(): Okay! Process=" + p + "; pn=" + pn);
        }
        return p;
    }
}
