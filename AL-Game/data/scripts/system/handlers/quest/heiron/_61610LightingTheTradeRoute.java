package quest.heiron;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

public class _61610LightingTheTradeRoute extends QuestHandler {
    private final static int questId = 61610;
    private final static int[] FIRES = { 703506, 703507, 703508, 703509, 703510, 703511 };
    private final static int DOARUNERK = 820143;
    public _61610LightingTheTradeRoute() { super(questId); }
    @Override
    public void register() {
        for (int fire : FIRES) qe.registerQuestNpc(fire).addOnTalkEvent(questId);
        qe.registerQuestNpc(DOARUNERK).addOnTalkEvent(questId);
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
            for (int fire : FIRES) {
                if (targetId == fire) {
                    if (dialog == DialogAction.USE_OBJECT) {
                        int var = qs.getQuestVarById(0);
                        if (var < 6) {
                            qs.setQuestVar(var + 1);
                            updateQuestStatus(env);
                            if (var + 1 == 6) {
                                qs.setStatus(QuestStatus.REWARD);
                                updateQuestStatus(env);
                            }
                            return true;
                        }
                    }
                }
            }
        } else if (qs.getStatus() == QuestStatus.REWARD) {
            if (targetId == DOARUNERK) return sendQuestEndDialog(env);
        }
        return false;
    }
}
