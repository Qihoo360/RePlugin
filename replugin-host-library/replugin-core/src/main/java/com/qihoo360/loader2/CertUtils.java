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

package com.qihoo360.loader2;

import android.content.pm.PackageInfo;
import android.content.pm.Signature;
import android.text.TextUtils;

import com.qihoo360.loader.utils.StringUtils;
import com.qihoo360.replugin.helper.LogDebug;
import com.qihoo360.replugin.helper.LogRelease;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import static com.qihoo360.replugin.helper.LogDebug.LOG;
import static com.qihoo360.replugin.helper.LogDebug.PLUGIN_TAG;

/**
 * @author RePlugin Team
 */
public class CertUtils {

    /**
     *
     */
    public static final ArrayList<String> SIGNATURES = new ArrayList<String>();

    public static final boolean isPluginSignatures(PackageInfo info) {
        if (info == null) {
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "info is null");
            }
            return false;
        }

        if (info.signatures == null) {
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "signatures is null");
            }
            return false;
        }

        for (Signature signature : info.signatures) {
            boolean match = false;
            String md5 = StringUtils.toHexString(md5NonE(signature.toByteArray()));
            for (String element : SIGNATURES) {
                if (TextUtils.equals(md5, element)) {
                    match = true;
                    if (LOG) {
                        LogDebug.i(PLUGIN_TAG, "isPluginSignatures: match. " + md5 + " package=" + info.packageName);
                    }
                    break;
                }
            }
            if (!match) {
                if (LOG) {
                    LogDebug.e(PLUGIN_TAG, "isPluginSignatures: unknown signature: " + md5 + " package=" + info.packageName);
                }
                if (LogRelease.LOGR) {
                    LogRelease.e(PLUGIN_TAG, "ibs: us " + md5);
                }
                return false;
            }
        }

        return true;
    }

    public static final byte[] md5(byte buffer[]) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        digest.update(buffer, 0, buffer.length);
        return digest.digest();
    }

    public static final byte[] md5NonE(byte buffer[]) {
        try {
            return md5(buffer);
        } catch (NoSuchAlgorithmException e) {
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, e.getMessage(), e);
            }
        }
        return new byte[0];
    }
}
