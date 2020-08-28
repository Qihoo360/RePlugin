package com.qihoo360.replugin.loader;

import android.annotation.TargetApi;
import android.content.res.AssetFileDescriptor;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.Movie;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.TypedValue;

import com.qihoo360.replugin.RePlugin;

import java.io.InputStream;

public class PluginResource extends Resources {

    private Resources mPluginResource;
    private Resources mHostResources;

    public PluginResource(Resources resources) {
        super(resources.getAssets(), resources.getDisplayMetrics(), resources.getConfiguration());
        this.mPluginResource = resources;
        if (RePlugin.isHostInitialized()) {
            mHostResources = RePlugin.getHostContext().getResources();
        }
    }

    @Override
    public CharSequence getText(int id) throws NotFoundException {
        try {
            return mPluginResource.getText(id);
        } catch (NotFoundException e) {
            e.printStackTrace();
            return mHostResources.getText(id);
        }
    }

    @Override
    public CharSequence getQuantityText(int id, int quantity) throws NotFoundException {
        try {
            return mPluginResource.getQuantityText(id, quantity);
        } catch (NotFoundException e) {
            e.printStackTrace();
            return mHostResources.getQuantityText(id, quantity);
        }
    }

    @Override
    public String getString(int id) throws NotFoundException {
        try {
            return mPluginResource.getString(id);
        } catch (NotFoundException e) {
            e.printStackTrace();
            return mHostResources.getString(id);
        }
    }

    @Override
    public String getString(int id, Object... formatArgs) throws NotFoundException {
        try {
            return mPluginResource.getString(id, formatArgs);
        } catch (NotFoundException e) {
            e.printStackTrace();
            return mHostResources.getString(id, formatArgs);
        }
    }

    @Override
    public String getQuantityString(int id, int quantity, Object... formatArgs)
            throws NotFoundException {
        try {
            return mPluginResource.getQuantityString(id, quantity, formatArgs);
        } catch (NotFoundException e) {
            e.printStackTrace();
            return mHostResources.getQuantityString(id, quantity, formatArgs);
        }
    }

    @Override
    public String getQuantityString(int id, int quantity) throws NotFoundException {
        try {
            return mPluginResource.getQuantityString(id, quantity);
        } catch (NotFoundException e) {
            e.printStackTrace();
            return mHostResources.getQuantityString(id, quantity);
        }
    }

    @Override
    public CharSequence getText(int id, CharSequence def) {
        try {
            return mPluginResource.getText(id, def);
        } catch (Exception e) {
            e.printStackTrace();
            return mHostResources.getText(id, def);
        }
    }

    @Override
    public CharSequence[] getTextArray(int id) throws NotFoundException {
        try {
            return mPluginResource.getTextArray(id);
        } catch (NotFoundException e) {
            e.printStackTrace();
            return mHostResources.getTextArray(id);
        }
    }

    @Override
    public String[] getStringArray(int id) throws NotFoundException {
        try {
            return mPluginResource.getStringArray(id);
        } catch (NotFoundException e) {
            e.printStackTrace();
            return mHostResources.getStringArray(id);
        }
    }

    @Override
    public int[] getIntArray(int id) throws NotFoundException {
        try {
            return mPluginResource.getIntArray(id);
        } catch (NotFoundException e) {
            e.printStackTrace();
            return mHostResources.getIntArray(id);
        }
    }

    @Override
    public TypedArray obtainTypedArray(int id) throws NotFoundException {
        try {
            return mPluginResource.obtainTypedArray(id);
        } catch (NotFoundException e) {
            e.printStackTrace();
            return mHostResources.obtainTypedArray(id);
        }
    }

    @Override
    public float getDimension(int id) throws NotFoundException {
        try {
            return mPluginResource.getDimension(id);
        } catch (NotFoundException e) {
            e.printStackTrace();
            return mHostResources.getDimension(id);
        }
    }

    @Override
    public int getDimensionPixelOffset(int id) throws NotFoundException {
        try {
            return mPluginResource.getDimensionPixelOffset(id);
        } catch (NotFoundException e) {
            e.printStackTrace();
            return mHostResources.getDimensionPixelOffset(id);
        }
    }

