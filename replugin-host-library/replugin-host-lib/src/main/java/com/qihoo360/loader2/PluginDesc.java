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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.qihoo360.replugin.utils.Charsets;
import com.qihoo360.replugin.utils.CloseableUtils;
import com.qihoo360.mobilesafe.core.BuildConfig;
import com.qihoo360.replugin.RePlugin;

import com.qihoo360.replugin.utils.IOUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.HashMap;

/**
 * 获取插件介绍有关的信息，包括：
 * 插件显示名、简介、是否为大插件等。此文件可被云控
 *
 * 注意：此类有别于PluginInfo，前者主要记录版本、路径等
 *
 * @author RePlugin Team
 */
public class PluginDesc {
    private static final String TAG = PluginDesc.class.getSimpleName();
    private static final boolean DEBUG = BuildConfig.DEBUG;

    private static final byte[] INSTANCE_LOCKER = new byte[0];

    private static volatile BroadcastReceiver sUpdateReceiver;
    private static final byte[] REG_RECEIVER_LOCKER = new byte[0];
    private static volatile boolean sChanged;

    public static final String ACTION_UPDATE = "com.qihoo360.mobilesafe.plugin_desc_update";

    private String mDisplay;
    private String mPlugin;
    private String mDesc;
    private boolean mLarge;

    private static volatile HashMap<String, PluginDesc> sMap;

    /**
     * 通过插件名来获取PluginDesc对象
     */
    public static PluginDesc get(String pn) {
        return getCurrentMap().get(pn);
    }

    private static HashMap<String, PluginDesc> getCurrentMap() {
        registerReceiverIfNeeded();
        if (sMap != null && !sChanged) {
            return sMap;
        }
        synchronized (INSTANCE_LOCKER) {
            if (sMap != null && !sChanged) {
                return sMap;
            }
            if (DEBUG) {
                Log.d(TAG, "load(): Change, Ready to load");
            }
            sMap = new HashMap<>();
            load(PMF.getApplicationContext());

            sChanged = false;
        }
        return sMap;
    }

    public PluginDesc(String plugin) {
        mPlugin = plugin;
    }

    /**
     * 获取插件名
     */
    public String getPluginName() {
        return mPlugin;
    }

    /**
     * 下载时显示插件的名字
     * @return 如果display为空，则返回插件名
     */
    public String getDisplayName() {
        if (!TextUtils.isEmpty(mDisplay)) {
            return mDisplay;
        }

        return mPlugin;
    }

    /**
     * 下载时的具体描述信息
     */
    public String getDescription() {
        return mDesc;
    }

    /**
     * 是否为“大插件”，也即首次加载其Activity前，需要弹窗让用户等待
     * 绝大多数插件都比较小，但如“手心”、“通讯录”等可能需要特殊对待
     */
    public boolean isLarge() {
        return mLarge;
    }

    private static boolean load(Context context) {
        JSONArray jsonArray = loadArray(context);
        if (jsonArray == null) {
            return false;
        }
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jo = jsonArray.optJSONObject(i);
            if (jo == null) {
                continue;
            }
            String pn = jo.optString("name");
            if (TextUtils.isEmpty(pn)) {
                continue;
            }

            PluginDesc pi = new PluginDesc(pn);
            pi.mDisplay = jo.optString("display");
            pi.mDesc = jo.optString("desc");
            pi.mLarge = jo.optBoolean("large");
            sMap.put(pn, pi);
        }
        return true;
    }

    private static JSONArray loadArray(Context context) {
        InputStream in;

        // 读取内部配置
        in = null;
        try {
            in = RePlugin.getConfig().getCallbacks().openLatestFile(context, "plugins-list.json");
            if (in != null) {
                String str = IOUtils.toString(in, Charsets.UTF_8);
                return new JSONArray(str);
            }
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, e.getMessage(), e);
            }
        } finally {
            CloseableUtils.closeQuietly(in);
        }

        return null;
    }

    private static void registerReceiverIfNeeded() {
        if (sUpdateReceiver != null) {
            return;
        }
        synchronized (REG_RECEIVER_LOCKER) {
            if (sUpdateReceiver != null) {
                return;
            }
            sUpdateReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    // 标记已改，可以重新Load了
                    if (DEBUG) {
                        Log.d(TAG, "Receiver.onReceive(): Mark change!");
                    }
                    // 重新加载
                    sChanged = true;
                    getCurrentMap();
                }
            };
            IntentFilter filter = new IntentFilter(ACTION_UPDATE);
            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(PMF.getApplicationContext());
            lbm.registerReceiver(sUpdateReceiver, filter);
        }
    }
}
