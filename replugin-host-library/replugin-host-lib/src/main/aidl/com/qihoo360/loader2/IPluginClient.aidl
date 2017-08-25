package com.qihoo360.loader2;

import com.qihoo360.replugin.component.service.server.IPluginServiceServer;

/**
 * @author RePlugin Team
 */
interface IPluginClient {

    // 参数 plugin, process 可能有冗余，目前临时使用，后续可能优化
    String allocActivityContainer(String plugin, int process, String target, in Intent intent);

    // 参数 plugin 用来处理多插件单进程情况
    IBinder queryBinder(String plugin, String binder);

    void releaseBinder();

    oneway void sendIntent(in Intent intent);

    void sendIntentSync(in Intent intent);

    int sumActivities();

    IPluginServiceServer fetchServiceServer();

    /**
     * 插件收到广播
     *
     * @param plugin   插件名称
     * @param receiver Receiver 名称
     * @param Intent   广播的 Intent 数据
     */
    void onReceive(String plugin, String receiver, in Intent intent);

    /**
     * dump通过插件化框架启动起来的Service信息
     */
    String dumpServices();

    /**
     * dump插件化框架中存储的详细Activity坑位映射表
     */
    String dumpActivities();
}