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
 *
 */

package com.qihoo360.replugin.gradle.plugin.injector

import com.qihoo360.replugin.gradle.plugin.injector.identifier.GetIdentifierInjector
import com.qihoo360.replugin.gradle.plugin.injector.localbroadcast.LocalBroadcastInjector
import com.qihoo360.replugin.gradle.plugin.injector.provider.ProviderInjector
import com.qihoo360.replugin.gradle.plugin.injector.provider.ProviderInjector2
import com.qihoo360.replugin.gradle.plugin.injector.loaderactivity.LoaderActivityInjector

/**
 * @author RePlugin Team
 */
public enum Injectors {

    LOADER_ACTIVITY_CHECK_INJECTOR('LoaderActivityInjector', new LoaderActivityInjector(), '替换 Activity 为 LoaderActivity'),
    LOCAL_BROADCAST_INJECTOR('LocalBroadcastInjector', new LocalBroadcastInjector(), '替换 LocalBroadcast 调用'),
    PROVIDER_INJECTOR('ProviderInjector', new ProviderInjector(), '替换 Provider 调用'),
    PROVIDER_INJECTOR2('ProviderInjector2', new ProviderInjector2(), '替换 ContentProviderClient 调用'),
    GET_IDENTIFIER_INJECTOR('GetIdentifierInjector', new GetIdentifierInjector(), '替换 Resource.getIdentifier 调用')

    IClassInjector injector
    String nickName
    String desc

    Injectors(String nickName, IClassInjector injector, String desc) {
        this.injector = injector
        this.nickName = nickName
        this.desc = desc;
    }
}

