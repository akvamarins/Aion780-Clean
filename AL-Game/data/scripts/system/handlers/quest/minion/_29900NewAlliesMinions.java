package quest.minion;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.services.item.ItemService;

public class _29900NewAlliesMinions extends QuestHandler {

    private final static int questId = 29900;

    public _29900NewAlliesMinions() {
        super(questId);
    }

    @Override
    public void register() {
        qe.registerQuestNpc(836074).addOnQuestStart(questId);
        qe.registerQuestNpc(836074).addOnTalkEvent(questId);
        qe.registerOnLevelUp(questId);
        qe.registerOnEnterWorld(questId);
    }

    @Override
    public boolean onLvlUpEvent(QuestEnv env) {
        Player player = env.getPlayer();
        if (player == null) return false;
        if (player.getRace() != Race.ASMODIANS) return false;
        if (player.getLevel() < 10) return false;
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        if (qs == null || qs.getStatus() == QuestStatus.NONE) {
            return QuestService.startQuest(env);
        }
        return false;
    }

    @Override
    public boolean onEnterWorldEvent(QuestEnv env) {
        Player player = env.getPlayer();
        if (player == null) return false;
        if (player.getRace() != Race.ASMODIANS) return false;
        if (player.getLevel() < 10) return false;
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        if (qs == null) {
            return QuestService.startQuest(env);
        }
        return false;
    }

    @Override
    public boolean onDialogEvent(QuestEnv env) {
        Player player = env.getPlayer();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        if (player.getRace() != Race.ASMODIANS) {
            return false;
        }
        int targetId = env.getTargetId();
        DialogAction dialog = env.getDialog();

        if (qs == null || qs.getStatus() == QuestStatus.NONE) {
            if (targetId == 836074) {
                if (dialog == DialogAction.QUEST_SELECT) {
                    return sendQuestDialog(env, 1011);
                } else {
                    if (QuestService.startQuest(env)) {
                        giveQuestItem(env, 190080010, 1);
                        return sendQuestDialog(env, 1003);
                    } else {
                        ItemService.addItem(player, 190080010, 1);
                        return sendQuestDialog(env, 1003);
                    }
                }
            }
            return false;
        }

        int var = qs.getQuestVarById(0);

        if (qs.getStatus() == QuestStatus.START) {
            if (targetId == 836074) {
                switch (dialog) {
                    case QUEST_SELECT: {
                        if (var == 0) {
                            return sendQuestDialog(env, 1011);
                        }
                        if (var == 1) {
                            return sendQuestDialog(env, 1352);
                        }
                        return false;
                    }
                    case SETPRO1: {
                        if (var == 0) {
                            qs.setQuestVar(1);
                            updateQuestStatus(env);
                            return sendQuestDialog(env, 1352);
                        }
                        return false;
                    }
                    case SELECT_QUEST_REWARD: {
                        qs.setQuestVar(1);
                        qs.setStatus(QuestStatus.REWARD);
                        updateQuestStatus(env);
                        return sendQuestDialog(env, 5);
                    }
                    default:
                        break;
                }
            }
        } else if (qs.getStatus() == QuestStatus.REWARD) {
            if (targetId == 836074) {
                if (dialog == DialogAction.USE_OBJECT) {
                    return sendQuestDialog(env, 5);
                }
                return sendQuestEndDialog(env);
            }
        }
        return false;
    }
}
