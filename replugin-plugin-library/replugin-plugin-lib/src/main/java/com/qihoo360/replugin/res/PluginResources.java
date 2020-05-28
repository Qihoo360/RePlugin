package com.qihoo360.replugin.res;

import android.content.res.AssetFileDescriptor;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.Movie;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.TypedValue;

import com.qihoo360.replugin.RePlugin;

import java.io.InputStream;

public class PluginResources extends ResourcesWrapper {
    private Resources mHostResources;

    public PluginResources(Resources resources) {
        super(resources);
        mHostResources = RePlugin.getHostContext().getResources();
    }

    @Override
    public CharSequence getText(int id) throws NotFoundException {
        if (isSystemId(id)) {
            return mHostResources.getText(id);
        }
        try {
            return super.getText(id);
        } catch (NotFoundException e) {
            e.printStackTrace();
            return mHostResources.getText(id);
        }
    }

    @Override
    public CharSequence getQuantityText(int id, int quantity) throws NotFoundException {
        if (isSystemId(id)) {
            return mHostResources.getQuantityText(id, quantity);
        }
        try {
            return super.getQuantityText(id, quantity);
        } catch (NotFoundException e) {
            e.printStackTrace();
            return mHostResources.getQuantityText(id, quantity);
        }
    }

    @Override
    public String getString(int id) throws NotFoundException {
        if (isSystemId(id)) {
            return mHostResources.getString(id);
        }
        try {
            return super.getString(id);
        } catch (NotFoundException e) {
            e.printStackTrace();
            return mHostResources.getString(id);
        }
    }

    @Override
    public String getString(int id, Object... formatArgs) throws NotFoundException {
        if (isSystemId(id)) {
            return mHostResources.getString(id, formatArgs);
        }
        try {
            return super.getString(id, formatArgs);
        } catch (NotFoundException e) {
            e.printStackTrace();
            return mHostResources.getString(id, formatArgs);
        }
    }

    @Override
    public String getQuantityString(int id, int quantity, Object... formatArgs)
            throws NotFoundException {
        if (isSystemId(id)) {
            return mHostResources.getQuantityString(id, quantity, formatArgs);

        }
        try {
            return super.getQuantityString(id, quantity, formatArgs);
        } catch (NotFoundException e) {
            e.printStackTrace();
            return mHostResources.getQuantityString(id, quantity, formatArgs);
        }
    }

    @Override
    public String getQuantityString(int id, int quantity) throws NotFoundException {
        if (isSystemId(id)) {
            return mHostResources.getQuantityString(id, quantity);
        }
        try {
            return super.getQuantityString(id, quantity);
        } catch (NotFoundException e) {
            e.printStackTrace();
            return mHostResources.getQuantityString(id, quantity);
        }
    }

    @Override
    public CharSequence getText(int id, CharSequence def) {
        if (isSystemId(id)) {
            return mHostResources.getText(id, def);
        }
        try {
            return super.getText(id, def);
        } catch (Exception e) {
            e.printStackTrace();
            return mHostResources.getText(id, def);
        }
    }

    @Override
    public CharSequence[] getTextArray(int id) throws NotFoundException {
        if (isSystemId(id)) {
            return mHostResources.getTextArray(id);
        }
        try {
            return super.getTextArray(id);
        } catch (NotFoundException e) {
            e.printStackTrace();
            return mHostResources.getTextArray(id);
        }
    }

    @Override
    public String[] getStringArray(int id) throws NotFoundException {
        if (isSystemId(id)) {
            return mHostResources.getStringArray(id);
        }
        try {
            return super.getStringArray(id);
        } catch (NotFoundException e) {
            e.printStackTrace();
            return mHostResources.getStringArray(id);
        }
    }

    @Override
    public int[] getIntArray(int id) throws NotFoundException {
        if (isSystemId(id)) {
            return mHostResources.getIntArray(id);
        }
        try {
            return super.getIntArray(id);
        } catch (NotFoundException e) {
            e.printStackTrace();
            return mHostResources.getIntArray(id);
        }
    }

    @Override
    public TypedArray obtainTypedArray(int id) throws NotFoundException {
        if (isSystemId(id)) {
            return mHostResources.obtainTypedArray(id);
        }
        try {
            return super.obtainTypedArray(id);
        } catch (NotFoundException e) {
            e.printStackTrace();
            return mHostResources.obtainTypedArray(id);
        }
    }

    @Override
    public float getDimension(int id) throws NotFoundException {
        if (isSystemId(id)) {
            return mHostResources.getDimension(id);
        }
        try {
            return super.getDimension(id);
        } catch (NotFoundException e) {
            e.printStackTrace();
            return mHostResources.getDimension(id);
        }
    }

