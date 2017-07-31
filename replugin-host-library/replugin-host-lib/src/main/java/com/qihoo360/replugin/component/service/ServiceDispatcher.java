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

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.AndroidRuntimeException;

import com.qihoo360.loader2.mgr.IServiceConnection;
import com.qihoo360.replugin.utils.basic.ArrayMap;

import java.lang.ref.WeakReference;

/**
 * 用来在Client端管理、调度Service的类。仅Client端可用
 * 负责同目标进程（Server）进行通信，以及关联“死亡周期”，缓存等功能。
 *
 * NOTE 类似于Android的ServiceDispatcher
 *
 * @hide 框架内部使用
 * @author RePlugin Team
 */

final class ServiceDispatcher {
    private final ServiceDispatcher.InnerConnection mIServiceConnection;
    private final ServiceConnection mConnection;
    private final Context mContext;
    private final Handler mActivityThread;
    private final ServiceConnectionLeaked mLocation;
    private final int mFlags;

    // 指定要绑定的进程
    // Added by Jiongxuan Zhang
    private final int mProcess;

    private RuntimeException mUnbindLocation;

//    private boolean mDied;
    private boolean mForgotten;

    private static class ConnectionInfo {
        IBinder binder;
        IBinder.DeathRecipient deathMonitor;
    }

    private static class InnerConnection extends IServiceConnection.Stub {
        final WeakReference<ServiceDispatcher> mDispatcher;

        InnerConnection(ServiceDispatcher sd) {
            mDispatcher = new WeakReference<ServiceDispatcher>(sd);
        }

        public void connected(ComponentName name, IBinder service) throws RemoteException {
            ServiceDispatcher sd = mDispatcher.get();
            if (sd != null) {
                sd.connected(name, service);
            }
        }
    }

    private final ArrayMap<ComponentName, ConnectionInfo> mActiveConnections = new ArrayMap<>();

    ServiceDispatcher(ServiceConnection conn,
                      Context context, Handler activityThread, int flags, int process) {
        mIServiceConnection = new InnerConnection(this);
        mConnection = conn;
        mContext = context;
        mActivityThread = activityThread;
        mLocation = new ServiceConnectionLeaked(null);
        mLocation.fillInStackTrace();
        mFlags = flags;
        mProcess = process;
    }

    void validate(Context context, Handler activityThread) {
        if (mContext != context) {
            throw new RuntimeException(
                    "ServiceConnection " + mConnection +
                            " registered with differing Context (was " +
                            mContext + " now " + context + ")");
        }
        if (mActivityThread != activityThread) {
            throw new RuntimeException(
                    "ServiceConnection " + mConnection +
                            " registered with differing handler (was " +
                            mActivityThread + " now " + activityThread + ")");
        }
    }

    void doForget() {
        synchronized (this) {
            for (int i = 0; i < mActiveConnections.size(); i++) {
                ServiceDispatcher.ConnectionInfo ci = mActiveConnections.valueAt(i);
                ci.binder.unlinkToDeath(ci.deathMonitor, 0);
            }
            mActiveConnections.clear();
            mForgotten = true;
        }
    }

    ServiceConnectionLeaked getLocation() {
        return mLocation;
    }

    ServiceConnection getServiceConnection() {
        return mConnection;
    }

    IServiceConnection getIServiceConnection() {
        return mIServiceConnection;
    }

    int getFlags() {
        return mFlags;
    }

    void setUnbindLocation(RuntimeException ex) {
        mUnbindLocation = ex;
    }

    RuntimeException getUnbindLocation() {
        return mUnbindLocation;
    }

    int getProcess() {
        return mProcess;
    }

    public void connected(ComponentName name, IBinder service) {
        if (mActivityThread != null) {
            mActivityThread.post(new RunConnection(name, service, 0));
        } else {
            doConnected(name, service);
        }
    }

    public void death(ComponentName name, IBinder service) {
        ServiceDispatcher.ConnectionInfo old;

        synchronized (this) {
//            mDied = true;
            old = mActiveConnections.remove(name);
            if (old == null || old.binder != service) {
                // Death for someone different than who we last
                // reported...  just ignore it.
                return;
            }
            old.binder.unlinkToDeath(old.deathMonitor, 0);
        }

        if (mActivityThread != null) {
            mActivityThread.post(new RunConnection(name, service, 1));
        } else {
            doDeath(name, service);
        }
    }

    public void doConnected(ComponentName name, IBinder service) {
        ServiceDispatcher.ConnectionInfo old;
        ServiceDispatcher.ConnectionInfo info;

        synchronized (this) {
            if (mForgotten) {
                // We unbound before receiving the connection; ignore
                // any connection received.
                return;
            }
            old = mActiveConnections.get(name);
            if (old != null && old.binder == service) {
                // Huh, already have this one.  Oh well!
                return;
            }

            if (service != null) {
                // A new service is being connected... set it all up.
//                mDied = false;
                info = new ConnectionInfo();
                info.binder = service;
                info.deathMonitor = new DeathMonitor(name, service);
                try {
                    service.linkToDeath(info.deathMonitor, 0);
                    mActiveConnections.put(name, info);
                } catch (RemoteException e) {
                    // This service was dead before we got it...  just
                    // don't do anything with it.
                    mActiveConnections.remove(name);
                    return;
                }

            } else {
                // The named service is being disconnected... clean up.
                mActiveConnections.remove(name);
            }

            if (old != null) {
                old.binder.unlinkToDeath(old.deathMonitor, 0);
            }
        }

        // If there was an old service, it is not disconnected.
        if (old != null) {
            mConnection.onServiceDisconnected(name);
        }
        // If there is a new service, it is now connected.
        if (service != null) {
            mConnection.onServiceConnected(name, service);
        }
    }

    public void doDeath(ComponentName name, IBinder service) {
        mConnection.onServiceDisconnected(name);
    }

    private final class RunConnection implements Runnable {
        RunConnection(ComponentName name, IBinder service, int command) {
            mName = name;
            mService = service;
            mCommand = command;
        }

        public void run() {
            if (mCommand == 0) {
                doConnected(mName, mService);
            } else if (mCommand == 1) {
                doDeath(mName, mService);
            }
        }

        final ComponentName mName;
        final IBinder mService;
        final int mCommand;
    }

    private final class DeathMonitor implements IBinder.DeathRecipient {
        DeathMonitor(ComponentName name, IBinder service) {
            mName = name;
            mService = service;
        }

        public void binderDied() {
            death(mName, mService);
        }

        final ComponentName mName;
        final IBinder mService;
    }
}

final class ServiceConnectionLeaked extends AndroidRuntimeException {
    public ServiceConnectionLeaked(String msg) {
        super(msg);
    }
}
