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

import android.content.Intent;
import android.os.Bundle;

/**
 * PluginContext 相关流程逻辑注入， 可通过回调自定义PluginContext（插件Context）中的某些行为逻辑
 *
 * @author RePlugin Team
 */

public interface ContextInjector {

    /**
     * 回调时机：调用super.startActivity之前
     * <p>
     * 可供业务层修改Intent参数或者加入自定义逻辑
     *
     * @param intent
     */
    void startActivityBefore(Intent intent);

    /**
     * 回调时机：调用super.startActivity之后
     * <p>
     * 可供业务层修改Intent参数或者加入自定义逻辑
     *
     * @param intent
     */
    void startActivityAfter(Intent intent);

    /**
     * 回调时机：调用super.startActivity之前
     * <p>
     * 可供业务层修改Intent参数或者加入自定义逻辑
     *
     * @param intent
     */
    void startActivityBefore(Intent intent, Bundle options);

    /**
     * 回调时机：调用super.startActivity之后
     * <p>
     * 可供业务层修改Intent参数或者加入自定义逻辑
     *
     * @param intent
     */
    void startActivityAfter(Intent intent, Bundle options);

    /**
     * 回调时机：调用super.startService之前
     * <p>
     * 可供业务层修改Intent参数或者加入自定义逻辑
     *
     * @param service
     */
    void startServiceBefore(Intent service);

    /**
     * 回调时机：调用super.startService之后
     * <p>
     * 可供业务层修改Intent参数或者加入自定义逻辑
     *
     * @param service
     */
    void startServiceAfter(Intent service);
}
