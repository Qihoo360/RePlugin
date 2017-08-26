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

import android.os.IBinder;
import android.util.Log;

import com.qihoo360.replugin.RePluginInternal;

import java.io.FileDescriptor;
import java.io.PrintWriter;

/**
 * 运行时 dump 工具类
 *
 * @author RePlugin Team
 */
public class DumpUtils {

    private static final String TAG = RePluginInternal.FOR_DEV ? DumpUtils.class.getSimpleName() : "DumpUtils";

    /**
     * dump RePlugin框架运行时的详细信息，包括：Activity 坑位映射表，正在运行的 Service，以及详细的插件信息
     *
     * @param fd
     * @param writer
     * @param args
     */
    public static void dump(FileDescriptor fd, PrintWriter writer, String[] args) {

        IBinder binder = PluginProviderStub.proxyFetchHostBinder(RePluginInternal.getAppContext());

        if (binder == null) {
            return;
        }

        IPluginHost pluginHost = IPluginHost.Stub.asInterface(binder);

        try {
            String dumpInfo = pluginHost.dump();

            if (RePluginInternal.FOR_DEV) {
                Log.d(TAG, "dumpInfo:" + dumpInfo);
            }

            if (writer != null) {
                writer.println(dumpInfo);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}