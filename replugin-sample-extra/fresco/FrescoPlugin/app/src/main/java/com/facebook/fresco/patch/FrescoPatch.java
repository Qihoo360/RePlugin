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

package com.facebook.fresco.patch;

import android.content.Context;

import com.facebook.drawee.generic.GenericDraweeHierarchyInflater;
import com.facebook.drawee.view.SimpleDraweeView;

/**
 * 为 “自己编译好的fresco drawee 模块(对应drawee-modified-1.7.1.jar)” 设置回调接口
 *
 * @author RePlugin Team
 */
public class FrescoPatch {

    /**
     * 初始化
     * <p>
     * 为SimpleDraweeView设置回调
     * 为GenericDraweeHierarchyInflater设置回调
     */
    public static void initialize(Context context) {
        DraweeStyleableCallbackImpl draweeStyleableCallback = new DraweeStyleableCallbackImpl(context);
        SimpleDraweeView.setDraweeStyleableCallback(draweeStyleableCallback);
        GenericDraweeHierarchyInflater.setDraweeStyleableCallback(draweeStyleableCallback);
    }
}