    @Override
    public int getDimensionPixelSize(int id) throws NotFoundException {
        try {
            return mPluginResource.getDimensionPixelSize(id);
        } catch (NotFoundException e) {
            e.printStackTrace();
            try {
                return mHostResources.getDimensionPixelSize(id);
            } catch (NotFoundException e1) {
                return 0;
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    @Override
    public float getFraction(int id, int base, int pbase) {
        try {
            return mPluginResource.getFraction(id, base, pbase);
        } catch (Exception e) {
            e.printStackTrace();
            return mHostResources.getFraction(id, base, pbase);
        }
    }

    @Override
    public Drawable getDrawable(int id) throws NotFoundException {
        try {
            return mPluginResource.getDrawable(id);
        } catch (NotFoundException e) {
            e.printStackTrace();
            return mHostResources.getDrawable(id);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public Drawable getDrawable(int id, Theme theme) throws NotFoundException {
        try {
            return mPluginResource.getDrawable(id, theme);
        } catch (NotFoundException e) {
            e.printStackTrace();
            return mHostResources.getDrawable(id, theme);
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    @Override
    public Drawable getDrawableForDensity(int id, int density) throws NotFoundException {
        try {
            return mPluginResource.getDrawableForDensity(id, density);
        } catch (NotFoundException e) {
            e.printStackTrace();
            return mHostResources.getDrawableForDensity(id, density);
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    @Override
    public Drawable getDrawableForDensity(int id, int density, Theme theme) {
        try {
            return mPluginResource.getDrawableForDensity(id, density);
        } catch (NotFoundException e) {
            e.printStackTrace();
            return mHostResources.getDrawableForDensity(id, density);
        }
    }

    @Override
    public Movie getMovie(int id) throws NotFoundException {
        try {
            return mPluginResource.getMovie(id);
        } catch (NotFoundException e) {
            e.printStackTrace();
            return mHostResources.getMovie(id);
        }
    }

    @Override
    public int getColor(int id) throws NotFoundException {
        try {
            return mPluginResource.getColor(id);
        } catch (NotFoundException e) {
            e.printStackTrace();
            return mHostResources.getColor(id);
        }
    }

    @Override
    public ColorStateList getColorStateList(int id) throws NotFoundException {
        try {
            return mPluginResource.getColorStateList(id);
        } catch (NotFoundException e) {
            e.printStackTrace();
            return mHostResources.getColorStateList(id);
        }
    }

    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    @Override
    public boolean getBoolean(int id) throws NotFoundException {
        try {
            return mPluginResource.getBoolean(id);
        } catch (NotFoundException e) {
            e.printStackTrace();
            return mHostResources.getBoolean(id);
        }
    }

    @Override
    public int getInteger(int id) throws NotFoundException {
        try {
            return mPluginResource.getInteger(id);
        } catch (NotFoundException e) {
            e.printStackTrace();
            return mHostResources.getInteger(id);
        }
    }

    @Override
    public XmlResourceParser getLayout(int id) throws NotFoundException {
        try {
            return mPluginResource.getLayout(id);
        } catch (NotFoundException e) {
            e.printStackTrace();
            return mHostResources.getLayout(id);
        }
    }

    @Override
    public XmlResourceParser getAnimation(int id) throws NotFoundException {
        try {
            return mPluginResource.getAnimation(id);
        } catch (NotFoundException e) {
            e.printStackTrace();
            return mHostResources.getAnimation(id);
        }
    }

    @Override
    public XmlResourceParser getXml(int id) throws NotFoundException {
        try {
            return mPluginResource.getXml(id);
        } catch (NotFoundException e) {
            e.printStackTrace();
            return mHostResources.getXml(id);
        }
    }

    @Override
    public InputStream openRawResource(int id) throws NotFoundException {
        try {
            return mPluginResource.openRawResource(id);
        } catch (NotFoundException e) {
            e.printStackTrace();
            return mHostResources.openRawResource(id);
        }
    }

    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    @Override
    public InputStream openRawResource(int id, TypedValue value) throws NotFoundException {
        try {
            return mPluginResource.openRawResource(id, value);
        } catch (NotFoundException e) {
            e.printStackTrace();
            return mHostResources.openRawResource(id, value);
        }
    }

    @Override
    public AssetFileDescriptor openRawResourceFd(int id) throws NotFoundException {
        try {
            return mPluginResource.openRawResourceFd(id);
        } catch (NotFoundException e) {
            e.printStackTrace();
            return mHostResources.openRawResourceFd(id);

        }
    }

    @Override
    public void getValue(int id, TypedValue outValue, boolean resolveRefs)
            throws NotFoundException {
        try {
            mPluginResource.getValue(id, outValue, resolveRefs);
        } catch (NotFoundException e) {
            e.printStackTrace();
            mHostResources.getValue(id, outValue, resolveRefs);
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    @Override
    public void getValueForDensity(int id, int density, TypedValue outValue, boolean resolveRefs)
            throws NotFoundException {
        try {
            mPluginResource.getValueForDensity(id, density, outValue, resolveRefs);
        } catch (NotFoundException e) {
            e.printStackTrace();
            mHostResources.getValueForDensity(id, density, outValue, resolveRefs);

        }
    }

    @Override
    public String getResourceName(int resid) throws NotFoundException {
        try {
            return mPluginResource.getResourceName(resid);
        } catch (NotFoundException e) {
            e.printStackTrace();
            return mHostResources.getResourceName(resid);
        }
    }

    @Override
    public String getResourcePackageName(int resid) throws NotFoundException {
        try {
            return mPluginResource.getResourcePackageName(resid);
        } catch (NotFoundException e) {
            e.printStackTrace();
            return mHostResources.getResourcePackageName(resid);

        }
    }

    @Override
    public String getResourceTypeName(int resid) throws NotFoundException {
        try {
            return mPluginResource.getResourceTypeName(resid);
        } catch (NotFoundException e) {
            e.printStackTrace();
            return mHostResources.getResourceTypeName(resid);
        }
    }

    @Override
    public String getResourceEntryName(int resid) throws NotFoundException {
        try {
            return mPluginResource.getResourceEntryName(resid);
        } catch (NotFoundException e) {
            e.printStackTrace();
            return mHostResources.getResourceEntryName(resid);
        }
    }

    @Override
    public int getIdentifier(String name, String defType, String defPackage) {
        try {
            return mPluginResource.getIdentifier(name, defType, defPackage);
        } catch (Exception e) {
            e.printStackTrace();
            return mHostResources.getIdentifier(name, defType, defPackage);
        }
    }

    @Override
    public Configuration getConfiguration() {
        try {
            return mPluginResource.getConfiguration();
        } catch (Exception e) {
            e.printStackTrace();
            return mHostResources.getConfiguration();
        }
    }
}
