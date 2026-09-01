package com.aionemu.commons.scripting.impl.javacompiler;

import com.aionemu.commons.scripting.ScriptClassLoader;
import java.util.HashMap;
import java.util.Map;

/**
 * FIXED for Java 17 - child-first with package definition and dependency-order support
 */
public class ScriptClassLoaderImpl extends ScriptClassLoader {

    private final Map<String, Class<?>> loaded = new HashMap<>();

    public ScriptClassLoaderImpl(ClassLoader parent) {
        super(parent);
    }

    public Class<?> loadAndDefine(String name, byte[] bytes) {
        synchronized (getClassLoadingLock(name)) {
            Class<?> c = findLoadedClass(name);
            if (c != null) return c;
            c = loaded.get(name);
            if (c != null) return c;

            int lastDot = name.lastIndexOf('.');
            if (lastDot != -1) {
                String packageName = name.substring(0, lastDot);
                if (getPackage(packageName) == null) {
                    try {
                        definePackage(packageName, null, null, null, null, null, null, null);
                    } catch (IllegalArgumentException e) {}
                }
            }

            try {
                c = defineClass(name, bytes, 0, bytes.length);
                if (c != null) {
                    resolveClass(c);
                    loaded.put(name, c);
                    return c;
                }
            } catch (java.lang.NoClassDefFoundError e) {
                // Preserve for dependency sorting
                throw e;
            } catch (LinkageError e) {
                try {
                    return getParent().loadClass(name);
                } catch (ClassNotFoundException ex) {
                    throw new RuntimeException("Failed to define class " + name + ": " + e.getMessage(), e);
                }
            }
            throw new RuntimeException("Failed to define class " + name);
        }
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> c = findLoadedClass(name);
            if (c != null) {
                if (resolve) resolveClass(c);
                return c;
            }
            c = loaded.get(name);
            if (c != null) {
                if (resolve) resolveClass(c);
                return c;
            }
            
            if (name.startsWith("ai.") || name.startsWith("handlers.") || name.startsWith("quest.") || name.startsWith("instance.") || name.startsWith("zone.")) {
                try {
                    c = findClass(name);
                    if (c != null) {
                        if (resolve) resolveClass(c);
                        return c;
                    }
                } catch (ClassNotFoundException e) {}
            }

            try {
                c = super.loadClass(name, resolve);
                return c;
            } catch (ClassNotFoundException e) {
                c = loaded.get(name);
                if (c != null) {
                    if (resolve) resolveClass(c);
                    return c;
                }
                throw e;
            }
        }
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        Class<?> c = loaded.get(name);
        if (c != null) return c;
        throw new ClassNotFoundException(name);
    }
}
