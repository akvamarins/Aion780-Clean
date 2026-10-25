package quest.heiron;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

public class _60203InvestigatingTheKisharKrall extends QuestHandler {
    private final static int questId = 60203;
    private final static int INTEL_ROYER = 820036;
    private final static int ATALANTE = 204602;
    private final static int MABANGTAH = 204020;
    public _60203InvestigatingTheKisharKrall() { super(questId); }
    @Override
    public void register() {
        qe.registerOnLevelUp(questId);
        qe.registerQuestNpc(INTEL_ROYER).addOnTalkEvent(questId);
        qe.registerQuestNpc(ATALANTE).addOnTalkEvent(questId);
        qe.registerQuestNpc(MABANGTAH).addOnTalkEvent(questId);
    }
            @Override
        public boolean onLvlUpEvent(QuestEnv env) { return defaultOnLvlUpEvent(env, 60202, true); }
    @Override
    public boolean onDialogEvent(QuestEnv env) {
        Player player = env.getPlayer();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        if (qs == null) return false;
        int targetId = env.getTargetId();
        DialogAction dialog = env.getDialog();
        int var = qs.getQuestVarById(0);
        if (qs.getStatus() == QuestStatus.START) {
            if (targetId == INTEL_ROYER && var == 0) {
                if (dialog == DialogAction.QUEST_SELECT) return sendQuestDialog(env, 1011);
                if (dialog == DialogAction.SETPRO1) { qs.setQuestVar(1); updateQuestStatus(env); return closeDialogWindow(env); }
            } else if (targetId == INTEL_ROYER && var == 1) {
                if (dialog == DialogAction.QUEST_SELECT) return sendQuestDialog(env, 1352);
                if (dialog == DialogAction.SETPRO2) { qs.setQuestVar(2); updateQuestStatus(env); return closeDialogWindow(env); }
            } else if (targetId == ATALANTE && var == 2) {
                if (dialog == DialogAction.QUEST_SELECT) return sendQuestDialog(env, 1693);
                if (dialog == DialogAction.SET_SUCCEED) { qs.setStatus(QuestStatus.REWARD); updateQuestStatus(env); return closeDialogWindow(env); }
            }
        } else if (qs.getStatus() == QuestStatus.REWARD) {
            if (targetId == MABANGTAH) return sendQuestEndDialog(env);
        }
        return false;
    }
}
