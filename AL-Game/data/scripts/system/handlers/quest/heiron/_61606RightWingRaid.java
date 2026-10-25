package quest.heiron;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

public class _61606RightWingRaid extends QuestHandler {
    private final static int questId = 61606;
    private final static int IDARUS = 820024;
    public _61606RightWingRaid() { super(questId); }
    @Override
    public void register() {
        qe.registerQuestNpc(IDARUS).addOnQuestStart(questId);
        qe.registerQuestNpc(IDARUS).addOnTalkEvent(questId);
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
        if (qs.getStatus() == QuestStatus.START && targetId == IDARUS) {
            if (dialog == DialogAction.QUEST_SELECT) return sendQuestDialog(env, 1011);
            if (dialog == DialogAction.SETPRO1) { qs.setQuestVar(1); updateQuestStatus(env); return closeDialogWindow(env); }
        } else if (qs.getStatus() == QuestStatus.REWARD && targetId == IDARUS) {
            return sendQuestEndDialog(env);
        }
        return false;
    }
}
