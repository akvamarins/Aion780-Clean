package quest.verteron;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author MrPoke (aion-unique), adapted for Aion780
 */
public class _1130SummonsToTheCitadel extends QuestHandler {

    private final static int questId = 1130;

    public _1130SummonsToTheCitadel() {
        super(questId);
    }

    @Override
    public void register() {
        qe.registerQuestNpc(203098).addOnTalkEvent(questId); // Spiros
        qe.registerOnEnterZone(ZoneName.get("VERTERON_CITADEL_210030000"), questId);
    }

    @Override
    public boolean onDialogEvent(QuestEnv env) {
        Player player = env.getPlayer();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        if (qs == null) return false;
        int targetId = env.getTargetId();
        DialogAction dialog = env.getDialog();
        if (targetId != 203098) return false;

        if (qs.getStatus() == QuestStatus.START) {
            if (dialog == DialogAction.QUEST_SELECT) {
                qs.setQuestVar(1);
                qs.setStatus(QuestStatus.REWARD);
                updateQuestStatus(env);
                return sendQuestDialog(env, 1011);
            } else {
                return sendQuestStartDialog(env);
            }
        } else if (qs.getStatus() == QuestStatus.REWARD) {
            if (dialog == DialogAction.SELECTED_QUEST_NOREWARD) {
                int[] ids = { 1011, 1012, 1013, 1014, 1015, 1016, 1017, 1018, 1019, 1020, 1021, 1022, 1023 };
                for (int id : ids) {
                    QuestService.startQuest(new QuestEnv(env.getVisibleObject(), player, id, 0), QuestStatus.LOCKED);
                }
            }
            return sendQuestEndDialog(env);
        }
        return false;
    }

    @Override
    public boolean onEnterZoneEvent(QuestEnv env, ZoneName zoneName) {
        if (zoneName != ZoneName.get("VERTERON_CITADEL_210030000")) return false;
        Player player = env.getPlayer();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        if (qs != null) return false;
        env.setQuestId(questId);
        QuestService.startQuest(env);
        return true;
    }
}
