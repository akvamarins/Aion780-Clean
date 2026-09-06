
package quest.poeta;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

public class _60008PursuitOfTheOdiumTransportTrack extends QuestHandler {
    private final static int questId = 60008;
    public _60008PursuitOfTheOdiumTransportTrack() { super(questId); }
    @Override public void register() {
        qe.registerQuestNpc(820010).addOnTalkEvent(questId);
        qe.registerQuestNpc(820011).addOnTalkEvent(questId);
        qe.registerQuestNpc(203086).addOnTalkEvent(questId);
        qe.registerOnLevelUp(questId);
        qe.registerOnEnterWorld(questId);
    }
    @Override public boolean onEnterWorldEvent(QuestEnv env) {
        Player player = env.getPlayer();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        if (qs == null) { env.setQuestId(questId); QuestService.startQuest(env); }
        return false;
    }
    @Override public boolean onDialogEvent(final QuestEnv env) {
        Player player = env.getPlayer();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        if (qs == null) return false;
        int targetId = env.getTargetId();
        DialogAction action = env.getDialog();
        if (qs.getStatus() == QuestStatus.START) {
            int var = qs.getQuestVarById(0);
            if (targetId == 820010 && var == 0) {
                switch (action) {
                case USE_OBJECT:
                    changeQuestStep(env, 0, 1, false);
                    return closeDialogWindow(env);
                case QUEST_SELECT:
                    qs.setQuestVar(1); updateQuestStatus(env); return closeDialogWindow(env);
                default: break;
                }
            } else if (targetId == 820011 && var == 1) {
                switch (action) {
                case USE_OBJECT:
                    changeQuestStep(env, 1, 2, true);
                    return closeDialogWindow(env);
                case QUEST_SELECT:
                    qs.setQuestVar(2); qs.setStatus(QuestStatus.REWARD); updateQuestStatus(env); return closeDialogWindow(env);
                default: break;
                }
            }
        } else if (qs.getStatus() == QuestStatus.REWARD) {
            if (targetId == 203086) {
                switch (action) {
                case USE_OBJECT: return sendQuestDialog(env, 10002);
                case SELECT_QUEST_REWARD: return sendQuestDialog(env, 5);
                case SELECTED_QUEST_NOREWARD: return sendQuestEndDialog(env);
                default: break;
                }
            }
        }
        return false;
    }
    @Override public boolean onLvlUpEvent(QuestEnv env) { return defaultOnLvlUpEvent(env); }
}
