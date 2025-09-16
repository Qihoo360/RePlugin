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

package com.qihoo360.replugin.gradle.matrix

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

/**
 * Matrix APM Integration Plugin for RePlugin
 * @author RePlugin Team
 */
public class MatrixPlugin implements Plugin<Project> {

    def static TAG = AppConstant.TAG
    def project
    def config

    @Override
    public void apply(Project project) {
        println "${TAG} Welcome to RePlugin Matrix APM integration!"

        this.project = project

        /* Extensions */
        project.extensions.create(AppConstant.USER_CONFIG, MatrixConfig)

        // Apply Matrix configuration after project evaluation
        project.afterEvaluate {
            applyMatrixConfiguration()
            createMatrixTasks()
        }
    }

    /**
     * Apply Matrix configuration to the project
     */
    def applyMatrixConfiguration() {
        config = project.extensions.getByName(AppConstant.USER_CONFIG)
        
        if (!config.enable) {
            println "${TAG} Matrix integration is disabled"
            return
        }

        println "${TAG} Applying Matrix APM configuration..."
        
        // Apply APM configuration
        if (config.apm.enable) {
            println "${TAG} Enabling Matrix APM components..."
            // Note: In a real implementation, we would add Matrix APM dependencies here
            // For now, we just log the configuration
        }
    }

    /**
     * Create Matrix-related tasks
     */
    def createMatrixTasks() {
        // Create Matrix initialization task
        Task initMatrixTask = project.task(AppConstant.TASK_INIT_MATRIX) {
            group = AppConstant.TASKS_GROUP
            description = 'Initialize Matrix APM configuration for RePlugin'
            
            doLast {
                def currentConfig = project.extensions.getByName(AppConstant.USER_CONFIG)
                def apmConfig = currentConfig.apm
                def traceConfig = currentConfig.trace
                
                println "${TAG} Matrix APM initialization completed"
                println "${TAG} Configuration:"
                println "${TAG}   - Matrix enabled: ${currentConfig.enable}"
                println "${TAG}   - APM enabled: ${apmConfig.enable}"
                println "${TAG}   - Trace enabled: ${traceConfig.enable}"
                println "${TAG}   - IO Canary: ${apmConfig.ioCanary}"
                println "${TAG}   - Battery Canary: ${apmConfig.batteryCanary}"
                println "${TAG}   - SQLite Canary: ${apmConfig.sqliteCanary}"
                println "${TAG}   - Memory Canary: ${apmConfig.memoryCanary}"
            }
        }

        // Create Matrix config generation task
        Task generateConfigTask = project.task(AppConstant.TASK_GENERATE_MATRIX_CONFIG) {
            group = AppConstant.TASKS_GROUP
            description = 'Generate Matrix configuration file'
            
            doLast {
                def currentConfig = project.extensions.getByName(AppConstant.USER_CONFIG)
                generateMatrixConfigFile(currentConfig)
            }
        }

        // Make init task depend on config generation
        initMatrixTask.dependsOn generateConfigTask
    }

    /**
     * Generate Matrix configuration file
     */
    def generateMatrixConfigFile(currentConfig) {
        def configFile = new File(project.projectDir, "matrix_config.json")
        
        // Access nested properties properly
        def apmConfig = currentConfig.apm
        def traceConfig = currentConfig.trace
        
        def configContent = """
{
  "matrix": {
    "enable": ${currentConfig.enable},
    "apm": {
      "enable": ${apmConfig.enable},
      "ioCanary": ${apmConfig.ioCanary},
      "batteryCanary": ${apmConfig.batteryCanary},
      "sqliteCanary": ${apmConfig.sqliteCanary},
      "memoryCanary": ${apmConfig.memoryCanary}
    },
    "trace": {
      "enable": ${traceConfig.enable},
      "baseMethodMapFile": "${traceConfig.baseMethodMapFile ?: ''}",
      "blackListFile": "${traceConfig.blackListFile ?: ''}"
    }
  }
}
""".trim()
        
        configFile.text = configContent
        println "${TAG} Generated Matrix configuration file: ${configFile.absolutePath}"
    }
}

/**
 * Matrix Configuration Extension
 */
class MatrixConfig {
    /** Enable Matrix integration */
    def enable = true

    /** APM Configuration */
    def apm = new ApmConfig()

    /** Trace Configuration */
    def trace = new TraceConfig()

    /** Remove unused resources configuration */
    def removeUnusedResources = new RemoveUnusedResourcesConfig()
}

/**
 * APM Configuration
 */
class ApmConfig {
    /** Enable APM monitoring */
    def enable = true
    
    /** IO Canary for file I/O monitoring */
    def ioCanary = true
    
    /** Battery Canary for power consumption monitoring */
    def batteryCanary = true
    
    /** SQLite Canary for database operation monitoring */
    def sqliteCanary = true
    
    /** Memory Canary for memory leak detection */
    def memoryCanary = true
}

/**
 * Trace Configuration
 */
class TraceConfig {
    /** Enable trace monitoring */
    def enable = true
    
    /** Base method map file path */
    def baseMethodMapFile = null
    
    /** Black list file path */
    def blackListFile = null
}

/**
 * Remove Unused Resources Configuration
 */
class RemoveUnusedResourcesConfig {
    /** Enable unused resources removal */
    def enable = false
    
    /** Variant to apply */
    def variant = 'release'
    
    /** Whether to sign the APK */
    def needSign = true
}