package quest.heiron;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

public class _60205DecipheringTheKunpapaDocument extends QuestHandler {
    private final static int questId = 60205;
    private final static int YAVLANTIA = 800413;
    private final static int LAMIDROS = 204610;
    public _60205DecipheringTheKunpapaDocument() { super(questId); }
    @Override
    public void register() {
        qe.registerOnLevelUp(questId);
        qe.registerQuestNpc(YAVLANTIA).addOnTalkEvent(questId);
        qe.registerQuestNpc(LAMIDROS).addOnTalkEvent(questId);
        qe.registerQuestItem(182216258, questId);
    }
            @Override
        public boolean onLvlUpEvent(QuestEnv env) { return defaultOnLvlUpEvent(env, 60204, true); }
    @Override
    public boolean onDialogEvent(QuestEnv env) {
        Player player = env.getPlayer();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        if (qs == null) return false;
        int targetId = env.getTargetId();
        DialogAction dialog = env.getDialog();
        int var = qs.getQuestVarById(0);
        if (qs.getStatus() == QuestStatus.START) {
            if (targetId == YAVLANTIA && var == 0) {
                if (dialog == DialogAction.QUEST_SELECT) return sendQuestDialog(env, 1011);
                if (dialog == DialogAction.SETPRO1) { qs.setQuestVar(1); updateQuestStatus(env); return closeDialogWindow(env); }
            } else if (targetId == YAVLANTIA && var == 1) {
                if (dialog == DialogAction.QUEST_SELECT) return sendQuestDialog(env, 1352);
                if (dialog == DialogAction.SETPRO2) { giveQuestItem(env, 182216258, 1); qs.setQuestVar(2); updateQuestStatus(env); return closeDialogWindow(env); }
            } else if (targetId == LAMIDROS && var == 2) {
                if (dialog == DialogAction.QUEST_SELECT) return sendQuestDialog(env, 1693);
                if (dialog == DialogAction.SET_SUCCEED) { qs.setStatus(QuestStatus.REWARD); updateQuestStatus(env); return closeDialogWindow(env); }
            }
        } else if (qs.getStatus() == QuestStatus.REWARD) {
            if (targetId == LAMIDROS) return sendQuestEndDialog(env);
        }
        return false;
    }
}
