package ai;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.configs.main.GroupConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.model.templates.chest.ChestTemplate;
import com.aionemu.gameserver.model.templates.chest.KeyItem;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.drop.DropRegistrationService;
import com.aionemu.gameserver.services.drop.DropService;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * Treasure chest AI. Opening a chest needs the key items from chest_templates.xml (itemid 0 = no key needed), then the chest
 * "dies" silently and its drop list is shown to the player (and to the group/alliance members in range).
 * <p>
 * The original code used lambdaj maxFrom(...) to get the highest level of the looters; it is replaced by Java streams
 * (lambdaj does not work with Java 17).
 *
 * @author ATracer, xTz
 */
@AIName("chest")
public class ChestAI2 extends ActionItemNpcAI2 {

	private ChestTemplate chestTemplate;

	@Override
	protected void handleDialogStart(final Player player) {
		chestTemplate = DataManager.CHEST_DATA.getChestTemplate(getNpcId());

		if (chestTemplate == null) {
			return;
		}
		super.handleDialogStart(player);
	}

	@Override
	protected void handleUseItemFinish(Player player) {
		if (chestTemplate == null) {
			chestTemplate = DataManager.CHEST_DATA.getChestTemplate(getNpcId());
			if (chestTemplate == null) {
				return;
			}
		}
		if (analyzeOpening(player)) {
			if (getOwner().isInState(CreatureState.DEAD)) {
				AuditLogger.info(player, "Attempted multiple Chest looting!");
				return;
			}

			AI2Actions.dieSilently(this, player);
			Collection<Player> players = new HashSet<Player>();
			if (player.isInGroup2()) {
				for (Player member : player.getPlayerGroup2().getOnlineMembers()) {
					if (MathUtil.isIn3dRange(member, getOwner(), GroupConfig.GROUP_MAX_DISTANCE)) {
						players.add(member);
					}
				}
			}
			else if (player.isInAlliance2()) {
				for (Player member : player.getPlayerAlliance2().getOnlineMembers()) {
					if (MathUtil.isIn3dRange(member, getOwner(), GroupConfig.GROUP_MAX_DISTANCE)) {
						players.add(member);
					}
				}
			}
			else {
				players.add(player);
			}
			if (players.isEmpty()) {
				players.add(player);
			}
			DropRegistrationService.getInstance().registerDrop(getOwner(), player, getHighestLevel(players), players);
			// level is a byte in this core, getHighestLevel() returns int, matching registerDrop(Npc, Player, int, Collection)
			DropService.getInstance().requestDropList(player, getObjectId());
			super.handleUseItemFinish(player);
		}
		else {
			PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1111301));
		}
	}

	/**
	 * Replacement of lambdaj maxFrom(players).getLevel()
	 */
	private int getHighestLevel(Collection<Player> players) {
		int highest = 1;
		for (Player p : players) {
			highest = Math.max(highest, p.getLevel());
		}
		return highest;
	}

	private boolean analyzeOpening(final Player player) {
		List<KeyItem> keyItems = chestTemplate.getKeyItem();
		int i = 0;
		for (KeyItem keyItem : keyItems) {
			if (keyItem.getItemId() == 0) {
				return true;
			}
			Item item = player.getInventory().getFirstItemByItemId(keyItem.getItemId());
			if (item != null) {
				if (item.getItemCount() != keyItem.getQuantity()) {
					int _i = 0;
					for (Item findedItem : player.getInventory().getItemsByItemId(keyItem.getItemId())) {
						_i += findedItem.getItemCount();
					}
					if (_i < keyItem.getQuantity()) {
						return false;
					}
				}
				i++;
				continue;
			}
			else {
				return false;
			}
		}
		if (i == keyItems.size()) {
			for (KeyItem keyItem : keyItems) {
				player.getInventory().decreaseByItemId(keyItem.getItemId(), keyItem.getQuantity());
			}
			return true;
		}
		return false;
	}

	@Override
	protected void handleDialogFinish(Player player) {
	}
}
