package quest.heiron;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

public class _61603InvestigatingTheLepharists extends QuestHandler {
    private final static int questId = 61603;
    private final static int SCOUT_ROYER = 820018;
    private final static int RILEY = 204576;
    public _61603InvestigatingTheLepharists() { super(questId); }
    @Override
    public void register() {
        qe.registerOnLevelUp(questId);
        qe.registerQuestNpc(SCOUT_ROYER).addOnTalkEvent(questId);
        qe.registerQuestNpc(RILEY).addOnTalkEvent(questId);
        qe.registerQuestItem(182216272, questId);
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
            if (targetId == SCOUT_ROYER) {
                if (var == 1 && dialog == DialogAction.QUEST_SELECT) return sendQuestDialog(env, 1352);
                if (var == 1 && dialog == DialogAction.SETPRO2) { qs.setQuestVar(2); updateQuestStatus(env); return closeDialogWindow(env); }
                if (var == 3 && dialog == DialogAction.QUEST_SELECT) return sendQuestDialog(env, 2034);
                if (var == 3 && dialog == DialogAction.SETPRO4) { giveQuestItem(env, 182216272, 1); qs.setQuestVar(4); updateQuestStatus(env); return closeDialogWindow(env); }
            } else if (targetId == RILEY && var == 4) {
                if (dialog == DialogAction.CHECK_USER_HAS_QUEST_ITEM) {
                    if (QuestService.collectItemCheck(env, true)) {
                        qs.setStatus(QuestStatus.REWARD);
                        updateQuestStatus(env);
                        return sendQuestDialog(env, 5);
                    } else return sendQuestDialog(env, 10001);
                }
            }
        } else if (qs.getStatus() == QuestStatus.REWARD) {
            if (targetId == RILEY) return sendQuestEndDialog(env);
        }
        return false;
    }
}
