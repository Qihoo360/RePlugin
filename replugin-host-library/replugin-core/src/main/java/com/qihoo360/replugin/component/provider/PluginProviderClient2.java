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
import android.database.Cursor;
import android.net.Uri;
import android.os.CancellationSignal;
import android.os.RemoteException;
import android.util.Log;

import com.qihoo360.replugin.helper.LogDebug;

import static com.qihoo360.replugin.component.provider.PluginProviderClient.toCalledUri;

/**
 * 此工具类为插件提供 ContentProviderClient 的转换接口
 *
 * @author RePlugin Team
 */
public class PluginProviderClient2 {

    private static final String TAG = "PluginProviderClient2";

    /**
     * 调用插件里的Provider
     *
     * @see android.content.ContentProviderClient#query(Uri, String[], String, String[], String)
     */
    public static Cursor query(Context c, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        ContentProviderClient client = PluginProviderClient.acquireContentProviderClient(c, "");
        if (client != null) {
            try {
                Uri toUri = toCalledUri(c, uri);
                return client.query(toUri, projection, selection, selectionArgs, sortOrder);
            } catch (RemoteException e) {
                if (LogDebug.LOG) {
                    Log.d(TAG, e.toString());
                }
            }
        }
        if (LogDebug.LOG) {
            Log.d(TAG, String.format("call query1 %s fail", uri.toString()));
        }
        return null;
    }

    /**
     * 调用插件里的Provider
     *
     * @see android.content.ContentProviderClient#query(Uri, String[], String, String[], String, CancellationSignal)
     */
    @TargetApi(16)
    public static Cursor query(Context c, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder, CancellationSignal cancellationSignal) {
        ContentProviderClient client = PluginProviderClient.acquireContentProviderClient(c, "");
        if (client != null) {
            try {
                Uri toUri = toCalledUri(c, uri);
                return client.query(toUri, projection, selection, selectionArgs, sortOrder, cancellationSignal);
            } catch (RemoteException e) {
                if (LogDebug.LOG) {
                    Log.d(TAG, e.toString());
                }
            }
        }

        if (LogDebug.LOG) {
            Log.d(TAG, String.format("call query2 %s fail", uri.toString()));
        }

        return null;
    }

    /**
     * 调用插件里的Provider
     *
     * @see android.content.ContentProviderClient#update(Uri, ContentValues, String, String[])
     */
    public static int update(Context c, Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        ContentProviderClient client = PluginProviderClient.acquireContentProviderClient(c, "");
        if (client != null) {
            try {
                Uri toUri = toCalledUri(c, uri);
                return client.update(toUri, values, selection, selectionArgs);
            } catch (RemoteException e) {
                if (LogDebug.LOG) {
                    Log.d(TAG, e.toString());
                }
            }
        }
        if (LogDebug.LOG) {
            Log.d(TAG, String.format("call update %s", uri.toString()));
        }
        return -1;
    }
}
