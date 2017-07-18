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

package com.qihoo360.replugin.model;

import android.content.Context;
import android.text.TextUtils;

import com.qihoo360.loader2.Constant;
import com.qihoo360.replugin.helper.JSONHelper;
import com.qihoo360.replugin.helper.LogDebug;

import com.qihoo360.replugin.utils.Charsets;
import com.qihoo360.replugin.utils.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author RePlugin Team
 */

public class PluginInfoList implements Iterable<PluginInfo> {

    private static final String TAG = "PluginInfoList";

    private final ConcurrentHashMap<String, PluginInfo> mMap = new ConcurrentHashMap<>();

    // FIXME 确认是否有必要
    private final List<PluginInfo> mList = new ArrayList<>();

    private JSONArray mJson = new JSONArray();

    public void add(PluginInfo pi) {
        if (get(pi.getName()) != null) {
            // 已有？不能再加入
            return;
        }
        mJson.put(pi.getJSON());

        addToMap(pi);
    }

    private void addToMap(PluginInfo pi) {
        mMap.put(pi.getName(), pi);
        mMap.put(pi.getAlias(), pi);
        mList.add(pi);
    }

    public void remove(String pn) {
        for (int i = 0; i < mJson.length(); i++) {
            JSONObject jo = mJson.optJSONObject(i);
            if (TextUtils.equals(pn, jo.optString("name"))) {
                JSONHelper.remove(mJson, i);
            }
        }
        if (mMap.containsKey(pn)) {
            mMap.remove(pn);
        }
        removeListElement(mList, pn);
    }

    private void removeListElement(List<PluginInfo> list, String pn) {
        Iterator<PluginInfo> iterator = list.iterator();
        while(iterator.hasNext()) {
            PluginInfo pluginInfo = iterator.next();
            if(TextUtils.equals(pn, pluginInfo.getName())) {
                iterator.remove();
            }
        }
    }

    public PluginInfo get(String pn) {
        return mMap.get(pn);
    }

    public List<PluginInfo> cloneList() {
        return new ArrayList<>(mList);
    }

    public boolean load(Context context) {
         try {
            // 1. 新建或打开文件
            File d = context.getDir(Constant.LOCAL_PLUGIN_APK_SUB_DIR, 0);
            File f = new File(d, "p.l");
            if (!f.exists()) {
                // 不存在？直接创建一个新的即可
                if (!f.createNewFile()) {
                    if (LogDebug.LOG) {
                        LogDebug.e(TAG, "load: Create error!");
                    }
                    return false;
                } else {
                    if (LogDebug.LOG) {
                        LogDebug.i(TAG, "load: Create a new list file");
                    }
                    return true;
                }
            }

            // 2. 读出字符串
            String result = FileUtils.readFileToString(f, Charsets.UTF_8);
            if (TextUtils.isEmpty(result)) {
                if (LogDebug.LOG) {
                    LogDebug.e(TAG, "load: Read Json error!");
                }
                return false;
            }

            // 3. 解析出JSON
            mJson = new JSONArray(result);

        } catch (IOException e) {
            if (LogDebug.LOG) {
                LogDebug.e(TAG, "load: Load error!", e);
            }
            return false;
        } catch (JSONException e) {
            if (LogDebug.LOG) {
                LogDebug.e(TAG, "load: Parse Json Error!", e);
            }
            return false;
        }

        for (int i = 0; i < mJson.length(); i++) {
            JSONObject jo = mJson.optJSONObject(i);
            if (jo != null) {
                PluginInfo pi = PluginInfo.createByJO(jo);
                if (pi == null) {
                    if (LogDebug.LOG) {
                        LogDebug.e(TAG, "load: PluginInfo Invalid. Ignore! jo=" + jo);
                    }
                    continue;
                }
                addToMap(pi);
            }
        }
        return true;
    }

    public boolean save(Context context) {
        try {
            File d = context.getDir(Constant.LOCAL_PLUGIN_APK_SUB_DIR, 0);
            File f = new File(d, "p.l");
            FileUtils.writeStringToFile(f, mJson.toString(), Charsets.UTF_8);

            return true;
        } catch (IOException e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
            return false;
        }
    }

    @Override
    public Iterator<PluginInfo> iterator() {
        return mList.iterator();
    }
}
