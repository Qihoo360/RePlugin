/*
 * Copyright (c) 2016, Liu Dong
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.qihoo360.replugin.ext.parser;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


/**
 * ApkParser and result holder.
 * This class is not thread-safe.
 *
 * @author dongliu
 */
public class ApkParser extends AbstractApkParser implements Closeable {

    private final ZipFile zf;

    public ApkParser(File apkFile) throws IOException {
        // create zip file cost time, use one zip file for apk parser life cycle
        this.zf = new ZipFile(apkFile);
    }

    public ApkParser(String filePath) throws IOException {
        this(new File(filePath));
    }

    @Override
    public byte[] getFileData(String path) throws IOException {
        ZipEntry entry = zf.getEntry(path);
        if (entry == null) {
            return null;
        }

        InputStream inputStream = zf.getInputStream(entry);
        return toByteArray(inputStream);
    }

    @Override
    public void close() throws IOException {
        super.close();
        zf.close();
    }

    private static byte[] toByteArray(InputStream in) throws IOException {
        try {
            byte[] buf = new byte[1024 * 8];
            ByteArrayOutputStream bos = null;
            try {
                bos = new ByteArrayOutputStream();
                int len;
                while ((len = in.read(buf)) != -1) {
                    bos.write(buf, 0, len);
                }
                return bos.toByteArray();
            } finally {
                if (bos != null) {
                    bos.close();
                }
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }
}
