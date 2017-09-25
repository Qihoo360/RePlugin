package com.qihoo360.replugin.component.service.server;

import android.content.ComponentName;
import android.os.Messenger;

import com.qihoo360.loader2.mgr.IServiceConnection;

/**
 * 负责Server端的服务调度、提供等工作，是服务的提供方，核心类之一
 *
 * @hide 框架内部使用
 * @author RePlugin Team
 */
interface IPluginServiceServer {
    ComponentName startService(in Intent intent, in Messenger client);
    int stopService(in Intent intent, in Messenger client);

    int bindService(in Intent intent, in IServiceConnection conn, int flags, in Messenger client);
    boolean unbindService(in IServiceConnection conn);

    String dump();
}