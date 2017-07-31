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

/**
 * 在byte[] 和 int long 之间进行类型转换， 打印byte[]到string 里面
 *
 * @author RePlugin Team
 */
public class ByteConvertor {
    public static String toHex(byte[] buf) {
        if (buf == null) {
            return "";
        }
        StringBuilder result = new StringBuilder(2 * buf.length);
        for (byte element : buf) {
            appendHex(result, element);
        }
        return result.toString();
    }

    private static final  String HEX = "0123456789ABCDEF";

    private static void appendHex(StringBuilder sb, byte b) {
        // bigEnd sb.append(HEX.charAt(b & 0x0f)).append(HEX.charAt((b >> 4) &
        // 0x0f));
        sb.append(HEX.charAt((b >> 4) & 0x0f)).append(HEX.charAt(b & 0x0f));
    }

    public static int toInt(byte[] byteArray4) {
        int intValue = 0;
        intValue |= byteArray4[3] & 0xff;
        intValue <<= 8;
        intValue |= byteArray4[2] & 0xff;
        intValue <<= 8;
        intValue |= byteArray4[1] & 0xff;
        intValue <<= 8;
        intValue |= byteArray4[0] & 0xff;
        return intValue;
    }

    public static long toLong(byte[] byteArray8) {
        long longValue = 0;
        longValue |= byteArray8[7] & 0xff;
        longValue <<= 8;
        longValue |= byteArray8[6] & 0xff;
        longValue <<= 8;
        longValue |= byteArray8[5] & 0xff;
        longValue <<= 8;
        longValue |= byteArray8[4] & 0xff;
        longValue <<= 8;
        longValue |= byteArray8[3] & 0xff;
        longValue <<= 8;
        longValue |= byteArray8[2] & 0xff;
        longValue <<= 8;
        longValue |= byteArray8[1] & 0xff;
        longValue <<= 8;
        longValue |= byteArray8[0] & 0xff;
        return longValue;
    }

    public static byte[] toBytes(int intValue) {
        byte[] byteValue = new byte[4];
        byteValue[0] = (byte) (intValue & 0xff);
        byteValue[1] = (byte) ((intValue & 0xff00) >> 8);
        byteValue[2] = (byte) ((intValue & 0xff0000) >> 16);
        byteValue[3] = (byte) ((intValue & 0xff000000) >> 24);
        return byteValue;
    }

    public static byte[] toBytes(long longValue) {
        byte[] byteValue = new byte[8];
        byteValue[0] = (byte) (longValue & 0xffl);
        byteValue[1] = (byte) ((longValue & 0xff00l) >> 8);
        byteValue[2] = (byte) ((longValue & 0xff0000l) >> 16);
        byteValue[3] = (byte) ((longValue & 0xff000000l) >> 24);
        byteValue[4] = (byte) ((longValue & 0xff00000000l) >> 32);
        byteValue[5] = (byte) ((longValue & 0xff0000000000l) >> 40);
        byteValue[6] = (byte) ((longValue & 0xff000000000000l) >> 48);
        byteValue[7] = (byte) ((longValue & 0xff00000000000000l) >> 56);
        return byteValue;
    }

    public static byte[] subBytes(byte[] buf, int from, int len) {
        byte[] subBuf = new byte[len];
        for (int i = 0; i < len; i++) {
            subBuf[i] = buf[from + i];
        }
        return subBuf;
    }

    /**
     * Converts a byte array into a String hexidecimal characters in lower case
     *
     * null returns null
     */
    public static String bytesToHexString(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        String table = "0123456789abcdef";
        StringBuilder ret = new StringBuilder(2 * bytes.length);

        for (byte c : bytes) {
            int b;
            b = 0x0f & (c >> 4);
            ret.append(table.charAt(b));
            b = 0x0f & c;
            ret.append(table.charAt(b));
        }

        return ret.toString();
    }


    /**
     * Converts a hex String to a byte array.
     *
     * @param s
     *            A string of hexadecimal characters, must be an even number of
     *            chars long
     *
     * @return byte array representation
     *
     * @throws RuntimeException
     *             on invalid format
     */
    public static byte[] hexStringToBytes(String s) {
        byte[] ret;

        if (s == null) {
            return null;
        }

        int sz = s.length();
        ret = new byte[sz / 2];
        for (int i = 0; i < sz; i += 2) {
            ret[i / 2] = (byte) ((hexCharToInt(s.charAt(i)) << 4) | hexCharToInt(s.charAt(i + 1)));
        }

        return ret;
    }
    public static int hexCharToInt(char c) {
        if (c >= '0' && c <= '9') {
            return (c - '0');
        }
        if (c >= 'A' && c <= 'F') {
            return (c - 'A' + 10);
        }
        if (c >= 'a' && c <= 'f') {
            return (c - 'a' + 10);
        }

        throw new RuntimeException("invalid hex char '" + c + "'");
    }
}
