package quest.theobomos;

import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAY_MOVIE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_USE_OBJECT;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
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
public class _1094ProjectDrakanhammer extends QuestHandler {

    private final static int questId = 1094;
    private final static int[] npc_ids = { 203834, 798155, 700411, 730153 };

    public _1094ProjectDrakanhammer() {
        super(questId);
    }

    @Override
    public void register() {
        qe.registerOnLevelUp(questId);
        qe.registerQuestItem(182208017, questId);
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
            if (targetId == 203834) {
                int dialogId = env.getDialogId();
                if (dialogId >= 8 && dialogId <= 23) { // reward finish
                    QuestService.collectItemCheck(env, true);
                }
                return sendQuestEndDialog(env);
            }
            return false;
        } else if (qs.getStatus() != QuestStatus.START) {
            return false;
        }
        if (targetId == 203834) {
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
        } else if (targetId == 798155) {
            switch (env.getDialogId()) {
                case 31: // QUEST_SELECT
                    if (var == 1)
                        return sendQuestDialog(env, 1352);
                    break;
                case 1353: // SELECT_ACTION_1353
                    PacketSendUtility.sendPacket(player, new SM_PLAY_MOVIE(0, 367));
                    break;
                case 10001: // SETPRO2
                    if (var == 1) {
                        qs.setQuestVarById(0, var + 1);
                        updateQuestStatus(env);
                        PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
                        return true;
                    }
                    break;
            }
        } else if (targetId == 700411) {
            return giveItemObject(env, qs, player, var);
        } else if (targetId == 730153) {
            return finishObject(env, qs, player, var);
        }
        return false;
    }

    private boolean giveItemObject(final QuestEnv env, final QuestState qs, final Player player, int var) {
        if (env.getDialogId() != -1) // USE_OBJECT
            return false;
        if (var == 2 && player.getInventory().getItemCountByItemId(182208017) == 0) {
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

                    ItemService.addItem(player, 182208017, 1);
                }
            }, 3000);
            return true;
        }
        return false;
    }

    private boolean finishObject(final QuestEnv env, final QuestState qs, final Player player, int var) {
        if (env.getDialogId() != -1) // USE_OBJECT
            return false;
        if (var == 3) {
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

                    player.getInventory().decreaseByItemId(182208018, 1);
                    player.getInventory().decreaseByItemId(182208019, 1);
                    player.getInventory().decreaseByItemId(182208020, 1);
                    player.getInventory().decreaseByItemId(182208021, 1);
                    qs.setStatus(QuestStatus.REWARD);
                    qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
                    updateQuestStatus(env);
                }
            }, 3000);
            return true;
        }
        return false;
    }

    @Override
    public HandlerResult onItemUseEvent(final QuestEnv env, Item item) {
        final Player player = env.getPlayer();
        final QuestState qs = player.getQuestStateList().getQuestState(questId);
        final int id = item.getItemTemplate().getTemplateId();
        final int itemObjId = item.getObjectId();

        if (id != 182208017 || qs == null)
            return HandlerResult.UNKNOWN;

        if (qs.getQuestVarById(0) != 2)
            return HandlerResult.FAILED;

        PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), itemObjId, id, 1000, 0, 0), true);
        ThreadPoolManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), itemObjId, id, 0, 1, 0), true);
                player.getInventory().decreaseByItemId(182208017, 1);
                qs.setQuestVarById(0, 3);
                updateQuestStatus(env);
            }
        }, 1000);
        return HandlerResult.SUCCESS;
    }
}
