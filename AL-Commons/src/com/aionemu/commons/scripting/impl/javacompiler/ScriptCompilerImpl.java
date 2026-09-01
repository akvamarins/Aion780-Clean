package com.aionemu.commons.scripting.impl.javacompiler;

import com.aionemu.commons.scripting.CompilationResult;
import com.aionemu.commons.scripting.ScriptClassLoader;
import com.aionemu.commons.scripting.ScriptCompiler;

import javax.tools.*;
import java.io.File;
import java.util.*;

public class ScriptCompilerImpl extends ScriptCompiler {

    private final JavaCompiler compiler;

    public ScriptCompilerImpl() {
        compiler = ToolProvider.getSystemJavaCompiler();
    }

    @Override
    public String[] getSupportedFileTypes() {
        return new String[] { "java" };
    }

    @Override
    public CompilationResult compile(File[] files, ScriptClassLoader classLoader) {
        return compile(Arrays.asList(files), classLoader);
    }

    @Override
    public CompilationResult compile(Iterable<File> files, ScriptClassLoader classLoader) {
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
        StandardJavaFileManager stdFileManager = compiler.getStandardFileManager(diagnostics, null, null);
        ScriptClassLoaderImpl scl = (ScriptClassLoaderImpl) classLoader;
        ClassFileManager fileManager = scl.getClassFileManager();
        if (fileManager == null) {
            fileManager = new ClassFileManager(stdFileManager, scl);
        }
        fileManager.setParentClassLoader(classLoader);
        if (libraries != null) {
            fileManager.addLibraries(libraries);
        }

        List<JavaFileObject> compilationUnits = new ArrayList<JavaFileObject>();
        for (File f : files) {
            if (f != null && f.exists()) {
                compilationUnits.add(new JavaSourceFromFile(f));
            }
        }

        List<String> options = new ArrayList<String>();
        options.add("-g");

        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, options, null, compilationUnits);
        boolean success = task.call();

        CompilationResult result = new CompilationResult();
        result.setClassLoader(classLoader);

        if (success) {
            Map<String, BinaryClass> classes = fileManager.getCompiledClasses();
            for (Map.Entry<String, BinaryClass> entry : classes.entrySet()) {
                try {
                    String name = entry.getKey();
                    BinaryClass bc = entry.getValue();
                    byte[] bytes = bc.getBytes();
                    Class<?> clazz = scl.defineClassPublic(name, bytes);
                    bc.setDefinedClass(clazz);
                    scl.setDefinedClass(name, clazz);
                    result.addCompiledClass(clazz);
                } catch (Throwable e) {
                }
            }
        }
        return result;
    }

    public static class JavaSourceFromFile extends SimpleJavaFileObject {
        private final File file;
        public JavaSourceFromFile(File file) {
            super(file.toURI(), Kind.SOURCE);
            this.file = file;
        }
        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws java.io.IOException {
            return new String(java.nio.file.Files.readAllBytes(file.toPath()));
        }
    }
}
