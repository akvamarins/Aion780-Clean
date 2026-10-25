package quest.heiron;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

public class _60206HiddenFortress extends QuestHandler {
    private final static int questId = 60206;
    private final static int FIRST_CLUE = 820047;
    private final static int SECOND_CLUE = 820048;
    private final static int WAITING_ROYER = 820046;
    private final static int BARANTINA = 802050;
    public _60206HiddenFortress() { super(questId); }
    @Override
    public void register() {
        qe.registerOnLevelUp(questId);
        qe.registerQuestNpc(FIRST_CLUE).addOnTalkEvent(questId);
        qe.registerQuestNpc(SECOND_CLUE).addOnTalkEvent(questId);
        qe.registerQuestNpc(WAITING_ROYER).addOnTalkEvent(questId);
        qe.registerQuestNpc(BARANTINA).addOnTalkEvent(questId);
        qe.registerQuestItem(182216259, questId);
    }
            @Override
        public boolean onLvlUpEvent(QuestEnv env) { return defaultOnLvlUpEvent(env, 60205, true); }
    @Override
    public boolean onDialogEvent(QuestEnv env) {
        Player player = env.getPlayer();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        if (qs == null) return false;
        int targetId = env.getTargetId();
        DialogAction dialog = env.getDialog();
        int var = qs.getQuestVarById(0);
        if (qs.getStatus() == QuestStatus.START) {
            if (targetId == FIRST_CLUE && var == 0) {
                if (dialog == DialogAction.USE_OBJECT) {
                    qs.setQuestVar(3);
                    updateQuestStatus(env);
                    return true;
                }
            } else if (targetId == SECOND_CLUE && var == 3) {
                if (dialog == DialogAction.USE_OBJECT) {
                    qs.setQuestVar(4);
                    updateQuestStatus(env);
                    return true;
                }
            } else if (targetId == WAITING_ROYER) {
                if (var == 4) {
                    if (dialog == DialogAction.QUEST_SELECT) return sendQuestDialog(env, 2375);
                    if (dialog == DialogAction.SETPRO5) {
                        giveQuestItem(env, 182216259, 1);
                        qs.setQuestVar(5);
                        updateQuestStatus(env);
                        return closeDialogWindow(env);
                    }
                } else if (var == 5) {
                    if (dialog == DialogAction.QUEST_SELECT) return sendQuestDialog(env, 2716);
                    if (dialog == DialogAction.CHECK_USER_HAS_QUEST_ITEM) {
                        long cnt = player.getInventory().getItemCountByItemId(182216259);
                        if (cnt >= 1) {
                            qs.setStatus(QuestStatus.REWARD);
                            updateQuestStatus(env);
                            return sendQuestDialog(env, 5);
                        } else return sendQuestDialog(env, 10001);
                    }
                }
            }
        } else if (qs.getStatus() == QuestStatus.REWARD) {
            if (targetId == BARANTINA) return sendQuestEndDialog(env);
        }
        return false;
    }
}
