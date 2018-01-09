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

package com.qihoo360.replugin.fresco.host;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.fresco.patch.FrescoPatch;
import com.qihoo360.replugin.RePluginApplication;
import com.qihoo360.replugin.RePluginConfig;

/**
 * HostApp
 *
 * @author RePlugin Team
 */
public class HostApp extends RePluginApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        // 初始化Fresco
        Fresco.initialize(this);

        // 初始化FrescoPath
        FrescoPatch.initialize(this);
    }

    /**
     * RePlugin允许提供各种“自定义”的行为，让您“无需修改源代码”，即可实现相应的功能
     */
    @Override
    protected RePluginConfig createConfig() {
        RePluginConfig c = new RePluginConfig();

        // 允许“插件使用宿主类”
        // 打开这个开关之后，当插件ClassLoader找不到类时，会去看宿主是否有这个类
        // 从而，实现插件复用宿主中的Java类
        c.setUseHostClassIfNotFound(true);

        return c;
    }
}