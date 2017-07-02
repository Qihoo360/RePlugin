
package com.qihoo360.replugin.gen;

/**
 * 注意：此文件由插件化框架自动生成，请不要手动修改。
 */
public class RePluginHostConfig {

    // 背景透明的坑的数量（每种 launchMode 不同）
    public static int ACTIVITY_PIT_COUNT_TS_STANDARD = 2;
    public static int ACTIVITY_PIT_COUNT_TS_SINGLE_TOP = 2;
    public static int ACTIVITY_PIT_COUNT_TS_SINGLE_TASK = 2;
    public static int ACTIVITY_PIT_COUNT_TS_SINGLE_INSTANCE = 2;

    // 背景不透明的坑的数量（每种 launchMode 不同）
    public static int ACTIVITY_PIT_COUNT_NTS_STANDARD = 6;
    public static int ACTIVITY_PIT_COUNT_NTS_SINGLE_TOP = 2;
    public static int ACTIVITY_PIT_COUNT_NTS_SINGLE_TASK = 3;
    public static int ACTIVITY_PIT_COUNT_NTS_SINGLE_INSTANCE = 2;

    // TaskAffinity 组数
    public static int ACTIVITY_PIT_COUNT_TASK = 2;

    // 是否使用 AppCompat 库
    public static boolean ACTIVITY_PIT_USE_APPCOMPAT = false;

    //------------------------------------------------------------
    // 主程序支持的插件版本范围
    //------------------------------------------------------------

    // HOST 向下兼容的插件版本
    public static int ADAPTER_COMPATIBLE_VERSION = 10;

    // HOST 插件版本
    public static int ADAPTER_CURRENT_VERSION = 12;
}