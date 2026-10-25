package quest.heiron;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

public class _80004EventHeartHeadbandSport extends QuestHandler {
    private final static int questId = 80004;
    private final static int REGAN = 799776;
    public _80004EventHeartHeadbandSport() { super(questId); }
    @Override
    public void register() {
        qe.registerQuestNpc(REGAN).addOnQuestStart(questId);
        qe.registerQuestNpc(REGAN).addOnTalkEvent(questId);
        qe.registerQuestItem(182215200, questId);
    }
            @Override
        public boolean onLvlUpEvent(QuestEnv env) { return false; }
    @Override
    public boolean onDialogEvent(QuestEnv env) {
        Player player = env.getPlayer();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        int targetId = env.getTargetId();
        DialogAction dialog = env.getDialog();
        if (qs == null || qs.getStatus() == QuestStatus.NONE || qs.canRepeat()) {
            if (targetId == REGAN) {
                if (dialog == DialogAction.QUEST_SELECT) return sendQuestDialog(env, 4762);
                if (dialog == DialogAction.QUEST_ACCEPT_1 || dialog == DialogAction.QUEST_ACCEPT_SIMPLE) return sendQuestStartDialog(env);
            }
        } else if (qs.getStatus() == QuestStatus.START) {
            if (targetId == REGAN) {
                if (dialog == DialogAction.QUEST_SELECT) return sendQuestDialog(env, 1011);
                if (dialog == DialogAction.CHECK_USER_HAS_QUEST_ITEM) {
                    long cnt = player.getInventory().getItemCountByItemId(182215200);
                    if (cnt >= 10) {
                        qs.setStatus(QuestStatus.REWARD);
                        updateQuestStatus(env);
                        return sendQuestDialog(env, 5);
                    } else return sendQuestDialog(env, 10001);
                }
            }
        } else if (qs.getStatus() == QuestStatus.REWARD) {
            if (targetId == REGAN) return sendQuestEndDialog(env);
        }
        return false;
    }
}
