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

package com.qihoo360.replugin.component.service.server;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.qihoo360.i.Factory;
import com.qihoo360.loader2.mgr.IServiceConnection;
import com.qihoo360.mobilesafe.core.BuildConfig;
import com.qihoo360.replugin.RePlugin;
import com.qihoo360.replugin.base.IPC;
import com.qihoo360.replugin.base.ThreadUtils;
import com.qihoo360.replugin.component.ComponentList;
import com.qihoo360.replugin.component.utils.PluginClientHelper;
import com.qihoo360.replugin.helper.JSONHelper;
import com.qihoo360.replugin.helper.LogDebug;
import com.qihoo360.replugin.helper.LogRelease;
import com.qihoo360.replugin.utils.basic.ArrayMap;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.Callable;

import static com.qihoo360.replugin.helper.LogDebug.LOG;
import static com.qihoo360.replugin.helper.LogDebug.PLUGIN_TAG;
import static com.qihoo360.replugin.helper.LogRelease.LOGR;

/**
 * 负责Server端的服务调度、提供等工作，是服务的提供方，核心类之一
 *
 * @author RePlugin Team
 */
public class PluginServiceServer {

    private static final String TAG = "PluginServiceServer";

    private static final byte[] LOCKER = new byte[0];

    /**
     * Service onStartCommand
     */
    private static final int WHAT_ON_START_COMMAND = 1;

    private final Context mContext;

    private final Stub mStub;

    private Method mAttachBaseContextMethod;

    /**
     * PID -> ProcessRecord对象
     */
    final ArrayMap<Integer, ProcessRecord> mProcesses = new ArrayMap<>();

    /**
     * K：IServiceConnection（ServiceConnect）对象
     * V：此SC旗下的所有Binder连接。有可能一个IServiceConnection就连接了多个服务
     */
    final ArrayMap<IBinder, ArrayList<ConnectionBindRecord>> mServiceConnections = new ArrayMap<>();

    private final ArrayMap<ComponentName, ServiceRecord> mServicesByName = new ArrayMap<>();
    private final ArrayMap<Intent.FilterComparison, ServiceRecord> mServicesByIntent = new ArrayMap<>();

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case WHAT_ON_START_COMMAND:
                    Bundle data = msg.getData();
                    Intent intent = data.getParcelable("intent");

                    ServiceRecord sr = (ServiceRecord)msg.obj;

