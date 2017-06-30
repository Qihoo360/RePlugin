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

/**
 * 保存自定义进程中，每个进程里的坑位信息
 *
 * @author RePlugin Team
 */

class ProcessStates {

    /**
     * 保存非默认 TaskAffinity 下，坑位的状态信息。
     */
    TaskAffinityStates mTaskAffinityStates = new TaskAffinityStates();

    /**
     * 保存默认 TaskAffinity 下，坑位的状态信息。
     */
    LaunchModeStates mLaunchModeStates = new LaunchModeStates();
}
