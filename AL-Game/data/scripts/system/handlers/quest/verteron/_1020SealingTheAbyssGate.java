package quest.verteron;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAY_MOVIE;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.services.instance.InstanceService;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author Atomics (aion-unique), adapted for Aion780
 */
public class _1020SealingTheAbyssGate extends QuestHandler {

    private final static int questId = 1020;
    private final static int KARAMATIS = 310030000;

    public _1020SealingTheAbyssGate() {
        super(questId);
    }

    @Override
    public void register() {
        qe.registerOnEnterWorld(questId);
        qe.registerOnLevelUp(questId);
        qe.registerOnDie(questId);
        qe.registerQuestNpc(203098).addOnTalkEvent(questId); // Spiros
        qe.registerQuestNpc(700141).addOnTalkEvent(questId); // Verteron Abyss Gate
        qe.registerQuestNpc(700142).addOnTalkEvent(questId); // Seal of the Abyss Gate
        qe.registerQuestNpc(700551).addOnTalkEvent(questId); // Abyss Gate (inside)
    }

    private boolean isCampaignComplete(Player player) {
        int[] quests = { 1130, 1023, 1022, 1021, 1019, 1018, 1017, 1016, 1015, 1014, 1013, 1012, 1011 };
        for (int id : quests) {
            QuestState qs2 = player.getQuestStateList().getQuestState(id);
            if (qs2 == null || qs2.getStatus() != QuestStatus.COMPLETE) return false;
        }
        return true;
    }

    @Override
    public boolean onLvlUpEvent(QuestEnv env) {
        Player player = env.getPlayer();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        if (qs == null || qs.getStatus() != QuestStatus.LOCKED || player.getLevel() < 15) return false;
        if (!isCampaignComplete(player)) return false;
        qs.setStatus(QuestStatus.START);
        updateQuestStatus(env);
        return true;
    }


    @Override
    public boolean onDialogEvent(QuestEnv env) {
        Player player = env.getPlayer();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        if (qs == null) return false;
        final int instanceId = player.getInstanceId();
        final int var = qs.getQuestVarById(0);
        int targetId = env.getTargetId();
        DialogAction dialog = env.getDialog();

        if (qs.getStatus() == QuestStatus.REWARD) {
            if (targetId == 203098) {
                return sendQuestEndDialog(env);
            }
            return false;
        } else if (qs.getStatus() != QuestStatus.START) {
            return false;
        }

        switch (targetId) {
            case 203098: // Spiros
                if (dialog == DialogAction.QUEST_SELECT && var == 0) {
                    return sendQuestDialog(env, 1011);
                } else if (dialog == DialogAction.SETPRO1 && var == 0) {
                    return defaultCloseDialog(env, 0, 1);
                }
                return false;
            case 700141: // Verteron Abyss Gate
                if (var == 1) {
                    final int targetObjectId = env.getVisibleObject().getObjectId();
                    PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.EMOTE, 0, targetObjectId), true);
                    ThreadPoolManager.getInstance().schedule(new Runnable() {
                        @Override
                        public void run() {
                            WorldMapInstance newInstance = InstanceService.getNextAvailableInstance(KARAMATIS);
                            InstanceService.registerPlayerWithInstance(newInstance, player);
                            TeleportService2.teleportTo(player, KARAMATIS, newInstance.getInstanceId(), 270.5f, 174.3f, 204.3f);
                            qs.setQuestVarById(0, 2);
                            updateQuestStatus(env);
                        }
                    }, 3000);
                    return true;
                } else if (var == 3) {
                    if (player.getInventory().getItemCountByItemId(182200024) >= 1) {
                        final int targetObjectId = env.getVisibleObject().getObjectId();
                        PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.EMOTE, 0, targetObjectId), true);
                        ThreadPoolManager.getInstance().schedule(new Runnable() {
                            @Override
                            public void run() {
                                TeleportService2.teleportTo(player, 210030000, 2684.308f, 1068.7382f, 199.375f, (byte) 0, TeleportAnimation.BEAM_ANIMATION);
                                qs.setQuestVarById(0, 4);
                                qs.setStatus(QuestStatus.REWARD);
                                updateQuestStatus(env);
                            }
                        }, 3000);
                        return true;
                    }
                }
            case 700551: // Abyss Gate (inside)
                if (var == 2) {
                    if (player.getInventory().getItemCountByItemId(182200024) >= 1) {
                        final int targetObjectId = env.getVisibleObject().getObjectId();
                        PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.EMOTE, 0, targetObjectId), true);
                        ThreadPoolManager.getInstance().schedule(new Runnable() {
                            @Override
                            public void run() {
                                PacketSendUtility.sendPacket(player, new SM_PLAY_MOVIE(0, 153));
                                qs.setQuestVarById(0, 3);
                                updateQuestStatus(env);
                            }
                        }, 3000);
                        return true;
                    }
                }
                return false;
            case 700142: // Seal of the Abyss Gate
                if (var == 2) {
                    final int targetObjectId = env.getVisibleObject().getObjectId();
                    PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.EMOTE, 0, targetObjectId), true);
                    ThreadPoolManager.getInstance().schedule(new Runnable() {
                        @Override
                        public void run() {
                            PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.EMOTE, 0, targetObjectId), true);
                            QuestService.addNewSpawn(KARAMATIS, instanceId, 210753, 258.89917f, 237.20166f, 217.06035f, (byte) 0, 30);
                        }
                    }, 3000);
                    return true;
                }
                return false;
            default:
                return false;
        }
    }


    @Override
    public boolean onDieEvent(QuestEnv env) {
        Player player = env.getPlayer();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        if (qs == null || qs.getStatus() != QuestStatus.START) return false;
        int var = qs.getQuestVarById(0);
        if (var == 2 || var == 3) {
            qs.setQuestVar(1);
            removeQuestItem(env, 182200024, 1);
            updateQuestStatus(env);
        }
        return false;
    }

    @Override
    public boolean onEnterWorldEvent(QuestEnv env) {
        Player player = env.getPlayer();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        if (qs == null) return false;

        if (qs.getStatus() == QuestStatus.START) {
            int var = qs.getQuestVarById(0);
            if ((var == 2 || var == 3) && player.getWorldId() != KARAMATIS) {
                qs.setQuestVar(1);
                removeQuestItem(env, 182200024, 1);
                updateQuestStatus(env);
            }
        } else if (qs.getStatus() == QuestStatus.LOCKED && player.getLevel() > 15) {
            if (!isCampaignComplete(player)) return false;
            qs.setStatus(QuestStatus.START);
            updateQuestStatus(env);
            return true;
        }
        return false;
    }
}
