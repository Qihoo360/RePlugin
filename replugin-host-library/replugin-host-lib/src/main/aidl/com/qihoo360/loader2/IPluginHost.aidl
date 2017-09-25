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

    /**
    * 注册某插件下所有静态声明的的 receiver 到常驻进程
    */
    void regReceiver(String plugin, in Map receiverFilterMap);

    void unregReceiver();

    /**
     * 插件收到广播
     *
     * @param plugin 插件名称
     * @param receiver Receiver 名称
     * @param Intent 广播的 Intent 数据
     */
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

    /**
     * 根据 Inent 查询所有插件中的与之匹配的 Receivers
     */
    List queryPluginsReceiverList(in Intent intent);

    /**
     * 获取“全新Service管理方案”在Server端的服务
     * Added by Jiongxuan Zhang
     */
    IPluginServiceServer fetchServiceServer();

    /**
     * 获取 IPluginManagerServer（纯APK方案使用）的插件服务
     * Added by Jiongxuan Zhang
     */
    IPluginManagerServer fetchManagerServer();

    /**
     * 根据 taskAffinity，判断应该取第几组 TaskAffinity
     * 由于 taskAffinity 是跨进程的属性，所以这里要将 taskAffinityGroup 的数据保存在常驻进程中
     * Added by hujunjie
     */
    int getTaskAffinityGroupIndex(String taskAffinity);

    /**
     * 通过进程名来获取PID
     */
    int getPidByProcessName(String processName);

    /**
     * 通过PID来获取进程名
     */
    String getProcessNameByPid(int pid);

    /**
     * dump详细的运行时信息
     */
    String dump();
}