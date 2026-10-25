package quest.heiron;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

public class _60202SuspiciousChangarnerkTrader extends QuestHandler {
    private final static int questId = 60202;
    private final static int NAMARINERK = 798074;
    private final static int MOJARUNERK = 798075;
    private final static int LIBERATED_ROYER = 820027;
    public _60202SuspiciousChangarnerkTrader() { super(questId); }
    @Override
    public void register() {
        qe.registerOnLevelUp(questId);
        qe.registerQuestNpc(NAMARINERK).addOnTalkEvent(questId);
        qe.registerQuestNpc(MOJARUNERK).addOnTalkEvent(questId);
        qe.registerQuestNpc(LIBERATED_ROYER).addOnTalkEvent(questId);
    }
            @Override
        public boolean onLvlUpEvent(QuestEnv env) { return defaultOnLvlUpEvent(env, 60201, true); }
    @Override
    public boolean onDialogEvent(QuestEnv env) {
        Player player = env.getPlayer();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        if (qs == null) return false;
        int targetId = env.getTargetId();
        DialogAction dialog = env.getDialog();
        int var = qs.getQuestVarById(0);
        if (qs.getStatus() == QuestStatus.START) {
            if (targetId == NAMARINERK && var == 0) {
                if (dialog == DialogAction.QUEST_SELECT) return sendQuestDialog(env, 1011);
                if (dialog == DialogAction.SETPRO1) { qs.setQuestVar(1); updateQuestStatus(env); return closeDialogWindow(env); }
            } else if (targetId == MOJARUNERK && var == 1) {
                if (dialog == DialogAction.QUEST_SELECT) return sendQuestDialog(env, 1352);
                if (dialog == DialogAction.SET_SUCCEED) { qs.setStatus(QuestStatus.REWARD); updateQuestStatus(env); return closeDialogWindow(env); }
            }
        } else if (qs.getStatus() == QuestStatus.REWARD) {
            if (targetId == LIBERATED_ROYER) return sendQuestEndDialog(env);
        }
        return false;
    }
}
