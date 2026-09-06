
package quest.poeta;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_USE_OBJECT;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

public class _60007ForestRescue extends QuestHandler {
    private final static int questId = 60007;
    public _60007ForestRescue() { super(questId); }
    @Override public void register() {
        qe.registerQuestNpc(730008).addOnTalkEvent(questId);
        qe.registerQuestNpc(820002).addOnTalkEvent(questId);
        qe.registerQuestNpc(820003).addOnTalkEvent(questId);
        qe.registerQuestNpc(651867).addOnKillEvent(questId);
        qe.registerQuestNpc(651848).addOnKillEvent(questId);
        qe.registerQuestNpc(700030).addOnTalkEvent(questId);
        qe.registerQuestItem(182216249, questId);
        qe.registerQuestItem(182216250, questId);
        qe.registerOnLevelUp(questId);
        qe.registerOnEnterWorld(questId);
    }
    @Override public boolean onEnterWorldEvent(QuestEnv env) {
        Player player = env.getPlayer();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        if (qs == null) { env.setQuestId(questId); QuestService.startQuest(env); }
        return false;
    }
    @Override public boolean onDialogEvent(final QuestEnv env) {
        final Player player = env.getPlayer();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        if (qs == null) return false;
        int targetId = env.getTargetId();
        DialogAction action = env.getDialog();
        int var = qs.getQuestVarById(0);
        if (qs.getStatus() == QuestStatus.START) {
            if (targetId == 730008) {
                if (var == 0) {
                    switch (action) {
                    case QUEST_SELECT:
                        final int targetObjectId = env.getVisibleObject().getObjectId();
                        PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), targetObjectId, 3000, 1));
                        PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.START_QUESTLOOT, 0, targetObjectId), true);
                        ThreadPoolManager.getInstance().schedule(new Runnable() {
                            @Override public void run() {
                                if (player.getTarget() == null || player.getTarget().getObjectId() != targetObjectId) return;
                                PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.END_QUESTLOOT, 0, targetObjectId), true);
                                PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), targetObjectId, 3000, 0));
                                PacketSendUtility.sendPacket(env.getPlayer(), new SM_DIALOG_WINDOW(targetObjectId, 1011, env.getQuestId()));
                            }
                        }, 3000);
                        return false;
                    case SETPRO1:
                        qs.setQuestVar(1); updateQuestStatus(env); return closeDialogWindow(env);
                    default: break;
                    }
                }
            } else if (targetId == 820002) {
                if (var == 1 || var == 2) {
                    switch (action) {
                    case QUEST_SELECT: return sendQuestDialog(env, 1352);
                    case SELECT_ACTION_1353: return sendQuestDialog(env, 1353);
                    case SETPRO2: qs.setQuestVar(2); updateQuestStatus(env); return closeDialogWindow(env);
                    case CHECK_USER_HAS_QUEST_ITEM: return checkQuestItems(env, 2, 3, false, 10000, 10001);
                    default: break;
                    }
                }
            } else if (targetId == 820003) {
                if (var == 2 || var == 3) {
                    switch (action) {
                    case QUEST_SELECT: return sendQuestDialog(env, 1693);
                    case CHECK_USER_HAS_QUEST_ITEM: return checkQuestItems(env, 2, 3, false, 10000, 10001);
                    default: break;
                    }
                }
            } else if (targetId == 700030) {
                if (var == 3) {
                    switch (action) {
                    case USE_OBJECT: qs.setQuestVar(4); qs.setStatus(QuestStatus.REWARD); updateQuestStatus(env); return closeDialogWindow(env);
                    default: break;
                    }
                }
            }
        } else if (qs.getStatus() == QuestStatus.REWARD) {
            if (targetId == 820003) {
                switch (action) {
                case USE_OBJECT: return sendQuestDialog(env, 10002);
                case SELECT_QUEST_REWARD: return sendQuestDialog(env, 5);
                case SELECTED_QUEST_NOREWARD: return sendQuestEndDialog(env);
                default: break;
                }
            }
        }
        return false;
    }
    @Override public boolean onLvlUpEvent(QuestEnv env) { return defaultOnLvlUpEvent(env); }
}
