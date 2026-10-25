package quest.heiron;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

public class _60110KaisinelsMission extends QuestHandler {
    private final static int questId = 60110;
    private final static int EREMITIA = 798600;
    private final static int KAISINEL = 800349;
    public _60110KaisinelsMission() { super(questId); }
    @Override
    public void register() {
        qe.registerOnLevelUp(questId);
        qe.registerQuestNpc(EREMITIA).addOnTalkEvent(questId);
        qe.registerQuestNpc(KAISINEL).addOnTalkEvent(questId);
    }
            @Override
        public boolean onLvlUpEvent(QuestEnv env) { return defaultOnLvlUpEvent(env, 14043, true); }
    @Override
    public boolean onDialogEvent(QuestEnv env) {
        Player player = env.getPlayer();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        if (qs == null) return false;
        int targetId = env.getTargetId();
        DialogAction dialog = env.getDialog();
        if (qs.getStatus() == QuestStatus.START) {
            if (targetId == EREMITIA && qs.getQuestVarById(0) == 0) {
                if (dialog == DialogAction.QUEST_SELECT) return sendQuestDialog(env, 1011);
                if (dialog == DialogAction.SETPRO1) { qs.setQuestVar(1); updateQuestStatus(env); return closeDialogWindow(env); }
            } else if (targetId == KAISINEL && qs.getQuestVarById(0) == 1) {
                if (dialog == DialogAction.QUEST_SELECT) return sendQuestDialog(env, 1352);
                if (dialog == DialogAction.SET_SUCCEED) { qs.setStatus(QuestStatus.REWARD); updateQuestStatus(env); return closeDialogWindow(env); }
            }
        } else if (qs.getStatus() == QuestStatus.REWARD) {
            if (targetId == EREMITIA) return sendQuestEndDialog(env);
        }
        return false;
    }
}
