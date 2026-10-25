package quest.heiron;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

public class _61624StrangeLeather extends QuestHandler {
    private final static int questId = 61624;
    private final static int APHESIUS = 204549;
    private final static int SENEA = 204583;
    public _61624StrangeLeather() { super(questId); }
    @Override
    public void register() {
        qe.registerQuestNpc(APHESIUS).addOnQuestStart(questId);
        qe.registerQuestNpc(APHESIUS).addOnTalkEvent(questId);
        qe.registerQuestNpc(SENEA).addOnTalkEvent(questId);
        qe.registerQuestItem(182216601, questId);
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
            if (targetId == APHESIUS && qs.getQuestVarById(0) == 0) {
                if (dialog == DialogAction.QUEST_SELECT) return sendQuestDialog(env, 1011);
                if (dialog == DialogAction.SETPRO1) { qs.setQuestVar(1); updateQuestStatus(env); return closeDialogWindow(env); }
            } else if (targetId == APHESIUS && qs.getQuestVarById(0) == 1) {
                if (dialog == DialogAction.QUEST_SELECT) return sendQuestDialog(env, 1352);
                if (dialog == DialogAction.CHECK_USER_HAS_QUEST_ITEM) {
                    long cnt = player.getInventory().getItemCountByItemId(182216601);
                    if (cnt >= 5) { qs.setStatus(QuestStatus.REWARD); updateQuestStatus(env); return sendQuestDialog(env, 5); }
                    else return sendQuestDialog(env, 10001);
                }
            }
        } else if (qs.getStatus() == QuestStatus.REWARD && targetId == SENEA) {
            return sendQuestEndDialog(env);
        }
        return false;
    }
}
