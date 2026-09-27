package com.aionemu.commons.scripting.impl.javacompiler;

import com.aionemu.commons.scripting.ScriptClassLoader;
import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class ClassFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {

    private final Map<String, BinaryClass> compiledClasses = new HashMap<String, BinaryClass>();
    private ScriptClassLoaderImpl classLoader;

    public ClassFileManager(StandardJavaFileManager standardManager) {
        super(standardManager);
    }

    public ClassFileManager(StandardJavaFileManager standardManager, ScriptClassLoaderImpl classLoader) {
        super(standardManager);
        this.classLoader = classLoader;
    }

    public void setParentClassLoader(ScriptClassLoader parent) {
        // compatibility with original ScriptContextImpl
    }

    public void addLibraries(Iterable<File> files) {
        // no-op, libs already on classpath via URLClassLoader parent
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
        BinaryClass bc = new BinaryClass(className);
        compiledClasses.put(className, bc);
        return bc;
    }

    @Override
    public Iterable<JavaFileObject> list(Location location, String packageName, Set<JavaFileObject.Kind> kinds, boolean recurse) throws IOException {
        Iterable<JavaFileObject> result = super.list(location, packageName, kinds, recurse);
        if (location == StandardLocation.CLASS_PATH && kinds.contains(JavaFileObject.Kind.CLASS) && classLoader != null) {
            Set<JavaFileObject> newResult = new HashSet<JavaFileObject>();
            for (JavaFileObject jfo : result) {
                newResult.add(jfo);
            }
            try {
                newResult.addAll(classLoader.getClassesForPackage(packageName));
            } catch (Exception e) {}
            result = newResult;
        }
        return result;
    }

    @Override
    public String inferBinaryName(Location location, JavaFileObject file) {
        if (file instanceof BinaryClass) {
            return ((BinaryClass) file).getClassName();
        }
        return super.inferBinaryName(location, file);
    }

    public Map<String, BinaryClass> getCompiledClasses() {
        return compiledClasses;
    }

    public Map<String, byte[]> getAllClasses() {
        Map<String, byte[]> result = new HashMap<String, byte[]>();
        for (Map.Entry<String, BinaryClass> e : compiledClasses.entrySet()) {
            result.put(e.getKey(), e.getValue().getBytes());
        }
        return result;
    }
}
