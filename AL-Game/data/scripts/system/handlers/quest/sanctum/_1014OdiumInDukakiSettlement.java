package quest.sanctum;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

public class _1014OdiumInDukakiSettlement extends QuestHandler {
    private final static int questId = 1014;
    private final static int LETO = 203700; // Leto
    public _1014OdiumInDukakiSettlement() { super(questId); }
    @Override
    public void register() {
        qe.registerOnLevelUp(questId);
        qe.registerQuestNpc(LETO).addOnTalkEvent(questId);
    }
    @Override
    public boolean onLvlUpEvent(QuestEnv env) { return defaultOnLvlUpEvent(env, questId, false); }
    @Override
    public boolean onDialogEvent(QuestEnv env) {
        Player player = env.getPlayer();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        if (qs == null) return false;
        int targetId = env.getTargetId();
        DialogAction dialog = env.getDialog();
        if (qs.getStatus() == QuestStatus.START) {
            if (targetId == LETO) {
                if (qs.getQuestVarById(0) == 0) {
                    if (dialog == DialogAction.QUEST_SELECT) return sendQuestDialog(env, 1011);
                    if (dialog == DialogAction.SETPRO1) {
                        qs.setQuestVar(1);
                        updateQuestStatus(env);
                        return closeDialogWindow(env);
                    }
                } else if (qs.getQuestVarById(0) == 1) {
                    if (dialog == DialogAction.QUEST_SELECT) return sendQuestDialog(env, 1352);
                    if (dialog == DialogAction.CHECK_USER_HAS_QUEST_ITEM) {
                        // real items from quest_data.xml: 182200011, 182200012
                        if (QuestService.collectItemCheck(env, true)) {
                            qs.setStatus(QuestStatus.REWARD);
                            updateQuestStatus(env);
                            return sendQuestDialog(env, 10002);
                        } else {
                            return sendQuestDialog(env, 10001);
                        }
                    }
                }
            }
        } else if (qs.getStatus() == QuestStatus.REWARD) {
            if (targetId == LETO) return sendQuestEndDialog(env);
        }
        return false;
    }
}