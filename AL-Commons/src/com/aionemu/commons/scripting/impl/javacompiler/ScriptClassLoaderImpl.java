package com.aionemu.commons.scripting.impl.javacompiler;

import com.aionemu.commons.scripting.ScriptClassLoader;
import com.aionemu.commons.utils.ClassUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ScriptClassLoaderImpl extends ScriptClassLoader {

    private static final Logger log = Logger.getLogger(ScriptClassLoaderImpl.class);
    private final ClassFileManager classFileManager;

    ScriptClassLoaderImpl(ClassFileManager classFileManager) {
        super(new URL[] {});
        this.classFileManager = classFileManager;
    }

    ScriptClassLoaderImpl(ClassFileManager classFileManager, ClassLoader parent) {
        super(new URL[] {}, parent);
        this.classFileManager = classFileManager;
    }

    public ClassFileManager getClassFileManager() {
        return classFileManager;
    }

    @Override
    public Set<String> getCompiledClasses() {
        return Collections.unmodifiableSet(classFileManager.getCompiledClasses().keySet());
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        // 1. Проверяем уже скомпилированные в памяти
        if (classFileManager != null) {
            BinaryClass bc = classFileManager.getCompiledClasses().get(name);
            if (bc != null && bc.getDefinedClass() != null) {
                return bc.getDefinedClass();
            }
        }
        // 2. Java 17 fix - создаем package
        int lastDot = name.lastIndexOf('.');
        if (lastDot != -1) {
            String packageName = name.substring(0, lastDot);
            if (getPackage(packageName) == null) {
                try {
                    definePackage(packageName, null, null, null, null, null, null, null);
                } catch (IllegalArgumentException e) {}
            }
        }
        // 3. Для ai.* сначала пробуем найти в памяти, потом уже parent
        if (name.startsWith("ai.") || name.startsWith("handlers.") || name.startsWith("quest.") || name.startsWith("instance.")) {
            if (classFileManager != null) {
                BinaryClass bc = classFileManager.getCompiledClasses().get(name);
                if (bc != null) {
                    byte[] bytes = bc.getBytes();
                    if (bytes != null && bytes.length > 0) {
                        try {
                            Class<?> clazz = defineClass(name, bytes, 0, bytes.length);
                            bc.setDefinedClass(clazz);
                            return clazz;
                        } catch (Throwable t) {
                            // fallback to super
                        }
                    }
                }
            }
        }
        return super.findClass(name);
    }

    public Class<?> defineClassPublic(String name, byte[] b) {
        int lastDot = name.lastIndexOf('.');
        if (lastDot != -1) {
            String packageName = name.substring(0, lastDot);
            if (getPackage(packageName) == null) {
                try { definePackage(packageName, null, null, null, null, null, null, null); } catch (IllegalArgumentException e) {}
            }
        }
        Class<?> clazz = defineClass(name, b, 0, b.length);
        // сохраняем
        if (classFileManager != null) {
            BinaryClass bc = classFileManager.getCompiledClasses().get(name);
            if (bc != null) bc.setDefinedClass(clazz);
        }
        return clazz;
    }

    // остальные методы как в FINAL
    public Set<JavaFileObject> getClassesForPackage(String packageName) throws IOException {
        Set<JavaFileObject> result = new HashSet<>();
        ClassLoader parent = getParent();
        if (parent instanceof ScriptClassLoaderImpl) {
            result.addAll(((ScriptClassLoaderImpl) parent).getClassesForPackage(packageName));
        }
        for (String cn : classFileManager.getCompiledClasses().keySet()) {
            if (ClassUtils.isPackageMember(cn, packageName)) {
                result.add(classFileManager.getCompiledClasses().get(cn));
            }
        }
        return result;
    }

    protected byte[] getRawClassByName(String name) throws IOException {
        URL resource = findResource(name.replace('.', '/').concat(".class"));
        try (InputStream is = resource.openStream()) {
            return IOUtils.toByteArray(is);
        }
    }

    @Override public byte[] getByteCode(String className) {
        BinaryClass bc = classFileManager.getCompiledClasses().get(className);
        return bc.getBytes().clone();
    }
    @Override public Class<?> getDefinedClass(String name) {
        BinaryClass bc = classFileManager.getCompiledClasses().get(name);
        return bc == null ? null : bc.getDefinedClass();
    }
    @Override public void setDefinedClass(String name, Class<?> clazz) {
        BinaryClass bc = classFileManager.getCompiledClasses().get(name);
        if (bc == null) throw new IllegalArgumentException("Not compiled: " + name);
        bc.setDefinedClass(clazz);
    }
}