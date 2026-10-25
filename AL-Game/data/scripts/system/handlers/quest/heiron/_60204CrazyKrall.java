package quest.heiron;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

public class _60204CrazyKrall extends QuestHandler {
    private final static int questId = 60204;
    private final static int MABANGTAH = 204020;
    private final static int HOKUMA = 204618;
    private final static int FIGHTING_ROYER = 820041;
    private final static int YAVLANTIA = 800413;
    private final static int[] mobs = { 652958, 212247 };
    public _60204CrazyKrall() { super(questId); }
    @Override
    public void register() {
        qe.registerOnLevelUp(questId);
        qe.registerQuestNpc(MABANGTAH).addOnTalkEvent(questId);
        qe.registerQuestNpc(HOKUMA).addOnTalkEvent(questId);
        qe.registerQuestNpc(FIGHTING_ROYER).addOnTalkEvent(questId);
        qe.registerQuestNpc(YAVLANTIA).addOnTalkEvent(questId);
        for (int mob : mobs) qe.registerQuestNpc(mob).addOnKillEvent(questId);
        qe.registerQuestItem(182216257, questId);
    }
            @Override
        public boolean onLvlUpEvent(QuestEnv env) { return defaultOnLvlUpEvent(env, 60203, true); }
    @Override
    public boolean onDialogEvent(QuestEnv env) {
        Player player = env.getPlayer();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        if (qs == null) return false;
        int targetId = env.getTargetId();
        DialogAction dialog = env.getDialog();
        int var = qs.getQuestVarById(0);
        if (qs.getStatus() == QuestStatus.START) {
            if (targetId == MABANGTAH && var == 0) {
                if (dialog == DialogAction.QUEST_SELECT) return sendQuestDialog(env, 1011);
                if (dialog == DialogAction.SETPRO1) { giveQuestItem(env, 182216257, 1); qs.setQuestVar(1); updateQuestStatus(env); return closeDialogWindow(env); }
            } else if (targetId == HOKUMA && var == 2) {
                if (dialog == DialogAction.QUEST_SELECT) return sendQuestDialog(env, 1693);
                if (dialog == DialogAction.SETPRO3) { qs.setQuestVar(3); updateQuestStatus(env); return closeDialogWindow(env); }
            } else if (targetId == FIGHTING_ROYER && var == 3) {
                if (dialog == DialogAction.QUEST_SELECT) return sendQuestDialog(env, 2375);
                if (dialog == DialogAction.SET_SUCCEED) { qs.setStatus(QuestStatus.REWARD); updateQuestStatus(env); return closeDialogWindow(env); }
            }
        } else if (qs.getStatus() == QuestStatus.REWARD) {
            if (targetId == YAVLANTIA) return sendQuestEndDialog(env);
        }
        return false;
    }
    @Override
    public boolean onKillEvent(QuestEnv env) {
        Player player = env.getPlayer();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        if (qs == null || qs.getStatus() != QuestStatus.START) return false;
        if (qs.getQuestVarById(0) != 1) return false;
        int targetId = env.getTargetId();
        if (targetId == 652958) {
            qs.setQuestVar(2);
            updateQuestStatus(env);
            return true;
        }
        return false;
    }
}
