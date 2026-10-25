package quest.brusthonin;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

/**
 * Meet the Reaper Squad - Brusthonin entry quest (Asmodian mirror of 1091).
 * Auto-starts on entering Brusthonin, talk to Surt to complete.
 *
 * @author adapted for Aion780 (no public script exists, written from quest_data)
 */
public class _2091MeettheReaperSquad extends QuestHandler {

    private final static int questId = 2091;

    public _2091MeettheReaperSquad() {
        super(questId);
    }

    @Override
    public void register() {
        qe.registerQuestNpc(205150).addOnTalkEvent(questId); // Surt
        qe.registerOnEnterWorld(questId);
    }

    @Override
    public boolean onDialogEvent(QuestEnv env) {
        final Player player = env.getPlayer();
        final QuestState qs = player.getQuestStateList().getQuestState(questId);
        if (qs == null)
            return false;

        int targetId = 0;
        if (env.getVisibleObject() instanceof Npc)
            targetId = ((Npc) env.getVisibleObject()).getNpcId();
        if (targetId != 205150)
            return false;
        if (qs.getStatus() == QuestStatus.START) {
            if (env.getDialogId() == 31) // QUEST_SELECT
                return sendQuestDialog(env, 10002);
            else if (env.getDialogId() == 1009) { // SELECT_ACTION_1009
                qs.setStatus(QuestStatus.REWARD);
                qs.setQuestVarById(0, 1);
                updateQuestStatus(env);
                return sendQuestDialog(env, 5);
            }
            return false;
        } else if (qs.getStatus() == QuestStatus.REWARD) {
            if (env.getDialogId() == 23) { // SELECTED_QUEST_NOREWARD
                // retail 7.9: chain is sequential 2091->2092->2093->2094
                QuestService.startQuest(new QuestEnv(env.getVisibleObject(), env.getPlayer(), 2092, env.getDialogId()), QuestStatus.LOCKED);
            }
            return sendQuestEndDialog(env);
        }
        return false;
    }

    @Override
    public boolean onEnterWorldEvent(QuestEnv env) {
        Player player = env.getPlayer();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        if (player.getWorldId() != 220050000)
            return false;

        boolean lvlCheck = QuestService.checkLevelRequirement(env.getQuestId(), player.getCommonData().getLevel());
        if (qs == null && lvlCheck) {
            QuestService.startQuest(env, QuestStatus.START);
            return true;
        }
        return false;
    }
}
