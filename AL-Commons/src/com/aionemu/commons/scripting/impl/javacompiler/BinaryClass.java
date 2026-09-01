package com.aionemu.commons.scripting.impl.javacompiler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import javax.tools.SimpleJavaFileObject;

public class BinaryClass extends SimpleJavaFileObject {

        private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        private final String className;

        public BinaryClass(String className) {
                super(URI.create("mem:///" + className.replace('.', '/') + Kind.CLASS.extension), Kind.CLASS);
                this.className = className;
        }

        @Override
        public OutputStream openOutputStream() throws IOException {
                return baos;
        }

        public byte[] getBytes() {
                return baos.toByteArray();
        }

        public String getClassName() {
                return className;
        }

        public String inferBinaryName(String s) {
                return className;
        }
}
