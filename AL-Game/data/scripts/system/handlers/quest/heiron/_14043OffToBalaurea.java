package quest.heiron;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

public class _14043OffToBalaurea extends QuestHandler {
    private final static int questId = 14043;
    private final static int EREMITIA = 798600;
    private final static int KUNBERUNERK = 798026;
    private final static int MAPIRERK = 798025;
    public _14043OffToBalaurea() { super(questId); }
    @Override
    public void register() {
        qe.registerOnLevelUp(questId);
        qe.registerQuestNpc(EREMITIA).addOnTalkEvent(questId);
        qe.registerQuestNpc(KUNBERUNERK).addOnTalkEvent(questId);
        qe.registerQuestNpc(MAPIRERK).addOnTalkEvent(questId);
        qe.registerQuestItem(182215351, questId);
        qe.registerQuestItem(182215352, questId);
    }
            @Override
        public boolean onLvlUpEvent(QuestEnv env) { return defaultOnLvlUpEvent(env, 60208, true); }
    @Override
    public boolean onDialogEvent(QuestEnv env) {
        Player player = env.getPlayer();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        if (qs == null) return false;
        int targetId = env.getTargetId();
        DialogAction dialog = env.getDialog();
        int var = qs.getQuestVarById(0);
        if (qs.getStatus() == QuestStatus.START) {
            if (targetId == EREMITIA && var == 0) {
                if (dialog == DialogAction.QUEST_SELECT) return sendQuestDialog(env, 1011);
                if (dialog == DialogAction.SETPRO1) { giveQuestItem(env, 182215351, 1); qs.setQuestVar(1); updateQuestStatus(env); return closeDialogWindow(env); }
            } else if (targetId == KUNBERUNERK) {
                if (var == 1 && dialog == DialogAction.QUEST_SELECT) return sendQuestDialog(env, 1352);
                if (var == 1 && dialog == DialogAction.SETPRO2) { qs.setQuestVar(2); updateQuestStatus(env); return closeDialogWindow(env); }
                if (var == 3 && dialog == DialogAction.QUEST_SELECT) return sendQuestDialog(env, 2034);
                if (var == 3 && dialog == DialogAction.SETPRO4) { removeQuestItem(env, 182215351, 1); giveQuestItem(env, 182215352, 1); qs.setQuestVar(4); updateQuestStatus(env); return closeDialogWindow(env); }
                if (var == 4 && dialog == DialogAction.QUEST_SELECT) return sendQuestDialog(env, 10002);
                if (var == 4 && dialog == DialogAction.SELECT_QUEST_REWARD) { qs.setStatus(QuestStatus.REWARD); updateQuestStatus(env); return sendQuestDialog(env, 5); }
            } else if (targetId == MAPIRERK && var == 2) {
                if (dialog == DialogAction.QUEST_SELECT) return sendQuestDialog(env, 1693);
                if (dialog == DialogAction.SETPRO3) { qs.setQuestVar(3); updateQuestStatus(env); return closeDialogWindow(env); }
            }
        } else if (qs.getStatus() == QuestStatus.REWARD) {
            if (targetId == KUNBERUNERK) return sendQuestEndDialog(env);
        }
        return false;
    }
}
