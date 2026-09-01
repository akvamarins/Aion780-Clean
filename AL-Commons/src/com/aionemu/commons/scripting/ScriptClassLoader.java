
package com.aionemu.commons.scripting;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class ScriptClassLoader extends URLClassLoader {

    protected final Set<String> libraryClasses = new HashSet<String>();

    public ScriptClassLoader(URL[] urls) {
        super(urls);
    }

    public ScriptClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    public void addLibraryClass(String className) {
        libraryClasses.add(className);
    }

    public Set<String> getLibraryClasses() {
        return Collections.unmodifiableSet(libraryClasses);
    }

    public abstract Set<String> getCompiledClasses();
    public abstract byte[] getByteCode(String className);
    public abstract Class<?> getDefinedClass(String name);
    public abstract void setDefinedClass(String name, Class<?> clazz);
}
