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

package com.qihoo360.replugin.sample.host;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;

import com.qihoo360.replugin.DefaultRePluginCallbacks;
import com.qihoo360.replugin.PluginDexClassLoader;
import com.qihoo360.replugin.RePlugin;
import com.qihoo360.replugin.RePluginConfig;

/**
 * @author RePlugin Team
 */
public class SampleApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        // ======= REPLUGIN =======
        RePlugin.App.attachBaseContext(this, new RePluginConfig()
                .setUseHostClassIfNotFound(true)//开启插件使用宿主的类
                .setVerifySign(false)//关闭签名校验
        );
        // ========================
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // ======= REPLUGIN =======
        RePlugin.App.onCreate();
        // Open the debug function
        RePlugin.enableDebugger(this,true);
        // ========================
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

        // ======= REPLUGIN =======
        RePlugin.App.onLowMemory();
        // ========================
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);

        // ======= REPLUGIN =======
        RePlugin.App.onTrimMemory(level);
        // ========================
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // ======= REPLUGIN =======
        RePlugin.App.onConfigurationChanged(newConfig);
        // ========================
    }
}
