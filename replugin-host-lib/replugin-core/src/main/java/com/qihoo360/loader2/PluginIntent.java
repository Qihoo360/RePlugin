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

package com.qihoo360.loader2;

import android.content.ComponentName;
import android.content.Intent;
import android.text.TextUtils;

import com.qihoo360.i.IPluginManager;
import com.qihoo360.replugin.helper.LogRelease;

import java.util.Set;

import static com.qihoo360.replugin.helper.LogDebug.PLUGIN_TAG;
import static com.qihoo360.replugin.helper.LogRelease.LOGR;

/**
 * @author RePlugin Team
 */
class PluginIntent {

    public static final String EXTRA_PLUGIN = "plugin:";

    public static final String EXTRA_ACTIVITY = "activity:";

    public static final String EXTRA_PROCESS = "process:";

    public static final String EXTRA_CONTAINER = "container:";

    public static final String EXTRA_COUNTER = "counter:";

    private final Intent mIntent;

    PluginIntent(Intent intent) {
        mIntent = intent;
    }

    private final void remove(String prefix) {
        Set<String> categories = mIntent.getCategories();
        if (categories != null) {
            for (String category : categories) {
                if (category.startsWith(prefix)) {
                    mIntent.removeCategory(category);
                    break;
                }
            }
        }
    }

    private final String getS(String prefix) {
        Set<String> categories = mIntent.getCategories();
        if (categories != null) {
            for (String category : categories) {
                if (category.startsWith(prefix)) {
                    return category.substring(prefix.length());
                }
            }
        }
        return null;
    }

    private final void setS(String prefix, String value) {
        remove(prefix);
        mIntent.addCategory(prefix + value);
    }

    private final int getI(String prefix, int defValue) {
        Set<String> categories = mIntent.getCategories();
        if (categories != null) {
            String v = "";
            for (String category : categories) {
                if (category.startsWith(prefix)) {
                    v = category.substring(prefix.length());
                    break;
                }
            }
            if (!TextUtils.isEmpty(v)) {
                try {
                    int i = Integer.parseInt(v);
                    return i;
                } catch (Throwable e) {
                    if (LOGR) {
                        LogRelease.e(PLUGIN_TAG, e.getMessage(), e);
                    }
                }
            }
        }
        return defValue;
    }

    private final void setI(String prefix, int value) {
        remove(prefix);
        mIntent.addCategory(prefix + value);
    }

    /**
     * @return
     */
    final String getOriginal() {
        ComponentName cn = mIntent.getComponent();
        if (cn != null) {
            return cn.getClassName();
        }
        return null;
    }

    final String getPlugin() {
        return getS(EXTRA_PLUGIN);
    }

    final void setPlugin(String plugin) {
        setS(EXTRA_PLUGIN, plugin);
    }

    final String getActivity() {
        return getS(EXTRA_ACTIVITY);
    }

    final void setActivity(String activity) {
        setS(EXTRA_ACTIVITY, activity);
    }

    final int getProcess() {
        return getI(EXTRA_PROCESS, IPluginManager.PROCESS_AUTO);
    }

    final void setProcess(int process) {
        setI(EXTRA_PROCESS, process);
    }

    final String getContainer() {
        return getS(EXTRA_CONTAINER);
    }

    final void setContainer(String container) {
        setS(EXTRA_CONTAINER, container);
    }

    final int getCounter() {
        return getI(EXTRA_COUNTER, 0);
    }

    final void setCounter(int counter) {
        setI(EXTRA_COUNTER, counter);
    }
}
