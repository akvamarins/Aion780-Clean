package com.aionemu.commons.callbacks.enhancer;

import java.io.ByteArrayInputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.callbacks.Callback;
import com.aionemu.commons.callbacks.CallbackResult;
import com.aionemu.commons.callbacks.EnhancedObject;
import com.aionemu.commons.callbacks.metadata.ObjectCallback;
import com.aionemu.commons.callbacks.util.CallbacksUtil;
import com.aionemu.commons.callbacks.util.ObjectCallbackHelper;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.LoaderClassPath;
import javassist.Modifier;
import javassist.NotFoundException;

public class ObjectCallbackEnhancer extends CallbackClassFileTransformer {

        private static final Logger log = LoggerFactory.getLogger(ObjectCallbackEnhancer.class);

        public static final String FIELD_NAME_CALLBACKS = "$$$callbacks";
        public static final String FIELD_NAME_CALLBACKS_LOCK = "$$$callbackLock";

        @Override
        protected byte[] transformClass(ClassLoader loader, byte[] clazzBytes) throws Exception {
                // FIX for Java 17: use parent ClassPool with system path
                ClassPool cp = new ClassPool(ClassPool.getDefault());
                cp.appendSystemPath();
                if (loader != null) {
                    cp.appendClassPath(new LoaderClassPath(loader));
                }
                CtClass clazz = cp.makeClass(new ByteArrayInputStream(clazzBytes), false);

                Set<CtMethod> methdosToEnhance = new HashSet<CtMethod>();

                for (CtMethod method : clazz.getDeclaredMethods()) {
                        if (!isEnhanceable(method)) {
                                continue;
                        }
                        methdosToEnhance.add(method);
                }

                if (!methdosToEnhance.isEmpty()) {
                        CtClass eo = cp.get(EnhancedObject.class.getName());
                        for (CtClass i : clazz.getInterfaces()) {
                                if (i.getName().equals(eo.getName())) {
                                        // already enhanced
                                        clazz.detach();
                                        return null;
                                }
                        }

                        log.info("[JAVA17 FIX] Enhancing class: " + clazz.getName());
                        writeEnhancedObjectImpl(clazz);

                        for (CtMethod method : methdosToEnhance) {
                                log.debug("Enhancing method: " + method.getLongName());
                                enhanceMethod(method);
                        }

                        byte[] bytecode = clazz.toBytecode();
                        clazz.detach();
                        return bytecode;
                } else {
                        clazz.detach();
                        return null;
                }
        }

        protected void enhanceMethod(CtMethod method)
                        throws CannotCompileException, NotFoundException, ClassNotFoundException {
                ClassPool cp = method.getDeclaringClass().getClassPool();

                method.addLocalVariable("___cbr", cp.get(CallbackResult.class.getName()));

                CtClass listenerClazz = cp.get(((ObjectCallback) method.getAnnotation(ObjectCallback.class)).value().getName());

                String listenerFieldName = "$$$" + listenerClazz.getSimpleName();

                CtClass clazz = method.getDeclaringClass();
                try {
                        clazz.getField(listenerFieldName);
                } catch (NotFoundException e) {
                        clazz.addField(CtField.make(
                                        "Class " + listenerFieldName + " = Class.forName(\"" + listenerClazz.getName() + "\");", clazz));
                }

                int paramLength = method.getParameterTypes().length;

                method.insertBefore(writeBeforeMethod(method, paramLength, listenerFieldName));
                method.insertAfter(writeAfterMethod(method, paramLength, listenerFieldName));
        }

        protected String writeBeforeMethod(CtMethod method, int paramLength, String listenerFieldName)
                        throws NotFoundException {
                StringBuilder sb = new StringBuilder();
                sb.append('{');

                sb.append(" ___cbr = ");
                sb.append(ObjectCallbackHelper.class.getName()).append(".beforeCall((");
                sb.append(EnhancedObject.class.getName()).append(")this, " + listenerFieldName + ", ");
                if (paramLength > 0) {
                        sb.append("new Object[]{");
                        for (int i = 1; i <= paramLength; i++) {
                                sb.append("($w)$").append(i);
                                if (i < paramLength) {
                                        sb.append(',');
                                }
                        }
                        sb.append("}");
                } else {
                        sb.append("null");
                }
                sb.append(");");

                sb.append("if(___cbr.isBlockingCaller()){");

                CtClass returnType = method.getReturnType();
                if (returnType.equals(CtClass.voidType)) {
                        sb.append("return");
                } else if (returnType.equals(CtClass.booleanType)) {
                        sb.append("return false");
                } else if (returnType.equals(CtClass.charType)) {
                        sb.append("return 'a'");
                } else if (returnType.equals(CtClass.byteType) || returnType.equals(CtClass.shortType)
                                || returnType.equals(CtClass.intType) || returnType.equals(CtClass.floatType)
                                || returnType.equals(CtClass.longType) || returnType.equals(CtClass.longType)) {
                        sb.append("return 0");
                }
                sb.append(";}}");
                return sb.toString();
        }

