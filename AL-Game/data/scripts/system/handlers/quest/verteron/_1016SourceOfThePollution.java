package quest.verteron;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Rhys2002 + Nephis (aion-unique), adapted for Aion780
 */
public class _1016SourceOfThePollution extends QuestHandler {

    private final static int questId = 1016;

    public _1016SourceOfThePollution() {
        super(questId);
    }

    @Override
    public void register() {
        qe.registerOnLevelUp(questId);
        qe.registerQuestNpc(210318).addOnKillEvent(questId); // Tursin Fanatic
        int[] talkNpcs = { 203149, 203148, 203832, 203705, 203822, 203761, 203098, 203195 };
        for (int id : talkNpcs) {
            qe.registerQuestNpc(id).addOnTalkEvent(questId);
        }
    }

    @Override
    public boolean onLvlUpEvent(QuestEnv env) {
        Player player = env.getPlayer();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        if (qs == null || player.getLevel() < 11 || qs.getStatus() != QuestStatus.LOCKED) return false;
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

        if (qs.getStatus() == QuestStatus.REWARD) {
            if (targetId == 203098) {
                if (dialog == DialogAction.USE_OBJECT) {
                    return sendQuestDialog(env, 4080);
                } else if (dialog == DialogAction.SELECT_QUEST_REWARD) {
                    return sendQuestDialog(env, 5);
                } else {
                    return sendQuestEndDialog(env);
                }
            }
            return false;
        } else if (qs.getStatus() == QuestStatus.START) {
            switch (targetId) {
                case 203149: // Phomona
                    if (dialog == DialogAction.QUEST_SELECT) {
                        if (var == 0) return sendQuestDialog(env, 1011);
                        else if (var == 2) return sendQuestDialog(env, 1693);
                        else if (var == 7) return sendQuestDialog(env, 3398);
                    } else if (dialog == DialogAction.SETPRO1 && var == 0) {
                        return defaultCloseDialog(env, 0, 1);
                    } else if (dialog == DialogAction.SETPRO2 && var == 2) {
                        return defaultCloseDialog(env, 2, 3);
                    } else if (dialog == DialogAction.SETPRO8 && var == 7) {
                        return defaultCloseDialog(env, 7, 8);
                    }
                    break;
                case 203148: // Nanis
                    if (dialog == DialogAction.QUEST_SELECT && var == 1) {
                        return sendQuestDialog(env, 1352);
                    } else if (dialog == DialogAction.SETPRO2 && var == 1) {
                        qs.setQuestVarById(0, 2);
                        updateQuestStatus(env);
                        giveQuestItem(env, 182200017, 1); // contaminated water
                        return closeDialogWindow(env);
                    }
                    break;
                case 203832: // Malodor
                    if (dialog == DialogAction.QUEST_SELECT && var == 3) {
                        return sendQuestDialog(env, 2034);
                    } else if (dialog == DialogAction.SETPRO4 && var == 3) {
                        qs.setQuestVarById(0, 4);
                        updateQuestStatus(env);
                        giveQuestItem(env, 182200013, 1); // guide to rare poisons
                        return closeDialogWindow(env);
                    }
                    break;

                case 203705: // Spatalos
                    if (dialog == DialogAction.QUEST_SELECT && var == 4) {
                        return sendQuestDialog(env, 2375);
                    } else if (dialog == DialogAction.SETPRO5 && var == 4) {
                        return defaultCloseDialog(env, 4, 5);
                    }
                    break;
                case 203822: // Kato
                    if (dialog == DialogAction.QUEST_SELECT && var == 5) {
                        return sendQuestDialog(env, 2716);
                    } else if (dialog == DialogAction.SETPRO6 && var == 5) {
                        qs.setQuestVarById(0, 6);
                        updateQuestStatus(env);
                        removeQuestItem(env, 182200017, 1);
                        giveQuestItem(env, 182200018, 1); // letter of introduction
                        return closeDialogWindow(env);
                    }
                    break;
                case 203761: // Demro
                    if (dialog == DialogAction.QUEST_SELECT && var == 6) {
                        return sendQuestDialog(env, 3057);
                    } else if (dialog == DialogAction.SETPRO7 && var == 6) {
                        qs.setQuestVarById(0, 7);
                        updateQuestStatus(env);
                        removeQuestItem(env, 182200018, 1);
                        giveQuestItem(env, 182200014, 1); // Yustiel's Tear
                        return closeDialogWindow(env);
                    }
                    break;
                case 203195: // Poisoned Anasia
                    if (dialog == DialogAction.QUEST_SELECT && var == 9) {
                        return sendQuestDialog(env, 3739);
                    } else if (dialog == DialogAction.SETPRO9 && var == 9) {
                        qs.setStatus(QuestStatus.REWARD);
                        updateQuestStatus(env);
                        removeQuestItem(env, 182200015, 2);
                        giveQuestItem(env, 182200016, 1); // poison research diary
                        final Npc npc = (Npc) env.getVisibleObject();
                        ThreadPoolManager.getInstance().schedule(new Runnable() {
                            @Override
                            public void run() {
                                npc.getController().onDelete();
                            }
                        }, 40000);
                        return closeDialogWindow(env);
                    }
                    break;
            }
        }
        return false;
    }

    @Override
    public boolean onKillEvent(QuestEnv env) {
        Player player = env.getPlayer();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        if (qs == null || qs.getStatus() != QuestStatus.START) return false;
        final int instanceId = player.getInstanceId();
        int var = qs.getQuestVarById(0);
        int targetId = env.getTargetId();
        final Npc npc = (Npc) env.getVisibleObject();

        if (targetId == 210318 && var == 8) {
            qs.setQuestVar(9);
            updateQuestStatus(env);
            ThreadPoolManager.getInstance().schedule(new Runnable() {
                @Override
                public void run() {
                    QuestService.addNewSpawn(210030000, instanceId, 203195, (float) npc.getX(),
                            (float) npc.getY(), (float) npc.getZ(), (byte) 0, 30);
                }
            }, 5000);
            return true;
        }
        return false;
    }
}
