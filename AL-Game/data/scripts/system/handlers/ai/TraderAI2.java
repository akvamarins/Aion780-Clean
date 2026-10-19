package ai;

import com.aionemu.gameserver.ai2.AIName;

/**
 * Dialogs of this NPC type are handled by DialogService; quests are handled by QuestEngine.
 * Both are done in GeneralNpcAI2 (TalkEventHandler.onTalk), so no overrides are needed.
 */
@AIName("trader")
public class TraderAI2 extends GeneralNpcAI2 {
}
