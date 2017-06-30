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

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.qihoo360.replugin.helper.LogDebug;

import org.json.JSONException;
import org.json.JSONObject;

import static com.qihoo360.replugin.helper.LogDebug.LOG;
import static com.qihoo360.replugin.helper.LogDebug.PLUGIN_TAG;

/**
 * 用来管理插件的状态：正常运行、被禁用，还是其它情况
 *
 * @author RePlugin Team
 */
public class PluginStatusController {
    private static final String PREF_FILE = "plugins";
    private static final String KEY_STATUS_NAME_PREFIX = "ps-";

    /**
     * 表示插件是正常的
     * 值：0（小于0的结果都是异常情况，可直接判断）
     */
    public static final int STATUS_OK = 0;

    /**
     * 因为崩溃次数过多而被禁用
     * 值：-1
     */
    public static final int STATUS_DISABLE_BY_CRASH = -1;

    /**
     * 因为被云端Push而禁用
     * 值：-2
     */
    public static final int STATUS_DISABLE_BY_CLOUD = -2;

    @SuppressLint("StaticFieldLeak")
    private static Application sAppContext;

    /**
     * 设置指定版本的插件的状态
     *
     * @param pn 要修改状态的插件名
     * @param ver 要修改状态的插件的版本号。若设置状态为OK，则忽略此参数
     * @param status 最终修改的状态
     */
    public static void setStatus(String pn, int ver, int status) {
        // 若要设置状态为“OK”，不管版本为何，都需删除此插件的状态记录（解禁）
        if (status == STATUS_OK) {
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "PStatusC.setStatus(): Status is OK, Clear. pn=" + pn + "; ver=" + ver);
            }
            removeStatusToPref(sAppContext, pn);
            return;
        }
        PluginStatus ps = new PluginStatus(pn, ver, status);
        addStatusToPref(sAppContext, pn, ps.toJsonString());

        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "PStatusC.setStatus(): Set Status, pn=" + pn + "; ver=" + ver + "; st=" + status);
        }
    }

    /**
     * 不关心版本，直接获取当前插件的状态
     *
     * @param pn 要获取状态的插件名
     * @return PluginStatus中的任何一个常量
     */
    public static int getStatus(String pn) {
        // 注意：不能使用MP.getPlugin来获取版本号，因为此时插件已“无效”，自然不会有PluginInfo
        // PluginInfo pi = MP.getPlugin(pn);
        return getStatus(pn, -1);
    }

    /**
     * 获取指定版本的插件的状态
     *
     * @param pn 要获取状态的插件名
     * @param ver 要获取状态的插件的版本号
     * @return PluginStatus中的任何一个常量
     */
    public static int getStatus(String pn, int ver) {
        PluginStatus ps = getStatusImpl(pn);

        // 获取PS有任何异常？直接返回
        if (ps == null) {
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "PStatusC.getStatus(): ps is null. pn=" + pn);
            }
            return STATUS_OK;
        }

        // 不是此版本，可直接忽略
        if (ver != -1 && ps.getVersion() != ver) {
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "PStatusC.getStatus(): ver not match. ver=" + ver + "; expect=" + ps.getVersion() + "; pn=" + pn);
            }
            return STATUS_OK;
        }
        int st = ps.getStatus();
        if (LOG) {
            LogDebug.d(PLUGIN_TAG, "PStatusC.getStatus(): ver match. ver=" + ver + "; pn=" + pn + "; st=" + st);
        }
        return st;
    }

    /**
     * 清除所有插件的状态（完全解禁）
     * 通常在“卫士主程序”升级上来以后才会生效
     */
    public static void clearStatus() {
        SharedPreferences pref = sAppContext.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor e = pref.edit();
        for (String key : pref.getAll().keySet()) {
            if (key.contains(KEY_STATUS_NAME_PREFIX)) {
                e.remove(key);
            }
        }
        e.commit();
    }

    /** 设置ApplicationContext，仅在MobileSafeApplication中使用 */
    public static void setAppContext(Application context) {
        sAppContext = context;
    }

    private static PluginStatus getStatusImpl(String pn) {
        String pst = getStatusFromPref(sAppContext, pn);
        if (TextUtils.isEmpty(pst)) {
            return null;
        }

        PluginStatus ps;
        try {
            ps = new PluginStatus(pst);
        } catch (JSONException e) {
            // 解析出错，删除
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "PStatusC.getStatus(): json err.", e);
            }
            removeStatusToPref(sAppContext, pn);
            return null;
        }
        return ps;
    }

    private static void addStatusToPref(Context context, String pn, String json) {
        SharedPreferences pref = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_STATUS_NAME_PREFIX + pn, json).commit();
    }

    private static void removeStatusToPref(Context context, String pn) {
        SharedPreferences pref = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
        pref.edit().remove(KEY_STATUS_NAME_PREFIX + pn).commit();
    }

    private static String getStatusFromPref(Context context, String pn) {
        SharedPreferences pref = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
        return pref.getString(KEY_STATUS_NAME_PREFIX + pn, null);
    }

    private static class PluginStatus {

        JSONObject mJo;

        PluginStatus(String pn, int ver, int status) {
            try {
                mJo = new JSONObject();
                mJo.put("pn", pn);
                mJo.put("ver", ver);
                mJo.put("ctime", System.currentTimeMillis());
                mJo.put("st", status);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        PluginStatus(String json) throws JSONException {
            mJo = new JSONObject(json);
        }

        public int getVersion() {
            return mJo.optInt("ver");
        }

        public long getChangeTime() {
            return mJo.optLong("ctime");
        }

        public int getStatus() {
            return mJo.optInt("st");
        }

        String toJsonString() {
            return mJo.toString();
        }
    }
}
