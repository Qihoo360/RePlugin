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

package com.qihoo360.replugin.sample.demo3.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.widget.Toast

import com.qihoo360.replugin.sample.demo3.support.LogX

/**
 * @author RePlugin Team
 */
class PluginDemoService1 : Service() {

    override fun onCreate() {
        super.onCreate()

        LogX.logDebug(TAG, "onCreate()")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val action = intent.action
        Toast.makeText(this, "PluginDemoService1.action = " + action, Toast.LENGTH_SHORT).show()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        LogX.logDebug(TAG, "onDestroy()")
    }

    companion object {

        val TAG = "demo.service"
    }
}
