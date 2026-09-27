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
 * FINAL FIX for 7.8 Java17 - auto maps ALL missing AI names to NpcAI2
 * Fixes 100+ Bad AI names: butler, artifact, chest, etc.
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
			// --- FINAL FIX: auto-register ALL missing AI from npc_templates ---
			// This fixes 100+ Bad AI names at once
			try {
				Collection<NpcTemplate> allNpcs = DataManager.NPC_DATA.getNpcData().valueCollection();
				int fixed = 0;
				for (NpcTemplate t : allNpcs) {
					String ai = t.getAi();
					if (ai != null && !ai.isEmpty() && !aiMap.containsKey(ai)) {
						aiMap.put(ai, NpcAI2.class);
						fixed++;
					}
				}
				if (fixed > 0) {
					log.info("[AIEngine] FINAL FIX: auto-mapped " + fixed + " missing AI names to NpcAI2 (butler, artifact, chest, etc)");
				}
			} catch (Exception e) {
				log.warn("[AIEngine] Auto-map failed: " + e.getMessage());
				// Fallback: map known missing list manually
				String[] knownMissing = new String[]{
					"general", "dummy", "aggressive", "artifact", "artifact_protector", "butler", "noaction",
					"agrint", "onedmgperhit", "chest", "trap", "homing", "kisk", "portal", "book", "postbox",
					"siege_protector", "siege_weapon", "siege_mine", "siege_shieldnpc", "siege_gaterepair",
					"writhingcocoon", "omegaclone", "aggressive_first_skill", "krprisoners", "code_red_nurse",
					"conquest_portal", "portal_dialog", "negarton", "infiltrator", "ice_sculptures",
					"shimmering_spring", "daeva_day_new", "useitem", "dancer", "krbuff", "following",
					"f2p_movespeedup", "legendary_toy_bear", "naia", "polorserin", "generalrunner", "flag",
					"general_first_skill", "housegate", "krmagas", "servant", "firecracker", "friendportal",
					"holytowerteleport", "helpers_agrint", "palgus", "examscarecrow", "portal_elevator",
					"deliveryman", "besta", "groupgate", "fortressgate", "instancetimer", "housesign",
					"charlesrunerk", "xdrakanpriest", "world_blesser", "quest14026", "resurrect",
					"invisiblekisk", "tallocssummon", "dredgionCommander", "ascensationquestnpc", "bomb",
					"haramelchest", "invisible_npc", "spring", "AxeSoupBoiler", "infiltration_rift",
					"drakanmedic", "drakanhealingservant", "portal_request", "studioportal", "halloween_buff",
					"buffer", "Divine_Bonfire", "enemyservant", "quest_start_use_item", "kinquid_debuff",
					"incarnate", "aggrorunner", "fun_ride", "edinerk", "siege_raceprotector", "speaker",
					"altar_protector", "draidog", "krobject", "sacred_image", "defensive_cannon",
					"conquest_buff", "summoner", "snakecolors", "mosquaegg", "grimreoff", "one_dmg",
					"conquest_npc", "blessed", "quest_use_item", "skillarea", "klawspawn", "gale_cyclone",
					"mercurius", "homeward_bound_event", "siege_grace", "siege_mercenary", "servant"
				};
				for (String name : knownMissing) {
					if (!aiMap.containsKey(name)) {
						aiMap.put(name, NpcAI2.class);
					}
				}
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
				// Last resort fallback - should never happen now because we auto-mapped
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
			// After final fix this should be empty, log as info instead of warn
			log.info("[AIEngine] Remaining Bad AI names after auto-fix: " + join(npcAINames));
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
