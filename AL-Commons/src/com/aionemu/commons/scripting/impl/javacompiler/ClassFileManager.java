package com.aionemu.commons.scripting.impl.javacompiler;

import javax.tools.*;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.*;

public class ClassFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {

    private final Map<String, ByteArrayOutputStream> compiled = new HashMap<>();

    public ClassFileManager(StandardJavaFileManager fileManager) {
        super(fileManager);
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        compiled.put(className, baos);
        return new SimpleJavaFileObject(URI.create("mem:///" + className.replace('.', '/') + kind.extension), kind) {
            @Override
            public OutputStream openOutputStream() {
                return baos;
            }
        };
    }

    public Map<String, byte[]> getAllClasses() {
        Map<String, byte[]> result = new HashMap<>();
        compiled.forEach((k, v) -> result.put(k, v.toByteArray()));
        return result;
    }

    public byte[] getClassBytes(String className) {
        ByteArrayOutputStream baos = compiled.get(className);
        return baos != null ? baos.toByteArray() : null;
    }
}