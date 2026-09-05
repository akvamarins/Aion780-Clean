package com.aionemu.commons.scripting.impl.javacompiler;

import com.aionemu.commons.scripting.CompilationResult;
import com.aionemu.commons.scripting.ScriptClassLoader;
import com.aionemu.commons.scripting.ScriptCompiler;

import javax.tools.*;
import java.io.File;
import java.net.URL;
import java.util.*;

public class ScriptCompilerImpl extends ScriptCompiler {

    private final JavaCompiler compiler;

    public ScriptCompilerImpl() {
        this.compiler = ToolProvider.getSystemJavaCompiler();
        if (this.compiler == null) {
            throw new IllegalStateException("Cannot find System Java Compiler. Use JDK 17, not JRE. java.version=" + System.getProperty("java.version"));
        }
    }

    @Override
    public CompilationResult compile(File[] files, ScriptClassLoader parentClassLoader) {
        return compile(Arrays.asList(files), parentClassLoader);
    }

    @Override
    public CompilationResult compile(Iterable<File> files, ScriptClassLoader parentClassLoader) {
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager stdFileManager = compiler.getStandardFileManager(diagnostics, null, null);
        ClassFileManager fileManager = new ClassFileManager(stdFileManager);

        // Build classpath from system + libraries
        StringBuilder cp = new StringBuilder();
        cp.append(System.getProperty("java.class.path"));
        if (this.libraries != null) {
            for (File lib : this.libraries) {
                cp.append(File.pathSeparator).append(lib.getAbsolutePath());
            }
        }
        // Also add parent classloader urls if possible
        // Java 17 compatible options
        List<String> options = new ArrayList<>();
        options.add("-classpath");
        options.add(cp.toString());
        options.add("-Xlint:none");
        options.add("--release");
        options.add("8");

        List<File> fileList = new ArrayList<>();
        for (File f : files) {
            fileList.add(f);
        }

        Iterable<? extends JavaFileObject> compilationUnits = stdFileManager.getJavaFileObjectsFromFiles(fileList);
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, options, null, compilationUnits);
        boolean success = task.call();

        // Prepare classloader with compiled bytes
        URL[] urls = new URL[0];
        // Convert libraries to URLs for parent loader if needed
        if (this.libraries != null) {
            List<URL> urlList = new ArrayList<>();
            for (File lib : this.libraries) {
                try {
                    urlList.add(lib.toURI().toURL());
                } catch (Exception e) {
                    // ignore
                }
            }
            urls = urlList.toArray(new URL[0]);
        }

        ClassLoader parent = parentClassLoader != null ? parentClassLoader : Thread.currentThread().getContextClassLoader();
        // If parent is ScriptClassLoader, keep it as parent for delegation
        ScriptClassLoaderImpl classLoader = new ScriptClassLoaderImpl(fileManager, urls, parent);

        CompilationResult result = new CompilationResult();
        result.setClassLoader(classLoader);

        if (!success) {
            // Log diagnostics for debugging
            System.err.println("=== Script compilation failed ===");
            for (Diagnostic<? extends JavaFileObject> d : diagnostics.getDiagnostics()) {
                System.err.println(d.toString());
            }
            // Return empty result but with classloader
            return result;
        }

        // Load each compiled class and add to result
        for (String className : fileManager.getAllClasses().keySet()) {
            try {
                Class<?> clazz = classLoader.loadClass(className);
                result.addCompiledClass(clazz);
            } catch (ClassNotFoundException e) {
                System.err.println("Failed to load compiled class: " + className + " - " + e.getMessage());
            }
        }

        return result;
    }

    @Override
    public String[] getSupportedFileTypes() {
        return new String[] { "java" };
    }
}