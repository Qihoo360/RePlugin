package com.qihoo360.replugin.packages;

import com.qihoo360.replugin.model.PluginInfo;
import com.qihoo360.replugin.packages.PluginRunningList;

/**
 * 插件管理器。用来控制插件的安装、卸载、获取等。运行在常驻进程中
 * <p>
 * 补充：涉及到插件交互、运行机制有关的管理器，在IPluginHost中
 *
 * @author RePlugin Team
 */
interface IPluginManagerServer {

    /**
     * 安装一个插件
     * <p>
     * 注意：若为旧插件（p-n开头），则应使用IPluginHost的pluginDownloaded方法
     *
     * @return 安装的插件的PluginInfo对象
     */
    PluginInfo install(String path);

    /**
     * 卸载一个插件
     * <p>
     * 注意：只针对“纯APK”插件方案
     *
     * @param info 插件信息
     * @return 是否成功卸载插件？
     */
    boolean uninstall(in PluginInfo info);

    /**
     * 加载插件列表，方便之后使用
     * <p>
     * TODO 这里只返回"新版插件"，供PmBase使用。将来会合并
     *
     * @return PluginInfo的列表
     */
    List<PluginInfo> load();

    /**
     * 更新所有插件列表
     *
     * @return PluginInfo的列表
     */
    List<PluginInfo> updateAll();

    /**
     * 设置isUsed状态，并通知所有进程更新
     *
     * @param pluginName 插件名
     * @param used 是否已经使用
     */
    void updateUsed(String pluginName, boolean used);

    /**
     * 获取正在运行的插件列表
     *
     * @return 正在运行的插件名列表
     */
    PluginRunningList getRunningPlugins();

    /**
     * 插件是否正在运行？
     *
     * @param pluginName 插件名
     * @param process 指定进程名，如为Null则表示查所有
     * @return 是否在运行？
     */
    boolean isPluginRunning(String pluginName, String process);

    /**
     * 当进程启动时，同步正在运行的插件状态到Server端
     *
     * @param list         正在运行的插件名列表
     */
    void syncRunningPlugins(in PluginRunningList list);

    /**
     * 当进程启动时，同步正在运行的插件状态到Server端
     *
     * @param processName  进程名
     * @param pluginName   正在运行的插件名
     */
    void addToRunningPlugins(String processName, int pid, String pluginName);

    /**
     * 获取正在运行此插件的进程名列表
     *
     * @param pluginName 要查询的插件名
     * @return 正在运行此插件的进程名列表。一定不会为Null
     */
    String[] getRunningProcessesByPlugin(String pluginName);
}
