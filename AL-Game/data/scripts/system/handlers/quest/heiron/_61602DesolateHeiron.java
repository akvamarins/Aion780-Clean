package quest.heiron;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

public class _61602DesolateHeiron extends QuestHandler {
    private final static int questId = 61602;
    private final static int ROYER = 820018;
    private final static int[] mobs = { 652574, 652591, 652657, 652658 };
    public _61602DesolateHeiron() { super(questId); }
    @Override
    public void register() {
        qe.registerQuestNpc(ROYER).addOnQuestStart(questId);
        qe.registerQuestNpc(ROYER).addOnTalkEvent(questId);
        for (int mob : mobs) qe.registerQuestNpc(mob).addOnKillEvent(questId);
    }
            @Override
        public boolean onLvlUpEvent(QuestEnv env) { return false; }
    @Override
    public boolean onDialogEvent(QuestEnv env) {
        Player player = env.getPlayer();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        DialogAction dialog = env.getDialog();
        int targetId = env.getTargetId();
        if (qs == null || qs.getStatus() == QuestStatus.NONE) {
            if (targetId == ROYER) {
                if (dialog == DialogAction.QUEST_SELECT) return sendQuestDialog(env, 4762);
                if (dialog == DialogAction.QUEST_ACCEPT_1 || dialog == DialogAction.QUEST_ACCEPT_SIMPLE) return sendQuestStartDialog(env);
            }
        } else if (qs.getStatus() == QuestStatus.START) {
            if (targetId == ROYER) {
                if (dialog == DialogAction.QUEST_SELECT) return sendQuestDialog(env, 1011);
            }
        } else if (qs.getStatus() == QuestStatus.REWARD) {
            if (targetId == ROYER) return sendQuestEndDialog(env);
        }
        return false;
    }
    @Override
    public boolean onKillEvent(QuestEnv env) {
        Player player = env.getPlayer();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        if (qs == null || qs.getStatus() != QuestStatus.START) return false;
        int var = qs.getQuestVarById(0);
        if (var < 5) {
            qs.setQuestVar(var + 1);
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
