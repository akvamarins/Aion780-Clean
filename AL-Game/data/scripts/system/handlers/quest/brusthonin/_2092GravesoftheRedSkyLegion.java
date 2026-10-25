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
public class _2092GravesoftheRedSkyLegion extends QuestHandler {

    private final static int questId = 2092;
    private final static int[] npc_ids = { 205150, 205188, 700394, 205190, 205208, 205214, 205213, 205212, 205210, 205209 };

    public _2092GravesoftheRedSkyLegion() {
        super(questId);
    }

    @Override
    public void register() {
        qe.registerOnLevelUp(questId);
        qe.registerQuestNpc(214402).addOnKillEvent(questId);
        qe.registerQuestNpc(214403).addOnKillEvent(questId);
        for (int npc_id : npc_ids)
            qe.registerQuestNpc(npc_id).addOnTalkEvent(questId);
    }

    @Override
    public boolean onLvlUpEvent(QuestEnv env) {
        return defaultOnLvlUpEvent(env);
    }

    @Override
    public boolean onKillEvent(QuestEnv env) {
        int[] mobs = { 214402, 214403 };
        return defaultOnKillEvent(env, mobs, 6, 20) || defaultOnKillEvent(env, mobs, 20, true);
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
                if (env.getDialogId() == 23) { // SELECTED_QUEST_NOREWARD -> unlock 2093 (client: finished_quest_cond1=Q2092)
                    QuestService.startQuest(new QuestEnv(env.getVisibleObject(), env.getPlayer(), 2093, env.getDialogId()), QuestStatus.LOCKED);
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
                    PacketSendUtility.sendPacket(player, new SM_PLAY_MOVIE(0, 395));
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
        } else if (targetId == 205188) {
            switch (env.getDialogId()) {
                case 31: // QUEST_SELECT
                    if (var == 2)
                        return sendQuestDialog(env, 1693);
                    break;
                case 10002: // SETPRO3
                    if (var == 2) {
                        qs.setQuestVarById(0, var + 1);
                        updateQuestStatus(env);
                        PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
                        return true;
                    }
                    break;
            }
        } else if (targetId == 205190) {
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
                            ItemService.addItem(player, 182209009, 1);
                            return sendQuestDialog(env, 10000);
                        } else
                            return sendQuestDialog(env, 10001);
                    }
                    break;
            }
        } else if (targetId == 700394) {
            return graveObject(env, qs, player, var);
        } else if (targetId == 205208) {
            return graveNpc(env, qs, player, var, 2717);
        } else if (targetId == 205209) {
            return graveNpc(env, qs, player, var, 2802);
        } else if (targetId == 205210) {
            return graveNpc(env, qs, player, var, 2887);
        } else if (targetId == 205212) {
            return graveNpc(env, qs, player, var, 2972);
        } else if (targetId == 205213) {
            return graveNpc(env, qs, player, var, 3058);
        } else if (targetId == 205214) {
            return graveNpc(env, qs, player, var, 3143);
        }
        return false;
    }

    private boolean graveNpc(final QuestEnv env, final QuestState qs, final Player player, int var, int pageId) {
        switch (env.getDialogId()) {
            case 31: // QUEST_SELECT
                if (var == 5)
                    return sendQuestDialog(env, pageId);
                break;
            case 10005: // SETPRO6
                if (var == 5) {
                    qs.setQuestVarById(0, var + 1);
                    updateQuestStatus(env);
                    player.getInventory().decreaseByItemId(182209009, 1);
                    PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
                    return true;
                }
                break;
        }
        return false;
    }

    private boolean graveObject(final QuestEnv env, final QuestState qs, final Player player, int var) {
        if (env.getDialogId() != -1) // USE_OBJECT
            return false;
        if (var == 1) {
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

                    qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
                    updateQuestStatus(env);
                }
            }, 3000);
            return true;
        }
        return false;
    }
}

