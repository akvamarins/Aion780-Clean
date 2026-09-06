
package quest.poeta;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

public class _1127AncientCube extends QuestHandler {
    private final static int questId = 1127;
    public _1127AncientCube() { super(questId); }
    @Override public void register() {
        qe.registerQuestNpc(798008).addOnTalkEvent(questId);
        qe.registerQuestNpc(700001).addOnTalkEvent(questId);
    }
    @Override public boolean onDialogEvent(QuestEnv env) {
        Player player = env.getPlayer();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        int targetId = env.getTargetId();
        DialogAction action = env.getDialog();
        if (qs == null || qs.getStatus() == QuestStatus.NONE) {
            if (targetId == 798008) {
                if (action == DialogAction.QUEST_SELECT) return sendQuestDialog(env, 1011);
                if (action == DialogAction.QUEST_ACCEPT_1) return sendQuestStartDialog(env);
            }
        } else if (qs.getStatus() == QuestStatus.START) {
            int var = qs.getQuestVarById(0);
            if (targetId == 700001 && var == 0) {
                if (action == DialogAction.USE_OBJECT) {
                    giveQuestItem(env, 182200215, 1);
                    qs.setQuestVar(1);
                    updateQuestStatus(env);
                    return closeDialogWindow(env);
                }
            } else if (targetId == 798008 && var == 1) {
                if (action == DialogAction.QUEST_SELECT) return sendQuestDialog(env, 1352);
                if (action == DialogAction.SELECT_QUEST_REWARD) {
                    qs.setStatus(QuestStatus.REWARD);
                    updateQuestStatus(env);
                    return sendQuestDialog(env, 5);
                }
            }
        } else if (qs.getStatus() == QuestStatus.REWARD) {
            if (targetId == 798008) {
                try {
                    // Try to expand cube via inventory - works in 7.8 without xml
                    if (player.getInventory() != null) {
                        player.getInventory().setLimit(36);
                    }
                } catch (Exception e) {}
                return sendQuestEndDialog(env);
            }
        }
        return false;
    }
}
