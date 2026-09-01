package com.aionemu.commons.scripting;

import java.io.File;
import java.util.Collection;

public abstract class ScriptCompiler {

    protected Iterable<File> libraries;
    protected ScriptClassLoader parentClassLoader;

    public void setLibraires(Iterable<File> files) {
        this.libraries = files;
    }

    public void setLibraries(Iterable<File> files) {
        this.libraries = files;
    }

    public void setParentClassLoader(ScriptClassLoader classLoader) {
        this.parentClassLoader = classLoader;
    }

    public void setParentClassLoader(ClassLoader classLoader) {
        if (classLoader instanceof ScriptClassLoader) {
            this.parentClassLoader = (ScriptClassLoader) classLoader;
        }
    }

    public abstract CompilationResult compile(File[] files, ScriptClassLoader classLoader);
    public abstract CompilationResult compile(Iterable<File> files, ScriptClassLoader classLoader);

    // Original API used by ScriptContextImpl: compile(Collection<File>) using stored parentClassLoader
    public CompilationResult compile(Collection<File> files) {
        return compile(files, parentClassLoader);
    }

    public CompilationResult compile(Iterable<File> files) {
        return compile(files, parentClassLoader);
    }

    public abstract String[] getSupportedFileTypes();
}
