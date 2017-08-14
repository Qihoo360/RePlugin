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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.RemoteException;
import android.text.TextUtils;

import com.qihoo360.loader2.sp.IPref;
import com.qihoo360.loader2.sp.PrefImpl;
import com.qihoo360.replugin.base.IPC;
import com.qihoo360.replugin.component.process.ProcessPitProviderBase;
import com.qihoo360.replugin.component.process.ProcessPitProviderPersist;
import com.qihoo360.replugin.helper.LogDebug;
import com.qihoo360.replugin.utils.CloseableUtils;

import java.util.Arrays;

import static com.qihoo360.replugin.helper.LogDebug.LOG;
import static com.qihoo360.replugin.helper.LogDebug.PLUGIN_TAG;

/**
 * @author RePlugin Team
 */
public class PluginProviderStub {

    private static final String KEY_METHOD = "main_method";

    private static final String KEY_COOKIE = "cookie";

    private static final String URL_PARAM_KEY_LOADED = "loaded";

    private static final String PROJECTION_MAIN[] = {
        "main"
    };

    private static final String SELECTION_MAIN_BINDER = "main_binder";

    private static final String SELECTION_MAIN_PREF = "main_pref";

    private static final String METHOD_START_PROCESS = "start_process";

    /**
     * 需要枷锁否？
     */
    static PrefImpl sPrefImpl;

    /**
     *
     */
    static IPref sPref;

    public static final Cursor stubMain(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        //
        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "stubMain projection=" + Arrays.toString(projection) + " selection=" + selection);
        }

        if (SELECTION_MAIN_BINDER.equals(selection)) {
            return BinderCursor.queryBinder(PMF.sPluginMgr.getHostBinder());
        }

        if (SELECTION_MAIN_PREF.equals(selection)) {
            // 需要枷锁否？
            initPref();
            return BinderCursor.queryBinder(sPrefImpl);
        }

        return null;
    }

    /**
     * 在目标插件进程中运行
     * @param uri
     * @param values
     * @return
     */
    public static final Uri stubPlugin(Uri uri, ContentValues values) {
        //
        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "stubPlugin values=" + values);
        }

        String method = values.getAsString(KEY_METHOD);

        if (TextUtils.equals(method, METHOD_START_PROCESS)) {
            //
            Uri rc = new Uri.Builder().scheme("content").authority("process").encodedPath("status").encodedQuery(URL_PARAM_KEY_LOADED + "=" + 1).build();
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "plugin provider: return uri=" + rc);
            }

            long cookie = values.getAsLong(KEY_COOKIE);

            // 首次
            if (PMF.sPluginMgr.mLocalCookie == 0L) {
                if (LOG) {
                    LogDebug.d(PLUGIN_TAG, "set cookie: " + cookie);
                }
                //
                PMF.sPluginMgr.mLocalCookie = cookie;
            } else {
                // 常驻进程重新启动了
                if (PMF.sPluginMgr.mLocalCookie != cookie) {
                    if (LOG) {
                        LogDebug.d(PLUGIN_TAG, "reset cookie: " + cookie);
                    }
                    //
                    PMF.sPluginMgr.mLocalCookie = cookie;
                    //
                    PluginProcessMain.connectToHostSvc();
                }
            }

            return rc;
        }

        return null;
    }

    /**
     * @param context
     * @return
     */
    static final IBinder proxyFetchHostBinder(Context context) {
        return proxyFetchHostBinder(context, SELECTION_MAIN_BINDER);
    }

    /**
     * @param context
     * @return
     */
    static final IBinder proxyFetchHostPref(Context context) {
        return proxyFetchHostBinder(context, SELECTION_MAIN_PREF);
    }

    /**
     * @param context
     * @param selection
     * @return
     */
    private static final IBinder proxyFetchHostBinder(Context context, String selection) {
        //
        Cursor cursor = null;
        try {
            Uri uri = ProcessPitProviderPersist.URI;
            cursor = context.getContentResolver().query(uri, PROJECTION_MAIN, selection, null, null);
            if (cursor == null) {
                if (LOG) {
                    LogDebug.d(PLUGIN_TAG, "proxy fetch binder: cursor is null");
                }
                return null;
            }
            while (cursor.moveToNext()) {
                //
            }
            IBinder binder = BinderCursor.getBinder(cursor);
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "proxy fetch binder: binder=" + binder);
            }
            return binder;
        } finally {
            CloseableUtils.closeQuietly(cursor);
        }
    }

    /**
     * @param context
     * @param index
     * @return
     */
    static final boolean proxyStartPluginProcess(Context context, int index) {
        //
        ContentValues values = new ContentValues();
        values.put(KEY_METHOD, METHOD_START_PROCESS);
        values.put(KEY_COOKIE, PMF.sPluginMgr.mLocalCookie);
        Uri uri = context.getContentResolver().insert(ProcessPitProviderBase.buildUri(index), values);
        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "proxyStartPluginProcess insert.rc=" + (uri != null ? uri.toString() : "null"));
        }
        if (uri == null) {
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "proxyStartPluginProcess failed");
            }
            return false;
        }

        return true;
    }

    /**
     * @param context
     * @return
     * @throws RemoteException
     */
    public static final IPref getPref(Context context) throws RemoteException {
        if (sPref == null) {
            if (IPC.isPersistentProcess()) {
                // 需要枷锁否？
                initPref();
            } else {
                IBinder b = PluginProviderStub.proxyFetchHostPref(context);
                b.linkToDeath(new DeathRecipient() {

                    @Override
                    public void binderDied() {
                        sPref = null;
                    }
                }, 0);
                sPref = IPref.Stub.asInterface(b);
            }
        }
        return sPref;
    }

    static final void initPref() {
        if (sPrefImpl == null) {
            sPrefImpl = new PrefImpl();
            sPref = sPrefImpl;
        }
    }
}
