package quest.heiron;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.world.zone.ZoneName;

public class _61601InTheAetherFlow extends QuestHandler {
    private final static int questId = 61601;
    private final static int AMENE = 204513;
    private final static int APHESIUS = 204549;
    public _61601InTheAetherFlow() { super(questId); }
    @Override
    public void register() {
        qe.registerOnLevelUp(questId);
        qe.registerQuestNpc(AMENE).addOnTalkEvent(questId);
        qe.registerQuestNpc(APHESIUS).addOnTalkEvent(questId);
        qe.registerOnEnterZone(ZoneName.get("HEIRON_OBSERVATORY_210040000"), questId);
    }
            @Override
        public boolean onLvlUpEvent(QuestEnv env) { return false; }
    @Override
    public boolean onEnterZoneEvent(QuestEnv env, ZoneName zoneName) {
        if (zoneName != ZoneName.get("HEIRON_OBSERVATORY_210040000")) return false;
        Player player = env.getPlayer();
        if (player == null) return false;
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        if (qs != null && qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 2) {
            qs.setStatus(QuestStatus.REWARD);
            updateQuestStatus(env);
            return true;
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
        if (qs.getStatus() == QuestStatus.START) {
            if (targetId == AMENE && qs.getQuestVarById(0) == 0) {
                if (dialog == DialogAction.QUEST_SELECT) return sendQuestDialog(env, 1011);
                if (dialog == DialogAction.SETPRO1) { qs.setQuestVar(1); updateQuestStatus(env); return closeDialogWindow(env); }
            } else if (targetId == AMENE && qs.getQuestVarById(0) == 1) {
                if (dialog == DialogAction.QUEST_SELECT) return sendQuestDialog(env, 1352);
                if (dialog == DialogAction.SETPRO2) { qs.setQuestVar(2); updateQuestStatus(env); return closeDialogWindow(env); }
            }
        } else if (qs.getStatus() == QuestStatus.REWARD) {
            if (targetId == APHESIUS) return sendQuestEndDialog(env);
        }
        return false;
    }
}
