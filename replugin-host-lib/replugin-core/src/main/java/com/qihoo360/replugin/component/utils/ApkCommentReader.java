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

package com.qihoo360.replugin.component.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;

/**
 * ApkCommentReader
 * <p>
 * 读取 apk 文件的 comment。
 * apk 的 comment 段为【内容(content)+内容长度(LITTLE_ENDIAN两字节)+魔法标记(MAGIC)】，
 *
 * @author RePlugin Team
 */
public class ApkCommentReader {

    /**
     * 魔法标记
     */
    private static final byte[] MAGIC = new byte[]{0x28, 0x4d, 0x53, 0x2d, 0x50, 0x4c, 0x47, 0x29}; // (MS-PLG)

    /**
     * 读取 apk 的注释
     *
     * @param path apk 文件路径
     * @return comment 内容
     */
    public static String readComment(String path) {
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(path, "r");
            return decompress(getComment(raf));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (raf != null) {
                try {
                    raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return "";
    }

    /**
     * 获取解压前的注释内容
     */
    private static byte[] getComment(RandomAccessFile raf) {
        if (null == raf) {
            return null;
        }

        try {
            /* 判断文件结尾是否具有魔法标记，没有就直接返回 */
            long index = raf.length();
            index -= MAGIC.length;
            raf.seek(index);
            byte[] magicBuffer = new byte[MAGIC.length];
            raf.readFully(magicBuffer);
            if (!Arrays.equals(magicBuffer, MAGIC)) {
                return null;
            }

            /* 读取内容, 直接滑动文件索引到读取“内容长度”位置，长度和 zip 定义一致，为2字节 */
            index -= 2;
            raf.seek(index);
            byte[] contentLen = new byte[2];
            raf.readFully(contentLen);
            // 2byte 转无符号 short
            int length = (contentLen[1] << 8 & 0xFF00) | (contentLen[0] & 0xFF);
            if (length > 0) {
                index -= length;
                raf.seek(index);
                byte[] bytes = new byte[length];
                raf.readFully(bytes);
                return bytes;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 对读取到了字节数据进行是解压，得到最终注释内容
     */
    private static String decompress(byte[] str) {
        if (str == null) {
            return "";
        }

        ByteArrayOutputStream out = null;
        try {
            out = new ByteArrayOutputStream();
            ByteArrayInputStream bis = new ByteArrayInputStream(str);
            GZIPInputStream gzip = new GZIPInputStream(bis);
            byte[] buffer = new byte[256];
            int n;
            while ((n = gzip.read(buffer)) >= 0) {
                out.write(buffer, 0, n);
            }
            return out.toString("utf-8");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return "";
    }
}
