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

package com.qihoo360.replugin;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;

/**
 * 方便宿主直接继承RePluginApplication来完成插件框架的注册 <p>
 * 此外，也可以无需继承此Application，自己择机调用相关方法来使用
 *
 * @author RePlugin Team
 */

public class RePluginApplication extends Application {

    /**
     * 子类可以复写此方法来自定义RePluginConfig。请参见 RePluginConfig 的说明
     *
     * @see RePluginConfig
     * @return 新的RePluginConfig对象
     */
    protected RePluginConfig createConfig() {
        return new RePluginConfig();
    }

    /**
     * 子类可以复写此方法来自定义RePluginCallbacks。请参见 RePluginCallbacks 的说明 <p>
     * 注意：若在createConfig的RePluginConfig内同时也注册了Callbacks，则以这里创建出来的为准
     *
     * @see RePluginCallbacks
     * @return 新的RePluginCallbacks对象，可以为空
     */
    protected RePluginCallbacks createCallbacks() {
        return null;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        RePluginConfig c = createConfig();
        if (c == null) {
            c = new RePluginConfig();
        }

        RePluginCallbacks cb = createCallbacks();
        if (cb != null) {
            c.setCallbacks(cb);
        }

        RePlugin.App.attachBaseContext(this, c);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        RePlugin.App.onCreate();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

        // 如果App的minSdkVersion >= 14，该方法可以不调用
        RePlugin.App.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);

        // 如果App的minSdkVersion >= 14，该方法可以不调用
        RePlugin.App.onTrimMemory(level);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // 如果App的minSdkVersion >= 14，该方法可以不调用
        RePlugin.App.onConfigurationChanged(newConfig);
    }
}
