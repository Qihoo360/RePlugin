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

package com.qihoo360.i;

/**
 * 注意：这里目前仅放置一些常量，大部分方法已移动至 PluginCommImpl 中
 *
 * @author RePlugin Team
 */
public interface IPluginManager {

    /**
     * 插件Activity上下文通过startActivity启动，用系统默认的启动方法
     * 如果设置该值，总是为boolean值true
     */
    String KEY_COMPATIBLE = "compatible";

    /**
     * 通过Intent的extra参数指出目标插件名
     * 插件Activity上下文通过startActivity启动其它插件Activity时用到
     * 如果不指定，则默认用当前Activity的插件
     * 如果设置了KEY_COMPATIBLE，则忽略此参数
     */
    String KEY_PLUGIN = "plugin";

    /**
     * 通过Intent的extra参数指出目标Activity名
     * 如果不指定，则默认用ComponentName参数的类名来启动
     * 如果设置了KEY_COMPATIBLE，则忽略此参数
     */
    String KEY_ACTIVITY = "activity";

    /**
     * 通过Intent的extra参数指出需要在指定进程中启动
     * 只能启动standard的Activity，不能启动singleTask、singleInstance等
     * 不指定时，自动分配插件进程，即PROCESS_AUTO
     */
    String KEY_PROCESS = "process";

    /**
     * 自动分配插件进程
     */
    int PROCESS_AUTO = Integer.MIN_VALUE;

    /**
     * UI进程
     */
    int PROCESS_UI = -1;

    /**
     * 常驻进程
     */
    int PROCESS_PERSIST = -2;
}
