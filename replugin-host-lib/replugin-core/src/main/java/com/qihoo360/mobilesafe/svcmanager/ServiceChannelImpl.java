
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

import android.database.MatrixCursor;
import android.os.DeadObjectException;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.qihoo360.mobilesafe.core.BuildConfig;
import com.qihoo360.replugin.IBinderGetter;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Service管理的后台实现，存活在常驻进程，其他进程的请求都是经过远端持有此实现Proxy把请求传递到常驻进程 再由此进行处理或者分发的。
 * 此外，常驻进程的静态服务也是在此类的initServices()函数中添加的。
 *
 * @author RePlugin Team
 */
/* PACKAGE */class ServiceChannelImpl {

    private static final boolean DEBUG = BuildConfig.DEBUG;

    private static final String TAG = DEBUG ? "ServiceChannelImpl" : ServiceChannelImpl.class.getSimpleName();

    private static ConcurrentHashMap<String, IBinder> sServices = new ConcurrentHashMap<String, IBinder>();

    // 添加延迟释放IBinder的Map
    // Added by Jiongxuan Zhang
    private static ConcurrentHashMap<String, IBinderGetter> sDelayedServices = new ConcurrentHashMap<>();

    /**
     * ServiceChannel的实现
     */
    static IServiceChannel.Stub sServiceChannelImpl = new IServiceChannel.Stub() {

        @Override
        public IBinder getService(String serviceName) throws RemoteException {
            if (DEBUG) {
                Log.d(TAG, "[getService] --> serviceName = " + serviceName);
            }

            if (TextUtils.isEmpty(serviceName)) {
                throw new IllegalArgumentException();
            }

            IBinder service = sServices.get(serviceName);

            // 若没有注册此服务，则尝试从“延迟IBinder”中获取
            // Added by Jiongxuan Zhang
            if (service == null) {
                return fetchFromDelayedMap(serviceName);
            }

            // 判断Binder是否挂掉
            if (!service.isBinderAlive() || !service.pingBinder()) {
                if (DEBUG) {
                    Log.d(TAG, "[getService] --> service died:" + serviceName);
                }

                sServices.remove(serviceName);
                return null;
            } else {
                return service;
            }
        }

        // 尝试从“延迟IBinder”中获取
        // Added by Jiongxuan Zhang
        private IBinder fetchFromDelayedMap(String serviceName) {
            IBinderGetter sc = sDelayedServices.get(serviceName);
            if (sc == null) {
                return null;
            }

            try {
                IBinder s = sc.get();
                addService(serviceName, s);
                return s;
            } catch (DeadObjectException e) {
                if (DEBUG) {
                    e.printStackTrace();
                }

                // 因为远端Callback所在进程已经挂掉，再使用它已没有意义，必须重新注册才行
                sDelayedServices.remove(serviceName);
            } catch (RemoteException e) {
                if (DEBUG) {
                    e.printStackTrace();
                }

                // 可能不是因为远端进程挂掉，只是TransactionTooLarge等错误，故可不删除
            }
            return null;
        }

        @Override
        public void addService(String serviceName, IBinder service) throws RemoteException {
            sServices.put(serviceName, service);
        }

        @Override
        public void addServiceDelayed(String serviceName, IBinderGetter getter) throws RemoteException {
            sDelayedServices.put(serviceName, getter);
        }

        @Override
        public void removeService(String serviceName) throws RemoteException {
            sServices.remove(serviceName);
        }

        @Override
        public IBinder getPluginService(String pluginName, String serviceName, IBinder deathMonitor) throws RemoteException {
            return PluginServiceManager.getPluginService(pluginName, serviceName, getCallingPid(), deathMonitor);
        }

        @Override
        public void onPluginServiceRefReleased(String pluginName, String serviceName) throws RemoteException {
            PluginServiceManager.onRefReleased(pluginName, serviceName, getCallingPid());
        }
    };

    static MatrixCursor sServiceChannelCursor = ServiceChannelCursor.makeCursor(sServiceChannelImpl);
}
