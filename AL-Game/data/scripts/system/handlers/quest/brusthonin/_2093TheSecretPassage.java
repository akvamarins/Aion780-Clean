package quest.brusthonin;

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
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Hellboy Aion4Free, adapted for Aion780
 */
public class _2093TheSecretPassage extends QuestHandler {

    private final static int questId = 2093;
    private final static int[] npc_ids = { 205150, 205159, 205164, 205197, 205198, 730174, 700395 };

    public _2093TheSecretPassage() {
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
            if (targetId == 205150) {
                int dialogId = env.getDialogId();
                if (dialogId == 23) { // SELECTED_QUEST_NOREWARD -> unlock 2094 (client: finished_quest_cond1=Q2093)
                    QuestService.startQuest(new QuestEnv(env.getVisibleObject(), env.getPlayer(), 2094, env.getDialogId()), QuestStatus.LOCKED);
                }
                if (dialogId >= 8 && dialogId <= 23) { // reward finish
                    player.getInventory().decreaseByItemId(182209012, 1);
                }
                return sendQuestEndDialog(env);
            }
            return false;
        } else if (qs.getStatus() != QuestStatus.START) {
            return false;
        }
        if (targetId == 205150) {
            switch (env.getDialogId()) {
                case 31: // QUEST_SELECT
                    if (var == 0)
                        return sendQuestDialog(env, 1011);
                    break;
                case 1012: // SELECT_ACTION_1012
                    PacketSendUtility.sendPacket(player, new SM_PLAY_MOVIE(0, 397));
                    break;
                case 10000: // SETPRO1
                    if (var == 0) {
                        qs.setQuestVarById(0, var + 1);
                        updateQuestStatus(env);
                        PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
                        return true;
                    }
                    break;
            }
        } else if (targetId == 205159) {
            return talkStep(env, qs, player, var, 1, 1352, 10001);
        } else if (targetId == 205164) {
            return talkStep(env, qs, player, var, 2, 1693, 10002);
        } else if (targetId == 205197) {
            switch (env.getDialogId()) {
                case 31: // QUEST_SELECT
                    if (var == 3)
                        return sendQuestDialog(env, 2034);
                    if (var == 4)
                        return sendQuestDialog(env, 2375);
                    break;
                case 10003: // SETPRO4
                    if (var == 3) {
                        qs.setQuestVarById(0, var + 1);
                        updateQuestStatus(env);
                        PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
                        return true;
                    }
                    break;
                case 10004: // SETPRO5
                    if (var == 4) {
                        PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
                        return true;
                    }
                    break;
                case 39: // CHECK_USER_HAS_QUEST_ITEM
                    if (var == 4) {
                        if (QuestService.collectItemCheck(env, true)) {
                            qs.setQuestVarById(0, var + 1);
                            updateQuestStatus(env);
                            return sendQuestDialog(env, 10000);
                        } else
                            return sendQuestDialog(env, 10001);
                    }
                    break;
            }
        } else if (targetId == 205198) {
            switch (env.getDialogId()) {
                case 31: // QUEST_SELECT
                    if (var == 5)
                        return sendQuestDialog(env, 2716);
                    else if (var == 7)
                        return sendQuestDialog(env, 3398);
                    break;
                case 3399: // SELECT_ACTION_3399
                    PacketSendUtility.sendPacket(player, new SM_PLAY_MOVIE(0, 398));
                    player.getInventory().decreaseByItemId(182209011, 1);
                    break;
                case 10005: // SETPRO6
                    if (var == 5) {
                        qs.setQuestVarById(0, var + 1);
                        updateQuestStatus(env);
                        PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
                        return true;
                    }
                    break;
                case 10007: // SETPRO8
                    if (var == 7) {
                        qs.setQuestVarById(0, var + 1);
                        updateQuestStatus(env);
                        PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
                        return true;
                    }
                    break;
            }
        } else if (targetId == 730174) {
            return passageObject(env, qs, player, var, 6, 182209011, false);
        } else if (targetId == 700395) {
            return passageObject(env, qs, player, var, 8, 182209012, true);
        }
        return false;
    }

    private boolean talkStep(final QuestEnv env, final QuestState qs, final Player player, int var, int requiredVar, int pageId, int setproId) {
        switch (env.getDialogId()) {
            case 31: // QUEST_SELECT
                if (var == requiredVar)
                    return sendQuestDialog(env, pageId);
                break;
            default:
                if (env.getDialogId() == setproId && var == requiredVar) {
                    qs.setQuestVarById(0, var + 1);
                    updateQuestStatus(env);
                    PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
                    return true;
                }
                break;
        }
        return false;
    }

    private boolean passageObject(final QuestEnv env, final QuestState qs, final Player player, int var, final int requiredVar, final int itemId, final boolean toReward) {
        if (env.getDialogId() != -1) // USE_OBJECT
            return false;
        if (var == requiredVar) {
            final int targetObjectId = env.getVisibleObject().getObjectId();
            PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), targetObjectId, 3000, 1));
            PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.NEUTRALMODE2, 0, targetObjectId), true);
            ThreadPoolManager.getInstance().schedule(new Runnable() {
                @Override
                public void run() {
                    if (qs.getQuestVarById(0) == requiredVar) {
                        Npc npc = (Npc) player.getTarget();
                        if (npc == null || npc.getObjectId() != targetObjectId)
                            return;
                        PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), targetObjectId, 3000, 0));
                        PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.START_LOOT, 0, targetObjectId), true);

                        ItemService.addItem(player, itemId, 1);
                        if (toReward) {
                            qs.setStatus(QuestStatus.REWARD);
                        } else {
                            qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
                        }
                        updateQuestStatus(env);
                    }
                }
            }, 3000);
            return false;
        }
        return false;
    }
}
