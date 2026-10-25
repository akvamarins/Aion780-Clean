package quest.minion;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

public class _60615ElsiesFirstGift extends QuestHandler {

    private static final int questId = 60615;
    private static final int NPC_LEANOR = 820321; // Leanor (Sanctum / Pandaemonium)
    private static final int ITEM_BALL = 182216786; // Leanor's Crystal Ball (quest_60615a)

    public _60615ElsiesFirstGift() {
        super(questId);
    }

    @Override
    public void register() {
        qe.registerOnLevelUp(questId);
        qe.registerOnEnterWorld(questId);
        qe.registerQuestNpc(NPC_LEANOR).addOnTalkEvent(questId);
        qe.registerQuestItem(ITEM_BALL, questId);
    }

    private boolean tryStartQuest(QuestEnv env) {
        Player player = env.getPlayer();
        if (player.getLevel() < 20 || player.getLevel() > 79) {
            return false;
        }
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        if (qs != null && qs.getStatus() != QuestStatus.NONE) {
            return false;
        }
        env.setQuestId(questId);
        if (QuestService.startQuest(env)) {
            giveQuestItem(env, ITEM_BALL, 1);
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

        if (qs.getStatus() == QuestStatus.START) {
            if (targetId == NPC_LEANOR) {
                if (action == DialogAction.QUEST_SELECT) {
                    return sendQuestDialog(env, 1011);
                } else if (action == DialogAction.SELECT_QUEST_REWARD) {
                    qs.setStatus(QuestStatus.REWARD);
                    updateQuestStatus(env);
                    return sendQuestDialog(env, 5);
                }
            }
        } else if (qs.getStatus() == QuestStatus.REWARD) {
            if (targetId == NPC_LEANOR) {
                if (action == DialogAction.USE_OBJECT) {
                    return sendQuestDialog(env, 10002);
                } else if (action == DialogAction.SELECT_QUEST_REWARD) {
                    return sendQuestDialog(env, 5);
                } else if (action == DialogAction.SELECTED_QUEST_REWARD1 || action == DialogAction.SELECTED_QUEST_REWARD2) {
                    removeQuestItem(env, ITEM_BALL, 1);
                    return sendQuestEndDialog(env);
                } else if (action == DialogAction.SELECTED_QUEST_NOREWARD) {
                    removeQuestItem(env, ITEM_BALL, 1);
                    return sendQuestEndDialog(env);
                }
            }
        }
        return false;
    }
}
