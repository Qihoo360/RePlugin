package com.qihoo360.mobilesafe.svcmanager;

import com.qihoo360.replugin.IBinderGetter;

interface IServiceChannel {

    IBinder getService(String serviceName);

    void addService(String serviceName, IBinder service);

    void addServiceDelayed(String serviceName, IBinderGetter getter);

    void removeService(String serviceName);

    IBinder getPluginService(String pluginName, String serviceName, IBinder deathMonitor);

    void onPluginServiceRefReleased(String pluginName, String serviceName);
}