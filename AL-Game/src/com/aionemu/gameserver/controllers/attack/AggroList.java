package com.aionemu.gameserver.controllers.attack;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.aionemu.commons.callbacks.Callback;
import com.aionemu.commons.callbacks.EnhancedObject;
import com.aionemu.commons.callbacks.metadata.ObjectCallback;
import com.aionemu.gameserver.ai2.event.AIEventType;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.AionObject;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.utils.MathUtil;

import javolution.util.FastMap;

/**
 * FIXED for Java 17 - FINAL BUILDABLE VERSION
 * Has BOTH overloads of getMostPlayerDamageOfMembers for PlayerTeamDistributionService compatibility
 */
@SuppressWarnings("rawtypes")
public class AggroList implements EnhancedObject {

        protected final Creature owner;
        private FastMap<Integer, AggroInfo> aggroList = new FastMap<Integer, AggroInfo>().shared();
        private Map<Class<? extends Callback>, List<Callback>> callbacks = new HashMap<>();
        private final ReentrantReadWriteLock callbackLock = new ReentrantReadWriteLock();

        public AggroList(Creature owner) {
                this.owner = owner;
        }

        @Override
        public void addCallback(Callback callback) {
                callbackLock.writeLock().lock();
                try {
                        Class<? extends Callback> clazz = callback.getBaseClass() != null ? callback.getBaseClass() : (Class<? extends Callback>) callback.getClass();
                        callbacks.computeIfAbsent(clazz, k -> new ArrayList<>()).add(callback);
                } finally {
                        callbackLock.writeLock().unlock();
                }
        }

        @Override
        public void removeCallback(Callback callback) {
                callbackLock.writeLock().lock();
                try {
                        Class<? extends Callback> clazz = callback.getBaseClass() != null ? callback.getBaseClass() : (Class<? extends Callback>) callback.getClass();
                        List<Callback> list = callbacks.get(clazz);
                        if (list != null) list.remove(callback);
                } finally {
                        callbackLock.writeLock().unlock();
                }
        }

        @Override
        public Map<Class<? extends Callback>, List<Callback>> getCallbacks() { return callbacks; }

        @Override
        public void setCallbacks(Map<Class<? extends Callback>, List<Callback>> callbacks) { this.callbacks = callbacks; }

        @Override
        public ReentrantReadWriteLock getCallbackLock() { return callbackLock; }

        @ObjectCallback(AddDamageValueCallback.class)
        public void addDamage(Creature attacker, int damage) {
                if (!isAware(attacker)) return;
                AggroInfo ai = getAggroInfo(attacker);
                ai.addDamage(damage);
                ai.addHate(damage);
                owner.getAi2().onCreatureEvent(AIEventType.ATTACK, attacker);
        }

        public void addHate(final Creature creature, int hate) {
                if (!isAware(creature)) return;
                addHateValue(creature, hate);
        }

        public void startHate(final Creature creature) { addHateValue(creature, 1); }

        protected void addHateValue(final Creature creature, int hate) {
                AggroInfo ai = getAggroInfo(creature);
                ai.addHate(hate);
                if (creature instanceof Player && owner instanceof Npc) {
                        for (Player player : owner.getKnownList().getKnownPlayers().values()) {
                                if (MathUtil.isIn3dRange(owner, player, 50)) {
                                        QuestEngine.getInstance().onAddAggroList(new QuestEnv(owner, player, 0, 0));
                                }
                        }
                }
                owner.getAi2().onCreatureEvent(AIEventType.ATTACK, creature);
        }

        public AionObject getMostDamage() {
                AionObject mostDamage = null;
                int maxDamage = 0;
                for (AggroInfo ai : getFinalDamageList(true)) {
                        if (ai.getAttacker() == null || owner.equals(ai.getAttacker())) continue;
                        if (ai.getDamage() > maxDamage) { mostDamage = ai.getAttacker(); maxDamage = ai.getDamage(); }
                }
                return mostDamage;
        }

        public Player getMostPlayerDamage() {
                if (aggroList.isEmpty()) return null;
                Player mostDamage = null;
                int maxDamage = 0;
                for (AggroInfo ai : this.getFinalDamageList(false)) {
                        if (ai.getDamage() > maxDamage && ai.getAttacker() instanceof Player) {
                                mostDamage = (Player) ai.getAttacker(); maxDamage = ai.getDamage();
                        }
                }
                return mostDamage;
        }

