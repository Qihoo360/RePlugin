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

package com.qihoo360.replugin.sample.demo3.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.widget.Toast

/**
 * @author RePlugin Team
 */
class PluginDemo3Receiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (!TextUtils.isEmpty(action)) {
            if (action == ACTION) {
                val name = intent.getStringExtra("name")
                Toast.makeText(context, "Plugin3-action: $action, name = $name", Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {

        val ACTION = "com.qihoo360.repluginapp.replugin.receiver.ACTION3"
    }
}
