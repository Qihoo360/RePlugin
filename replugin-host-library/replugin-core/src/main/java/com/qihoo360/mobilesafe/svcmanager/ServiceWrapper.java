
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
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

import com.qihoo360.mobilesafe.core.BuildConfig;

import java.io.FileDescriptor;

/**
 * A Binder Wrapper class that monitors the death of the underlying remote
 * Binder and recovers it if needed.
 *
 * @author RePlugin Team
 */
/* PACKAGE */class ServiceWrapper implements IBinder, IBinder.DeathRecipient {

    private static final boolean DEBUG = BuildConfig.DEBUG;

    private static final String TAG = DEBUG ? "ServiceWrapper" : ServiceWrapper.class.getSimpleName();

    private final Context mAppCpntext;

    private IBinder mRemote;

    private final String mName;

    public static IBinder factory(Context context, String name, IBinder binder) {
        String descriptor = null;
        try {
            descriptor = binder.getInterfaceDescriptor();
        } catch (RemoteException e) {
            if (DEBUG) {
                Log.d(TAG, "getInterfaceDescriptor()", e);
            }
        }
        android.os.IInterface iin = binder.queryLocalInterface(descriptor);
        if (iin != null) {
            /**
             * If the requested interface has local implementation, meaning that
             * it's living in the same process as the one who requests for it,
             * return the binder directly since in such cases our wrapper does
             * not help in any way.
             */
            return binder;
        }
        return new ServiceWrapper(context, name, binder);
    }

    private ServiceWrapper(Context context, String name, IBinder binder) {
        mAppCpntext = context.getApplicationContext();
        mRemote = binder;
        mName = name;
        try {
            mRemote.linkToDeath(this, 0);
        } catch (RemoteException e) {
            if (DEBUG) {
                Log.d(TAG, "linkToDeath ex", e);
            }
        }
    }

    private IBinder getRemoteBinder() throws RemoteException {
        IBinder remote = mRemote;
        if (remote != null) {
            return remote;
        }
        IServiceChannel serviceChannel = QihooServiceManager.getServerChannel(mAppCpntext);
        if (serviceChannel == null) {
            // 在获取Cursor时，可能恰巧常驻进程被停止，则有可能出现获取失败的情况
            // Added by Jiongxuan Zhang
            Log.e(TAG, "sw.grb: s is n");
            throw new RemoteException();
        }
        remote = serviceChannel.getService(mName);
        if (remote == null) {
            throw new RemoteException();
        }

        mRemote = remote;
        return remote;
    }

    @Override
    public String getInterfaceDescriptor() throws RemoteException {
        return getRemoteBinder().getInterfaceDescriptor();
    }

    @Override
    public boolean pingBinder() {
        try {
            return getRemoteBinder().pingBinder();
        } catch (RemoteException e) {
            if (DEBUG) {
                Log.d(TAG, "getRemoteBinder()", e);
            }
        }
        return false;
    }

    @Override
    public boolean isBinderAlive() {
        try {
            return getRemoteBinder().isBinderAlive();
        } catch (RemoteException e) {
            if (DEBUG) {
                Log.d(TAG, "isBinderAlive()", e);
            }
        }
        return false;
    }

    @Override
    public IInterface queryLocalInterface(String descriptor) {
        try {
            return getRemoteBinder().queryLocalInterface(descriptor);
        } catch (RemoteException e) {
            if (DEBUG) {
                Log.d(TAG, "queryLocalInterface()", e);
            }
        }
        return null;
    }

    @Override
    public void dump(FileDescriptor fd, String[] args) throws RemoteException {
        getRemoteBinder().dump(fd, args);
    }

    @Override
    public boolean transact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        return getRemoteBinder().transact(code, data, reply, flags);
    }

    @Override
    public void linkToDeath(DeathRecipient recipient, int flags) throws RemoteException {
        /**
         * ServiceWrapper存在的意义在于能自动检测远程Binder进程死去并且在需要的时候重新获取，
         * 因此对于ServiceWrapper来说再设置DeathRecipient没有任何意义。
         */

        if (DEBUG) {
            throw new UnsupportedOperationException("ServiceWrapper does NOT support Death Recipient!");
        }
    }

    @Override
    public boolean unlinkToDeath(DeathRecipient recipient, int flags) {
        /**
         * ServiceWrapper存在的意义在于能自动检测远程Binder进程死去并且在需要的时候重新获取，
         * 因此对于ServiceWrapper来说再设置DeathRecipient没有任何意义。
         */
        return false;
    }

    @Override
    public void binderDied() {
        if (DEBUG) {
            Log.d(TAG, "ServiceWrapper [binderDied]: " + mName);
        }
        mRemote = null;
    }

    // Override API 15
    public void dumpAsync(FileDescriptor fd, String[] args) throws RemoteException {
    }
}
