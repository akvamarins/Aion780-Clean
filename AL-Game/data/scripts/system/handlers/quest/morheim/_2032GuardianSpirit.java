package quest.morheim;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

/**
 * Morheim mission 2032 (collect quest, retail drop data from quest_data.xml)
 * Simplified handler: Aegir -> collect -> Aegir
 */
public class _2032GuardianSpirit extends QuestHandler {

    private final static int questId = 2032;
    private final static int NPC_AEGIR = 204301;

    public _2032GuardianSpirit() {
        super(questId);
    }

    @Override
    public void register() {
        qe.registerOnLevelUp(questId);
        qe.registerQuestNpc(NPC_AEGIR).addOnTalkEvent(questId);
    }

    @Override
    public boolean onLvlUpEvent(QuestEnv env) {
        Player player = env.getPlayer();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        if (qs == null || player.getLevel() < 20 || qs.getStatus() != QuestStatus.LOCKED) return false;
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

        if (qs.getStatus() == QuestStatus.START) {
            if (targetId == NPC_AEGIR) {
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
            }
        } else if (qs.getStatus() == QuestStatus.REWARD) {
            if (targetId == NPC_AEGIR) {
                return sendQuestEndDialog(env);
            }
        }
        return false;
    }
}
