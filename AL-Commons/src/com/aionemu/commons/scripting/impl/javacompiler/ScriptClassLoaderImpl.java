package com.aionemu.commons.scripting.impl.javacompiler;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;

import com.aionemu.commons.scripting.ScriptClassLoader;

public class ScriptClassLoaderImpl extends ScriptClassLoader {

    private final JavaFileManager fileManager;
    private final Map<String, Class<?>> loadedClasses = new HashMap<String, Class<?>>();

    public ScriptClassLoaderImpl(JavaFileManager fileManager) {
        super(new URL[0], ClassLoader.getSystemClassLoader());
        this.fileManager = fileManager;
    }

    public ScriptClassLoaderImpl(JavaFileManager fileManager, ScriptClassLoader parent) {
        super(new URL[0], parent);
        this.fileManager = fileManager;
    }

    public ScriptClassLoaderImpl(ClassLoader parent) {
        super(new URL[0], parent);
        this.fileManager = null;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        Class<?> c = loadedClasses.get(name);
        if (c != null) return c;
        c = findLoadedClass(name);
        if (c != null) return c;

        // Try to get from ClassFileManager compiled classes
        byte[] bytes = null;
        if (fileManager instanceof ClassFileManager) {
            ClassFileManager cfm = (ClassFileManager) fileManager;
            Map<String, BinaryClass> compiled = cfm.getCompiledClasses();
            BinaryClass bc = compiled.get(name);
            if (bc != null) {
                bytes = bc.getBytes();
            }
        }

        if (bytes != null) {
            int lastDot = name.lastIndexOf('.');
            if (lastDot != -1) {
                String pkgName = name.substring(0, lastDot);
                if (getPackage(pkgName) == null) {
                    try {
                        definePackage(pkgName, null, null, null, null, null, null, null);
                    } catch (IllegalArgumentException e) {}
                }
            }
            try {
                Class<?> clazz = defineClass(name, bytes, 0, bytes.length);
                if (clazz != null) {
                    resolveClass(clazz);
                    loadedClasses.put(name, clazz);
                    return clazz;
                }
            } catch (NoClassDefFoundError e) {
                throw e;
            } catch (LinkageError e) {
                try {
                    return getParent().loadClass(name);
                } catch (ClassNotFoundException ex) {
                    throw new ClassNotFoundException(name, e);
                }
            }
        }

        return super.findClass(name);
    }

    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        // Child-first for script packages - DAO, ai, handlers etc
        if (name.startsWith("mysql5.") || name.startsWith("ai.") || name.startsWith("handlers.") || name.startsWith("quest.") || name.startsWith("instance.") || name.startsWith("zone.")) {
            try {
                Class<?> c = findClass(name);
                if (c != null) {
                    if (resolve) resolveClass(c);
                    return c;
                }
            } catch (ClassNotFoundException e) {
                // fall through
            }
        }
        return super.loadClass(name, resolve);
    }

    public void addJarFile(File file) throws IOException {
        if (file != null) {
            addURL(file.toURI().toURL());
        }
    }

    public Collection<JavaFileObject> getClassesForPackage(String packageName) {
        List<JavaFileObject> result = new ArrayList<JavaFileObject>();
        if (fileManager instanceof ClassFileManager) {
            Map<String, BinaryClass> compiled = ((ClassFileManager) fileManager).getCompiledClasses();
            for (Map.Entry<String, BinaryClass> e : compiled.entrySet()) {
                if (e.getKey().startsWith(packageName)) {
                    result.add(e.getValue());
                }
            }
        }
        return result;
    }

    public Class<?> loadAndDefine(String name, byte[] bytes) {
        synchronized (getClassLoadingLock(name)) {
            Class<?> c = findLoadedClass(name);
            if (c != null) return c;
            c = loadedClasses.get(name);
            if (c != null) return c;

            int lastDot = name.lastIndexOf('.');
            if (lastDot != -1) {
                String pkgName = name.substring(0, lastDot);
                if (getPackage(pkgName) == null) {
                    try {
                        definePackage(pkgName, null, null, null, null, null, null, null);
                    } catch (IllegalArgumentException e) {}
                }
            }

            try {
                c = defineClass(name, bytes, 0, bytes.length);
                if (c != null) {
                    resolveClass(c);
                    loadedClasses.put(name, c);
                    return c;
                }
            } catch (NoClassDefFoundError e) {
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
}
