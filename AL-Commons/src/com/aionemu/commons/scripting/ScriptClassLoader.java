package com.aionemu.commons.scripting;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

public abstract class ScriptClassLoader extends URLClassLoader {

    private final Map<String, Class<?>> definedClasses = new HashMap<>();
    private final Map<String, byte[]> byteCodes = new HashMap<>();

    public ScriptClassLoader(URL[] urls) {
        super(urls);
    }

    public ScriptClassLoader(ClassLoader parent) {
        super(new URL[0], parent);
    }

    public ScriptClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    public Class<?> loadAndDefine(String name, byte[] bytes) {
        byteCodes.put(name, bytes);
        Class<?> clazz = defineClass(name, bytes, 0, bytes.length);
        definedClasses.put(name, clazz);
        return clazz;
    }

    public byte[] getByteCode(String name) {
        return byteCodes.get(name);
    }

    public void setDefinedClass(String name, Class<?> clazz) {
        definedClasses.put(name, clazz);
    }

    public Class<?> getDefinedClass(String name) {
        return definedClasses.get(name);
    }

    public void addClass(Class<?> clazz) {
        definedClasses.put(clazz.getName(), clazz);
    }

    public Map<String, Class<?>> getDefinedClasses() {
        return definedClasses;
    }
}
