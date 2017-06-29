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
 *
 */

package com.qihoo360.replugin.gradle.plugin.util

import com.qihoo360.replugin.gradle.plugin.AppConstant

/**
 * @author RePlugin Team
 */
class CmdUtil {

    /**
     * 同步阻塞执行命令
     * @param cmd 命令
     * @return 命令执行完毕返回码
     */
    public static int syncExecute(String cmd){

        int cmdReturnCode

        try {
            println "${AppConstant.TAG} \$ ${cmd}"

            Process process = cmd.execute()
            process.inputStream.eachLine {
                println "${AppConstant.TAG} - ${it}"
            }
            process.waitFor()

            cmdReturnCode = process.exitValue()

        }catch (Exception e){
            System.err.println "${AppConstant.TAG} the cmd run error !!!"
            System.err.println "${AppConstant.TAG} ${e}"
            return -1
        }

        return cmdReturnCode
    }

    private CmdUtil() {}
}
