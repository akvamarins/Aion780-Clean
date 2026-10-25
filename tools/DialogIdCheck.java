import com.aionemu.gameserver.model.DialogAction;

public class DialogIdCheck {
    public static void main(String[] a) {
        int[] ids = { 31, 29, 30, 34, 33, 35, 36, 37, 1007, 1008, 1009, 1352, 1353, 1693, 1694, 2035, 2375, 2376, 2378, 3057, 3399, 10255, 8, 23, 10000, 10007, -1, 55, 56 };
        for (int id : ids) {
            DialogAction d = DialogAction.getActionByDialogId(id);
            System.out.println(id + " -> " + (d == null ? "null" : d.name()));
        }
        System.out.println("QUEST_SELECT=" + DialogAction.QUEST_SELECT.id());
        System.out.println("SETPRO1=" + DialogAction.SETPRO1.id());
        System.out.println("CHECK_USER_HAS_QUEST_ITEM=" + DialogAction.CHECK_USER_HAS_QUEST_ITEM.id());
        System.out.println("CHECK_USER_HAS_QUEST_ITEM_SIMPLE=" + DialogAction.CHECK_USER_HAS_QUEST_ITEM_SIMPLE.id());
        System.out.println("SET_SUCCEED=" + DialogAction.SET_SUCCEED.id());
        System.out.println("ASK_QUEST_ACCEPT=" + DialogAction.ASK_QUEST_ACCEPT.id());
        System.out.println("QUEST_ACCEPT=" + DialogAction.QUEST_ACCEPT.id());
        System.out.println("SELECTED_QUEST_NOREWARD=" + DialogAction.SELECTED_QUEST_NOREWARD.id());
        System.out.println("FINISH_DIALOG=" + DialogAction.FINISH_DIALOG.id());
        System.out.println("SELECT_QUEST_REWARD=" + DialogAction.SELECT_QUEST_REWARD.id());
    }
}
