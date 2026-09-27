package com.aionemu.commons.callbacks.enhancer;

import java.io.ByteArrayInputStream;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.callbacks.CallbackResult;
import com.aionemu.commons.callbacks.metadata.GlobalCallback;
import com.aionemu.commons.callbacks.util.CallbacksUtil;
import com.aionemu.commons.callbacks.util.GlobalCallbackHelper;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.LoaderClassPath;
import javassist.Modifier;
import javassist.NotFoundException;

public class GlobalCallbackEnhancer extends CallbackClassFileTransformer {

        private static final Logger log = LoggerFactory.getLogger(GlobalCallbackEnhancer.class);

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
                        log.info("[JAVA17 FIX] Enhancing (Global) class: " + clazz.getName());
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

                method.addLocalVariable("___globalCallbackResult", cp.get(CallbackResult.class.getName()));

                CtClass listenerClazz = cp.get(((GlobalCallback) method.getAnnotation(GlobalCallback.class)).value().getName());

                boolean isStatic = Modifier.isStatic(method.getModifiers());
                String listenerFieldName = "$$$" + (isStatic ? "Static" : "") + listenerClazz.getSimpleName();

                CtClass clazz = method.getDeclaringClass();
                try {
                        clazz.getField(listenerFieldName);
                } catch (NotFoundException e) {
                        clazz.addField(CtField.make((isStatic ? "static " : "") + "Class " + listenerFieldName
                                        + " = Class.forName(\"" + listenerClazz.getName() + "\");", clazz));
                }

                int paramLength = method.getParameterTypes().length;

                method.insertBefore(writeBeforeMethod(method, paramLength, listenerFieldName));
                method.insertAfter(writeAfterMethod(method, paramLength, listenerFieldName));
        }

        protected String writeBeforeMethod(CtMethod method, int paramLength, String listenerFieldName)
                        throws NotFoundException {
                StringBuilder sb = new StringBuilder();
                sb.append('{');
                sb.append(" ___globalCallbackResult = ");
                sb.append(GlobalCallbackHelper.class.getName()).append(".beforeCall(");
                if (Modifier.isStatic(method.getModifiers())) {
                        sb.append(method.getDeclaringClass().getName()).append(".class, ").append(listenerFieldName).append(", ");
                } else {
                        sb.append("this, ").append(listenerFieldName).append(", ");
                }
                if (paramLength > 0) {
                        sb.append("new Object[]{");
                        for (int i = 1; i <= paramLength; i++) {
                                sb.append("($w)$").append(i);
                                if (i < paramLength) sb.append(',');
                        }
                        sb.append("}");
                } else {
                        sb.append("null");
                }
                sb.append(");");
                sb.append("if(___globalCallbackResult.isBlockingCaller()){");
                CtClass returnType = method.getReturnType();
                if (returnType.equals(CtClass.voidType)) sb.append("return");
                else if (returnType.equals(CtClass.booleanType)) sb.append("return false");
                else if (returnType.equals(CtClass.charType)) sb.append("return 'a'");
                else sb.append("return 0");
                sb.append(";}}");
                return sb.toString();
        }

        protected String writeAfterMethod(CtMethod method, int paramLength, String listenerFieldName)
                        throws NotFoundException {
                StringBuilder sb = new StringBuilder();
                sb.append('{');
                if (!method.getReturnType().equals(CtClass.voidType)) {
                        sb.append("if(___globalCallbackResult.isBlockingCaller()){");
                        sb.append("$_ = ($r)($w)___globalCallbackResult.getResult();");
                        sb.append("}");
                }
                sb.append("___globalCallbackResult = ").append(GlobalCallbackHelper.class.getName()).append(".afterCall(");
                if (Modifier.isStatic(method.getModifiers())) {
                        sb.append(method.getDeclaringClass().getName()).append(".class, ").append(listenerFieldName).append(", ");
                } else {
                        sb.append("this, ").append(listenerFieldName).append(", ");
                }
                if (paramLength > 0) {
                        sb.append("new Object[]{");
                        for (int i = 1; i <= paramLength; i++) {
                                sb.append("($w)$").append(i);
                                if (i < paramLength) sb.append(',');
                        }
                        sb.append("}");
                } else {
                        sb.append("null");
                }
                sb.append(", ($w)$_);");
                sb.append("if(___globalCallbackResult.isBlockingCaller()){");
                if (method.getReturnType().equals(CtClass.voidType)) sb.append("return;");
                else sb.append("return ($r)($w)___globalCallbackResult.getResult();");
                sb.append("}");
                sb.append("else {return $_;}");
                sb.append("}");
                return sb.toString();
        }

        protected boolean isEnhanceable(CtMethod method) {
                int modifiers = method.getModifiers();
                return !(Modifier.isAbstract(modifiers) || Modifier.isNative(modifiers))
                                && CallbacksUtil.isAnnotationPresent(method, GlobalCallback.class);
        }
}
