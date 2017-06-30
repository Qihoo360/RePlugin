package com.qihoo360.loader2.sp;

import android.os.Bundle;

/**
 * @author RePlugin Team
 */
interface IPref {

    String get(String category, String key, String defValue);

    void set(String category, String key, String value);

    Bundle getAll(String category);
}
