package quest.heiron;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

public class _61622InvestigationOfHeironsInhabitants extends QuestHandler {
    private final static int questId = 61622;
    private final static int NEKENTOS = 204527;
    private final static int GUNES = 204529;
    private final static int NARGIS = 204619;
    private final static int ROYER = 820016;
    public _61622InvestigationOfHeironsInhabitants() { super(questId); }
    @Override
    public void register() {
        qe.registerQuestNpc(NEKENTOS).addOnTalkEvent(questId);
        qe.registerQuestNpc(GUNES).addOnTalkEvent(questId);
        qe.registerQuestNpc(NARGIS).addOnTalkEvent(questId);
        qe.registerQuestNpc(ROYER).addOnTalkEvent(questId);
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
        int var = qs.getQuestVarById(0);
        if (qs.getStatus() == QuestStatus.START) {
            if (targetId == NEKENTOS && var == 0) {
                if (dialog == DialogAction.QUEST_SELECT) return sendQuestDialog(env, 1011);
                if (dialog == DialogAction.SETPRO1) { qs.setQuestVar(1); updateQuestStatus(env); return closeDialogWindow(env); }
            } else if (targetId == GUNES && var == 1) {
                if (dialog == DialogAction.QUEST_SELECT) return sendQuestDialog(env, 1352);
                if (dialog == DialogAction.SETPRO2) { qs.setQuestVar(2); updateQuestStatus(env); return closeDialogWindow(env); }
            } else if (targetId == NARGIS && var == 2) {
                if (dialog == DialogAction.QUEST_SELECT) return sendQuestDialog(env, 1693);
                if (dialog == DialogAction.SET_SUCCEED) { qs.setStatus(QuestStatus.REWARD); updateQuestStatus(env); return closeDialogWindow(env); }
            }
        } else if (qs.getStatus() == QuestStatus.REWARD && targetId == ROYER) {
            return sendQuestEndDialog(env);
        }
        return false;
    }
}
