package quest.minion;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

public class _15545PreciousAllyMinion extends QuestHandler {

    private static final int questId = 15545;
    private static final int NPC_JOWYN = 836073; // Jowyn (Sanctum)
    private static final int NPC_SAMIS = 838071; // Samis (Sanctum)
    private static final int ITEM_LESSER = 190080010; // Lesser Minion Contract (quest_15545a)
    private static final int PREV_QUEST_ID = 19900;

    public _15545PreciousAllyMinion() {
        super(questId);
    }

    @Override
    public void register() {
        qe.registerOnLevelUp(questId);
        qe.registerOnEnterWorld(questId);
        qe.registerQuestNpc(NPC_JOWYN).addOnTalkEvent(questId);
        qe.registerQuestNpc(NPC_SAMIS).addOnTalkEvent(questId);
        qe.registerQuestItem(ITEM_LESSER, questId);
    }

    private boolean tryStartQuest(QuestEnv env) {
        Player player = env.getPlayer();
        if (player.getLevel() < 20 || player.getRace() != Race.ELYOS) {
            return false;
        }
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        if (qs != null && qs.getStatus() != QuestStatus.NONE) {
            return false;
        }
        // Retail start condition from quest_data.xml: <unfinished>19900</unfinished>
        QuestState prev = player.getQuestStateList().getQuestState(PREV_QUEST_ID);
        if (prev != null && prev.getStatus() == QuestStatus.COMPLETE) {
            return false;
        }
        env.setQuestId(questId);
        if (QuestService.startQuest(env)) {
            giveQuestItem(env, ITEM_LESSER, 1);
            return true;
        }
        return false;
    }

    @Override
    public boolean onLvlUpEvent(QuestEnv env) {
        return tryStartQuest(env);
    }

    @Override
    public boolean onEnterWorldEvent(QuestEnv env) {
        return tryStartQuest(env);
    }

    @Override
    public boolean onDialogEvent(QuestEnv env) {
        Player player = env.getPlayer();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        if (qs == null) return false;
        int targetId = env.getTargetId();
        DialogAction action = env.getDialog();
        int var = qs.getQuestVarById(0);

        if (qs.getStatus() == QuestStatus.START) {
            if (targetId == NPC_JOWYN) {
                if (var == 0 && action == DialogAction.QUEST_SELECT) {
                    return sendQuestDialog(env, 1011);
                } else if (var == 0 && action == DialogAction.SETPRO1) {
                    return defaultCloseDialog(env, 0, 1);
                }
            } else if (targetId == NPC_SAMIS) {
                if (var == 1 && action == DialogAction.QUEST_SELECT) {
                    return sendQuestDialog(env, 1352);
                } else if (var == 1 && action == DialogAction.SELECT_QUEST_REWARD) {
                    qs.setQuestVar(2);
                    qs.setStatus(QuestStatus.REWARD);
                    updateQuestStatus(env);
                    return sendQuestDialog(env, 5);
                }
            }
        } else if (qs.getStatus() == QuestStatus.REWARD) {
            if (targetId == NPC_SAMIS) {
                if (action == DialogAction.USE_OBJECT) {
                    return sendQuestDialog(env, 10002);
                } else if (action == DialogAction.SELECT_QUEST_REWARD) {
                    return sendQuestDialog(env, 5);
                } else if (action == DialogAction.SELECTED_QUEST_NOREWARD) {
                    removeQuestItem(env, ITEM_LESSER, 1);
                    return sendQuestEndDialog(env);
                }
            }
        }
        return false;
    }
}
