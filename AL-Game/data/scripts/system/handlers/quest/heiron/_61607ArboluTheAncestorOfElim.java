package quest.heiron;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

public class _61607ArboluTheAncestorOfElim extends QuestHandler {
    private final static int questId = 61607;
    private final static int ARBOLU = 204647;
    public _61607ArboluTheAncestorOfElim() { super(questId); }
    @Override
    public void register() {
        qe.registerQuestNpc(ARBOLU).addOnQuestStart(questId);
        qe.registerQuestNpc(ARBOLU).addOnTalkEvent(questId);
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
        if (qs.getStatus() == QuestStatus.START && targetId == ARBOLU) {
            if (dialog == DialogAction.QUEST_SELECT) return sendQuestDialog(env, 1011);
            if (dialog == DialogAction.CHECK_USER_HAS_QUEST_ITEM) {
                return checkQuestItems(env, 0, 0, true, 5, 2716);
            }
        } else if (qs.getStatus() == QuestStatus.REWARD && targetId == ARBOLU) {
            return sendQuestEndDialog(env);
        }
        return false;
    }
}
