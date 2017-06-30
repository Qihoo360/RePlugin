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
import android.text.TextUtils;

import com.qihoo360.i.Factory;
import com.qihoo360.mobilesafe.api.Tasks;
import com.qihoo360.replugin.helper.LogDebug;

import java.util.HashMap;

import static com.qihoo360.replugin.component.receiver.PluginReceiverProxy.loadClassSafety;
import static com.qihoo360.replugin.helper.LogDebug.LOG;

/**
 * @author RePlugin Team
 */
public class PluginReceiverHelper {

    /**
     * 插件静态注册的广播，从常驻代理到插件时，调用此方法
     */
    public static void onPluginReceiverReceived(final String plugin,
                                                final String receiverName,
                                                final HashMap<String, BroadcastReceiver> receivers,
                                                final Intent intent) {

        if (TextUtils.isEmpty(plugin) || TextUtils.isEmpty(receiverName)) {
            if (LOG) {
                LogDebug.d(PluginReceiverProxy.TAG, "plugin or receiver or intent is null, return.");
            }
            return;
        }

        // 使用插件的 Context 对象
        final Context pContext = Factory.queryPluginContext(plugin);
        if (pContext == null) {
            return;
        }

        String key = String.format("%s-%s", plugin, receiverName);

        BroadcastReceiver receiver = null;
        if (receivers == null || !receivers.containsKey(key)) {
            try {
                // 使用插件的 ClassLoader 加载 BroadcastReceiver
                Class c = loadClassSafety(pContext.getClassLoader(), receiverName);
                if (c != null) {
                    receiver = (BroadcastReceiver) c.newInstance();
                    if (receivers != null) {
                        receivers.put(key, receiver);
                    }

                    if (LOG) {
                        LogDebug.d(PluginReceiverProxy.TAG, String.format("反射创建 Receiver 实例 %s", receiverName));
                    }
                }
            } catch (Throwable e) {
                if (LOG) {
                    LogDebug.d(PluginReceiverProxy.TAG, e.toString());
                }
            }
        } else {
            receiver = receivers.get(key);
        }

        if (receiver != null) {
            final BroadcastReceiver finalReceiver = receiver;
            // 转到 ui 线程
            Tasks.post2UI(new Runnable() {
                @Override
                public void run() {
                    if (LOG) {
                        LogDebug.d(PluginReceiverProxy.TAG, String.format("调用 %s.onReceive()", receiverName));
                    }

                    finalReceiver.onReceive(pContext, intent);
                }
            });
        }
    }
}
