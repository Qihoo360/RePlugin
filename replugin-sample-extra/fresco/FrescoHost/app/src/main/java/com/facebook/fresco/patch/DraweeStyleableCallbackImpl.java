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

package com.facebook.fresco.patch;

import android.content.Context;

import com.facebook.drawee.generic.DraweeStyleableCallback;

import java.lang.reflect.Field;

/**
 * DraweeStyleableCallbackImpl
 *
 * 自定义属性回调接口
 *
 * @author RePlugin Team
 */
public class DraweeStyleableCallbackImpl implements DraweeStyleableCallback {

    private Context mContext;

    DraweeStyleableCallbackImpl(Context context) {
        this.mContext = context;
    }

    /**
     * 反射得到样式表数组，如：R.styleable.SimpleDraweeView
     *
     * @param context
     * @param name
     * @return
     */
    private static int[] getStyleableArray(Context context, String name) {
        try {
            String className = context.getPackageName() + ".R$styleable";
            Field[] fields = Class.forName(className).getFields();
            for (Field field : fields) {
                if (field.getName().equals(name)) {
                    return (int[]) field.get(null);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 反射得到样式表数组下的具体资源，如：R.styleable.GenericDraweeHierarchy_placeholderImage
     *
     * @param context
     * @param styleableName
     * @param styleableFieldName
     * @return
     */
    private static int getStyleableFieldId(Context context, String styleableName, String styleableFieldName) {
        String className = context.getPackageName() + ".R";
        String type = "styleable";
        String name = styleableName + "_" + styleableFieldName;

        try {
            Class<?> cla = Class.forName(className);
            for (Class<?> childClass : cla.getClasses()) {
                String simpleName = childClass.getSimpleName();
                if (simpleName.equals(type)) {
                    for (Field field : childClass.getFields()) {
                        String fieldName = field.getName();
                        if (fieldName.equals(name)) {
                            return (int) field.get(null);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    @Override
    public int[] getSimpleDraweeView() {
        // 替代 R.styleable.SimpleDraweeView;
        return getStyleableArray(mContext, "SimpleDraweeView");
    }

    @Override
    public int[] getGenericDraweeHierarchy() {
        // 替代 R.styleable.GenericDraweeHierarchy;
        return getStyleableArray(mContext, "GenericDraweeHierarchy");
    }

    @Override
    public int getActualImageScaleType() {
        // 替代 R.styleable.GenericDraweeHierarchy_actualImageScaleType;
        return getStyleableFieldId(mContext, "GenericDraweeHierarchy", "actualImageScaleType");
    }

    @Override
    public int getPlaceholderImage() {
        // 替代：R.styleable.GenericDraweeHierarchy_placeholderImage
        return getStyleableFieldId(mContext, "GenericDraweeHierarchy", "placeholderImage");
    }

    @Override
    public int getPressedStateOverlayImage() {
        // 替代：R.styleable.GenericDraweeHierarchy_pressedStateOverlayImage
        return getStyleableFieldId(mContext, "GenericDraweeHierarchy", "pressedStateOverlayImage");
    }

    @Override
    public int getProgressBarImage() {
        // R.styleable.GenericDraweeHierarchy_progressBarImage;
        return getStyleableFieldId(mContext, "GenericDraweeHierarchy", "progressBarImage");
    }

    @Override
    public int getFadeDuration() {
        //  R.styleable.GenericDraweeHierarchy_fadeDuration;
        return getStyleableFieldId(mContext, "GenericDraweeHierarchy", "fadeDuration");
    }

    @Override
    public int getViewAspectRatio() {
        //  R.styleable.GenericDraweeHierarchy_viewAspectRatio;
        return getStyleableFieldId(mContext, "GenericDraweeHierarchy", "viewAspectRatio");
    }

    @Override
    public int getPlaceholderImageScaleType() {
        //  R.styleable.GenericDraweeHierarchy_placeholderImageScaleType;
        return getStyleableFieldId(mContext, "GenericDraweeHierarchy", "placeholderImageScaleType");
    }

    @Override
    public int getRetryImage() {
        //  R.styleable.GenericDraweeHierarchy_retryImage;
        return getStyleableFieldId(mContext, "GenericDraweeHierarchy", "retryImage");
    }

    @Override
    public int getRetryImageScaleType() {
        //  R.styleable.GenericDraweeHierarchy_retryImageScaleType;
        return getStyleableFieldId(mContext, "GenericDraweeHierarchy", "retryImageScaleType");
    }

    @Override
    public int getFailureImage() {
        //  return R.styleable.GenericDraweeHierarchy_failureImage;
        return getStyleableFieldId(mContext, "GenericDraweeHierarchy", "failureImage");
    }

    @Override
    public int getFailureImageScaleType() {
        //  R.styleable.GenericDraweeHierarchy_failureImageScaleType;
        return getStyleableFieldId(mContext, "GenericDraweeHierarchy", "failureImageScaleType");
    }

    @Override
    public int getProgressBarImageScaleType() {
        //  R.styleable.GenericDraweeHierarchy_progressBarImageScaleType;
        return getStyleableFieldId(mContext, "GenericDraweeHierarchy", "progressBarImageScaleType");
    }

    @Override
    public int getProgressBarAutoRotateInterval() {
        //  R.styleable.GenericDraweeHierarchy_progressBarAutoRotateInterval;
        return getStyleableFieldId(mContext, "GenericDraweeHierarchy", "progressBarAutoRotateInterval");
    }

    @Override
    public int getBackgroundImage() {
        //  R.styleable.GenericDraweeHierarchy_backgroundImage;
        return getStyleableFieldId(mContext, "GenericDraweeHierarchy", "backgroundImage");
    }

    @Override
    public int getOverlayImage() {
        //  R.styleable.GenericDraweeHierarchy_overlayImage;
        return getStyleableFieldId(mContext, "GenericDraweeHierarchy", "overlayImage");
    }

    @Override
    public int getRoundAsCircle() {
        //  R.styleable.GenericDraweeHierarchy_roundAsCircle;
        return getStyleableFieldId(mContext, "GenericDraweeHierarchy", "roundAsCircle");
    }

    @Override
    public int getRoundedCornerRadius() {
        //  R.styleable.GenericDraweeHierarchy_roundedCornerRadius;
        return getStyleableFieldId(mContext, "GenericDraweeHierarchy", "roundedCornerRadius");
    }

    @Override
    public int getRoundTopLeft() {
        //  R.styleable.GenericDraweeHierarchy_roundTopLeft;
        return getStyleableFieldId(mContext, "GenericDraweeHierarchy", "roundTopLeft");
    }

    @Override
    public int getRoundTopRight() {
        //  R.styleable.GenericDraweeHierarchy_roundTopRight;
        return getStyleableFieldId(mContext, "GenericDraweeHierarchy", "roundTopRight");
    }

    @Override
    public int getRoundBottomLeft() {
        //  R.styleable.GenericDraweeHierarchy_roundBottomLeft;
        return getStyleableFieldId(mContext, "GenericDraweeHierarchy", "roundBottomLeft");
    }

    @Override
    public int getRoundBottomRight() {
        //  R.styleable.GenericDraweeHierarchy_roundBottomRight;
        return getStyleableFieldId(mContext, "GenericDraweeHierarchy", "roundBottomRight");
    }

    @Override
    public int getRoundTopStart() {
        //  R.styleable.GenericDraweeHierarchy_roundTopStart;
        return getStyleableFieldId(mContext, "GenericDraweeHierarchy", "roundTopStart");
    }

    @Override
    public int getRoundTopEnd() {
        //  R.styleable.GenericDraweeHierarchy_roundTopEnd;
        return getStyleableFieldId(mContext, "GenericDraweeHierarchy", "roundTopEnd");
    }

    @Override
    public int getRoundBottomStart() {
        //  R.styleable.GenericDraweeHierarchy_roundBottomStart;
        return getStyleableFieldId(mContext, "GenericDraweeHierarchy", "roundBottomStart");
    }

    @Override
    public int getRoundBottomEnd() {
        //  R.styleable.GenericDraweeHierarchy_roundBottomEnd;
        return getStyleableFieldId(mContext, "GenericDraweeHierarchy", "roundBottomEnd");
    }

    @Override
    public int getRoundWithOverlayColor() {
        //  R.styleable.GenericDraweeHierarchy_roundWithOverlayColor;
        return getStyleableFieldId(mContext, "GenericDraweeHierarchy", "roundWithOverlayColor");
    }

    @Override
    public int getRoundingBorderWidth() {
        //  R.styleable.GenericDraweeHierarchy_roundingBorderWidth;
        return getStyleableFieldId(mContext, "GenericDraweeHierarchy", "roundingBorderWidth");
    }

    @Override
    public int getRoundingBorderColor() {
        //  R.styleable.GenericDraweeHierarchy_roundingBorderColor;
        return getStyleableFieldId(mContext, "GenericDraweeHierarchy", "roundingBorderColor");
    }

    @Override
    public int getRoundingBorderPadding() {
        //  R.styleable.GenericDraweeHierarchy_roundingBorderPadding;
        return getStyleableFieldId(mContext, "GenericDraweeHierarchy", "roundingBorderPadding");
    }

    @Override
    public int getActualImageUri() {
        //  R.styleable.SimpleDraweeView_actualImageUri;
        return getStyleableFieldId(mContext, "SimpleDraweeView", "actualImageUri");
    }

    @Override
    public int getActualImageResource() {
        //  R.styleable.SimpleDraweeView_actualImageResource;
        return getStyleableFieldId(mContext, "SimpleDraweeView", "actualImageResource");
    }
}