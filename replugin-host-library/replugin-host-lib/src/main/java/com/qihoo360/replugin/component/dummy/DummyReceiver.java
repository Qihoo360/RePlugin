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

package com.qihoo360.replugin.component.dummy;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.qihoo360.replugin.helper.LogRelease;

import static com.qihoo360.replugin.helper.LogDebug.PLUGIN_TAG;
import static com.qihoo360.replugin.helper.LogRelease.LOGR;

/**
 * 表示一个“仿造的”BroadcastReceiver，收到消息后什么事情也不做 <p>
 * 此类可防止系统调用插件时因类找不到而崩溃。请参见 registerHookingClass 的说明
 *
 * @see com.qihoo360.replugin.RePlugin#registerHookingClass(String, ComponentName, Class)
 * @author RePlugin Team
 */
public class DummyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // INFO dummy receiver on receive occur
        if (LOGR) {
            LogRelease.i(PLUGIN_TAG, "d.r o.c f");
        }

        // Nothing
    }
}
