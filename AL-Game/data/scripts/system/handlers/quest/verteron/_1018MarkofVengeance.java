package quest.verteron;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

/**
 * @author Rhys2002 + MrPoke (aion-unique), adapted for Aion780
 */
public class _1018MarkofVengeance extends QuestHandler {

    private final static int questId = 1018;

    public _1018MarkofVengeance() {
        super(questId);
    }

    @Override
    public void register() {
        qe.registerOnLevelUp(questId);
        qe.registerQuestNpc(203098).addOnTalkEvent(questId); // Spiros
    }

    @Override
    public boolean onLvlUpEvent(QuestEnv env) {
        Player player = env.getPlayer();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        if (qs == null || player.getLevel() < 13 || qs.getStatus() != QuestStatus.LOCKED) return false;
        qs.setStatus(QuestStatus.START);
        updateQuestStatus(env);
        return true;
    }

    @Override
    public boolean onDialogEvent(QuestEnv env) {
        Player player = env.getPlayer();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        if (qs == null) return false;
        int targetId = env.getTargetId();
        DialogAction dialog = env.getDialog();

        if (targetId == 203098 && qs.getStatus() == QuestStatus.START) {
            if (dialog == DialogAction.QUEST_SELECT) {
                return sendQuestDialog(env, 1011);
            } else if (dialog == DialogAction.CHECK_USER_HAS_QUEST_ITEM) {
                if (QuestService.collectItemCheck(env, true)) {
                    qs.setQuestVarById(0, 1);
                    qs.setStatus(QuestStatus.REWARD);
                    updateQuestStatus(env);
                    return sendQuestDialog(env, 5);
                } else {
                    return sendQuestDialog(env, 1097);
                }
            }
        } else if (qs.getStatus() == QuestStatus.REWARD) {
            if (targetId == 203098) {
                return sendQuestEndDialog(env);
            }
        }
        return false;
    }
}