    @Override
    public int getDimensionPixelOffset(int id) throws NotFoundException {
        if (isSystemId(id)) {
            return mHostResources.getDimensionPixelOffset(id);
        }
        try {
            return super.getDimensionPixelOffset(id);
        } catch (NotFoundException e) {
            e.printStackTrace();
            return mHostResources.getDimensionPixelOffset(id);
        }
    }

    @Override
    public int getDimensionPixelSize(int id) throws NotFoundException {
        if (isSystemId(id)) {
            return mHostResources.getDimensionPixelSize(id);

        }
        return super.getDimensionPixelSize(id);
    }

    @Override
    public float getFraction(int id, int base, int pbase) {
        if (isSystemId(id)) {
            return mHostResources.getFraction(id, base, pbase);
        }
        try {
            return super.getFraction(id, base, pbase);
        } catch (Exception e) {
            e.printStackTrace();
            return mHostResources.getFraction(id, base, pbase);
        }
    }

    @Override
    public Drawable getDrawable(int id) throws NotFoundException {
        if (isSystemId(id)) {
            return mHostResources.getDrawable(id);
        }
        try {
            return super.getDrawable(id);
        } catch (NotFoundException e) {
            e.printStackTrace();
            return mHostResources.getDrawable(id);
        }
    }

    @Override
    public Drawable getDrawable(int id, Theme theme) throws NotFoundException {
        if (isSystemId(id)) {
            return getHostDrawable(id, theme);
        }
        try {
            return super.getDrawable(id, theme);
        } catch (NotFoundException e) {
            e.printStackTrace();
            return getHostDrawable(id, theme);
        }
    }

