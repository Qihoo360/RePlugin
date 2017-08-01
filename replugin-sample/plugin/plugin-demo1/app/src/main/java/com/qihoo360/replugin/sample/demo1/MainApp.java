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

package com.qihoo360.replugin.sample.demo1;

import android.app.Application;
import android.content.Intent;

import com.qihoo360.replugin.sample.demo1.service.PluginDemoAppService;

/**
 * @author RePlugin Team
 */
public class MainApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // 在插件启动时就去开启一个服务，以模拟个别插件的复杂行为
        testStartService();
    }

    private void testStartService() {
        Intent i = new Intent(this, PluginDemoAppService.class);
        i.setAction("MyNameIsApp");
        startService(i);
    }
}
