package quest.heiron;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

public class _61604LepharistExperiments extends QuestHandler {
    private final static int questId = 61604;
    private final static int RIALA = 204606;
    private final static int DOOR = 820062;
    private final static int URIEL = 820021;
    private final static int[] mobs = { 652821, 652822 };
    public _61604LepharistExperiments() { super(questId); }
    @Override
    public void register() {
        qe.registerQuestNpc(RIALA).addOnQuestStart(questId);
        qe.registerQuestNpc(RIALA).addOnTalkEvent(questId);
        qe.registerQuestNpc(DOOR).addOnTalkEvent(questId);
        qe.registerQuestNpc(URIEL).addOnTalkEvent(questId);
        for (int mob : mobs) qe.registerQuestNpc(mob).addOnKillEvent(questId);
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
            if (targetId == RIALA && qs.getQuestVarById(0) == 0) {
                if (dialog == DialogAction.QUEST_SELECT) return sendQuestDialog(env, 1011);
                if (dialog == DialogAction.SETPRO1) { qs.setQuestVar(1); updateQuestStatus(env); return closeDialogWindow(env); }
            } else if (targetId == DOOR && qs.getQuestVarById(0) == 1) {
                // after kills
                if (dialog == DialogAction.QUEST_SELECT) return sendQuestDialog(env, 1352);
            }
        } else if (qs.getStatus() == QuestStatus.REWARD) {
            if (targetId == URIEL) return sendQuestEndDialog(env);
        }
        return false;
    }
    @Override
    public boolean onKillEvent(QuestEnv env) {
        Player player = env.getPlayer();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        if (qs == null || qs.getStatus() != QuestStatus.START) return false;
        if (qs.getQuestVarById(0) != 1) return false;
        int var1 = qs.getQuestVarById(1);
        int targetId = env.getTargetId();
        if (targetId == 652821 || targetId == 652822) {
            if (var1 < 2) {
                qs.setQuestVarById(1, var1 + 1);
                updateQuestStatus(env);
                if (var1 + 1 == 2) {
                    qs.setQuestVar(2);
                    updateQuestStatus(env);
                }
                return true;
            }
        }
        return false;
    }
}
