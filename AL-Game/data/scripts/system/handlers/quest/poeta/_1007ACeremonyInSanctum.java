/**
 * Fixed for Aion 7.9 - Elyos Ascension Ceremony
 * Previously missing handler caused Elyos to show Pandaemonium quest
 * This handler correctly teleports Elyos to Sanctum (110010000) not Pandaemonium (120010000)
 */
package quest.poeta;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.teleport.TeleportService2;

public class _1007ACeremonyInSanctum extends QuestHandler {

    private final static int questId = 1007;

    public _1007ACeremonyInSanctum() {
        super(questId);
    }

    @Override
    public void register() {
        qe.registerQuestNpc(790001).addOnTalkEvent(questId); // Pernos
        qe.registerQuestNpc(203725).addOnTalkEvent(questId); // Jucleas in Sanctum
        qe.registerQuestNpc(203989).addOnTalkEvent(questId); // Lephar in Sanctum
        qe.registerOnLevelUp(questId);
        qe.registerOnEnterWorld(questId);
    }

    @Override
    public boolean onDialogEvent(QuestEnv env) {
        Player player = env.getPlayer();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        if (qs == null) {
            return false;
        }
        int targetId = env.getTargetId();
        DialogAction action = env.getDialog();

        if (qs.getStatus() == QuestStatus.START) {
            int var = qs.getQuestVarById(0);
            if (targetId == 790001) { // Pernos in Poeta
                if (var == 0) {
                    switch (action) {
                        case QUEST_SELECT:
                            return sendQuestDialog(env, 1011);
                        case SETPRO1:
                            // FIXED: Teleport to Sanctum 110010000, NOT Pandaemonium 120010000
                            TeleportService2.teleportTo(player, 110010000, 1322.5433f, 1511.9865f, 89.662254f, (byte) 105, TeleportAnimation.BEAM_ANIMATION);
                            qs.setQuestVar(1);
                            updateQuestStatus(env);
                            return closeDialogWindow(env);
                        default:
                            break;
                    }
                }
            } else if (targetId == 203725) { // Jucleas in Sanctum
                if (var == 1) {
                    switch (action) {
                        case QUEST_SELECT:
                            return sendQuestDialog(env, 1352);
                        case SETPRO2:
                            qs.setQuestVar(2);
                            updateQuestStatus(env);
                            return closeDialogWindow(env);
                        default:
                            break;
                    }
                }
            } else if (targetId == 203989) { // Lephar
                if (var == 2) {
                    switch (action) {
                        case QUEST_SELECT:
                            return sendQuestDialog(env, 1693);
                        case SET_SUCCEED:
                            qs.setStatus(QuestStatus.REWARD);
                            updateQuestStatus(env);
                            return closeDialogWindow(env);
                        default:
                            break;
                    }
                }
            }
        } else if (qs.getStatus() == QuestStatus.REWARD) {
            if (targetId == 203989 || targetId == 203725) {
                return sendQuestEndDialog(env);
            }
        }
        return false;
    }

    @Override
    public boolean onLvlUpEvent(QuestEnv env) {
        return defaultOnLvlUpEvent(env, 1006, true);
    }
}
