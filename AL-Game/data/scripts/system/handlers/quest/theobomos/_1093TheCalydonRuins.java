package quest.theobomos;

import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAY_MOVIE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_USE_OBJECT;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Hellboy Aion4Free, adapted for Aion780
 */
public class _1093TheCalydonRuins extends QuestHandler {

    private final static int questId = 1093;
    private final static int[] npc_ids = { 798155, 203784, 798176, 700391, 700392, 700393, 798212 };

    public _1093TheCalydonRuins() {
        super(questId);
    }

    @Override
    public void register() {
        qe.registerOnLevelUp(questId);
        qe.registerQuestItem(182208013, questId);
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
                if (env.getDialogId() == 23) { // SELECTED_QUEST_NOREWARD -> unlock 1094 (client: finished_quest_cond1=Q1093)
                    QuestService.startQuest(new QuestEnv(env.getVisibleObject(), env.getPlayer(), 1094, env.getDialogId()), QuestStatus.LOCKED);
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
        } else if (targetId == 203784) {
            switch (env.getDialogId()) {
                case 31: // QUEST_SELECT
                    if (var == 1)
                        return sendQuestDialog(env, 1352);
                    break;
                case 10001: // SETPRO2
                    if (var == 1) {
                        qs.setQuestVarById(0, var + 1);
                        updateQuestStatus(env);
                        ItemService.addItem(player, 182208013, 1);
                        PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
                        return true;
                    }
                    break;
            }
        } else if (targetId == 798176) {
            switch (env.getDialogId()) {
                case 31: // QUEST_SELECT
                    if (var == 2)
                        return sendQuestDialog(env, 1693);
                    break;
                case 1694: // SELECT_ACTION_1694
                    PacketSendUtility.sendPacket(player, new SM_PLAY_MOVIE(0, 365));
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
        } else if (targetId == 798212) {
            switch (env.getDialogId()) {
                case 31: // QUEST_SELECT
                    if (var == 7)
                        return sendQuestDialog(env, 3398);
                    if (var == 8)
                        return sendQuestDialog(env, 3739);
                    break;
                case 10003: // SETPRO4
                    if (var == 7) {
                        PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
                        return true;
                    }
                    break;
                case 10255: // SET_SUCCEED
                    if (var == 8) {
                        qs.setStatus(QuestStatus.REWARD);
                        updateQuestStatus(env);
                        PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
                        return true;
                    }
                    break;
                case 39: // CHECK_USER_HAS_QUEST_ITEM
                    if (var == 7) {
                        if (QuestService.collectItemCheck(env, true)) {
                            qs.setQuestVarById(0, var + 1);
                            updateQuestStatus(env);
                            return sendQuestDialog(env, 3739);
                        } else
                            return sendQuestDialog(env, 10001);
                    }
                    break;
            }
        } else if (targetId == 700391) {
            return collectObject(env, qs, player, var, 4, 182208014, 5);
        } else if (targetId == 700392) {
            return collectObject(env, qs, player, var, 5, 182208015, 6);
        } else if (targetId == 700393) {
            return collectObject(env, qs, player, var, 6, 182208016, 7);
        }
        return false;
    }

    private boolean collectObject(final QuestEnv env, final QuestState qs, final Player player, int var, final int requiredVar, final int itemId, final int nextVar) {
        if (env.getDialogId() != -1) // USE_OBJECT
            return false;
        if (var == requiredVar) {
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

                    ItemService.addItem(player, itemId, 1);
                    qs.setQuestVarById(0, nextVar);
                    updateQuestStatus(env);
                }
            }, 3000);
            return true;
        }
        return false;
    }

    @Override
    public HandlerResult onItemUseEvent(QuestEnv env, Item item) {
        final Player player = env.getPlayer();
        final QuestState qs = player.getQuestStateList().getQuestState(questId);
        final int id = item.getItemTemplate().getTemplateId();

        if (id != 182208013)
            return HandlerResult.UNKNOWN;

        if (qs == null || qs.getQuestVarById(0) != 3)
            return HandlerResult.FAILED;

        player.getInventory().decreaseByItemId(182208013, 1);
        SkillEngine.getInstance().getSkill(player, 18469, 1, player).useSkill();
        qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
        updateQuestStatus(env);
        return HandlerResult.SUCCESS;
    }
}
