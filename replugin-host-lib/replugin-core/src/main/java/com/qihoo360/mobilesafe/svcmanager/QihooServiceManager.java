
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

package com.qihoo360.mobilesafe.svcmanager;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;

import com.qihoo360.mobilesafe.core.BuildConfig;
import com.qihoo360.replugin.IBinderGetter;
import com.qihoo360.replugin.base.IPC;

import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 方便的以同步调用的方式获取一个服务实现的接口类
 *
 * @author RePlugin Team
 */
public final class QihooServiceManager {

    private static final boolean DEBUG = BuildConfig.DEBUG;

    private static final String TAG = DEBUG ? "QihooServiceManager" : QihooServiceManager.class.getSimpleName();

    private static Uri sServiceChannelUri = null;

    private static IServiceChannel sServerChannel;

    private static Map<String, SoftReference<IBinder>> sCache;

    private static final IBinder PROCESS_DEATH_AGENT = new Binder();

    static {
        sCache = Collections.synchronizedMap(new HashMap<String, SoftReference<IBinder>>());
    }

    /**
     * 获取已注册服务的IBinder对象，前提是该服务是静态服务，即默认一直存在，或者自己已经启动并且向我们注册过；
     * 注意不能通过此借口获取一个插件的服务，除非明确知道该插件的服务已经主动注册过，否则使用getPluginService()
     *
     * @param context
     * @param serviceName 请求获取的service名称
     * @return 所请求的service实现对象
     */
    public static IBinder getService(Context context, String serviceName) {
        if (DEBUG) {
            Log.d(TAG, "[getService] begin = " + SystemClock.elapsedRealtime());
        }

        IBinder service = null;

        /**
         * 先考虑本地缓存
         */
        SoftReference<IBinder> ref = sCache.get(serviceName);
        if (ref != null) {
            service = ref.get();
            if (service != null) {
                if (service.isBinderAlive() && service.pingBinder()) {
                    if (DEBUG) {
                        Log.d(TAG, "[getService] Found service from cache: " + serviceName);
                        Log.d(TAG, "[getService] end = " + SystemClock.elapsedRealtime());
                    }
                    return service;
                } else {
                    sCache.remove(serviceName);
                }
            }
        }

        IServiceChannel serviceChannel = getServerChannel(context);
        if (serviceChannel == null) {
            return null;
        }

        try {
            service = serviceChannel.getService(serviceName);

            if (service != null) {
                if (DEBUG) {
                    Log.d(TAG, "[getService] Found service from remote service channel: " + serviceName);
                }
                service = ServiceWrapper.factory(context, serviceName, service);
                sCache.put(serviceName, new SoftReference<IBinder>(service));
            }
        } catch (RemoteException e) {
            if (DEBUG) {
                Log.e(TAG, "[getService] Error when getting service from service channel...", e);
            }
        }

        if (DEBUG) {
            Log.d(TAG, "[getService] end = " + SystemClock.elapsedRealtime());
        }

        return service;
    }

    /**
     * 动态添加（注册）一个服务
     *
     * @param context
     * @param serviceName 请求添加的service名称
     * @param service 请求添加的service实现对象
     */
    public static boolean addService(Context context, String serviceName, IBinder service) {
        IServiceChannel serviceChannel = getServerChannel(context);
        if (serviceChannel == null) {
            return false;
        }

        try {
            serviceChannel.addService(serviceName, service);
        } catch (RemoteException e) {
            if (DEBUG) {
                Log.e(TAG, "Add service failed...", e);
            }
        }
        return true;
    }

    /**
     * 动态添加（注册）一个服务
     *
     * @param context
     * @param serviceName 请求添加的service名称
     * @param getter 请求添加的service实现对象的Callback
     */
    public static boolean addService(Context context, String serviceName, IBinderGetter getter) {
        IServiceChannel serviceChannel = getServerChannel(context);
        if (serviceChannel == null) {
            return false;
        }

        try {
            serviceChannel.addServiceDelayed(serviceName, getter);
        } catch (RemoteException e) {
            if (DEBUG) {
                Log.e(TAG, "Add service failed...", e);
            }
        }
        return true;
    }

