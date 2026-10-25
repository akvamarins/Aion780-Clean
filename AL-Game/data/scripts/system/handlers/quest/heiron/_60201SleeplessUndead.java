package quest.heiron;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

public class _60201SleeplessUndead extends QuestHandler {
    private final static int questId = 60201;
    private final static int NIGHTMARE_ROYER = 820026;
    private final static int KACIAS = 204625;
    private final static int RELEASED_SOUL = 204624;
    public _60201SleeplessUndead() { super(questId); }
    @Override
    public void register() {
        qe.registerOnLevelUp(questId);
        qe.registerQuestNpc(NIGHTMARE_ROYER).addOnTalkEvent(questId);
        qe.registerQuestNpc(KACIAS).addOnTalkEvent(questId);
        qe.registerQuestNpc(RELEASED_SOUL).addOnTalkEvent(questId);
        qe.registerQuestItem(182216253, questId);
        qe.registerQuestItem(182216254, questId);
        qe.registerQuestItem(182216255, questId);
        qe.registerQuestItem(182216256, questId);
    }
            @Override
        public boolean onLvlUpEvent(QuestEnv env) { return defaultOnLvlUpEvent(env, 60200, true); }
    @Override
    public boolean onDialogEvent(QuestEnv env) {
        Player player = env.getPlayer();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        if (qs == null) return false;
        int targetId = env.getTargetId();
        DialogAction dialog = env.getDialog();
        int var = qs.getQuestVarById(0);
        if (qs.getStatus() == QuestStatus.START) {
            if (targetId == NIGHTMARE_ROYER && var == 0) {
                if (dialog == DialogAction.QUEST_SELECT) return sendQuestDialog(env, 1011);
                if (dialog == DialogAction.SETPRO1) { qs.setQuestVar(1); updateQuestStatus(env); return closeDialogWindow(env); }
            } else if (targetId == NIGHTMARE_ROYER && var == 1) {
                if (dialog == DialogAction.QUEST_SELECT) return sendQuestDialog(env, 1352);
                if (dialog == DialogAction.SETPRO2) { giveQuestItem(env, 182216253, 1); qs.setQuestVar(2); updateQuestStatus(env); return closeDialogWindow(env); }
            } else if (targetId == KACIAS && var == 2) {
                if (dialog == DialogAction.QUEST_SELECT) return sendQuestDialog(env, 1693);
                if (dialog == DialogAction.SETPRO3) { qs.setQuestVar(3); updateQuestStatus(env); return closeDialogWindow(env); }
            } else if (targetId == RELEASED_SOUL && var == 3) {
                if (dialog == DialogAction.QUEST_SELECT) return sendQuestDialog(env, 2034);
                if (dialog == DialogAction.SET_SUCCEED) { qs.setStatus(QuestStatus.REWARD); updateQuestStatus(env); return closeDialogWindow(env); }
            }
        } else if (qs.getStatus() == QuestStatus.REWARD) {
            if (targetId == NIGHTMARE_ROYER) return sendQuestEndDialog(env);
        }
        return false;
    }
}