                    if (intent != null && sr != null) {
                        sr.service.onStartCommand(intent, 0, 0);
                    }else{
                        if (LOG) {
                            LogDebug.e(PLUGIN_TAG, "pss.onStartCommand fail.");
                        }
                    }
                    break;
            }
        }
    };

    public PluginServiceServer(Context context) {
        mContext = context;
        mStub = new Stub();
    }

    // 启动插件Service。说明见PluginServiceClient的定义
    ComponentName startServiceLocked(Intent intent, Messenger client) {
        intent = cloneIntentLocked(intent);
        ComponentName cn = intent.getComponent();
//        ProcessRecord callerPr = retrieveProcessRecordLocked(client);
        final ServiceRecord sr = retrieveServiceLocked(intent);
        if (sr == null) {
            return null;
        }
        if (!installServiceIfNeededLocked(sr)) {
            return null;
        }

        sr.startRequested = true;

        // 加入到列表中，统一管理
        mServicesByName.put(cn, sr);

        if (LOG) {
            LogDebug.i(PLUGIN_TAG, "PSM.startService(): Start! in=" + intent + "; sr=" + sr);
        }

        // 从binder线程post到ui线程，去执行Service的onStartCommand操作
        Message message = mHandler.obtainMessage(WHAT_ON_START_COMMAND);
        Bundle data = new Bundle();
        data.putParcelable("intent", intent);
        message.setData(data);
        message.obj = sr;
        mHandler.sendMessage(message);

        return cn;
    }

    // 停止插件的Service。说明见PluginServiceClient的定义
    int stopServiceLocked(Intent intent) {
        intent = cloneIntentLocked(intent);
        ServiceRecord sr = getServiceLocked(intent);
        if (sr == null) {
            return 0;
        }
        sr.startRequested = false;
        recycleServiceIfNeededLocked(sr);

        if (LOG) {
            LogDebug.i(PLUGIN_TAG, "PSM.stopService(): Stop! in=" + intent + "; sr=" + sr);
        }
        return 1;
    }

    // 绑定插件Service。说明见PluginServiceClient的定义
    int bindServiceLocked(Intent intent, IServiceConnection connection, int flags, Messenger client) {
        intent = cloneIntentLocked(intent);
        ComponentName cn = intent.getComponent();
        ProcessRecord callerPr = retrieveProcessRecordLocked(client);
        ServiceRecord sr = retrieveServiceLocked(intent);
        if (sr == null) {
            return 0;
        }
        if (!installServiceIfNeededLocked(sr)) {
            return 0;
        }

        // 将ServiceConnection连接加入各种表中
        ProcessBindRecord b = sr.retrieveAppBindingLocked(intent, callerPr);
        insertConnectionToRecords(sr, b, connection, flags);

        // 判断是否已经绑定过
        if (b.intent.hasBound) {
            // 之前此Intent已绑定过，则直接返回。像系统那样
            // 注意：不管哪个进程，只要第一次绑定过了，其后直接返回。像系统那样
            callConnectedMethodLocked(connection, cn, b.intent.binder);
        } else {
            // 没有绑定，则直接调用onBind，且记录绑定状态
            if (b.intent.apps.size() > 0) {
                IBinder bd = sr.service.onBind(intent);
                b.intent.hasBound = true;
                b.intent.binder = bd;
                if (bd != null) {
                    // 为空就不会回调，但仍算绑定成功。像系统那样
                    callConnectedMethodLocked(connection, cn, bd);
                }
            }
        }
        if (LOG) {
            LogDebug.i(PLUGIN_TAG, "PSM.bindService(): Bind! inb=" + b + "; fl=" + flags + "; sr=" + sr);
        }
        return 1;
    }

    private void insertConnectionToRecords(ServiceRecord sr, ProcessBindRecord b, IServiceConnection connection, int flags) {
        ConnectionBindRecord c = new ConnectionBindRecord(b, connection, flags);
        IBinder binder = connection.asBinder();

        // ServiceRecord.connections<Map - Key:IBinder>
        ArrayList<ConnectionBindRecord> clist = sr.connections.get(binder);
        if (clist == null) {
            clist = new ArrayList<>();
            sr.connections.put(binder, clist);
        }
        clist.add(c);

        // ProcessBindRecord.connections<List>
        b.connections.add(c);

        // ProcessRecord.connections<List>
        b.client.connections.add(c);

        // PluginServiceServer.mServiceConnections<Map - Key:IBinder>
        clist = mServiceConnections.get(binder);
        if (clist == null) {
            clist = new ArrayList<>();
            mServiceConnections.put(binder, clist);
        }
        clist.add(c);
    }

    // 取消插件Service的绑定。说明见PluginServiceClient的定义
    boolean unbindServiceLocked(IServiceConnection connection) {
        // ServiceConnection可以绑定多个服务，这次需逐一解绑
        IBinder binder = connection.asBinder();
        ArrayList<ConnectionBindRecord> clist = mServiceConnections.get(binder);
        if (clist == null) {
            if (LOG) {
                LogDebug.i(PLUGIN_TAG, "PSM.unbindService(): clist is null!");
            }
            return false;
        }
        while (clist.size() > 0) {
            ConnectionBindRecord r = clist.get(0);
            removeConnectionLocked(r);
            if (clist.size() > 0 && clist.get(0) == r) {
                // In case it didn't get removed above, do it now.
                clist.remove(0);
            }
        }
        return true;
    }

    // 将此Connection（ServiceConnection）从各种表中清除，并按需调用onUnbind方法
    // NOTE 有点像ActiveServices.removeConnectionLocked，但在处理逻辑上有不同
    private void removeConnectionLocked(ConnectionBindRecord c) {
        IBinder binder = c.conn.asBinder();
        ProcessBindRecord b = c.binding;
        ServiceRecord s = b.service;

        // ServiceRecord.connections<Map - Key:IBinder>
        ArrayList<ConnectionBindRecord> clist = s.connections.get(binder);
        if (clist != null) {
            clist.remove(c);
            if (clist.size() == 0) {
                s.connections.remove(binder);
            }
        }
        // ProcessBindRecord.connections<List>
        b.connections.remove(c);

        // ProcessRecord.connections<List>
        b.client.connections.remove(c);

        // PluginServiceServer.mServiceConnections<Map - Key:IBinder>
        clist = mServiceConnections.get(binder);
        if (clist != null) {
            clist.remove(c);
            if (clist.size() == 0) {
                mServiceConnections.remove(binder);
            }
        }

        // 若所有BindConnection都已不再连接，则清除其Map
        if (b.connections.size() == 0) {
            b.intent.apps.remove(b.client);
        }

        // 之前已经打算解绑了？无需再做
        if (c.serviceDead) {
            return;
        }

        // 当所有应用都已解绑后，则直接调用onUnbind
        if (b.intent.apps.size() == 0 && b.intent.hasBound) {
            b.intent.hasBound = false;
            s.service.onUnbind(b.intent.intent.getIntent());
            if (LOG) {
                LogDebug.i(PLUGIN_TAG, "PSM.removeConnectionLocked(): boundRef is 0, call onUnbind(), sr=" + s);
            }

            // 尝试释放Service对象
            if ((c.flags & Context.BIND_AUTO_CREATE) != 0) {
                recycleServiceIfNeededLocked(s);
            }
        } else {
            if (LOG) {
                LogDebug.i(PLUGIN_TAG, "PSM.removeConnectionLocked(): Not unbind, sr=" + s);
            }
        }
    }

    public IPluginServiceServer getService() {
        return mStub;
    }

    // 若Client和Server在同一进程，则两者的intent对象完全相同
    // 换言之，如果Client端修改了intent对象，则对应的，server端也会被修改，这不符合预期
    // 故，所有的Intent操作都必须Clone一份
    private Intent cloneIntentLocked(Intent intent) {
        return new Intent(intent);
    }

    // 通过Intent获取ServiceRecord服务，如无则直接返回Null
    private ServiceRecord getServiceLocked(Intent service) {
        ComponentName cn = service.getComponent();
        return mServicesByName.get(cn);
    }

    // 通过Intent对象创建或获取ServiceRecord服务。涉及到插件信息的获取
    private ServiceRecord retrieveServiceLocked(Intent service) {
        ComponentName cn = service.getComponent();
        ServiceRecord sr = mServicesByName.get(cn);
        if (sr != null) {
            return sr;
        }

        Intent.FilterComparison fi = new Intent.FilterComparison(service);
        sr = mServicesByIntent.get(fi);
        if (sr != null) {
            return sr;
        }
        String pn = cn.getPackageName();
        String name = cn.getClassName();

        // 看这个Plugin是否可以被打开
        if (!RePlugin.isPluginInstalled(pn)) {
            if (LOGR) {
                LogRelease.e(PLUGIN_TAG, "psm.is: p n ex " + name);
            }
            return null;
        }

        // 开始尝试获取插件的ServiceInfo
        ComponentList col = Factory.queryPluginComponentList(pn);
        if (col == null) {
            if (LOG) {
                Log.e(TAG, "installServiceLocked(): Fetch Component List Error! pn=" + pn);
            }
            return null;
        }
        ServiceInfo si = col.getService(cn.getClassName());
        if (si == null) {
            if (LOG) {
                Log.e(TAG, "installServiceLocked(): Not register! pn=" + pn);
            }
            return null;
        }

        // 构建，放入表中

        sr = new ServiceRecord(cn, fi, si);
        mServicesByName.put(cn, sr);
        mServicesByIntent.put(fi, sr);
        return sr;
    }

    // 判断是否已加载过Service对象，如无则创建它
    private boolean installServiceIfNeededLocked(final ServiceRecord sr) {
        if (sr.service != null) {
            return true;
        }

        try {
            Boolean result = ThreadUtils.syncToMainThread(new Callable<Boolean>() {
                @Override
                public Boolean call() {
                    return installServiceLocked(sr);
                }
            }, 6000);

            if (result == null) {
                return false;
            }
            return result;
        } catch (Throwable e) {
            if (LOG) {
                LogDebug.e(PLUGIN_TAG, "pss.isinl e:", e);
            }
            return false;
        }
    }

    // 加载插件，获取Service对象，并将其缓存起来
    private boolean installServiceLocked(ServiceRecord sr) {
        // 通过ServiceInfo创建Service对象
        Context plgc = Factory.queryPluginContext(sr.plugin);
        if (plgc == null) {
            if (LogDebug.LOG) {
                Log.e(TAG, "installServiceLocked(): Fetch Context Error! pn=" + sr.plugin);
            }
            return false;
        }
        ClassLoader cl = plgc.getClassLoader();
        if (cl == null) {
            if (LOGR) {
                LogRelease.e(PLUGIN_TAG, "psm.is: cl n " + sr.className);
            }
            return false;
        }

        // 构建Service对象
        Service s;
        try {
            s = (Service) cl.loadClass(sr.serviceInfo.name).newInstance();
        } catch (Throwable e) {
            if (LOGR) {
                LogRelease.e(TAG, "isl: ni f " + sr.plugin, e);
            }
            return false;
        }

        // 只复写Context，别的都不做
        try {
            attachBaseContextLocked(s, plgc);
        } catch (Throwable e) {
            if (LOGR) {
                LogRelease.e(PLUGIN_TAG, "psm.is: abc e", e);
            }
            return false;
        }
        s.onCreate();
        sr.service = s;

        // 开启“坑位”服务，防止进程被杀
        ComponentName pitCN = getPitComponentName();
        sr.pitComponentName = pitCN;
        startPitService(pitCN);
        return true;
    }

    // 最终会调用Client端的ServiceConnection
    private void callConnectedMethodLocked(IServiceConnection conn, ComponentName cn, IBinder b) {
        try {
            conn.connected(cn, b);
        } catch (RemoteException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }
    }

    // 判断是否绑定或启动服务，根据情况来释放Service
    // NOTE 有点像ActiveServices.bringDownServiceIfNeededLocked，但在处理逻辑上有不同
    private void recycleServiceIfNeededLocked(ServiceRecord sr) {
        // 是否已被startService开启？
        if (sr.startRequested) {
            if (LOG) {
                LogDebug.i(PLUGIN_TAG, "PSM.recycleServiceIfNeededLocked(): Not Recycle because startRequested is true! sr=" + sr);
            }
            return;
        }
        // 服务是否有被绑定的？
        boolean hasConn = sr.hasAutoCreateConnections();
        if (hasConn) {
            if (LOG) {
                LogDebug.i(PLUGIN_TAG, "PSM.recycleServiceIfNeededLocked(): Not Recycle because bindingCount > 0! sr=" + sr);
            }
            return;
        }
        // 开始回收服务
        recycleServiceLocked(sr);
    }

    // 释放Service的全部资源，调用onDestroy方法，并停止“坑位服务”
    // NOTE 有点像ActiveServices.bringDownServiceLocked，但在处理逻辑上有不同
    private void recycleServiceLocked(ServiceRecord r) {
        if (LOG) {
            LogDebug.i(PLUGIN_TAG, "PSM.recycleServiceLocked(): Recycle Now!");
        }
        // Report to all of the connections that the service is no longer
        // available.
        for (int conni = r.connections.size() - 1; conni >= 0; conni--) {
            ArrayList<ConnectionBindRecord> c = r.connections.valueAt(conni);
            for (int i = 0; i < c.size(); i++) {
                ConnectionBindRecord cr = c.get(i);
                // There is still a connection to the service that is
                // being brought down.  Mark it as dead.
                cr.serviceDead = true;
                callConnectedMethodLocked(cr.conn, r.name, null);
            }
        }
        mServicesByName.remove(r.name);
        mServicesByIntent.remove(r.intent);

        if (r.bindings.size() > 0) {
            r.bindings.clear();
        }

        r.service.onDestroy();

        // 停止“坑位”服务，系统可以根据需要来回收了
        ComponentName pitCN = getPitComponentName();
        r.pitComponentName = pitCN;
        stopPitService(pitCN);
    }

    // 通过反射调用Service.attachBaseContext方法（Protected的）
    private void attachBaseContextLocked(ContextWrapper cw, Context c) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        if (mAttachBaseContextMethod == null) {
            mAttachBaseContextMethod = ContextWrapper.class.getDeclaredMethod("attachBaseContext", Context.class);
            mAttachBaseContextMethod.setAccessible(true);
        }
        mAttachBaseContextMethod.invoke(cw, c);

        // init Application
        Field applicationField = Service.class.getDeclaredField("mApplication");
        if (applicationField != null) {
            applicationField.setAccessible(true);
            applicationField.set(cw, c.getApplicationContext());
        }
    }

    class Stub extends IPluginServiceServer.Stub {

        @Override
        public ComponentName startService(Intent intent, Messenger client) throws RemoteException {
            synchronized (LOCKER) {
                return PluginServiceServer.this.startServiceLocked(intent, client);
            }
        }

        @Override
        public int stopService(Intent intent, Messenger client) throws RemoteException {
            synchronized (LOCKER) {
                return PluginServiceServer.this.stopServiceLocked(intent);
            }
        }

        @Override
        public int bindService(Intent intent, IServiceConnection conn, int flags, Messenger client) throws RemoteException {
            synchronized (LOCKER) {
                return PluginServiceServer.this.bindServiceLocked(intent, conn, flags, client);
            }
        }

        @Override
        public boolean unbindService(IServiceConnection conn) throws RemoteException {
            synchronized (LOCKER) {
                return PluginServiceServer.this.unbindServiceLocked(conn);
            }
        }

        @Override
        public String dump() throws RemoteException {
            synchronized (LOCKER) {
                return PluginServiceServer.this.dump();
            }
        }
    }

    // 通过Client端传来的IBinder（Messenger）来获取Pid，以及进程信息
    private ProcessRecord retrieveProcessRecordLocked(Messenger client) {
        int callerPid = Binder.getCallingPid();
        ProcessRecord pr = mProcesses.get(callerPid);
        if (pr == null) {
            pr = new ProcessRecord(callerPid, client);
            mProcesses.put(callerPid, pr);
        }
        return pr;
    }

    // 构建一个占坑服务
    private ComponentName getPitComponentName() {
        String pname = IPC.getCurrentProcessName();
        int process = PluginClientHelper.getProcessInt(pname);

        return PluginPitService.makeComponentName(mContext, process);
    }

    // 开启“坑位”服务，防止进程被杀
    private void startPitService(ComponentName pitCN) {
        // TODO 其实，有一种更好的办法……敬请期待

        if (LOG) {
            LogDebug.d(TAG, "startPitService: Start " + pitCN);
        }

        Intent intent = new Intent();
        intent.setComponent(pitCN);

        try {
            mContext.startService(intent);
        } catch (Exception e) {
            // 就算AMS出了问题（如system_server挂了，概率极低，和低配ROM有关），最多也就是服务容易被系统回收，但不能让它“不干活”
            e.printStackTrace();
        }
    }

    // 停止“坑位”服务，系统可以根据需要来回收了
    private void stopPitService(ComponentName pitCN) {

        if (LOG) {
            LogDebug.d(TAG, "stopPitService: Stop " + pitCN);
        }

        Intent intent = new Intent();
        intent.setComponent(pitCN);
        try {
            mContext.stopService(intent);
        } catch (Exception e) {
            // 就算AMS出了问题（如system_server挂了，概率极低，和低配ROM有关），最多也就是服务容易被系统回收，但不能让它“不干活”
            e.printStackTrace();
        }
    }

    /**
     * dump当前进程中运行的service详细信息，供client端使用
     *
     * @return
     */
    private String dump() {

        if (mServicesByName == null || mServicesByName.isEmpty()) {
            return null;
        }

        JSONArray jsonArray = new JSONArray();

        JSONObject serviceObj;
        for (Map.Entry<ComponentName, ServiceRecord> entry : mServicesByName.entrySet()) {
            ComponentName key = entry.getKey();
            ServiceRecord value = entry.getValue();

            serviceObj = new JSONObject();

            JSONHelper.putNoThrows(serviceObj, "className", key.getClassName());
            JSONHelper.putNoThrows(serviceObj, "process", value.getServiceInfo().processName);
            JSONHelper.putNoThrows(serviceObj, "plugin", value.getPlugin());
            JSONHelper.putNoThrows(serviceObj, "pitClassName", value.getPitComponentName().getClassName());

            jsonArray.put(serviceObj);
        }

        return jsonArray.toString();
    }
}