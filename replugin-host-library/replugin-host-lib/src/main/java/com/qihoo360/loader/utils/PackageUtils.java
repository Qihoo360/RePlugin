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

package com.qihoo360.loader.utils;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.qihoo360.replugin.helper.LogDebug;

/**
 * @author RePlugin Team
 */
public class PackageUtils {

    /**
     * 获取PackageInfo对象
     * <p>
     * 注：getPackageArchiveInfo Android 2.x上，可能拿不到signatures，本可以通过反射去获取，但是考虑到会触发Android 的灰/黑名单，这个方法就不再继续适配2.X了
     *
     * @return
     */
    public static PackageInfo getPackageArchiveInfo(PackageManager pm, String pkgFilePath, int flags) {
        PackageInfo info = null;
        try {
            info = pm.getPackageArchiveInfo(pkgFilePath, flags);
        } catch (Throwable e) {
            if (LogDebug.LOG) {
                e.printStackTrace();
            }
        }

        return info;
    }
}