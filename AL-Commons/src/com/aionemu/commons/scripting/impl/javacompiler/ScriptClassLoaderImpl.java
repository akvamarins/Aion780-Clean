package com.aionemu.commons.scripting.impl.javacompiler;

import com.aionemu.commons.scripting.ScriptClassLoader;

import java.net.URL;
import java.util.*;

public class ScriptClassLoaderImpl extends ScriptClassLoader {

    private final Map<String, byte[]> byteCodes = new HashMap<>();
    private final Map<String, Class<?>> definedClasses = new HashMap<>();

    public ScriptClassLoaderImpl(ClassFileManager fileManager) {
        super(new URL[0], Thread.currentThread().getContextClassLoader());
        if (fileManager != null) {
            this.byteCodes.putAll(fileManager.getAllClasses());
        }
    }

    public ScriptClassLoaderImpl(ClassFileManager fileManager, ClassLoader parent) {
        super(new URL[0], parent);
        if (fileManager != null) {
            this.byteCodes.putAll(fileManager.getAllClasses());
        }
    }

    public ScriptClassLoaderImpl(ClassFileManager fileManager, URL[] urls, ClassLoader parent) {
        super(urls, parent);
        if (fileManager != null) {
            this.byteCodes.putAll(fileManager.getAllClasses());
        }
    }

    @Override
    public Set<String> getCompiledClasses() {
        return Collections.unmodifiableSet(byteCodes.keySet());
    }

    @Override
    public byte[] getByteCode(String className) {
        return byteCodes.get(className);
    }

    @Override
    public Class<?> getDefinedClass(String name) {
        return definedClasses.get(name);
    }

    @Override
    public void setDefinedClass(String name, Class<?> clazz) {
        definedClasses.put(name, clazz);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] bytes = byteCodes.get(name);
        if (bytes != null) {
            Class<?> clazz = defineClass(name, bytes, 0, bytes.length);
            definedClasses.put(name, clazz);
            return clazz;
        }
        return super.findClass(name);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        // Check if already defined
        Class<?> c = findLoadedClass(name);
        if (c != null) {
            return c;
        }
        // Check if compiled in memory
        if (byteCodes.containsKey(name)) {
            return findClass(name);
        }
        return super.loadClass(name);
    }
}