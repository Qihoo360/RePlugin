package com.qihoo360.loader2;

import com.qihoo360.replugin.component.service.server.IPluginServiceServer;

/**
 * @author RePlugin Team
 */
interface IPluginClient {


    String allocActivityContainer(String plugin, int process, String target, in Intent intent);


    IBinder queryBinder(String plugin, String binder);

    void releaseBinder();

    oneway void sendIntent(in Intent intent);

    void sendIntentSync(in Intent intent);

    int sumActivities();

    IPluginServiceServer fetchServiceServer();


    void onReceive(String plugin, String receiver, in Intent intent);

    String dumpServices();

    String dumpActivities();
}