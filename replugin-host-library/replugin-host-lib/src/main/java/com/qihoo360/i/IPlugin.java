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
 * 此接口由插件负责导出
 * 表示一个具体的物理上的插件实体，例如barcode.jar
 * 具体导出细节可看Factory
 *
 * @author RePlugin Team
 *
 */
public interface IPlugin {

    /**
     * @param c 需要查询的interface的类
     * @return
     */
    IModule query(Class<? extends IModule> c);

}