        protected String writeAfterMethod(CtMethod method, int paramLength, String listenerFieldName)
                        throws NotFoundException {
                StringBuilder sb = new StringBuilder();
                sb.append('{');

                if (!method.getReturnType().equals(CtClass.voidType)) {
                        sb.append("if(___cbr.isBlockingCaller()){");
                        sb.append("$_ = ($r)($w)___cbr.getResult();");
                        sb.append("}");
                }

                sb.append("___cbr = ").append(ObjectCallbackHelper.class.getName()).append(".afterCall((");
                sb.append(EnhancedObject.class.getName()).append(")this, " + listenerFieldName + ", ");
                if (paramLength > 0) {
                        sb.append("new Object[]{");
                        for (int i = 1; i <= paramLength; i++) {
                                sb.append("($w)$").append(i);
                                if (i < paramLength) {
                                        sb.append(',');
                                }
                        }
                        sb.append("}");
                } else {
                        sb.append("null");
                }
                sb.append(", ($w)$_);");
                sb.append("if(___cbr.isBlockingCaller()){");
                if (method.getReturnType().equals(CtClass.voidType)) {
                        sb.append("return;");
                } else {
                        sb.append("return ($r)($w)___cbr.getResult();");
                }
                sb.append("}");
                sb.append("else {return $_;}");
                sb.append("}");
                return sb.toString();
        }

        protected void writeEnhancedObjectImpl(CtClass clazz) throws NotFoundException, CannotCompileException {
                ClassPool cp = clazz.getClassPool();
                clazz.addInterface(cp.get(EnhancedObject.class.getName()));
                writeEnhancedOBjectFields(clazz);
                writeEnhancedObjectMethods(clazz);
        }

        private void writeEnhancedOBjectFields(CtClass clazz) throws CannotCompileException, NotFoundException {
                ClassPool cp = clazz.getClassPool();

                CtField cbField = new CtField(cp.get(Map.class.getName()), FIELD_NAME_CALLBACKS, clazz);
                cbField.setModifiers(java.lang.reflect.Modifier.PRIVATE);
                clazz.addField(cbField, CtField.Initializer.byExpr("null;"));

                CtField cblField = new CtField(cp.get(ReentrantReadWriteLock.class.getName()), FIELD_NAME_CALLBACKS_LOCK, clazz);
                cblField.setModifiers(java.lang.reflect.Modifier.PRIVATE);
                clazz.addField(cblField, CtField.Initializer.byExpr("new " + ReentrantReadWriteLock.class.getName() + "();"));
        }

        private void writeEnhancedObjectMethods(CtClass clazz) throws NotFoundException, CannotCompileException {

                ClassPool cp = clazz.getClassPool();

                CtClass callbackClass = cp.get(Callback.class.getName());
                CtClass mapClass = cp.get(Map.class.getName());
                CtClass reentrantReadWriteLockClass = cp.get(ReentrantReadWriteLock.class.getName());

                CtMethod method = new CtMethod(CtClass.voidType, "addCallback", new CtClass[] { callbackClass }, clazz);
                method.setModifiers(java.lang.reflect.Modifier.PUBLIC);
                method.setBody("com.aionemu.commons.callbacks.util.ObjectCallbackHelper.addCallback($1, this);");
                clazz.addMethod(method);

                method = new CtMethod(CtClass.voidType, "removeCallback", new CtClass[] { callbackClass }, clazz);
                method.setModifiers(java.lang.reflect.Modifier.PUBLIC);
                method.setBody("com.aionemu.commons.callbacks.util.ObjectCallbackHelper.removeCallback($1, this);");
                clazz.addMethod(method);

                method = new CtMethod(mapClass, "getCallbacks", new CtClass[] {}, clazz);
                method.setModifiers(java.lang.reflect.Modifier.PUBLIC);
                method.setBody("return " + FIELD_NAME_CALLBACKS + ";");
                clazz.addMethod(method);

                method = new CtMethod(CtClass.voidType, "setCallbacks", new CtClass[] { mapClass }, clazz);
                method.setModifiers(java.lang.reflect.Modifier.PUBLIC);
                method.setBody("this." + FIELD_NAME_CALLBACKS + " = $1;");
                clazz.addMethod(method);

                method = new CtMethod(reentrantReadWriteLockClass, "getCallbackLock", new CtClass[] {}, clazz);
                method.setModifiers(java.lang.reflect.Modifier.PUBLIC);
                method.setBody("return " + FIELD_NAME_CALLBACKS_LOCK + ";");
                clazz.addMethod(method);
        }

        protected boolean isEnhanceable(CtMethod method) {
                int modifiers = method.getModifiers();
                return !(Modifier.isAbstract(modifiers) || Modifier.isNative(modifiers) || Modifier.isStatic(modifiers))
                                && CallbacksUtil.isAnnotationPresent(method, ObjectCallback.class);
        }
}