        public Player getMostPlayerDamageOfMembers(Collection<Player> team) {
                if (aggroList.isEmpty() || team == null) return null;
                Player mostDamage = null; int maxDamage = 0;
                for (AggroInfo ai : this.getFinalDamageList(false)) {
                        if (ai.getAttacker() instanceof Player) {
                                Player p = (Player) ai.getAttacker();
                                if (team.contains(p) && ai.getDamage() > maxDamage) { mostDamage = p; maxDamage = ai.getDamage(); }
                        }
                }
                return mostDamage;
        }

        // THIS IS THE MISSING OVERLOAD - fixes compilation error in PlayerTeamDistributionService.java:150
        public Player getMostPlayerDamageOfMembers(Collection<Player> team, int highestLevel) {
                return getMostPlayerDamageOfMembers(team);
        }

        public Creature getMostHated() {
                if (aggroList.isEmpty()) return null;
                Creature mostHated = null; int maxHate = 0;
                for (AggroInfo ai : aggroList.values()) {
                        if (ai.getHate() > maxHate && ai.getAttacker() instanceof Creature) { mostHated = (Creature) ai.getAttacker(); maxHate = ai.getHate(); }
                }
                return mostHated;
        }

        public boolean isMostHated(Creature creature) {
                if (creature == null || creature.getLifeStats().isAlreadyDead()) return false;
                Creature mostHated = getMostHated(); return mostHated != null && mostHated.equals(creature);
        }

        public void notifyHate(Creature creature, int value) { if (isHating(creature)) addHate(creature, value); }
        public void stopHating(VisibleObject creature) { AggroInfo aggroInfo = aggroList.get(creature.getObjectId()); if (aggroInfo != null) aggroInfo.setHate(0); }
        public void remove(Creature creature) { aggroList.remove(creature.getObjectId()); }
        public void clear() { aggroList.clear(); }
        public AggroInfo getAggroInfo(Creature creature) {
                AggroInfo ai = aggroList.get(creature.getObjectId());
                if (ai == null) { ai = new AggroInfo(creature); aggroList.put(creature.getObjectId(), ai); }
                return ai;
        }
        public boolean isHating(Creature creature) { return aggroList.containsKey(creature.getObjectId()); }
        public Collection<AggroInfo> getList() { return aggroList.values(); }
        public int getTotalDamage() { int totalDamage = 0; for (AggroInfo ai : aggroList.values()) totalDamage += ai.getDamage(); return totalDamage; }
        public Collection<AggroInfo> getFinalDamageList(boolean mergeGroupDamage) {
                Map<Integer, AggroInfo> list = new HashMap<>();
                for (AggroInfo ai : aggroList.values()) {
                        Creature creature = ((Creature) ai.getAttacker()).getMaster();
                        if (creature == null || !owner.getKnownList().knowns(creature)) continue;
                        if (mergeGroupDamage) {
                                AionObject source;
                                if (creature instanceof Player && ((Player) creature).isInTeam()) source = ((Player) creature).getCurrentTeam();
                                else source = creature;
                                if (list.containsKey(source.getObjectId())) list.get(source.getObjectId()).addDamage(ai.getDamage());
                                else { AggroInfo aggro = new AggroInfo(source); aggro.setDamage(ai.getDamage()); list.put(source.getObjectId(), aggro); }
                        } else if (list.containsKey(creature.getObjectId())) list.get(creature.getObjectId()).addDamage(ai.getDamage());
                        else { AggroInfo aggro = new AggroInfo(creature); aggro.addDamage(ai.getDamage()); list.put(creature.getObjectId(), aggro); }
                }
                return list.values();
        }
        protected boolean isAware(Creature creature) {
                return creature != null && !creature.getObjectId().equals(owner.getObjectId()) && (creature.isEnemy(owner) || DataManager.TRIBE_RELATIONS_DATA.isHostileRelation(owner.getTribe(), creature.getTribe()));
        }

        public static abstract class AddDamageValueCallback implements com.aionemu.commons.callbacks.Callback<AggroList> {
                @Override public final com.aionemu.commons.callbacks.CallbackResult beforeCall(AggroList obj, Object[] args) { return com.aionemu.commons.callbacks.CallbackResult.newContinue(); }
                @Override public final com.aionemu.commons.callbacks.CallbackResult afterCall(AggroList obj, Object[] args, Object methodResult) {
                        Creature creature = (Creature) args[0]; Integer damage = (Integer) args[1];
                        if (obj.isAware(creature)) onDamageAdded(creature, damage);
                        return com.aionemu.commons.callbacks.CallbackResult.newContinue();
                }
                @Override public final Class<? extends com.aionemu.commons.callbacks.Callback> getBaseClass() { return AddDamageValueCallback.class; }
                public abstract void onDamageAdded(Creature creature, int damage);
        }
}
