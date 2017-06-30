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

package com.qihoo360.replugin.packages;

import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.os.RemoteException;

import com.qihoo360.replugin.helper.LogDebug;
import com.qihoo360.replugin.helper.LogRelease;
import com.qihoo360.replugin.model.PluginInfo;

/**
 * 在UI进程中安装插件。借助其Provider来做。 <p>
 * 这里有两个好处： <p>
 * 1、Android 7.0及以上，若在UI进程中优化Dex，则比非UI进程要快4~5倍（如loan插件从4800ms到900ms） <p>
 *    这和“JIT/AOT”和“空闲时编译”的机制有关，参见Google官方说明，不在此赘述。 <p>
 * 2、系统在加载Dex后会将其mmap到内存中，若仅是为了“安装”而不运行，则完全没必要在“当前进程”中去占用这些内容 <p>
 *    而更应该放到容易释放的UI进程中来做。
 *
 * @author RePlugin Team
 */

public class PluginFastInstallProviderProxy {

    private static final String TAG = "PluginFastInstallPr";

    private static final byte[] LOCK = new byte[0];

    private static ContentProviderClient sProvider;

    /**
     * 根据PluginInfo的信息来通知UI进程去“安装”插件，包括释放Dex等。
     *
     * @param context Context对象
     * @param pi PluginInfo对象
     * @return 安装是否成功
     */
    public static boolean install(Context context, PluginInfo pi) {
        // 若Dex已经释放，则无需处理，直接返回
        if (pi.isDexExtracted()) {
            if (LogDebug.LOG) {
                LogDebug.w(TAG, "install: Already loaded, no need to install. pi=" + pi);
            }
            return true;
        }

        ContentProviderClient cpc = getProvider(context);
        if (cpc == null) {
            return false;
        }

        try {
            int r = cpc.update(PluginFastInstallProvider.CONTENT_URI,
                    PluginFastInstallProvider.makeInstallValues(pi),
                    PluginFastInstallProvider.SELECTION_INSTALL, null);
            if (LogDebug.LOG) {
                LogDebug.i(TAG, "install: Install. pi=" + pi + "; result=" + r);
            }
            return r > 0;
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return false;
    }

    private static ContentProviderClient getProvider(Context context) {
        if (sProvider != null) {
            return sProvider;
        }
        synchronized (LOCK) {
            if (sProvider != null) {
                return sProvider;
            }

            ContentResolver cr = context.getContentResolver();
            if (cr == null) {
                // 不太可能，但保险起见还是返回
                if (LogRelease.LOGR) {
                    LogRelease.e(LogDebug.PLUGIN_TAG, "pipp.gp: cr n");
                }
                return null;
            }

            ContentProviderClient cpc = cr.acquireContentProviderClient(PluginFastInstallProvider.CONTENT_URI);
            if (cpc == null) {
                // 获取Provider失败，可能性不大，先返回空
                if (LogRelease.LOGR) {
                    LogRelease.e(LogDebug.PLUGIN_TAG, "pipp.gp: cpc n");
                }
                return null;
            }

            // 缓存下来，以备后用
            sProvider = cpc;
            return cpc;
        }
    }
}
