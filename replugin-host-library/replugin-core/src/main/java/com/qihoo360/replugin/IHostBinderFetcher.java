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

package com.qihoo360.replugin;

import android.os.IBinder;

/**
 * 用来实现主程序提供IBinder给其他插件
 * <p>
 * 插件获取方法：Factory.query("main", "IShare")，返回值：IBinder
 * <p>
 * TODO 未来会废弃Factory类，并做些调整
 *
 * @author RePlugin Team
 */
public interface IHostBinderFetcher {

    /**
     * 主程序需实现此方法，来返回一个IBinder对象，供插件使用
     *
     * @param module 模块名
     * @return 一个IBinder对象
     */
    IBinder query(String module);
}
