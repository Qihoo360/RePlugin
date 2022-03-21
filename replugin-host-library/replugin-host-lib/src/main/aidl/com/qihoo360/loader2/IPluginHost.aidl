package com.qihoo360.loader2;

import android.content.IntentFilter;
import android.content.Intent;

import com.qihoo360.loader2.IPluginClient;
import com.qihoo360.loader2.PluginBinderInfo;
import com.qihoo360.replugin.model.PluginInfo;

import com.qihoo360.replugin.component.service.server.IPluginServiceServer;

import com.qihoo360.replugin.packages.IPluginManagerServer;

/**
 * @author RePlugin Team
 */
interface IPluginHost {

    void installBinder(String name, in IBinder binder);

    IBinder fetchBinder(String name);

    long fetchPersistentCookie();

    IPluginClient startPluginProcess(String plugin, int process, inout PluginBinderInfo info);

    String attachPluginProcess(String process, int index, in IBinder binder, String def);

    List<PluginInfo> listPlugins();

    void regActivity(int index, String plugin, String container, String activity);

    void unregActivity(int index, String plugin, String container, String activity);

    void regService(int index, String plugin, String service);

    void unregService(int index, String plugin, String service);

    void regPluginBinder(in PluginBinderInfo info, IBinder binder);

    void unregPluginBinder(in PluginBinderInfo info, IBinder binder);

    void regReceiver(String plugin, in Map receiverFilterMap);

    void unregReceiver();

    void onReceive(String plugin, String receiver, in Intent intent);

    int sumBinders(int index);

    void updatePluginInfo(in PluginInfo info);

    PluginInfo pluginDownloaded(String path);

    boolean pluginUninstalled(in PluginInfo info);

    boolean pluginExtracted(String path);

    oneway void sendIntent2Process(String target, in Intent intent);

    oneway void sendIntent2Plugin(String target, in Intent intent);

    void sendIntent2ProcessSync(String target, in Intent intent);

    void sendIntent2PluginSync(String target, in Intent intent);

    boolean isProcessAlive(String name);

    IBinder queryPluginBinder(String plugin, String binder);

    List queryPluginsReceiverList(in Intent intent);

    IPluginServiceServer fetchServiceServer();

    IPluginManagerServer fetchManagerServer();

    int getTaskAffinityGroupIndex(String taskAffinity);

    int getPidByProcessName(String processName);

    String getProcessNameByPid(int pid);

    String dump();
}