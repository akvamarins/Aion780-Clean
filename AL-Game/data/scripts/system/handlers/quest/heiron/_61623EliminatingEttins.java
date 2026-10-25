package quest.heiron;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

public class _61623EliminatingEttins extends QuestHandler {
    private final static int questId = 61623;
    private final static int LOKTANIA = 204507;
    private final static int WHITEHAWK_ETTIN = 652531;
    private final static int CRACKTOOTH_ETTIN = 652532;
    private final static int[] mobs = { 652531, 652532 };

    public _61623EliminatingEttins() {
        super(questId);
    }

    @Override
    public void register() {
        qe.registerQuestNpc(LOKTANIA).addOnQuestStart(questId);
        qe.registerQuestNpc(LOKTANIA).addOnTalkEvent(questId);
        for (int mob : mobs) {
            qe.registerQuestNpc(mob).addOnKillEvent(questId);
        }
    }

            @Override
        public boolean onLvlUpEvent(QuestEnv env) { return false; }

    @Override
    public boolean onDialogEvent(QuestEnv env) {
        Player player = env.getPlayer();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        if (qs == null) return false;

        int targetId = env.getTargetId();
        DialogAction dialog = env.getDialog();

        if (qs.getStatus() == QuestStatus.NONE) {
            if (targetId == LOKTANIA) {
                if (dialog == DialogAction.QUEST_SELECT) return sendQuestDialog(env, 4762);
                if (dialog == DialogAction.QUEST_ACCEPT_1 || dialog == DialogAction.QUEST_ACCEPT_SIMPLE) return sendQuestStartDialog(env);
            }
        } else if (qs.getStatus() == QuestStatus.START) {
            if (targetId == LOKTANIA) {
                if (dialog == DialogAction.QUEST_SELECT) {
                    // Show current progress
                    return sendQuestDialog(env, 1011);
                }
            }
        } else if (qs.getStatus() == QuestStatus.REWARD) {
            if (targetId == LOKTANIA) {
                if (dialog == DialogAction.USE_OBJECT || dialog == DialogAction.QUEST_SELECT) {
                    return sendQuestDialog(env, 10002);
                }
                return sendQuestEndDialog(env);
            }
        }
        return false;
    }

    @Override
    public boolean onKillEvent(QuestEnv env) {
        Player player = env.getPlayer();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        if (qs == null || qs.getStatus() != QuestStatus.START) return false;

        int var = qs.getQuestVarById(0); // 0-5 kills
        int targetId = env.getTargetId();

        // Only count Whitehawk and Cracktooth Ettin
        if (targetId != WHITEHAWK_ETTIN && targetId != CRACKTOOTH_ETTIN) return false;

        if (var >= 0 && var < 5) {
            qs.setQuestVarById(0, var + 1);
            updateQuestStatus(env);
            if (var + 1 == 5) {
                qs.setStatus(QuestStatus.REWARD);
                updateQuestStatus(env);
            }
            return true;
        }
        return false;
    }
}
