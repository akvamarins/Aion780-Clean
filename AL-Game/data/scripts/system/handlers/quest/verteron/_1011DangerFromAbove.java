package quest.verteron;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author MrPoke (aion-unique), adapted for Aion780
 */
public class _1011DangerFromAbove extends QuestHandler {

    private final static int questId = 1011;

    public _1011DangerFromAbove() {
        super(questId);
    }

    @Override
    public void register() {
        qe.registerQuestNpc(700091).addOnKillEvent(questId); // Abyss Fragment
        qe.registerQuestNpc(203109).addOnTalkEvent(questId); // Mirka
        qe.registerQuestNpc(203122).addOnTalkEvent(questId); // Hyacinte
        qe.registerOnLevelUp(questId);
    }

    @Override
    public boolean onKillEvent(QuestEnv env) {
        Player player = env.getPlayer();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        if (qs == null || qs.getStatus() != QuestStatus.START) return false;
        int var = qs.getQuestVarById(0);
        int targetId = env.getTargetId();
        if (targetId == 700091) {
            if (var > 0 && var < 4) {
                qs.setQuestVarById(0, var + 1);
                updateQuestStatus(env);
                return true;
            } else if (var == 4) {
                qs.setStatus(QuestStatus.REWARD);
                updateQuestStatus(env);
                return true;
            }
        }
        return false;
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
            if (targetId == 203109) {
                if (dialog == DialogAction.QUEST_SELECT && var == 0) {
                    return sendQuestDialog(env, 1011);
                } else if (dialog == DialogAction.SETPRO1 && var == 0) {
                    return defaultCloseDialog(env, 0, 1);
                }
            } else if (targetId == 203122) {
                if (dialog == DialogAction.QUEST_SELECT && var == 1) {
                    return sendQuestDialog(env, 1352);
                } else if (dialog == DialogAction.SETPRO2 && var == 1) {
                    return defaultCloseDialog(env, 1, 2);
                }
            }
        } else if (qs.getStatus() == QuestStatus.REWARD) {
            if (targetId == 203109) {
                return sendQuestEndDialog(env);
            }
        }
        return false;
    }

    @Override
    public boolean onLvlUpEvent(QuestEnv env) {
        Player player = env.getPlayer();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        if (qs == null || qs.getStatus() != QuestStatus.LOCKED) return false;
        QuestState qs2 = player.getQuestStateList().getQuestState(1130);
        if (qs2 == null || qs2.getStatus() != QuestStatus.COMPLETE) return false;
        qs.setStatus(QuestStatus.START);
        updateQuestStatus(env);
        return true;
    }
}
