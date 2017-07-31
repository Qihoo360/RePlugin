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

package com.qihoo360.replugin.utils.basic;

import com.qihoo360.replugin.utils.CloseableUtils;
import com.qihoo360.replugin.utils.FileUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class SecurityUtil {

    /** 计算给定 byte [] 串的 MD5 */
    public static byte[] MD5(byte[] input) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        if (md != null) {
            md.update(input);
            return md.digest();
        } else {
            return null;
        }
    }

    public static String getMD5(byte[] input) {
        return ByteConvertor.bytesToHexString(MD5(input));
    }

    public static String getMD5(String input) {
        if (input == null) {
            return "";
        }
        return getMD5(input.getBytes());
    }

    /** 计算文件 MD5，返回十六进制串 */
    public static String getFileMD5(String filename) {
        byte[] digest = MD5(filename);
        if (digest == null) {
            return null;
        } else {
            return ByteConvertor.bytesToHexString(digest);
        }
    }

    public static String getMd5ByFile(File file) {
        FileInputStream in = null;
        byte[] digest = null;
        try {
            in = new FileInputStream(file);
            MappedByteBuffer byteBuffer = in.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, file.length());
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(byteBuffer);
            digest = md5.digest();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != in) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (digest == null) {
            return null;
        } else {
            return ByteConvertor.bytesToHexString(digest);
        }
    }

    public static String getMD5(InputStream inputStream) {
        byte[] digest = null;
        BufferedInputStream in = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            in = new BufferedInputStream(inputStream);

            int theByte = 0;
            byte[] buffer = new byte[1024];
            while ((theByte = in.read(buffer)) != -1) {
                md.update(buffer, 0, theByte);
            }
            digest = md.digest();
        } catch (Exception e) {
            //ignore
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    //ignore
                }
            }
        }
        if (digest == null) {
            return null;
        } else {
            return ByteConvertor.bytesToHexString(digest);
        }
    }

    /** 计算文件 MD5，返回 byte []. 如果文件不存在，返回 null. */
    public static byte[] MD5(String filename) {
        return MD5(new File(filename));
    }

    public static byte[] MD5(File file) {
        InputStream in = null;
        try {
            in = FileUtils.openInputStream(file);
            return MD5(in);
        } catch (Exception e) {
            //ignore
        } finally {
            CloseableUtils.closeQuietly(in);
        }

        return null;
    }

    public static final byte[] MD5(InputStream in) throws NoSuchAlgorithmException, IOException {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        byte buffer[] = new byte[4096];
        int rc = 0;
        while ((rc = in.read(buffer)) >= 0) {
            if (rc > 0) {
                digest.update(buffer, 0, rc);
            }
        }
        return digest.digest();
    }
}
