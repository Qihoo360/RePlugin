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

package com.qihoo360.replugin.component.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.qihoo360.i.Factory2;
import com.qihoo360.i.IPluginManager;
import com.qihoo360.replugin.RePlugin;
import com.qihoo360.replugin.helper.LogDebug;

/**
 * 若插件中的某个类是动态注册的，但是这个插件未下载，就调用此Activity做中转，
 * 解析完插件信息后，调用下载框下载插件；若解析失败，则调用 finish()
 *
 * @author RePlugin Team
 */
public class DynamicClassProxyActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ComponentName cn = getIntent().getComponent();
        if (cn != null) {

            // 获取 动态代理类名称 和 插件名称
            String dynamicClassName = cn.getClassName();
            String plugin = Factory2.getPluginByDynamicClass(dynamicClassName);

            if (LogDebug.LOG) {
                LogDebug.d("loadClass", "DynamicClassProxyActivity.onCreate(), plugin = " + plugin + ", class = " + dynamicClassName);
            }

            // 如果插件不存在，回调宿主接口。
            if (!TextUtils.isEmpty(plugin) && !RePlugin.isPluginInstalled(plugin)) {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(plugin, dynamicClassName));
                RePlugin.getConfig().getCallbacks().onPluginNotExistsForActivity(this, plugin, intent, IPluginManager.PROCESS_UI);
            }
        }

        finish();
    }
}
