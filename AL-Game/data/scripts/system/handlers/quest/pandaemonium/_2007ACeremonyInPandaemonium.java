package quest.pandaemonium;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

public class _2007ACeremonyInPandaemonium extends QuestHandler {
    private final static int questId = 2007;
    public _2007ACeremonyInPandaemonium() { super(questId); }
    @Override
    public void register() {
        qe.registerOnLevelUp(questId);
        qe.registerQuestNpc(204050).addOnTalkEvent(questId);
        qe.registerQuestNpc(204051).addOnTalkEvent(questId);
    }
    @Override
    public boolean onLvlUpEvent(QuestEnv env) { return defaultOnLvlUpEvent(env, questId, false); }
    @Override
    public boolean onDialogEvent(QuestEnv env) {
        Player player = env.getPlayer();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        if (qs == null) return false;
        DialogAction dialog = env.getDialog();
        if (qs.getStatus() == QuestStatus.START) {
            if (qs.getQuestVarById(0) == 0) {
                if (dialog == DialogAction.QUEST_SELECT) return sendQuestDialog(env, 1011);
                if (dialog == DialogAction.SETPRO1) { qs.setQuestVar(1); updateQuestStatus(env); return closeDialogWindow(env); }
            } else if (qs.getQuestVarById(0) == 1) {
                if (dialog == DialogAction.QUEST_SELECT) return sendQuestDialog(env, 1352);
                if (dialog == DialogAction.SET_SUCCEED) { qs.setStatus(QuestStatus.REWARD); updateQuestStatus(env); return closeDialogWindow(env); }
            }
        } else if (qs.getStatus() == QuestStatus.REWARD) {
            return sendQuestEndDialog(env);
        }
        return false;
    }
}