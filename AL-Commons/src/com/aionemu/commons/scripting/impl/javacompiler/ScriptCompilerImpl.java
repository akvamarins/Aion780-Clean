/**
 * RETAIL CLEAN - ScriptCompilerImpl for Java 17
 * Clean retail version without --release 8 hack
 * Properly compiles AI and quest scripts on Java 17
 */
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
        this.compiler = ToolProvider.getSystemJavaCompiler();
        if (this.compiler == null) {
            throw new IllegalStateException("No Java compiler found. Use JDK 17, not JRE. java.version=" + System.getProperty("java.version"));
        }
    }

    @Override
    public String[] getSupportedFileTypes() {
        return new String[]{"java"};
    }

    @Override
    public CompilationResult compile(File[] files, ScriptClassLoader classLoader) {
        return compile(Arrays.asList(files), classLoader);
    }

    @Override
    public CompilationResult compile(Iterable<File> files, ScriptClassLoader classLoader) {
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager stdFileManager = compiler.getStandardFileManager(diagnostics, null, null);

        ScriptClassLoaderImpl scl;
        ClassFileManager fileManager;

        if (classLoader == null) {
            fileManager = new ClassFileManager(stdFileManager);
            scl = new ScriptClassLoaderImpl(fileManager);
        } else {
            scl = (ScriptClassLoaderImpl) classLoader;
            fileManager = scl.getClassFileManager();
            if (fileManager == null) {
                fileManager = new ClassFileManager(stdFileManager, scl);
            }
        }

        fileManager.setParentClassLoader(scl);
        if (libraries != null) {
            fileManager.addLibraries(libraries);
        }

        List<JavaFileObject> compilationUnits = new ArrayList<>();
        for (File file : files) {
            if (file != null && file.exists()) {
                compilationUnits.add(new JavaSourceFromFile(file));
            }
        }

        List<String> options = new ArrayList<>();
        options.add("-g");
        options.add("-nowarn");
        options.add("-Xlint:none");
        // Retail clean: no --release flag, compile with current JDK (17)
        // This allows modern Java features if needed, but keeps backward compat

        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, options, null, compilationUnits);
        boolean success = task.call();

        CompilationResult result = new CompilationResult();
        result.setClassLoader(scl);

        if (success) {
            for (Map.Entry<String, BinaryClass> entry : fileManager.getCompiledClasses().entrySet()) {
                try {
                    String className = entry.getKey();
                    BinaryClass binaryClass = entry.getValue();
                    Class<?> clazz = scl.defineClassPublic(className, binaryClass.getBytes());
                    binaryClass.setDefinedClass(clazz);
                    scl.setDefinedClass(className, clazz);
                    result.addCompiledClass(clazz);
                } catch (Throwable e) {
                    // Skip failed class definition
                }
            }
        } else {
            // Log compilation errors for debugging
            for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                if (diagnostic.getKind() == Diagnostic.Kind.ERROR) {
                    System.err.println("[RETAIL COMPILE ERROR] " + diagnostic.getMessage(null) + " at " + diagnostic.getSource() + ":" + diagnostic.getLineNumber());
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
