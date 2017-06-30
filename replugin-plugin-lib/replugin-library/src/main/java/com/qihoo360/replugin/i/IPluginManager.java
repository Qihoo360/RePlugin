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

package com.qihoo360.replugin.i;


/**
 * 负责插件和插件之间的interface互通，可通过插件Entry得到，也可通过wrapper类Factory直接调用
 *
 * @author RePlugin Team
 */
public interface IPluginManager {

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
