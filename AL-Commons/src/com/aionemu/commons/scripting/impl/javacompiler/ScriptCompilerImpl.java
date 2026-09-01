package com.aionemu.commons.scripting.impl.javacompiler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.scripting.CompilationResult;
import com.aionemu.commons.scripting.ScriptClassLoader;
import com.aionemu.commons.scripting.ScriptCompiler;

public class ScriptCompilerImpl implements ScriptCompiler {

        private static final Logger log = LoggerFactory.getLogger(ScriptCompilerImpl.class);
        protected final JavaCompiler javaCompiler;
        protected Iterable<File> libraries;
        protected ScriptClassLoader parentClassLoader;

        public ScriptCompilerImpl() {
                JavaCompiler comp = ToolProvider.getSystemJavaCompiler();
                if (comp == null) {
                        try {
                                comp = (JavaCompiler) Class.forName("com.sun.tools.javac.api.JavacTool").getMethod("create").invoke(null);
                        } catch (Throwable t) {}
                }
                this.javaCompiler = comp;
                if (javaCompiler == null) {
                        throw new RuntimeException(new InstantiationException("JavaCompiler is not aviable."));
                }
        }

        @Override
        public void setParentClassLoader(ScriptClassLoader classLoader) {
                this.parentClassLoader = classLoader;
        }

        @Override
        public void setLibraires(Iterable<File> files) {
                libraries = files;
        }

        @Override
        public CompilationResult compile(String className, String sourceCode) {
                return compile(new String[] { className }, new String[] { sourceCode });
        }

        @Override
        public CompilationResult compile(String[] classNames, String[] sourceCode) throws IllegalArgumentException {
                if (classNames.length != sourceCode.length) {
                        throw new IllegalArgumentException("Amount of classes is not equal to amount of sources");
                }
                List<JavaFileObject> compilationUnits = new ArrayList<JavaFileObject>();
                for (int i = 0; i < classNames.length; i++) {
                        JavaFileObject compilationUnit = new JavaSourceFromString(classNames[i], sourceCode[i]);
                        compilationUnits.add(compilationUnit);
                }
                return doCompilation(compilationUnits);
        }

        @Override
        public CompilationResult compile(Iterable<File> compilationUnits) {
                List<JavaFileObject> list = new ArrayList<JavaFileObject>();
                for (File f : compilationUnits) {
                        list.add(new JavaSourceFromFile(f, JavaFileObject.Kind.SOURCE));
                }
                return doCompilation(list);
        }

        protected CompilationResult doCompilation(Iterable<JavaFileObject> compilationUnits) {
                List<String> options = Arrays.asList("-encoding", "UTF-8", "-g", "-source", "8", "-target", "8");
                DiagnosticListener<JavaFileObject> listener = new ErrorListener();
                JavaCompiler compForManager = this.javaCompiler;
                if (compForManager == null) compForManager = ToolProvider.getSystemJavaCompiler();
                ClassFileManager manager = new ClassFileManager(compForManager, listener);
                manager.setParentClassLoader(parentClassLoader);

                if (libraries != null) {
                        try {
                                manager.addLibraries(libraries);
                        } catch (IOException e) {
                                log.error("Can't set libraries for compiler.", e);
                        }
                }

                JavaCompiler.CompilationTask task = javaCompiler.getTask(null, manager, listener, options, null,
                                compilationUnits);

                if (!task.call()) {
                        throw new RuntimeException("Error while compiling classes");
                }

                ScriptClassLoader cl = manager.getClassLoader(null);
                Class<?>[] compiledClasses = classNamesToClasses(manager.getCompiledClasses().keySet(), cl);
                return new CompilationResult(compiledClasses, cl);
        }

        protected Class<?>[] classNamesToClasses(Collection<String> classNames, ScriptClassLoader cl) {
                Class<?>[] classes = new Class<?>[classNames.size()];
                int i = 0;
                for (String className : classNames) {
                        try {
                                Class<?> clazz = cl.loadClass(className);
                                classes[i] = clazz;
                        } catch (ClassNotFoundException e) {
                                throw new RuntimeException(e);
                        }
                        i++;
                }
                return classes;
        }

        @Override
        public String[] getSupportedFileTypes() {
                return new String[] { "java" };
        }
}
