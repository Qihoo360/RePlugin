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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.qihoo360.loader2.PMF;
import com.qihoo360.replugin.helper.LogRelease;

import static com.qihoo360.replugin.helper.LogDebug.PLUGIN_TAG;
import static com.qihoo360.replugin.helper.LogRelease.LOGR;

/**
 * 若坑位出现丢失或错乱，则通过读取Intent.Category来做个中转
 *
 * @author RePlugin Team
 */
public class ForwardActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);

        // INFO forward activity on Create
        if (LOGR) {
            LogRelease.i(PLUGIN_TAG, "f.a: o.c");
        }

        Intent intent = getIntent();
        if (intent == null) {
            if (LOGR) {
                LogRelease.e(PLUGIN_TAG, "f.a: nul i");
            }
        }

        PMF.forward(this, intent);
    }
}
