package quest.heiron;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

public class _61605SecretLaboratory extends QuestHandler {
    private final static int questId = 61605;
    private final static int PUNCTUAL_ROYER = 820022;
    private final static int BATTLING_ROYER = 820023;
    public _61605SecretLaboratory() { super(questId); }
    @Override
    public void register() {
        qe.registerQuestNpc(PUNCTUAL_ROYER).addOnQuestStart(questId);
        qe.registerQuestNpc(PUNCTUAL_ROYER).addOnTalkEvent(questId);
        qe.registerQuestNpc(BATTLING_ROYER).addOnTalkEvent(questId);
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
        if (qs.getStatus() == QuestStatus.START) {
            if (targetId == PUNCTUAL_ROYER && qs.getQuestVarById(0) == 0) {
                if (dialog == DialogAction.QUEST_SELECT) return sendQuestDialog(env, 1011);
                if (dialog == DialogAction.SETPRO1) { qs.setQuestVar(1); updateQuestStatus(env); return closeDialogWindow(env); }
            }
        } else if (qs.getStatus() == QuestStatus.REWARD) {
            if (targetId == BATTLING_ROYER) return sendQuestEndDialog(env);
        }
        return false;
    }
}
