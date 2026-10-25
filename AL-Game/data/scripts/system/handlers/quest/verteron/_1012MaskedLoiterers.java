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
 * @author MrPoke + Dune11 (aion-unique), adapted for Aion780
 */
public class _1012MaskedLoiterers extends QuestHandler {

    private final static int questId = 1012;

    public _1012MaskedLoiterers() {
        super(questId);
    }

    @Override
    public void register() {
        qe.registerQuestNpc(203111).addOnTalkEvent(questId); // Hyacinte
        qe.registerOnEnterZone(ZoneName.get("Q1012_210030000"), questId);
        qe.registerOnLevelUp(questId);
    }

    @Override
    public boolean onLvlUpEvent(QuestEnv env) {
        Player player = env.getPlayer();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        if (qs == null || player.getLevel() < 10 || qs.getStatus() != QuestStatus.LOCKED) return false;
        qs.setStatus(QuestStatus.START);
        updateQuestStatus(env);
        return true;
    }

    @Override
    public boolean onDialogEvent(QuestEnv env) {
        Player player = env.getPlayer();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        if (qs == null) return false;
        int var = qs.getQuestVarById(0);
        int targetId = env.getTargetId();
        DialogAction dialog = env.getDialog();

        if (qs.getStatus() == QuestStatus.START) {
            if (targetId == 203111) {
                if (dialog == DialogAction.QUEST_SELECT) {
                    if (var == 0) return sendQuestDialog(env, 1011);
                    else if (var == 2) return sendQuestDialog(env, 1352);
                } else if (dialog == DialogAction.SETPRO1 && var == 0) {
                    return defaultCloseDialog(env, 0, 1);
                } else if (dialog == DialogAction.SETPRO2 && var == 2) {
                    return defaultCloseDialog(env, 2, 3);
                } else if (dialog == DialogAction.CHECK_USER_HAS_QUEST_ITEM && var == 3) {
                    if (QuestService.collectItemCheck(env, true)) {
                        qs.setQuestVarById(0, 4);
                        qs.setStatus(QuestStatus.REWARD);
                        updateQuestStatus(env);
                        return sendQuestDialog(env, 5);
                    } else {
                        return sendQuestDialog(env, 1779);
                    }
                }
            }
        } else if (qs.getStatus() == QuestStatus.REWARD) {
            if (targetId == 203111) {
                return sendQuestEndDialog(env);
            }
        }
        return false;
    }

    @Override
    public boolean onEnterZoneEvent(QuestEnv env, ZoneName zoneName) {
        if (zoneName != ZoneName.get("Q1012_210030000")) return false;
        Player player = env.getPlayer();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        if (qs == null || qs.getQuestVarById(0) != 1) return false;
        env.setQuestId(questId);
        qs.setQuestVarById(0, 2);
        updateQuestStatus(env);
        return true;
    }
}
