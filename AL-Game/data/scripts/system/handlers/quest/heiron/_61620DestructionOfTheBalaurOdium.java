package quest.heiron;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

public class _61620DestructionOfTheBalaurOdium extends QuestHandler {
    private final static int questId = 61620;
    private final static int ODIUM = 703513;
    public _61620DestructionOfTheBalaurOdium() { super(questId); }
    @Override
    public void register() {
        qe.registerQuestNpc(ODIUM).addOnQuestStart(questId);
        qe.registerQuestNpc(ODIUM).addOnTalkEvent(questId);
        qe.registerQuestNpc(ODIUM).addOnKillEvent(questId);
    }
            @Override
        public boolean onLvlUpEvent(QuestEnv env) { return false; }
    @Override
    public boolean onDialogEvent(QuestEnv env) {
        Player player = env.getPlayer();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        if (qs == null) return false;
        if (qs.getStatus() == QuestStatus.START && env.getTargetId() == ODIUM) {
            if (env.getDialog() == DialogAction.QUEST_SELECT) return sendQuestDialog(env, 1011);
        } else if (qs.getStatus() == QuestStatus.REWARD && env.getTargetId() == ODIUM) {
            return sendQuestEndDialog(env);
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