    /**
     * 移除一个之前添加过的服务
     *
     * @param context
     * @param serviceName 请求移除的service名称
     * @return 请求移除的service实现对象
     */
    public static boolean removeService(Context context, String serviceName, IBinder service) {
        IServiceChannel serviceChannel = getServerChannel(context);
        if (serviceChannel == null) {
            return false;
        }

        try {
            serviceChannel.removeService(serviceName);
        } catch (RemoteException e) {
            if (DEBUG) {
                Log.e(TAG, "Remove service failed...", e);
            }
        }
        return true;
    }

    /**
     * 请求一个由plugin实现的service的实现对象，如果需要的话会启动该plugin的进程。
     * 请求的过程由于可能初始化新的插件进程可能会比较耗时，因此不要再UI线程调用。
     *
     * @param context
     * @param pluginName 实现了所请求的service的plugin的名称
     * @param serviceName 所请求的service名称
     * @return
     */
    public static IBinder getPluginService(Context context, String pluginName, String serviceName) {
        IBinder service = getService(context, serviceName);
        if (service != null) {
            /**
             * 此Plugin的service已经主动注册
             */
            return service;
        }

        IServiceChannel serviceChannel = getServerChannel(context);
        if (serviceChannel == null) {
            return null;
        }

        try {
            /**
             * 这里面没有使用ServiceWrapper来包装plugin的service，因为一旦远程plugin
             * 进程死掉的话，重新获取其服务的过程又要花很多时间，在使用上会对使用者造成影响，因此不做Binder死掉自动重连的逻辑。
             */
            service = serviceChannel.getPluginService(pluginName, serviceName, PROCESS_DEATH_AGENT);
            PluginServiceReferenceManager.onPluginServiceObtained(context, pluginName, serviceName, service);
        } catch (RemoteException e) {
            if (DEBUG) {
                Log.e(TAG, "[getPluginService] Error when getting plugin service from service channel...", e);
            }
        }

        return service;
    }

    static IServiceChannel getServerChannel(Context context) {
        if (DEBUG) {
            Log.d(TAG, "[getServerChannel] begin = " + SystemClock.elapsedRealtime());
        }

        if (sServerChannel != null && sServerChannel.asBinder().isBinderAlive() && sServerChannel.asBinder().pingBinder()) {
            return sServerChannel;
        }

        /*
         * In server process we can return
         * ServerChannelCreator.serviceChannelImpl directly instead of querying
         * it using content resolver
         */
        if (IPC.isPersistentProcess()) {
            return ServiceChannelImpl.sServiceChannelImpl;
        }

        if (context == null) {
            return null;
        }

        IServiceChannel serviceChannel = null;
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(getServiceChannelUri(), null, null, null, null);
            IBinder binder = ServiceChannelCursor.getBinder(cursor);
            serviceChannel = IServiceChannel.Stub.asInterface(binder);
            sServerChannel = serviceChannel;
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "Error fetching service manager binder object using provider: ", e);
            }
        } finally {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    if (DEBUG) {
                        Log.e(TAG, "Error closing cursor: ", e);
                    }
                }
            }
        }

        if (DEBUG) {
            Log.d(TAG, "[getServerChannel] end = " + SystemClock.elapsedRealtime());
        }

        return serviceChannel;
    }

    static Uri getServiceChannelUri() {
        if (sServiceChannelUri == null) {
            sServiceChannelUri = Uri.parse("content://" + ServiceProvider.AUTHORITY + "/" + ServiceProvider.PATH_SERVER_CHANNEL);
        }

        return sServiceChannelUri;
    }

    static void onPluginServiceReleased(Context context, String pluginName, String serviceName) {
        IServiceChannel serviceChannel = getServerChannel(context);
        if (serviceChannel != null) {
            try {
                serviceChannel.onPluginServiceRefReleased(pluginName, serviceName);
            } catch (RemoteException e) {
                if (DEBUG) {
                    Log.d(TAG, "Error releaseing plugin service reference: ", e);
                }
            }
        }
    }
}
