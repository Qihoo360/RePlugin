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
 * 所有可查询的接口都从此interface继承
 * 在插件体系中，module是一种略高于interface的概念
 * 一个插件可导出一个到多个module，这些module可输出自己业务的各种interface
 *
 * @author RePlugin Team
 *
 */
public interface IModule {

    /**
     * 万能接口：当不能升级adapter.jar的时候再考虑使用
     * @param args
     * @return
     */
    Object invoke(Object...args);
}
