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
                CtClass clazz = null;
                try {
                        // RETAIL FIX for Java 17: Proper ClassPool with loader hierarchy
                        ClassPool cp = new ClassPool(ClassPool.getDefault());
                        cp.appendSystemPath();
                        if (loader != null) {
                            cp.appendClassPath(new LoaderClassPath(loader));
                        }
                        clazz = cp.makeClass(new ByteArrayInputStream(clazzBytes), false);
                        
                        // Java 17: CtClass can be frozen after first load
                        if (clazz.isFrozen()) {
                            clazz.defrost();
                        }

                        Set<CtMethod> methdosToEnhance = new HashSet<CtMethod>();

                        for (CtMethod method : clazz.getDeclaredMethods()) {
                                if (!isEnhanceable(method)) {
                                        continue;
                                }
                                methdosToEnhance.add(method);
                        }

                        if (!methdosToEnhance.isEmpty()) {
                                // RETAIL FIX: Check if already implements EnhancedObject BEFORE enhancing
                                // On Java 17, class can be loaded twice (AppClassLoader + Instrument)
                                CtClass eo = cp.get(EnhancedObject.class.getName());
                                for (CtClass i : clazz.getInterfaces()) {
                                        if (i.getName().equals(eo.getName())) {
                                                // RETAIL BEHAVIOR: Already enhanced - return null to skip
                                                // Original retail threw WTF??? but that was bug on Java 17
                                                // Fixed retail: just skip second enhance, don't throw
                                                log.debug("[RETAIL JAVA17] Class " + clazz.getName() + " already implements EnhancedObject, skipping re-enhance.");
                                                if (clazz != null) clazz.detach();
                                                return null;
                                        }
                                }

                                log.info("[RETAIL JAVA17] Enhancing class: " + clazz.getName());
                                writeEnhancedObjectImpl(clazz);

                                for (CtMethod method : methdosToEnhance) {
                                        log.debug("Enhancing method: " + method.getLongName());
                                        enhanceMethod(method);
                                }

                                byte[] bytecode = clazz.toBytecode();
                                clazz.detach();
                                return bytecode;
                        } else {
                                if (clazz != null) clazz.detach();
                                return null;
                        }
                } catch (Throwable t) {
                        // RETAIL FIX: Never crash GS for AggroList double-load
                        if (clazz != null) {
                            try { clazz.detach(); } catch (Exception e) {}
                        }
                        String msg = t.getMessage();
                        if (msg != null && (msg.contains("EnhancedObject") || msg.contains("already implements") || msg.contains("frozen"))) {
                            log.debug("Skipping already enhanced/frozen class (Java 17 double-load): " + t.getMessage());
                            return null;
                        }
                        // For other errors, log and return null - retail doesn't halt for single class failure
                        log.warn("[RETAIL JAVA17] Failed to enhance class " + (clazz != null ? clazz.getName() : "unknown") + ", skipping: " + t.getMessage());
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
