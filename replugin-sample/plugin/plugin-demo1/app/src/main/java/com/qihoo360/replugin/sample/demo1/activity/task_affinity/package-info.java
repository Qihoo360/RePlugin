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

package com.qihoo360.replugin.sample.demo1.activity.task_affinity;
/**
 * 测试 两个 TaskAffinity
 * MainActivity    : Standard
 * TA1Activity     : SingleTask + ta_1
 * TA2Activity     : SingleTask + ta_1
 * TA3Activity     : SingleTask + ta_2
 * TA4Activity     : SingleTask + ta_2
 *
 *
 * Path                    |  Stack
 * Main  --(start)--> TA1  | TA1
 * ......................  | Main
 *
 * TA1   --(start)--> TA3  | TA3
 * ........................| TA1
 * ........................| Main
 *
 * TA3   --(start)--> TA2  | Ta1 > TA2
 * ........................| TA3
 * ........................| Main
 *
 * TA2   --(start)--> TA4  | TA3 > TA4
 * ........................| Ta1 > TA2
 * ........................| Main
 */
