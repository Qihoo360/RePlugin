
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

package com.qihoo360.replugin.utils.pkg;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.qihoo360.loader2.Constant;
import com.qihoo360.mobilesafe.core.BuildConfig;
import com.qihoo360.replugin.RePluginInternal;
import com.qihoo360.replugin.helper.LogRelease;
import com.qihoo360.replugin.model.PluginInfo;
import com.qihoo360.replugin.utils.FileUtils;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author RePlugin Team
 */
public class PackageFilesUtil {
    private static final String TAG = "PackageFilesUtil";

    private static final String TIMESTAMP_EXT = ".timestamp";

    /**
     * 我们的很多文件，都是在 assets 目录里有一份，如果有更新，则在 files 目录里也有一份。原来的做法是把 assets 目录里的 copy
     * 到 files 目录，其实没有必要，这里用时间戳判断一下，哪个最新就直接读哪个
     */
    public static InputStream openLatestInputFile(Context c, String filename) {
        InputStream is = null;

        long timestampOfFile = getFileTimestamp(c, filename);
        long timestampOfAsset = getBundleTimestamp(c, filename);

        if (timestampOfFile >= timestampOfAsset) {
            // files 目录的时间戳更新，那么优先读取 files 目录的文件

            try {
                is = c.openFileInput(filename);

                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "Opening in files directory: " + filename);
                }
            } catch (Exception e) {
                if (BuildConfig.DEBUG) {
                    Log.i(TAG, filename + " in files directory not found, skip.");
                }
            }
        }

        if (is == null) {
            // is == null 表明没能从 files 目录读到文件，那么到 assets 目录去读读看

            try {
                is = c.getAssets().open(filename);
                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "Opening in assets: " + filename);
                }
            } catch (FileNotFoundException e) {
                // 找不到文件？很正常，不做任何处理
                // Ignore
            } catch (IOException e) {
                if (BuildConfig.DEBUG) {
                    Log.w(TAG, filename, e);
                }
            }
        }

        return is;
    }

    /** @see Utils#openLatestInputFile(Context, String) */
    public static long getLatestFileTimestamp(Context c, String filename) {
        long timestampOfFile = getFileTimestamp(c, filename);
        long timestampOfAsset = getBundleTimestamp(c, filename);

        return Math.max(timestampOfFile, timestampOfAsset);
    }

    /**
     * 检查 files 目录下个某个文件是否已经为最新（比 assets 目录下的新）
     *
     * @param c
     * @param filename
     * @return 如果文件比 assets 下的同名文件的时间戳旧，或者文件不存在，则返回 false.
     */
    public static boolean isFileUpdated(Context c, String filename) {
        File file = c.getFileStreamPath(filename);
        if (file == null) {
            return false;
        }
        if (!file.exists()) {
            return false;
        }

        long timestampOfFile = getFileTimestamp(c, filename);
        long timestampOfAsset = getBundleTimestamp(c, filename);

        return (timestampOfAsset <= timestampOfFile);
    }

    /** 读取文件的时间戳 */
    public static long getFileTimestamp(Context c, String filename) {
        FileInputStream fis = null;
        try {
            fis = c.openFileInput(filename + TIMESTAMP_EXT);
        } catch (Exception e) {
            //ignore
        }

        if (fis != null) {
            return getTimestampFromStream(fis);
        } else {
            return 0;
        }
    }

    // 对于打包的文件，都是放在 assets 目录的，时间戳自然也在 assets 目录
    public static long getBundleTimestamp(Context c, String filename) {
        InputStream fis = null;
        try {
            fis = c.getAssets().open(filename + TIMESTAMP_EXT);
        } catch (Exception e) {
            //ignore
        }

        if (fis != null) {
            return getTimestampFromStream(fis);
        } else {
            return 0;
        }
    }

    private static long getTimestampFromStream(InputStream fis) {

        DataInputStream dis = null;
        try {
            dis = new DataInputStream(fis);
            String s = dis.readLine();
            if (!TextUtils.isEmpty(s)) {
                long timeStamp = Long.parseLong(s);
                return timeStamp;
            }
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "", e);
            }
        } finally {
            try {
                if (dis != null) {
                    dis.close();
                }
                if (fis != null) {
                    fis.close();
                }
            } catch (Exception e) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, "", e);
                }
            }
        }

        return 0;
    }

    public static boolean isExtractedFromAssetsToFiles(Context c, String filename) {
        File file = c.getFileStreamPath(filename);
        if (file == null || !file.exists()) {
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "Extract no exist file from assets filename = " + filename);
            }
            return true;
        }
        // compare file version for extract
        return compareDataFileVersion(c, filename);
    }

    private static boolean compareDataFileVersion(Context c, String fileName) {
        int assetsVer = -1;
        int fileVer = -1;
        DataInputStream dis = null;
        byte[] magic = new byte[4];
        try {
            dis = new DataInputStream(c.getAssets().open(fileName));
            dis.read(magic);
            if (magic[0] == 'V' && magic[1] == 'D' && magic[2] == 'A' && magic[3] == 'T') {
                dis.readInt();
                dis.readInt();
                assetsVer = dis.readInt();
                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "Get assets version file=" + fileName + " version=" + assetsVer);
                }
            } else {
                return true;
            }
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "Get assets version error, file:" + fileName, e);
            }
        } finally {
            if (dis != null) {
                try {
                    dis.close();
                } catch (Exception e) {
                    if (BuildConfig.DEBUG) {
                        Log.e(TAG, "close error", e);
                    }
                }
            }
        }
        try {
            dis = new DataInputStream(new FileInputStream(c.getFileStreamPath(fileName)));
            dis.read(magic);
            if (magic[0] == 'V' && magic[1] == 'D' && magic[2] == 'A' && magic[3] == 'T') {
                dis.readInt();
                dis.readInt();
                fileVer = dis.readInt();
                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "Get local version file=" + fileName + " version=" + fileVer);
                }
            } else {
                return true;
            }
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "Get file version error, file:" + fileName, e);
            }
        } finally {
            if (dis != null) {
                try {
                    dis.close();
                } catch (Exception e) {
                    if (BuildConfig.DEBUG) {
                        Log.e(TAG, "close error", e);
                    }
                }
            }
        }
        if (assetsVer != -1 && fileVer != -1 && assetsVer <= fileVer) {
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "compare file version not extract");
            }
            return false;
        } else {
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "compare file version need extract");
            }
        }
        return true;
    }

    /**
     * 移除插件及其已释放的Dex、Native库等文件
     * <p>
     *
     * @param info 插件信息
     */
    public static void forceDelete(PluginInfo info) {
        if (info == null) {
            return;
        }

        try {
            // 删除插件APK
            final File apkFile = info.getApkFile();
            if (apkFile.exists()) {
                FileUtils.forceDelete(apkFile);
                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "delete " + info.getApkFile());
                }
            }

            // 删除释放后的odex
            final File dexFile = info.getDexFile();
            if (dexFile.exists()) {
                FileUtils.forceDelete(dexFile);
                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "delete " + info.getDexFile());
                }
            }

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {

                // 删除释放后的vdex
                File dexParent = info.getDexParentDir();
                String fileNameWithoutExt = FileUtils.getFileNameWithoutExt(info.getDexFile().getAbsolutePath());

                File vdexFile = new File(dexParent, fileNameWithoutExt + ".vdex");
                FileUtils.forceDelete(vdexFile);
                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "delete " + vdexFile);
                }

                // 删除 XXX.jar.prof 文件
                File profFile = new File(info.getApkFile().getAbsolutePath() + ".prof");
                FileUtils.forceDelete(profFile);
                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "delete " + profFile);
                }
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                FileUtils.forceDelete(info.getExtraOdexDir());
                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "delete " + info.getExtraOdexDir());
                }
            }

            // 删除Native文件
            final File libsFile = info.getNativeLibsDir();
            if (libsFile.exists()) {
                FileUtils.forceDelete(info.getNativeLibsDir());
                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "delete " + info.getNativeLibsDir());
                }
            }

            // 删除进程锁文件
            String lockFileName = String.format(Constant.LOAD_PLUGIN_LOCK, info.getApkFile().getName());
            File lockFile = new File(RePluginInternal.getAppContext().getFilesDir(), lockFileName);
            FileUtils.forceDelete(lockFile);
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "delete " + lockFile);
            }
        } catch (IOException e) {
            if (LogRelease.LOGR) {
                e.printStackTrace();
            }
        } catch (IllegalArgumentException e2) {
            if (LogRelease.LOGR) {
                e2.printStackTrace();
            }
        }
    }
}