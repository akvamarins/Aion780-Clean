package quest.heiron;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

public class _61614GatheringBrokenOdiumPieces extends QuestHandler {
    private final static int questId = 61614;
    private final static int YAVLANTIA = 800413;
    public _61614GatheringBrokenOdiumPieces() { super(questId); }
    @Override
    public void register() {
        qe.registerQuestNpc(YAVLANTIA).addOnTalkEvent(questId);
    }
            @Override
        public boolean onLvlUpEvent(QuestEnv env) { return false; }
    @Override
    public boolean onDialogEvent(QuestEnv env) {
        Player player = env.getPlayer();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        if (qs == null) return false;
        if (qs.getStatus() == QuestStatus.START && env.getTargetId() == YAVLANTIA) {
            if (env.getDialog() == DialogAction.CHECK_USER_HAS_QUEST_ITEM) {
                if (QuestService.collectItemCheck(env, true)) {
                    qs.setStatus(QuestStatus.REWARD);
                    updateQuestStatus(env);
                    return sendQuestDialog(env, 5);
                } else return sendQuestDialog(env, 10001);
            }
        } else if (qs.getStatus() == QuestStatus.REWARD && env.getTargetId() == YAVLANTIA) {
            return sendQuestEndDialog(env);
        }
        return false;
    }
}
