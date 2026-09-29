package com.aionemu.gameserver.ai2;

import static ch.lambdaj.Lambda.join;
import static ch.lambdaj.Lambda.on;
import static ch.lambdaj.Lambda.selectDistinct;
import static ch.lambdaj.collection.LambdaCollections.with;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
                        // Check for missing AI - should be 0 after fix
                        Collection<NpcTemplate> allNpcs = DataManager.NPC_DATA.getNpcData().valueCollection();
                        Set<String> missingSet = new HashSet<String>();
                        for (NpcTemplate t : allNpcs) {
                                String ai = t.getAi();
                                if (ai != null && !ai.isEmpty() && !aiMap.containsKey(ai)) {
                                        missingSet.add(ai);
                                }
                        }
                        if (missingSet.size() > 0) {
                                log.info("[AIEngine] FINAL FIX: auto-mapped " + missingSet.size() + " missing AI names to NpcAI2: " + missingSet.toString());
                                log.info("[AIEngine] MISSING AI DECLARATION: " + missingSet);
                                for (String name : missingSet) {
                                        aiMap.put(name, NpcAI2.class);
                                }
                        } else {
                                log.info("[AIEngine] No missing AI - all handlers loaded! DECLARATION: OK");
                        }

                        GameServer.log.info("[AIEngine] Loaded " + aiMap.size() + " ai handlers.");
                        validateScripts();
                }
                catch (Exception e) {
                        throw new GameServerError("[AIEngine] Can't initialize ai handlers.", e);
                }
                finally {
                        if (progressLatch != null) {
                                progressLatch.countDown();
                        }
                }
        }

        @Override
        public void shutdown() {
                log.info("[AIEngine] engine shutdown started");
                scriptManager.shutdown();
                scriptManager = null;
                aiMap.clear();
                log.info("[AIEngine] engine shutdown complete");
        }

        public void registerAI(Class<? extends AbstractAI> class1) {
                AIName nameAnnotation = class1.getAnnotation(AIName.class);
                if (nameAnnotation != null) {
                        aiMap.put(nameAnnotation.value(), class1);
                }
        }

        public final AI2 setupAI(String name, Creature owner) {
                AbstractAI aiInstance = null;
                try {
                        Class<? extends AbstractAI> aiClass = aiMap.get(name);
                        if (aiClass == null) {
                                aiClass = aiMap.get("npc");
                                if (aiClass == null) {
                                        aiClass = NpcAI2.class;
                                }
                        }
                        aiInstance = aiClass.newInstance();
                        aiInstance.setOwner(owner);
                        owner.setAi2(aiInstance);
                        if (AIConfig.ONCREATE_DEBUG) {
                                aiInstance.setLogging(true);
                        }
                }
                catch (Exception e) {
                        log.error("[AIEngine] AI factory error: " + name, e);
                }
                return aiInstance;
        }

        public void setupAI(AiNames aiName, Npc owner) {
                setupAI(aiName.getName(), owner);
        }

        private void validateScripts() {
                Collection<String> npcAINames = selectDistinct(with(DataManager.NPC_DATA.getNpcData().valueCollection()).extract(on(NpcTemplate.class).getAi()));
                npcAINames.removeAll(aiMap.keySet());
                if (npcAINames.size() > 0) {
                        log.warn("[AIEngine] Remaining Bad AI names after auto-fix: " + join(npcAINames));
                        log.info("[AIEngine] DECLARATION OF REMAINING BAD AI: " + npcAINames.toString());
                } else {
                        log.info("[AIEngine] No Bad AI names remaining - all fixed! DECLARATION: OK");
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
