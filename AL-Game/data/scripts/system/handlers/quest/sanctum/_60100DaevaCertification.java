package quest.sanctum;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.services.teleport.TeleportService2;

public class _60100DaevaCertification extends QuestHandler {

        private final static int questId = 60100;
        private final static int ITEM_SIMPLE_LEGIONARY_CHEST = 188073210;

        public _60100DaevaCertification() {
                super(questId);
        }

        @Override
        public void register() {
                qe.registerOnLevelUp(questId);
                qe.registerQuestNpc(820006).addOnTalkEvent(questId);
                qe.registerQuestNpc(203725).addOnTalkEvent(questId);
                qe.registerQuestNpc(203752).addOnTalkEvent(questId);
                qe.registerQuestNpc(730265).addOnTalkEvent(questId);
                qe.registerQuestNpc(798600).addOnTalkEvent(questId);
        }

        @Override
        public boolean onLvlUpEvent(QuestEnv env) {
                return defaultOnLvlUpEvent(env, 60000, false);
        }

        @Override
        public boolean onDialogEvent(QuestEnv env) {
                Player player = env.getPlayer();
                QuestState qs = player.getQuestStateList().getQuestState(questId);
                DialogAction dialog = env.getDialog();
                int targetId = env.getTargetId();

                if (qs == null) {
                        return false;
                }

                if (qs.getStatus() == QuestStatus.START) {
                        switch (targetId) {
                                case 820006: {
                                        switch (dialog) {
                                                case QUEST_SELECT: {
                                                        return sendQuestDialog(env, 1011);
                                                }
                                                case SETPRO1: {
                                                        qs.setQuestVar(1);
                                                        updateQuestStatus(env);
                                                        TeleportService2.teleportTo(player, 110010000, 1324.164f, 1511.7838f, 567.8750f, (byte) 1, TeleportAnimation.BEAM_ANIMATION);
                                                        return closeDialogWindow(env);
                                                }
                                                default: 
                                                        break;
                                        }
                                        break;
                                }
                                case 203725: {
                                        switch (dialog) {
                                                case QUEST_SELECT: {
                                                        return sendQuestDialog(env, 1352);
                                                }
                                                case SETPRO2: {
                                                        qs.setQuestVar(2);
                                                        updateQuestStatus(env);
                                                        return closeDialogWindow(env);
                                                }
                                                default: 
                                                        break;
                                        }
                                        break;
                                }
                                case 203752: {
                                        switch (dialog) {
                                                case QUEST_SELECT: {
                                                        return sendQuestDialog(env, 1693);
                                                }
                                                case SETPRO3: {
                                                        qs.setQuestVar(3);
                                                        updateQuestStatus(env);
                                                        return closeDialogWindow(env);
                                                }
                                                default: 
                                                        break;
                                        }
                                        break;
                                }
                                case 730265: {
                                        switch (dialog) {
                                                case QUEST_SELECT: {
                                                        return sendQuestDialog(env, 2034);
                                                }
                                                case SET_SUCCEED: {
                                                        qs.setQuestVar(4);
                                                        qs.setStatus(QuestStatus.REWARD);
                                                        updateQuestStatus(env);
                                                        TeleportService2.teleportTo(player, 110020000, 2156.5278f, 1570.3329f, 1204.8967f, (byte) 16, TeleportAnimation.BEAM_ANIMATION);
                                                        return closeDialogWindow(env);
                                                }
                                                default: 
                                                        break;
                                        }
                                        break;
                                }
                                default:
                                        break;
                        }
                } else if (qs.getStatus() == QuestStatus.REWARD) {
                        if (targetId == 798600) {
                                if (dialog == DialogAction.USE_OBJECT) {
                                        return sendQuestDialog(env, 10002);
                                }
                                if (dialog == DialogAction.SELECT_QUEST_REWARD || dialog == DialogAction.SELECTED_QUEST_NOREWARD) {
                                    ItemService.addItem(player, ITEM_SIMPLE_LEGIONARY_CHEST, 1);
                                }
                                return sendQuestEndDialog(env);
                        }
                }

                return false;
        }
}
