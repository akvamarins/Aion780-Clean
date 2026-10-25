package quest.minion;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

public class _60616ElsiesSecondGift extends QuestHandler {

    private static final int questId = 60616;
    private static final int NPC_LEANOR = 820321; // Leanor (Sanctum / Pandaemonium)
    private static final int PREV_QUEST_ID = 60615;

    public _60616ElsiesSecondGift() {
        super(questId);
    }

    @Override
    public void register() {
        qe.registerOnLevelUp(questId);
        qe.registerOnEnterWorld(questId);
        qe.registerQuestNpc(NPC_LEANOR).addOnTalkEvent(questId);
    }

    private boolean tryStartQuest(QuestEnv env) {
        Player player = env.getPlayer();
        if (player.getLevel() < 35) {
            return false;
        }
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        if (qs != null && qs.getStatus() != QuestStatus.NONE) {
            return false;
        }
        // Retail start condition from quest_data.xml: <finished quest_id="60615" />
        QuestState prev = player.getQuestStateList().getQuestState(PREV_QUEST_ID);
        if (prev == null || prev.getStatus() != QuestStatus.COMPLETE) {
            return false;
        }
        env.setQuestId(questId);
        return QuestService.startQuest(env);
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
        QuestState qs = env.getPlayer().getQuestStateList().getQuestState(questId);
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
                } else if (action == DialogAction.SELECTED_QUEST_NOREWARD) {
                    return sendQuestEndDialog(env);
                }
            }
        }
        return false;
    }
}
