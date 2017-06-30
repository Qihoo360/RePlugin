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

package com.qihoo360.replugin.component.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.text.TextUtils;
import android.util.Log;

import com.qihoo360.i.Factory;
import com.qihoo360.i.IPluginManager;
import com.qihoo360.loader2.IPluginClient;
import com.qihoo360.loader2.IPluginHost;
import com.qihoo360.loader2.MP;
import com.qihoo360.loader2.PluginBinderInfo;
import com.qihoo360.loader2.PluginProcessMain;
import com.qihoo360.replugin.component.ComponentList;
import com.qihoo360.replugin.component.utils.PluginClientHelper;
import com.qihoo360.replugin.helper.LogDebug;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.qihoo360.replugin.helper.LogDebug.LOG;

/**
 * @author RePlugin Team
 */
public class PluginReceiverProxy extends BroadcastReceiver {

    public static final String TAG = "ms-receiver";

    private HashMap<String, HashMap<String, List<String>>> mActionPluginComponents;

    /**
     * 保存 Receiver 与 process 的关系
     */
    private final HashMap<String, Integer> mReceiverProcess = new HashMap<>();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || mActionPluginComponents == null) {
            return;
        }

        String action = intent.getAction();
        if (!TextUtils.isEmpty(action)) {

            if (LOG) {
                LogDebug.d(TAG, String.format("代理 Receiver 收到 action: %s ", action));
            }

            // 根据 action 取得 map<plugin, List<receiver>>
            HashMap<String, List<String>> pc = mActionPluginComponents.get(action);
            if (pc != null) {

                // 遍历每一个插件
                for (HashMap.Entry<String, List<String>> entry : pc.entrySet()) {
                    String plugin = entry.getKey();
                    if (entry.getValue() == null) {
                        continue;
                    }

                    // 拷贝数据，防止多线程问题
                    List<String> receivers = new ArrayList<>(entry.getValue());
                    // 此插件所有声明的 receiver
                    for (String receiver : receivers) {
                        try {
                            // 在对应进程接收广播, 如果进程未启动，则拉起之
                            int process = getProcessOfReceiver(plugin, receiver);

                            // todo 合并 IPluginClient 和 IPluginHost
                            if (process == IPluginManager.PROCESS_PERSIST) {
                                IPluginHost host = PluginProcessMain.getPluginHost();
                                host.onReceive(plugin, receiver, intent);
                            } else {
                                IPluginClient client = MP.startPluginProcess(plugin, process, new PluginBinderInfo(PluginBinderInfo.NONE_REQUEST));
                                client.onReceive(plugin, receiver, intent);
                            }

                        } catch (Throwable e) {
                            if (LOG) {
                                Log.d(TAG, e.toString());
                            }
                        }
                    }
                }
            }
        }
    }

    public static Class loadClassSafety(ClassLoader classLoader, String className) throws ClassNotFoundException {
        return classLoader.loadClass(className);
    }

    /**
     * 获取插件 plugin 的 receiver 所在进程 ID
     *
     * @param plugin   插件名称
     * @param receiver Receiver 名称
     * @return Receiver 声明的进程ID
     */
    private int getProcessOfReceiver(String plugin, String receiver) {
        String key = plugin + "-" + receiver;
        if (!mReceiverProcess.containsKey(key)) {

            // 获得 ActivityInfo
            ComponentList components = Factory.queryPluginComponentList(plugin);
            if (components != null) {

                Map<String, ActivityInfo> receiverMap = components.getReceiverMap();
                if (receiverMap != null) {

                    ActivityInfo ai = receiverMap.get(receiver);
                    if (ai != null) {
                        mReceiverProcess.put(key, PluginClientHelper.getProcessInt(ai.processName));
                    }
                }
            }
        }
        if (mReceiverProcess.containsKey(key)) {
            return mReceiverProcess.get(key);
        }
        return IPluginManager.PROCESS_UI;
    }

    public void setActionPluginMap(HashMap<String, HashMap<String, List<String>>> map) {
        mActionPluginComponents = map;
    }
}
