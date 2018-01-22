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

package com.qihoo360.replugin.utils;

import android.os.Build;
import android.util.Log;

import com.qihoo360.replugin.RePluginInternal;

import java.io.File;
import java.io.IOException;

/**
 * @author RePlugin Team
 *
 * Art Dex2Oat utils
 */
public class Dex2OatUtils {

    public static final String TAG = "Dex2Oat";

    private static final boolean FOR_DEV = RePluginInternal.FOR_DEV;

    /**
     * 判断当前是否Art模式
     *
     * @return
     */
    public static boolean isArtMode() {
        return System.getProperty("java.vm.version", "").startsWith("2");
    }

    /**
     * 在真正loadDex之前 inject
     *
     * @param dexPath
     * @param optimizedDirectory
     * @param optimizedFileName
     * @return
     */
    public static void injectLoadDex(String dexPath, String optimizedDirectory, String optimizedFileName) {
        if (Dex2OatUtils.isArtMode()) {
            File odexFile = new File(optimizedDirectory, optimizedFileName);

            if (!odexFile.exists() || odexFile.length() <= 0) {

                if (FOR_DEV) {
                    Log.d(Dex2OatUtils.TAG, optimizedFileName + " 文件不存在");
                }

                long being = System.currentTimeMillis();
                boolean injectLoadDex = innerInjectLoadDex(dexPath, optimizedDirectory, optimizedFileName);

                if (FOR_DEV) {
                    Log.d(Dex2OatUtils.TAG, "injectLoadDex use:" + (System.currentTimeMillis() - being));
                    Log.d(Dex2OatUtils.TAG, "injectLoadDex result:" + injectLoadDex);
                }
            } else {
                if (FOR_DEV) {
                    Log.d(Dex2OatUtils.TAG, optimizedFileName + " 文件存在, 不需要inject，size:" + odexFile.length());
                }
            }
        }
    }

    private static boolean innerInjectLoadDex(String dexPath, String optimizedDirectory, String optimizedFileName) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            if (FOR_DEV) {
                Log.d(TAG, "before Android L, do nothing.");
            }
            return false;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1) {
            return injectLoadDex4Art(dexPath, optimizedDirectory, optimizedFileName);
        } else {
            return injectLoadDex4More();
        }
    }

    private static boolean injectLoadDexBeforeN() {
        if (isArtMode()) {

            long begin = System.currentTimeMillis();

            if (FOR_DEV) {
                Log.d(TAG, "Art before Android N, try 2 hook.");
            }

            try {
//                ArtAdapter.setDex2oatEnabledNative(true);
            } catch (Throwable e) {
                if (FOR_DEV) {
                    e.printStackTrace();
                    Log.e(TAG, "hook error");
                }
            }

            if (FOR_DEV) {
                Log.d(TAG, "hook end, use：" + (System.currentTimeMillis() - begin));
            }

            return true;
        }

        if (FOR_DEV) {
            Log.d(TAG, "not Art, do nothing.");
        }
        return false;
    }

    private static boolean injectLoadDex4Art(String dexPath, String optimizedDirectory, String optimizedFileName) {
        if (FOR_DEV) {
            Log.d(TAG, "Andorid Art, try 2 interpretDex2Oat, interpret-only.");
        }

        String odexAbsolutePath = (optimizedDirectory + File.separator + optimizedFileName);

        long begin = System.currentTimeMillis();
        try {
            InterpretDex2OatHelper.interpretDex2Oat(dexPath, odexAbsolutePath);
        } catch (IOException e) {
            if (FOR_DEV) {
                e.printStackTrace();
                Log.e(TAG, "interpretDex2Oat Error");
            }
            return false;
        }

        if (FOR_DEV) {
            Log.d(TAG, "interpretDex2Oat use:" + (System.currentTimeMillis() - begin));
            Log.d(TAG, "interpretDex2Oat odexSize:" + InterpretDex2OatHelper.getOdexSize(odexAbsolutePath));
        }

        return true;
    }

    private static boolean injectLoadDex4More() {
        return false;
    }
}