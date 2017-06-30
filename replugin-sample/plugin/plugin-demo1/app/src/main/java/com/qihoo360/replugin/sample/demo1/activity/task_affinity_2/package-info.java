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

package com.qihoo360.replugin.sample.demo1.activity.task_affinity_2;
/**
 * 测试 两个 TaskAffinity
 * MainActivity    : Standard
 * TA1Activity     : SingleTask + ta_3
 * TA2Activity     : SingleTask + ta_3
 * TA3Activity     : Standard + ta_3
 * TA4Activity     : Standard + ta_3
 *
 *
 * Path                    |  Stack
 * Main  --(start)--> TA1  | TA1
 * ......................  | Main
 *
 * TA1   --(start)--> TA2  | TA1 > TA2
 * ........................| Main
 *
 * TA2   --(start)--> TA3  | Ta1 > TA2 > T3
 * ........................| Main
 *
 * TA3   --(start)--> TA4  | Ta1 > TA2 > T3 > T4
 * ........................| Main
 *
 * TA4   --(start)--> TA3  | Ta1 > TA2 > T3 > T4 > T3
 * ........................| Main
 */
