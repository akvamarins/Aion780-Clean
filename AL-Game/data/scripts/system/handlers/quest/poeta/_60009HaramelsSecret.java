
package quest.poeta;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.world.zone.ZoneName;

public class _60009HaramelsSecret extends QuestHandler {
    private final static int questId = 60009;
    private final static int[] mobs = { 653196, 653205, 653218 };
    public _60009HaramelsSecret() { super(questId); }
    @Override public void register() {
        qe.registerOnLevelUp(questId);
        qe.registerQuestNpc(820012).addOnTalkEvent(questId);
        qe.registerQuestNpc(820133).addOnTalkEvent(questId);
        qe.registerQuestNpc(799524).addOnTalkEvent(questId);
        qe.registerQuestNpc(820006).addOnTalkEvent(questId);
        qe.registerQuestNpc(700834).addOnTalkEvent(questId);
        qe.registerOnEnterWorld(questId);
        qe.registerOnEnterZone(ZoneName.get("HARAMEL_TOWER_300200000"), questId);
        for (int mob : mobs) qe.registerQuestNpc(mob).addOnKillEvent(questId);
    }
    @Override public boolean onLvlUpEvent(QuestEnv env) { return defaultOnLvlUpEvent(env, 60000, false); }
    @Override public boolean onEnterWorldEvent(QuestEnv env) {
        Player player = env.getPlayer();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        if (qs == null) return false;
        if (qs.getStatus() == QuestStatus.START) {
            if (qs.getQuestVarById(0) == 1 && player.getWorldId() == 300200000) {
                qs.setQuestVar(2); updateQuestStatus(env); return true;
            }
        }
        return false;
    }
    @Override public boolean onEnterZoneEvent(QuestEnv env, ZoneName zoneName) {
        Player player = env.getPlayer();
        if (player == null) return false;
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        if (qs != null && qs.getStatus() == QuestStatus.START) {
            int var = qs.getQuestVarById(0);
            if (var == 4 && zoneName == ZoneName.get("HARAMEL_TOWER_300200000")) {
                changeQuestStep(env, 4, 5, false); return true;
            }
        }
        return false;
    }
    @Override public boolean onDialogEvent(QuestEnv env) {
        Player player = env.getPlayer();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        if (qs == null) return false;
        DialogAction dialog = env.getDialog();
        int targetId = env.getTargetId();
        int var = qs.getQuestVarById(0);
        if (qs.getStatus() == QuestStatus.START) {
            if (targetId == 820012) {
                if (dialog == DialogAction.USE_OBJECT && var == 0) {
                    qs.setQuestVar(1); updateQuestStatus(env); return closeDialogWindow(env);
                }
            } else if (targetId == 820133) {
                if (dialog == DialogAction.USE_OBJECT) return sendQuestDialog(env, 1693);
                if (dialog == DialogAction.SETPRO3 && var == 2) { qs.setQuestVar(3); updateQuestStatus(env); return closeDialogWindow(env); }
            } else if (targetId == 700834) {
                if (dialog == DialogAction.USE_OBJECT && var == 6) {
                    if (player.getInventory().getItemCountByItemId(182216580) < 3) {
                        giveQuestItem(env, 182216580, 3);
                    }
                    return true;
                }
                return true;
            } else if (targetId == 799524) {
                if (dialog == DialogAction.QUEST_SELECT) {
                    if (var == 6) {
                        if (QuestService.collectItemCheck(env, true)) return sendQuestDialog(env, 3057);
                        else return sendQuestDialog(env, 3398);
                    } else if (var == 7) return sendQuestDialog(env, 3057);
                    else return sendQuestDialog(env, 3398);
                }
                if (dialog == DialogAction.CHECK_USER_HAS_QUEST_ITEM) {
                    if (QuestService.collectItemCheck(env, true)) {
                        qs.setQuestVar(7); updateQuestStatus(env); return sendQuestDialog(env, 10000);
                    } else return sendQuestDialog(env, 10001);
                }
                if (dialog == DialogAction.SETPRO8) { qs.setQuestVar(8); updateQuestStatus(env); return closeDialogWindow(env); }
            }
        } else if (qs.getStatus() == QuestStatus.REWARD) {
            if (targetId == 820006) return sendQuestEndDialog(env);
        }
        return false;
    }
    @Override public boolean onKillEvent(QuestEnv env) {
        Player player = env.getPlayer();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        if (qs != null && qs.getStatus() == QuestStatus.START) {
            int var = qs.getQuestVarById(0);
            if (var == 3) return defaultOnKillEvent(env, 653196, 3, 4, 0);
            if (var == 5) return defaultOnKillEvent(env, 653205, 5, 6, 0);
            if (var == 8) { qs.setQuestVar(9); qs.setStatus(QuestStatus.REWARD); updateQuestStatus(env); return true; }
        }
        return false;
    }
}
