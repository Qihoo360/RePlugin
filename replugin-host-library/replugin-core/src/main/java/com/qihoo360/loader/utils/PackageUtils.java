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
import android.content.pm.Signature;
import android.os.Build;
import android.util.DisplayMetrics;

import com.qihoo360.replugin.helper.LogDebug;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;

import java.io.File;

import static com.qihoo360.replugin.helper.LogDebug.LOG;
import static com.qihoo360.replugin.helper.LogDebug.MISC_TAG;

/**
 * @author RePlugin Team
 */
public class PackageUtils {

    /**
     * @param archiveFilePath
     * @return
     */
    private static final Signature[] getPackageArchiveSignaturesInfoAndroid2x(String archiveFilePath) {
        //
        try {
            // 1. 新建PackageParser的实例
            Object packageParser = ConstructorUtils.invokeConstructor(ClassUtils.getClass("android.content.pm.PackageParser"), archiveFilePath);

            // 2. 调用PackageParser.parsePackage()方法，返回值为Package对象
            DisplayMetrics metrics = new DisplayMetrics();
            metrics.setToDefaults();

            Object pkg = MethodUtils.invokeMethod(packageParser, true, "parsePackage", new File(archiveFilePath), archiveFilePath, metrics, 0);
            if (pkg == null) {
                if (LOG) {
                    LogDebug.d(MISC_TAG, "failed to parsePackage: f=" + archiveFilePath);
                }
                return null;
            }

            // 3. 调用PackageParser.collectCertificates方法
            boolean rc = (Boolean) MethodUtils.invokeMethod(packageParser, "collectCertificates", pkg, 0);
            if (!rc) {
                return null;
            }

            // 4. 获取Package.mSignatures
            Object signatures[] = (Object[]) FieldUtils.readField(pkg, "mSignatures");
            int n = signatures.length;
            if (n <= 0) {
                if (LOG) {
                    LogDebug.d(MISC_TAG, "not found signatures: f=" + archiveFilePath);
                }
            }
            if (n > 0) {
                if (LOG) {
                    LogDebug.d(MISC_TAG, "found signatures for android 2.x: length=" + signatures.length);
                }
                Signature[] a = new Signature[n];
                System.arraycopy(signatures, 0, a, 0, n);
                return a;
            }
        } catch (Throwable e) {
            if (LOG) {
                LogDebug.d(MISC_TAG, e.getMessage(), e);
            }
        }
        return null;
    }

    /**
     * 获取PackageInfo对象
     * 该方法解决了Android 2.x环境不能获取签名的系统问题，故可以“全面使用”
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

        if (info == null) {
            return null;
        }

        // Android 2.x的系统通过常规手段（pm.getPackageArchiveInfo）时会返回null，只能通过底层实现
        // 判断依据：1、想要签名；2、没拿到签名；3、Android 4.0以前
        if ((flags & PackageManager.GET_SIGNATURES) != 0) {
            if (info.signatures == null) {
                if (Build.VERSION.SDK_INT < 14) {
                    info.signatures = getPackageArchiveSignaturesInfoAndroid2x(pkgFilePath);
                }
            }
        }

        return info;
    }
}
