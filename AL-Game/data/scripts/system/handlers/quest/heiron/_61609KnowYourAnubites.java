package quest.heiron;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

public class _61609KnowYourAnubites extends QuestHandler {
    private final static int questId = 61609;
    private final static int ERATO = 204630;
    public _61609KnowYourAnubites() { super(questId); }
    @Override
    public void register() {
        qe.registerQuestNpc(ERATO).addOnQuestStart(questId);
        qe.registerQuestNpc(ERATO).addOnTalkEvent(questId);
        qe.registerQuestItem(182216600, questId);
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
        if (qs.getStatus() == QuestStatus.START && targetId == ERATO) {
            if (dialog == DialogAction.QUEST_SELECT) return sendQuestDialog(env, 1011);
            if (dialog == DialogAction.CHECK_USER_HAS_QUEST_ITEM) {
                long cnt = player.getInventory().getItemCountByItemId(182216600);
                if (cnt >= 10) {
                    qs.setStatus(QuestStatus.REWARD);
                    updateQuestStatus(env);
                    return sendQuestDialog(env, 5);
                } else return sendQuestDialog(env, 10001);
            }
        } else if (qs.getStatus() == QuestStatus.REWARD && targetId == ERATO) {
            return sendQuestEndDialog(env);
        }
        return false;
    }
}
