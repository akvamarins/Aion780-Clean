package com.aionemu.commons.scripting.impl.javacompiler;
import javax.tools.SimpleJavaFileObject;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URI;
public class BinaryClass extends SimpleJavaFileObject {
    private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private final String className;
    private Class<?> definedClass;
    public BinaryClass(String name) {
        super(URI.create("mem:///" + name.replace('.', '/') + Kind.CLASS.extension), Kind.CLASS);
        this.className = name;
    }
    @Override public OutputStream openOutputStream() { return baos; }
    public byte[] getBytes() { return baos.toByteArray(); }
    public String getClassName() { return className; }
    public Class<?> getDefinedClass() { return definedClass; }
    public void setDefinedClass(Class<?> clazz) { this.definedClass = clazz; }
}