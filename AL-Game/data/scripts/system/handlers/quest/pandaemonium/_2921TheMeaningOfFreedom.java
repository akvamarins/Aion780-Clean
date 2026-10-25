package quest.pandaemonium;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

public class _2921TheMeaningOfFreedom extends QuestHandler {
    private final static int questId = 2921;
    private final static int FUCHSIA_ASMO = 204052;
    private final static int TELEMACHUS_ASMO = 204053;
    public _2921TheMeaningOfFreedom() { super(questId); }
    @Override
    public void register() {
        qe.registerOnLevelUp(questId);
        qe.registerOnEnterWorld(questId);
        qe.registerQuestNpc(FUCHSIA_ASMO).addOnTalkEvent(questId);
        qe.registerQuestNpc(TELEMACHUS_ASMO).addOnTalkEvent(questId);
    }
    @Override
    public boolean onLvlUpEvent(QuestEnv env) { return defaultOnLvlUpEvent(env, questId, false); }
    @Override
    public boolean onEnterWorldEvent(QuestEnv env) {
        Player player = env.getPlayer();
        if (player == null) return false;
        QuestState prev = player.getQuestStateList().getQuestState(2920);
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        if (qs == null && prev != null && prev.getStatus() == QuestStatus.COMPLETE) {
            env.setQuestId(questId);
            if (QuestService.startQuest(env)) return true;
        }
        return false;
    }
    @Override
    public boolean onDialogEvent(QuestEnv env) {
        Player player = env.getPlayer();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        if (qs == null) return false;
        int targetId = env.getTargetId();
        DialogAction dialog = env.getDialog();
        int var = qs.getQuestVarById(0);
        if (qs.getStatus() == QuestStatus.START) {
            if (targetId == FUCHSIA_ASMO && var == 0) {
                if (dialog == DialogAction.QUEST_SELECT) return sendQuestDialog(env, 1011);
                if (dialog == DialogAction.SETPRO1) { qs.setQuestVar(1); updateQuestStatus(env); return closeDialogWindow(env); }
            } else if (targetId == TELEMACHUS_ASMO) {
                if (var == 1) {
                    if (dialog == DialogAction.QUEST_SELECT) return sendQuestDialog(env, 1352);
                    if (dialog == DialogAction.SETPRO2) { qs.setQuestVar(2); updateQuestStatus(env); return closeDialogWindow(env); }
                } else if (var == 2) {
                    if (dialog == DialogAction.QUEST_SELECT) return sendQuestDialog(env, 1693);
                    if (dialog == DialogAction.SET_SUCCEED) { qs.setStatus(QuestStatus.REWARD); updateQuestStatus(env); return closeDialogWindow(env); }
                }
            }
        } else if (qs.getStatus() == QuestStatus.REWARD) {
            if (targetId == TELEMACHUS_ASMO) return sendQuestEndDialog(env);
        }
        return false;
    }
}