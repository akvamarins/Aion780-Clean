/**
 * This file is part of Aion-Lightning <aion-lightning.org>.
 *
 *  Aion-Lightning is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Aion-Lightning is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details. *
 *  You should have received a copy of the GNU General Public License
 *  along with Aion-Lightning.
 *  If not, see <http://www.gnu.org/licenses/>.
 */

package com.aionemu.commons.callbacks.enhancer;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.ExitCode;

/**
 * Basic class that checks if class can be transformed. JDK classes are not a
 * subject of transformation.
 * 
 * FIXED for Java 17 - Retail-like behavior:
 * - Java 17 has new ClassLoader names (AppClassLoader is jdk.internal.loader.ClassLoaders$AppClassLoader)
 * - Don't halt on already-enhanced classes (AggroList double-load fix)
 * - Skip JDK modules (java/, jdk/, sun/, etc.)
 *
 * @author SoulKeeper - fixed for Java 17 retail
 */
public abstract class CallbackClassFileTransformer implements ClassFileTransformer {

        private static final Logger log = LoggerFactory.getLogger(CallbackClassFileTransformer.class);

        @Override
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                        ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
                try {
                        // RETAIL FIX for Java 17: skip bootstrap, platform and JDK classes
                        // On Java 8: ExtClassLoader = sun.misc.Launcher$ExtClassLoader
                        // On Java 17: PlatformClassLoader and BootLoader, plus module system
                        if (className == null) {
                            return null;
                        }
                        
                        // Skip all JDK / internal classes - retail behavior
                        if (className.startsWith("java/") || className.startsWith("jdk/") || 
                            className.startsWith("sun/") || className.startsWith("com/sun/") ||
                            className.startsWith("org/ietf/") || className.startsWith("org/jcp/") ||
                            className.startsWith("org/w3c/") || className.startsWith("org/xml/")) {
                                log.trace("Class " + className + " ignored (JDK).");
                                return null;
                        }

                        // Skip bootstrap loader (null loader = bootstrap)
                        if (loader == null) {
                                log.trace("Class " + className + " ignored (bootstrap).");
                                return null;
                        }
                        
                        String loaderName = loader.getClass().getName();
                        // Java 8: sun.misc.Launcher$ExtClassLoader
                        // Java 17: jdk.internal.loader.ClassLoaders$PlatformClassLoader, BootClassLoader
                        if (loaderName.contains("ExtClassLoader") || 
                            loaderName.contains("PlatformClassLoader") ||
                            loaderName.contains("BootClassLoader") ||
                            loaderName.contains("BuiltinClassLoader")) {
                                log.trace("Class " + className + " ignored (platform/ext).");
                                return null;
                        }

                        // actual class transformation - retail logic
                        return transformClass(loader, classfileBuffer);
                } catch (Throwable e) {
                        String msg = e.getMessage();
                        // RETAIL FIX: AggroList double-enhance on Java 17 is normal, don't halt
                        // Original threw "Class already implements EnhancedObject interface, WTF???"
                        // Retail: just skip second transformation
                        if (msg != null && (msg.contains("EnhancedObject") || msg.contains("already implements"))) {
                            log.debug("Class " + className + " already enhanced, skipping second transform (Java 17 double-load).");
                            return null;
                        }

                        Error e1 = new Error("Can't transform class " + className, e);
                        log.error(e1.getMessage(), e);

                        // RETAIL FIX: Only halt for core AppClassLoader classes that are NOT AggroList/callback related
                        // Don't halt for callback enhancer failures - retail continues
                        if (loader != null) {
                            String lName = loader.getClass().getName();
                            boolean isAppLoader = lName.contains("AppClassLoader") || lName.equals("jdk.internal.loader.ClassLoaders$AppClassLoader");
                            if (isAppLoader) {
                                // Don't halt for AggroList or EnhancedObject issues - this is Java 17 retransform
                                if (className != null && (className.contains("AggroList") || className.contains("EnhancedObject"))) {
                                    log.warn("Skipping halt for " + className + " - Java 17 retransform issue, returning null (retail behavior).");
                                    return null;
                                }
                                log.error("Halting for core class transform failure: " + className);
                                Runtime.getRuntime().halt(ExitCode.CODE_ERROR);
                            }
                        }

                        throw e1;
                }
        }

        protected abstract byte[] transformClass(ClassLoader loader, byte[] clazzBytes) throws Exception;
}
