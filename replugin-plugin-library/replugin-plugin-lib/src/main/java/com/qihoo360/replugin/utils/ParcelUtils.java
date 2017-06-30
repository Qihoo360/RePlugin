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

package com.qihoo360.replugin.utils;

import android.os.Parcel;
import android.os.Parcelable;

import java.lang.reflect.Field;

/**
 * 和解析Parcel有关的工具类
 *
 * @author RePlugin Team
 */
public class ParcelUtils {

    /**
     * 根据Parcelable对象，构造出适合在任何地方使用的Parcel对象
     * 该方法可杜绝ClassCastException异常
     * <p>
     * 注意：
     * 用完必须调用p.recycle方法
     * <p>
     * Before:
     * <pre class="prettyprint">\
     * // ERROR: Might be "ClassCastException" Error
     * XXX x = intent.getParcelableExtra("XXX");
     * </pre>
     * <p>
     * After:
     * <pre class="prettyprint">
     * Parcelable pa = intent.getParcelableExtra("XXX");
     * Parcel p = ParcelUtils.createFromParcelable(pa);
     * <p>
     * // Create a new XXX object to avoid "ClassCastException"
     * XXX x = new XXX(p);
     * </pre>
     * <p>
     * 原因：
     * 即使包名、类名完全相同，若ClassLoader对象不同，则仍会抛出类型转换异常
     * 因此需要将其“重新”生成一份，等于用不同的ClassLoader生成两个对象，自然避免该问题
     * <p>
     * 常见于BroadcastReceiver中的Bundle，系统在判断源进程和目标进程一致时，会“透传”Bundle过来，
     * 故就算设置了setClassLoader，也不会做unparcel，自然也就会导致ClassCastException了
     *
     * @param pa 要构造的Parcelable对象
     * @return 可被构造函数使用的Parcel对象
     */
    public static Parcel createFromParcelable(Parcelable pa) {
        if (pa == null) {
            return null;
        }

        Parcel p = Parcel.obtain();
        pa.writeToParcel(p, 0);
        p.setDataPosition(0);

        return p;
    }

    /**
     * 调用另一个ClassLoader中的实现了Parcelable接口类的Parcelable$Creator成员
     * 进行远程Object的创建
     *
     * @param pa
     * @param loader
     * @return
     */
    public static Object createFromParcelable(Parcelable pa, ClassLoader loader, String cln) {
        try {
            Field f = loader.loadClass(cln).getField("CREATOR");
            // 以防万一
            f.setAccessible(true);

            Parcelable.Creator creator = (Parcelable.Creator) f.get(null);
            return creator.createFromParcel(createFromParcelable(pa));

        } catch (ClassNotFoundException e) {

        } catch (NoSuchFieldException e) {

        } catch (IllegalAccessException e) {

        }

        return null;
    }
}
