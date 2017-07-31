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

package com.qihoo360.replugin.component.service;

import android.content.Context;
import android.content.ServiceConnection;
import android.os.Handler;
import android.util.Log;

import com.qihoo360.replugin.utils.basic.ArrayMap;

import static com.qihoo360.replugin.helper.LogDebug.LOG;

/**
 * 用来管理ServiceDispatcher的类
 *
 * @author RePlugin Team
 */
public class PluginServiceDispatcherManager {

    private static final String TAG = "PSDM";

    private ArrayMap<Context, ArrayMap<ServiceConnection, ServiceDispatcher>> mServices = new ArrayMap<>();
    private ArrayMap<Context, ArrayMap<ServiceConnection, ServiceDispatcher>> mUnboundServices = new ArrayMap<>();
    private static final byte[] SERVICES_LOCKER = new byte[0];

    public ServiceDispatcher get(ServiceConnection c, Context context, Handler handler, int flags, int process) {
        synchronized (SERVICES_LOCKER) {
            ServiceDispatcher sd = null;
            ArrayMap<ServiceConnection, ServiceDispatcher> map = mServices.get(context);
            if (map != null) {
                sd = map.get(c);
            }
            if (sd == null) {
                sd = new ServiceDispatcher(c, context, handler, flags, process);
                if (map == null) {
                    map = new ArrayMap<>();
                    mServices.put(context, map);
                }
                map.put(c, sd);
            } else {
                sd.validate(context, handler);
            }
            return sd;
        }
    }

    public ServiceDispatcher forget(Context context, ServiceConnection c) {
        synchronized (SERVICES_LOCKER) {
            ArrayMap<ServiceConnection, ServiceDispatcher> map
                    = mServices.get(context);
            ServiceDispatcher sd = null;
            if (map != null) {
                sd = map.get(c);
                if (sd != null) {
                    map.remove(c);
                    sd.doForget();
                    if (map.size() == 0) {
                        mServices.remove(context);
                    }
                    if ((sd.getFlags() & Context.BIND_DEBUG_UNBIND) != 0) {
                        ArrayMap<ServiceConnection, ServiceDispatcher> holder
                                = mUnboundServices.get(context);
                        if (holder == null) {
                            holder = new ArrayMap<>();
                            mUnboundServices.put(context, holder);
                        }
                        RuntimeException ex = new IllegalArgumentException(
                                "Originally unbound here:");
                        ex.fillInStackTrace();
                        sd.setUnbindLocation(ex);
                        holder.put(c, sd);
                    }
                    return sd;
                }
            }
            ArrayMap<ServiceConnection, ServiceDispatcher> holder
                    = mUnboundServices.get(context);
            if (holder != null) {
                sd = holder.get(c);
                if (sd != null) {
                    // NOTE 和系统不同的是，这里我们不会那么激进的抛出异常，而是打一个Error。
                    // NOTE 主要是因为插件的加载方式不同于系统，若有问题则不应抛异常提示。
                    // NOTE 下同。 by Jiongxuan Zhang
                    RuntimeException ex = sd.getUnbindLocation();
                    Exception e = new IllegalArgumentException(
                            "Unbinding Service " + c
                                    + " that was already unbound", ex);
                    if (LOG) {
                        Log.e(TAG, "forgetServiceDispatcher(): Unbind Error!", e);
                    }
                    return null;
                }
            }
            if (context == null) {
                Exception e = new IllegalStateException("Unbinding Service " + c
                        + " from Context that is no longer in use");
                if (LOG) {
                    Log.e(TAG, "forgetServiceDispatcher(): Unbind Error!", e);
                }
                return null;
            } else {
                Exception e = new IllegalArgumentException("Service not registered: " + c);
                if (LOG) {
                    Log.e(TAG, "forgetServiceDispatcher(): Unbind Error!", e);
                }
                return null;
            }
        }
    }
}
