package quest.theobomos;

import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAY_MOVIE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_USE_OBJECT;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Hellboy Aion4Free, adapted for Aion780
 */
public class _1092JosnacksDilemma extends QuestHandler {

    private final static int questId = 1092;
    private final static int[] npc_ids = { 798155, 798206, 700389, 700388 };

    public _1092JosnacksDilemma() {
        super(questId);
    }

    @Override
    public void register() {
        qe.registerOnLevelUp(questId);
        for (int npc_id : npc_ids)
            qe.registerQuestNpc(npc_id).addOnTalkEvent(questId);
    }

    @Override
    public boolean onLvlUpEvent(QuestEnv env) {
        return defaultOnLvlUpEvent(env);
    }

    @Override
    public boolean onDialogEvent(final QuestEnv env) {
        final Player player = env.getPlayer();
        final QuestState qs = player.getQuestStateList().getQuestState(questId);
        if (qs == null)
            return false;

        int var = qs.getQuestVarById(0);
        int targetId = 0;
        if (env.getVisibleObject() instanceof Npc)
            targetId = ((Npc) env.getVisibleObject()).getNpcId();

        if (qs.getStatus() == QuestStatus.REWARD) {
            if (targetId == 798155) {
                if (env.getDialogId() == 23) { // SELECTED_QUEST_NOREWARD -> unlock 1093 (client: finished_quest_cond1=Q1092)
                    QuestService.startQuest(new QuestEnv(env.getVisibleObject(), env.getPlayer(), 1093, env.getDialogId()), QuestStatus.LOCKED);
                }
                return sendQuestEndDialog(env);
            }
            return false;
        } else if (qs.getStatus() != QuestStatus.START) {
            return false;
        }
        if (targetId == 798155) {
            switch (env.getDialogId()) {
                case 31: // QUEST_SELECT
                    if (var == 0)
                        return sendQuestDialog(env, 1011);
                    else if (var == 3)
                        return sendQuestDialog(env, 2034);
                    else if (var == 4)
                        return sendQuestDialog(env, 2375);
                    break;
                case 10000: // SETPRO1
                    if (var == 0) {
                        qs.setQuestVarById(0, var + 1);
                        updateQuestStatus(env);
                        PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
                        return true;
                    }
                    break;
                case 10003: // SETPRO4
                    if (var == 3) {
                        qs.setQuestVarById(0, var + 1);
                        updateQuestStatus(env);
                        PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
                        return true;
                    }
                    break;
                case 39: // CHECK_USER_HAS_QUEST_ITEM
                    if (var == 4) {
                        if (QuestService.collectItemCheck(env, true)) {
                            qs.setStatus(QuestStatus.REWARD);
                            updateQuestStatus(env);
                            return sendQuestDialog(env, 5);
                        } else
                            return sendQuestDialog(env, 10001);
                    }
                    break;
            }
        } else if (targetId == 798206) {
            switch (env.getDialogId()) {
                case 31: // QUEST_SELECT
                    if (var == 1)
                        return sendQuestDialog(env, 1352);
                    else if (var == 2)
                        return sendQuestDialog(env, 1693);
                    break;
                case 1353: // SELECT_ACTION_1353
                    PacketSendUtility.sendPacket(player, new SM_PLAY_MOVIE(0, 364));
                    break;
                case 10001: // SETPRO2
                    if (var == 1) {
                        qs.setQuestVarById(0, var + 1);
                        updateQuestStatus(env);
                        PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 0));
                        TeleportService2.teleportTo(player, 210060000, 926f, 3035f, 186f, (byte) 30);
                        return true;
                    }
                    break;
                case 10002: // SETPRO3
                    if (var == 2) {
                        PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 0));
                        TeleportService2.teleportTo(player, 210060000, 926f, 3035f, 186f, (byte) 30);
                        return true;
                    }
                    break;
            }
        } else if (targetId == 700389) {
            return useObjectStep(env, qs, player, var, 1);
        } else if (targetId == 700388) {
            return useObjectStep(env, qs, player, var, 2);
        }
        return false;
    }

    private boolean useObjectStep(final QuestEnv env, final QuestState qs, final Player player, int var, final int varIndex) {
        if (env.getDialogId() != -1) // USE_OBJECT
            return false;
        if (var == 2 && qs.getQuestVarById(varIndex) == 0) {
            final int targetObjectId = env.getVisibleObject().getObjectId();
            PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), targetObjectId, 3000, 1));
            PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.NEUTRALMODE2, 0, targetObjectId), true);
            ThreadPoolManager.getInstance().schedule(new Runnable() {
                @Override
                public void run() {
                    Npc npc = (Npc) player.getTarget();
                    if (npc == null || npc.getObjectId() != targetObjectId)
                        return;
                    PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), targetObjectId, 3000, 0));
                    PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.START_LOOT, 0, targetObjectId), true);

                    qs.setQuestVarById(varIndex, qs.getQuestVarById(varIndex) + 1);
                    updateQuestStatus(env);
                    int other = (varIndex == 1) ? 2 : 1;
                    if (qs.getQuestVarById(other) >= 1) {
                        qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
                        qs.setQuestVarById(1, 0);
                        qs.setQuestVarById(2, 0);
                        updateQuestStatus(env);
                    }
                }
            }, 3000);
            return true;
        }
        return false;
    }
}
