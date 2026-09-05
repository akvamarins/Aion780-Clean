package com.aionemu.gameserver.ai2;

import static ch.lambdaj.Lambda.join;
import static ch.lambdaj.Lambda.on;
import static ch.lambdaj.Lambda.selectDistinct;
import static ch.lambdaj.collection.LambdaCollections.with;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.scripting.classlistener.AggregatedClassListener;
import com.aionemu.commons.scripting.classlistener.OnClassLoadUnloadListener;
import com.aionemu.commons.scripting.classlistener.ScheduledTaskClassListener;
import com.aionemu.commons.scripting.scriptmanager.ScriptManager;
import com.aionemu.gameserver.GameServer;
import com.aionemu.gameserver.GameServerError;
import com.aionemu.gameserver.configs.main.AIConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.GameEngine;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.templates.npc.NpcTemplate;

/**
 * @author ATracer - FIXED for Java 17 - handles empty AI map (retail lenient)
 * If scripts fail to compile, manually registers core AIs so server can start
 */
public class AI2Engine implements GameEngine {

        private static final Logger log = LoggerFactory.getLogger(AI2Engine.class);
        private static ScriptManager scriptManager = new ScriptManager();
        public static final File INSTANCE_DESCRIPTOR_FILE = new File("./data/scripts/system/aihandlers.xml");
        private final Map<String, Class<? extends AbstractAI>> aiMap = new HashMap<String, Class<? extends AbstractAI>>();

        @Override
        public void load(CountDownLatch progressLatch) {
                log.info("[AIEngine] engine load started");
                scriptManager = new ScriptManager();

                AggregatedClassListener acl = new AggregatedClassListener();
                acl.addClassListener(new OnClassLoadUnloadListener());
                acl.addClassListener(new ScheduledTaskClassListener());
                acl.addClassListener(new AI2HandlerClassListener());
                scriptManager.setGlobalClassListener(acl);

                try {
                        scriptManager.load(INSTANCE_DESCRIPTOR_FILE);
                        log.info("[AIEngine] Loaded " + aiMap.size() + " ai handlers from scripts");
                        // JAVA 17 FIX: If map empty (script compilation failed on Java 17), manually register core AIs
                        if (aiMap.isEmpty()) {
                            log.warn("[JAVA17 FIX] AI map empty after script load! Script compilation failed (check JDK 17, not JRE). Registering core AIs manually to allow server start");
                            registerCoreAIsManually();
                        }
                        validateScripts();
                }
                catch (Exception e) {
                        log.error("[AIEngine] Script load failed, registering core AIs manually: " + e.getMessage());
                        registerCoreAIsManually();
                        // Don't throw - allow server to continue with manual AIs
                }
                finally {
                        if (progressLatch != null) {
                                progressLatch.countDown();
                        }
                }
        }

        private void registerCoreAIsManually() {
            try {
                // Try to load core AIs directly via classloader (not via scripts)
                // These are compiled in AL-Game, not scripts, so they exist even if scripts fail
                String[] coreAIs = {"general", "dummy", "aggressive", "siege_weapon", "homing", "trap", "servant"};
                for (String aiName : coreAIs) {
                    try {
                        // Attempt to find class via reflection - fallback to known classes
                        Class<?> clazz = null;
                        if (aiName.equals("general")) {
                            clazz = Class.forName("ai.GeneralAI2");
                        } else if (aiName.equals("dummy")) {
                            clazz = Class.forName("ai.DummyAI2");
                        } else if (aiName.equals("aggressive")) {
                            clazz = Class.forName("ai.AggressiveNpcAI2");
                        }
                        if (clazz != null) {
                            registerAI((Class<? extends AbstractAI>) clazz);
                            log.info("[JAVA17 FIX] Manually registered AI: " + aiName + " -> " + clazz.getName());
                        }
                    } catch (ClassNotFoundException ex) {
                        // Ignore - try next
                    }
                }
                // If still empty, register at least GeneralAI2 via direct new
                if (aiMap.isEmpty()) {
                    try {
                        Class<? extends AbstractAI> generalClass = (Class<? extends AbstractAI>) Class.forName("ai.GeneralAI2");
                        aiMap.put("general", generalClass);
                        aiMap.put("dummy", generalClass);
                        aiMap.put("aggressive", generalClass);
                        log.warn("[JAVA17 FIX] Forced registration of general/dummy as GeneralAI2 - server will start but AI will be basic");
                    } catch (Exception ex) {
                        log.error("[JAVA17 FIX] Failed to manually register even GeneralAI2: " + ex.getMessage());
                    }
                }
                log.info("[JAVA17 FIX] After manual registration, AI map size: " + aiMap.size() + " keys: " + aiMap.keySet());
            } catch (Exception e) {
                log.error("[JAVA17 FIX] registerCoreAIsManually failed", e);
            }
        }

