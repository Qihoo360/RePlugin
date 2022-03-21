package com.qihoo360.replugin.packages;

import com.qihoo360.replugin.model.PluginInfo;
import com.qihoo360.replugin.packages.PluginRunningList;

/**
 *
 * @author RePlugin Team
 */
interface IPluginManagerServer {

    /**

     */
    PluginInfo install(String path);

    /**

     */
    boolean uninstall(in PluginInfo info);

    /**

     */
    List<PluginInfo> load();


    List<PluginInfo> updateAll();


    void updateUsed(String pluginName, boolean used);


    PluginRunningList getRunningPlugins();


    boolean isPluginRunning(String pluginName, String process);


    void syncRunningPlugins(in PluginRunningList list);


    void addToRunningPlugins(String processName, int pid, String pluginName);

    String[] getRunningProcessesByPlugin(String pluginName);
}
