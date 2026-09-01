
package com.aionemu.commons.scripting.impl.javacompiler;

import com.aionemu.commons.scripting.CompilationResult;
import com.aionemu.commons.scripting.ScriptClassLoader;
import com.aionemu.commons.scripting.ScriptCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.tools.*;
import java.io.*;
import java.net.URI;
import java.util.*;

public class ScriptCompilerImpl implements ScriptCompiler {

    private static final Logger log = LoggerFactory.getLogger(ScriptCompilerImpl.class);

    private final JavaCompiler compiler;
    private final List<String> options;

    public ScriptCompilerImpl() {
        compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException("JavaCompiler not available - need JDK, not JRE");
        }
        options = Arrays.asList("-g", "-source", "8", "-target", "8");
    }

    @Override
    public CompilationResult compile(File[] files, ScriptClassLoader loader) {
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager stdFileManager = compiler.getStandardFileManager(diagnostics, null, null);
        ClassFileManager fileManager = new ClassFileManager(stdFileManager, (ScriptClassLoaderImpl) loader);

        List<JavaFileObject> compilationUnits = new ArrayList<>();
        for (File f : files) {
            if (f != null && f.exists()) {
                compilationUnits.add(new SourceFile(f));
            }
        }

        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, options, null, compilationUnits);
        boolean success = task.call();
        
        if (!success) {
            for (Diagnostic<? extends JavaFileObject> d : diagnostics.getDiagnostics()) {
                log.warn("Compile error: {}:{} {}", d.getSource() != null ? d.getSource().getName() : "unknown", d.getLineNumber(), d.getMessage(null));
            }
        }

        return doCompileWithManager(fileManager, loader);
    }

    private CompilationResult doCompileWithManager(ClassFileManager fileManager, ScriptClassLoader loader) {
        CompilationResult result = new CompilationResult();
        ScriptClassLoaderImpl impl = (ScriptClassLoaderImpl) loader;

        Map<String, byte[]> classes = fileManager.getAllClasses();
        if (classes == null || classes.isEmpty()) {
            return result;
        }

        // Sort by dependency depth: base classes first
        // Simple heuristic: shorter name + no inheritance chain first, or class with no superclass in same batch
        // We will iteratively try to load, deferring failed ones
        
        Map<String, byte[]> remaining = new LinkedHashMap<>(classes);
        Set<String> loadedNames = new HashSet<>();
        int retries = remaining.size() * 2; // max iterations
        
        while (!remaining.isEmpty() && retries-- > 0) {
            Iterator<Map.Entry<String, byte[]>> it = remaining.entrySet().iterator();
            boolean progress = false;
            
            while (it.hasNext()) {
                Map.Entry<String, byte[]> entry = it.next();
                String name = entry.getKey();
                byte[] bytes = entry.getValue();
                
                try {
                    Class<?> clazz = impl.loadAndDefine(name, bytes);
                    if (clazz != null) {
                        result.addCompiledClass(clazz);
                        loadedNames.add(name);
                        it.remove();
                        progress = true;
                    }
                } catch (NoClassDefFoundError | ClassNotFoundException | LinkageError e) {
                    // Check if missing dependency is in remaining set - then defer
                    String msg = e.getMessage();
                    if (msg != null) msg = msg.replace('/', '.');
                    Throwable cause = e.getCause();
                    String causeMsg = cause != null ? cause.getMessage() : "";
                    
                    // If dependency is still in remaining, defer
                    boolean depInRemaining = false;
                    if (msg != null) {
                        for (String rem : remaining.keySet()) {
                            if (msg.contains(rem) || (causeMsg != null && causeMsg.contains(rem))) {
                                depInRemaining = true;
                                break;
                            }
                        }
                    }
                    if (!depInRemaining) {
                        // Try to extract class name from error
                        // e.g. "ai/GeneralNpcAI2" -> "ai.GeneralNpcAI2"
                        String missing = null;
                        if (msg != null && msg.contains("ai.")) {
                            // try to find ai. class
                            int idx = msg.indexOf("ai.");
                            if (idx >= 0) {
                                missing = msg.substring(idx).split(" ")[0].trim();
                            }
                        }
                        if (missing != null && remaining.containsKey(missing)) {
                            depInRemaining = true;
                        }
                    }
                    
                    if (!depInRemaining) {
                        // Real error, not dependency order - log and remove to avoid infinite loop
                        // But only if retries low
                        if (retries < remaining.size()) {
                            log.warn("Failed to load {}: {}", name, e.toString());
                            // still remove to avoid blocking others
                            it.remove();
                        }
                    }
                    // else defer - keep in remaining for next iteration
                } catch (Exception ex) {
                    log.warn("Failed to load {}: {}", name, ex.toString());
                    it.remove();
                }
            }
            
            if (!progress) {
                // No progress, try to sort remaining by name length (base classes usually shorter)
                // and try once more with explicit sort
                List<Map.Entry<String, byte[]>> sorted = new ArrayList<>(remaining.entrySet());
                Collections.sort(sorted, new Comparator<Map.Entry<String, byte[]>>() {
                    @Override
                    public int compare(Map.Entry<String, byte[]> a, Map.Entry<String, byte[]> b) {
                        return Integer.compare(a.getKey().length(), b.getKey().length());
                    }
                });
                remaining.clear();
                for (Map.Entry<String, byte[]> e : sorted) {
                    remaining.put(e.getKey(), e.getValue());
                }
                // One more attempt without defer
                if (retries <= 0) break;
            }
        }
        
        // Final attempt: try to load whatever remains (may still fail, but we tried)
        for (Map.Entry<String, byte[]> entry : remaining.entrySet()) {
            try {
                Class<?> clazz = impl.loadAndDefine(entry.getKey(), entry.getValue());
                if (clazz != null) result.addCompiledClass(clazz);
            } catch (Throwable t) {
                log.error("Final fail to load class {}: {}", entry.getKey(), t.toString());
            }
        }

        return result;
    }

    private static class SourceFile extends SimpleJavaFileObject {
        private final File file;

        SourceFile(File file) {
            super(file.toURI(), Kind.SOURCE);
            this.file = file;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append('\n');
                }
            }
            return sb.toString();
        }
    }

    private static class ByteCode extends SimpleJavaFileObject {
        private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        private final String className;

        ByteCode(String className) {
            super(URI.create("mem:///" + className.replace('.', '/') + Kind.CLASS.extension), Kind.CLASS);
            this.className = className;
        }

        @Override
        public OutputStream openOutputStream() {
            return baos;
        }

        byte[] getBytes() {
            return baos.toByteArray();
        }

        String getClassName() {
            return className;
        }
    }
}
