
package com.aionemu.commons.scripting;

import java.util.*;

public class CompilationResult {

    private final List<Class<?>> compiledClasses = new ArrayList<Class<?>>();
    private final Set<String> classes = new HashSet<String>();
    private ScriptClassLoader classLoader;

    public CompilationResult() {}

    public CompilationResult(Class<?>[] classes, ScriptClassLoader classLoader) {
        this.classLoader = classLoader;
        for (Class<?> clazz : classes) {
            addCompiledClass(clazz);
        }
    }

    public void addCompiledClass(Class<?> clazz) {
        compiledClasses.add(clazz);
        classes.add(clazz.getName());
    }

    public Class<?>[] getCompiledClasses() {
        return compiledClasses.toArray(new Class<?>[compiledClasses.size()]);
    }

    public Set<String> getCompiledClassesSet() {
        return Collections.unmodifiableSet(classes);
    }

    public int getNumberOfCompiledClasses() {
        return compiledClasses.size();
    }

    public ScriptClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ScriptClassLoader cl) {
        this.classLoader = cl;
    }
}
