package com.qihoo360.replugin;

import com.qihoo360.loader2.PMF;

import java.lang.reflect.Field;

/**
 * 给宿主的ClassLoader新增一层parent ClassLoader.因为正常的ClassLoader都会遵循"双亲委派"机制,
 * 所以宿主的PathClassLoader会尊重这个ClassLoader返回的结果.从而可以在这个ClassLoader中返回PMF想要返回的类.
 * <p>
 * 方案来自：
 * https://github.com/Tencent/Shadow/blob/dev/projects/sdk/dynamic/dynamic-host/src/main/java/com/tencent/shadow/dynamic/host/DynamicRuntime.java
 *
 * @author cubershi@tencent.com
 */
public class PMFClassLoader extends ClassLoader {

    private PMFClassLoader(ClassLoader parent) {
        super(parent);
    }

    @Override
    protected Class<?> loadClass(String className, boolean resolve) throws ClassNotFoundException {
        //
        Class<?> c = null;
        c = PMF.loadClass(className, resolve);
        if (c != null) {
            return c;
        }
        return super.loadClass(className, resolve);
    }

    @Override
    protected Package getPackage(String name) {
        final Package aPackage = super.getPackage(name);
        if (aPackage == null) {
            return definePackage(name, "Unknown", "0.0", "Unknown", "Unknown", "0.0", "Unknown", null);
        } else {
            return aPackage;
        }
    }

    public static void hackHostClassLoader() {
        ClassLoader hostClassLoader = PMF.class.getClassLoader();
        final PMFClassLoader newParentClassLoader = new PMFClassLoader(hostClassLoader.getParent());
        try {
            PMFClassLoader.hackParentClassLoader(hostClassLoader, newParentClassLoader);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 修改ClassLoader的parent
     *
     * @param classLoader          需要修改的ClassLoader
     * @param newParentClassLoader classLoader的新的parent
     */
    private static void hackParentClassLoader(ClassLoader classLoader,
                                              ClassLoader newParentClassLoader) throws Exception {
        Field field = getParentField();
        if (field == null) {
            throw new Exception("在ClassLoader.class中没找到类型为ClassLoader的parent域");
        }
        field.setAccessible(true);
        field.set(classLoader, newParentClassLoader);
    }

    /**
     * 安全地获取到ClassLoader类的parent域
     *
     * @return ClassLoader类的parent域.或不能通过反射访问该域时返回null.
     */
    private static Field getParentField() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader parent = classLoader.getParent();
        Field field = null;
        for (Field f : ClassLoader.class.getDeclaredFields()) {
            try {
                boolean accessible = f.isAccessible();
                f.setAccessible(true);
                Object o = f.get(classLoader);
                f.setAccessible(accessible);
                if (o == parent) {
                    field = f;
                    break;
                }
            } catch (IllegalAccessException ignore) {
            }
        }
        return field;
    }

}
