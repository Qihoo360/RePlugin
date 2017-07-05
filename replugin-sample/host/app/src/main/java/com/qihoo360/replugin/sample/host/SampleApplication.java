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

import android.content.Context;

import com.qihoo360.replugin.RePlugin;
import com.qihoo360.replugin.RePluginApplication;
import com.qihoo360.replugin.RePluginConfig;

/**
 * @author RePlugin Team
 */
public class SampleApplication extends RePluginApplication {

    @Override
    protected RePluginConfig createConfig() {
        RePluginConfig c = new RePluginConfig();

        // FIXME RePlugin默认会对安装的外置插件进行签名校验，这里先关掉，避免调试时出现签名错误
        c.setVerifySign(!BuildConfig.DEBUG);

        return c;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        // FIXME 允许接收rpRunPlugin等Gradle Task，发布时请务必关掉，以免出现问题
        RePlugin.enableDebugger(base, BuildConfig.DEBUG);
    }
}