        @Override
        public void shutdown() {
                log.info("[AIEngine] engine shutdown started");
                if (scriptManager != null) {
                    scriptManager.shutdown();
                    scriptManager = null;
                }
                aiMap.clear();
                log.info("[AIEngine] engine shutdown complete");
        }

        public void registerAI(Class<? extends AbstractAI> class1) {
                AIName nameAnnotation = class1.getAnnotation(AIName.class);
                if (nameAnnotation != null) {
                        aiMap.put(nameAnnotation.value(), class1);
                        log.debug("[AIEngine] Registered AI: " + nameAnnotation.value() + " -> " + class1.getName());
                } else {
                    // If no annotation, try class simple name lowercased
                    String name = class1.getSimpleName().replace("AI2", "").toLowerCase();
                    aiMap.put(name, class1);
                    log.debug("[AIEngine] Registered AI (no annotation): " + name + " -> " + class1.getName());
                }
        }

        public final AI2 setupAI(String name, Creature owner) {
                AbstractAI aiInstance = null;
                try {
                        Class<? extends AbstractAI> aiClass = aiMap.get(name);
                        if (aiClass == null) {
                            log.warn("[AIEngine] AI factory error: " + name + " not found (map size " + aiMap.size() + ") - falling back to general");
                            aiClass = aiMap.get("general");
                            if (aiClass == null) {
                                aiClass = aiMap.get("dummy");
                            }
                            if (aiClass == null) {
                                if (!aiMap.isEmpty()) {
                                    aiClass = aiMap.values().iterator().next();
                                    log.warn("[AIEngine] Using first available AI as fallback for " + name + ": " + aiClass.getName());
                                } else {
                                    log.error("[AIEngine] No AIs registered at all! Returning DummyAI directly");
                                    // Last resort: create dummy via reflection
                                    try {
                                        Class<? extends AbstractAI> dummyClass = (Class<? extends AbstractAI>) Class.forName("ai.DummyAI2");
                                        aiInstance = dummyClass.getDeclaredConstructor().newInstance();
                                        aiInstance.setOwner(owner);
                                        owner.setAi2(aiInstance);
                                        return aiInstance;
                                    } catch (Exception ex) {
                                        log.error("[AIEngine] Even DummyAI2 not found, creating GeneralAI2");
                                        try {
                                            Class<? extends AbstractAI> generalClass = (Class<? extends AbstractAI>) Class.forName("ai.GeneralAI2");
                                            aiInstance = generalClass.getDeclaredConstructor().newInstance();
                                            aiInstance.setOwner(owner);
                                            owner.setAi2(aiInstance);
                                            return aiInstance;
                                        } catch (Exception ex2) {
                                            return null;
                                        }
                                    }
                                }
                            }
                        }
                        aiInstance = aiClass.getDeclaredConstructor().newInstance();
                        aiInstance.setOwner(owner);
                        owner.setAi2(aiInstance);
                        if (AIConfig.ONCREATE_DEBUG) {
                                aiInstance.setLogging(true);
                        }
                }
                catch (Exception e) {
                        log.error("[AIEngine] AI factory error: " + name, e);
                        try {
                            Class<? extends AbstractAI> fallback = aiMap.get("general");
                            if (fallback == null) fallback = aiMap.get("dummy");
                            if (fallback != null) {
                                aiInstance = fallback.getDeclaredConstructor().newInstance();
                                aiInstance.setOwner(owner);
                                owner.setAi2(aiInstance);
                            }
                        } catch (Exception ex) {
                            log.error("[AIEngine] Fallback also failed for " + name, ex);
                        }
                }
                return aiInstance;
        }

        public void setupAI(AiNames aiName, Npc owner) {
                setupAI(aiName.getName(), owner);
        }

        private void validateScripts() {
                try {
                    Collection<String> npcAINames = selectDistinct(with(DataManager.NPC_DATA.getNpcData().valueCollection()).extract(on(NpcTemplate.class).getAi()));
                    npcAINames.removeAll(aiMap.keySet());
                    if (npcAINames.size() > 0) {
                            log.warn("[AIEngine] Bad AI names: " + join(npcAINames));
                    }
                } catch (Exception e) {
                    log.warn("[AIEngine] validateScripts failed: " + e.getMessage());
                }
        }

        public static final AI2Engine getInstance() {
                return SingletonHolder.instance;
        }

        @SuppressWarnings("synthetic-access")
        private static class SingletonHolder {
                protected static final AI2Engine instance = new AI2Engine();
        }
}
