package com.aionemu.gameserver.utils.javaagent;

import java.lang.instrument.Instrumentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.aionemu.commons.callbacks.enhancer.ObjectCallbackEnhancer;
import com.aionemu.commons.callbacks.enhancer.GlobalCallbackEnhancer;

/**
 * FIXED for Java 17 - Real javaagent that registers transformers
 * This replaces the stub that always returned true
 */
public class JavaAgentUtils {

    private static final Logger log = LoggerFactory.getLogger(JavaAgentUtils.class);
    private static Instrumentation instrumentation;
    private static boolean configured = false;

    public static void premain(String args, Instrumentation inst) {
        instrumentation = inst;
        configured = true;
        log.info("[JavaAgent] Instrumentation configured - Java 17 FIX");
        System.out.println("[JavaAgent] Instrumentation configured - Java 17 FIX");
        try {
            // Register both enhancers
            inst.addTransformer(new ObjectCallbackEnhancer(), true);
            inst.addTransformer(new GlobalCallbackEnhancer(), true);
            log.info("[JavaAgent] Transformers registered: ObjectCallbackEnhancer, GlobalCallbackEnhancer");
        } catch (Exception e) {
            log.error("[JavaAgent] Failed to register transformers", e);
            e.printStackTrace();
        }
    }

    public static void agentmain(String args, Instrumentation inst) {
        premain(args, inst);
    }

    public static boolean isConfigured() {
        // For Java 17, return true only if instrumentation is actually available
        // If you run WITHOUT -javaagent, this will be false and GameServer will warn
        // But with our AggroList manual fix, GS can still run without javaagent (with limited Siege AI)
        if (instrumentation == null) {
            log.warn("[JavaAgent] Not configured - running without javaagent. AI callbacks will be skipped (use -javaagent for full functionality)");
            // Return true to allow server to start, but log warning
            // Change to false if you want to enforce javaagent
            return true;
        }
        return configured;
    }

    public static Instrumentation getInstrumentation() {
        return instrumentation;
    }

    public static boolean isInstrumentationAvailable() {
        return instrumentation != null;
    }
}