    private Drawable getHostDrawable(int id, Theme theme) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return mHostResources.getDrawable(id, theme);
        } else {
            return mHostResources.getDrawable(id);
        }
    }

    @Override
    public Drawable getDrawableForDensity(int id, int density) throws NotFoundException {
        if (isSystemId(id)) {
            return mHostResources.getDrawableForDensity(id, density);
        }
        try {
            return super.getDrawableForDensity(id, density);
        } catch (NotFoundException e) {
            e.printStackTrace();
            return mHostResources.getDrawableForDensity(id, density);
        }
    }

    @Override
    public Drawable getDrawableForDensity(int id, int density, Theme theme) {
        if (isSystemId(id)) {
            return mHostResources.getDrawableForDensity(id, density);
        }
        try {
            return super.getDrawableForDensity(id, density);
        } catch (NotFoundException e) {
            e.printStackTrace();
            return mHostResources.getDrawableForDensity(id, density);
        }
    }

    @Override
    public Movie getMovie(int id) throws NotFoundException {
        if (isSystemId(id)) {
            return mHostResources.getMovie(id);
        }
        try {
            return super.getMovie(id);
        } catch (NotFoundException e) {
            e.printStackTrace();
            return mHostResources.getMovie(id);
        }
    }

    @Override
    public int getColor(int id) throws NotFoundException {
        if (isSystemId(id)) {
            return mHostResources.getColor(id);
        }
        try {
            return super.getColor(id);
        } catch (NotFoundException e) {
            e.printStackTrace();
            return mHostResources.getColor(id);
        }
    }

    @Override
    public ColorStateList getColorStateList(int id) throws NotFoundException {
        if (isSystemId(id)) {
            return mHostResources.getColorStateList(id);
        }
        try {
            return super.getColorStateList(id);
        } catch (NotFoundException e) {
            e.printStackTrace();
            return mHostResources.getColorStateList(id);
        }
    }

    @Override
    public boolean getBoolean(int id) throws NotFoundException {
        if (isSystemId(id)) {
            return mHostResources.getBoolean(id);
        }
        try {
            return super.getBoolean(id);
        } catch (NotFoundException e) {
            e.printStackTrace();
            return mHostResources.getBoolean(id);
        }
    }

    @Override
    public int getInteger(int id) throws NotFoundException {
        if (isSystemId(id)) {
            return mHostResources.getInteger(id);
        }
        try {
            return super.getInteger(id);
        } catch (NotFoundException e) {
            e.printStackTrace();
            return mHostResources.getInteger(id);
        }
    }

    @Override
    public XmlResourceParser getLayout(int id) throws NotFoundException {
        if (isSystemId(id)) {
            return mHostResources.getLayout(id);
        }
        try {
            return super.getLayout(id);
        } catch (NotFoundException e) {
            e.printStackTrace();
            return mHostResources.getLayout(id);
        }
    }

    @Override
    public XmlResourceParser getAnimation(int id) throws NotFoundException {
        if (isSystemId(id)) {
            return mHostResources.getAnimation(id);
        }
        try {
            return super.getAnimation(id);
        } catch (NotFoundException e) {
            e.printStackTrace();
            return mHostResources.getAnimation(id);
        }
    }

    @Override
    public XmlResourceParser getXml(int id) throws NotFoundException {
        if (isSystemId(id)) {
            return mHostResources.getXml(id);
        }
        try {
            return super.getXml(id);
        } catch (NotFoundException e) {
            e.printStackTrace();
            return mHostResources.getXml(id);
        }
    }

    @Override
    public InputStream openRawResource(int id) throws NotFoundException {
        if (isSystemId(id)) {
            return mHostResources.openRawResource(id);
        }
        try {
            return super.openRawResource(id);
        } catch (NotFoundException e) {
            e.printStackTrace();
            return mHostResources.openRawResource(id);
        }
    }

    @Override
    public InputStream openRawResource(int id, TypedValue value) throws NotFoundException {
        if (isSystemId(id)) {
            return mHostResources.openRawResource(id, value);
        }
        try {
            return super.openRawResource(id, value);
        } catch (NotFoundException e) {
            e.printStackTrace();
            return mHostResources.openRawResource(id, value);
        }
    }

    @Override
    public AssetFileDescriptor openRawResourceFd(int id) throws NotFoundException {
        if (isSystemId(id)) {
            return mHostResources.openRawResourceFd(id);
        }
        try {
            return super.openRawResourceFd(id);
        } catch (NotFoundException e) {
            e.printStackTrace();
            return mHostResources.openRawResourceFd(id);

        }
    }

    @Override
    public void getValue(int id, TypedValue outValue, boolean resolveRefs)
            throws NotFoundException {

        if (isSystemId(id)) {
            mHostResources.getValue(id, outValue, resolveRefs);
        } else {
            try {
                super.getValue(id, outValue, resolveRefs);
            } catch (NotFoundException e) {
                e.printStackTrace();
                mHostResources.getValue(id, outValue, resolveRefs);
            }
        }
    }

    @Override
    public void getValueForDensity(int id, int density, TypedValue outValue, boolean resolveRefs)
            throws NotFoundException {
        if (isSystemId(id)) {
            mHostResources.getValueForDensity(id, density, outValue, resolveRefs);
        } else {
            try {
                super.getValueForDensity(id, density, outValue, resolveRefs);
            } catch (NotFoundException e) {
                e.printStackTrace();
                mHostResources.getValueForDensity(id, density, outValue, resolveRefs);

            }
        }
    }

    @Override
    public String getResourceName(int resid) throws NotFoundException {
        if (isSystemId(resid)) {
            return mHostResources.getResourceName(resid);
        }
        try {
            return super.getResourceName(resid);
        } catch (NotFoundException e) {
            e.printStackTrace();
            return mHostResources.getResourceName(resid);
        }
    }

    @Override
    public String getResourcePackageName(int resid) throws NotFoundException {
        if (isSystemId(resid)) {
            return mHostResources.getResourcePackageName(resid);
        }
        try {
            return super.getResourcePackageName(resid);
        } catch (NotFoundException e) {
            e.printStackTrace();
            return mHostResources.getResourcePackageName(resid);

        }
    }

    @Override
    public String getResourceTypeName(int resid) throws NotFoundException {
        if (isSystemId(resid)) {
            return mHostResources.getResourceTypeName(resid);
        }
        try {
            return super.getResourceTypeName(resid);
        } catch (NotFoundException e) {
            e.printStackTrace();
            return mHostResources.getResourceTypeName(resid);
        }
    }

    @Override
    public String getResourceEntryName(int resid) throws NotFoundException {
        if (isSystemId(resid)) {
            return mHostResources.getResourceEntryName(resid);
        }
        try {
            return super.getResourceEntryName(resid);
        } catch (NotFoundException e) {
            e.printStackTrace();
            return mHostResources.getResourceEntryName(resid);
        }
    }

    @Override
    public int getIdentifier(String name, String defType, String defPackage) {
        if (TextUtils.equals("android", defPackage)) {
            return mHostResources.getIdentifier(name, defType, defPackage);
        } else {
            return super.getIdentifier(name, defType, defPackage);
        }
    }

    @Override
    public Configuration getConfiguration() {
        if (RePlugin.isHostInitialized()) {
            return RePlugin.getHostContext().getResources().getConfiguration();
        } else {
            return super.getConfiguration();
        }
    }

    private boolean isSystemId(int id) {
        return id < 0x7F000000;
    }

}


