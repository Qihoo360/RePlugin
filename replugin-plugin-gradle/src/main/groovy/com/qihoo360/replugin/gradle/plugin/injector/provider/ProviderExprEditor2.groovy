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

package com.qihoo360.replugin.gradle.plugin.injector.provider

import javassist.CannotCompileException
import javassist.expr.ExprEditor
import javassist.expr.MethodCall

/**
 * @author RePlugin Team
 */
public class ProviderExprEditor2 extends ExprEditor {

    static def PROVIDER_CLASS = 'com.qihoo360.loader2.mgr.PluginProviderClient2'

    public def filePath

    @Override
    void edit(MethodCall m) throws CannotCompileException {
        String clsName = m.getClassName()
        String methodName = m.getMethodName()

        if (clsName.equalsIgnoreCase('android.content.ContentProviderClient')) {
            println " ${filePath} ContentProviderClient.${methodName}():${m.lineNumber}"
            if (!(methodName in ProviderInjector2.includeMethodCall)) {
                // println "跳过$methodName"
                return
            }
            replaceStatement(m, methodName, m.lineNumber)
        }
    }

    def private replaceStatement(MethodCall methodCall, String method, def line) {
        methodCall.replace('{$_ = ' + PROVIDER_CLASS + '.' + method + '(com.qihoo360.replugin.RePlugin.getPluginContext(), $$);}')
        println ">>> Replace: ${filePath} Provider.${method}():${line}"
    }
}
