/**
 * This file is part of Aion-Lightning <aion-lightning.org>.
 *
 *  Aion-Lightning is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Aion-Lightning is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details. *
 *  You should have received a copy of the GNU General Public License
 *  along with Aion-Lightning.
 *  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aionemu.gameserver.services.item;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.controllers.observer.ItemUseObserver;
import com.aionemu.gameserver.model.DescriptionId;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.PersistentState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_INVENTORY_UPDATE_ITEM;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

public class ItemTuningService {

    public void onTuneItem(final Player player, final Item item) {
        // Блокируем повторное использование во время каста
        if (player.getController().getTask(TaskId.ITEM_USE) != null) {
            return;
        }
        if (player.getInventory().getKinah() < (long) ItemTuningService.getReTuningByQuality(item) && item.getOptionalSocket() != -1) {
            PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_NOT_ENOUGH_MONEY);
            return;
        }
        final ItemTemplate template = item.getItemTemplate();
        final int nameId = template.getNameId();
        final int itemObjId = item.getObjectId();

        PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), item.getObjectId(), item.getItemId(), 2000, 0, 0), true);

        final ItemUseObserver moveObserver = new ItemUseObserver() {
            @Override
            public void abort() {
                player.getController().cancelTask(TaskId.ITEM_USE);
                player.getObserveController().removeObserver(this);
                ItemPacketService.updateItemAfterInfoChange(player, item);
                player.removeItemCoolDown(template.getUseLimits().getDelayId());
                PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1300427));
                PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), item.getObjectId(), item.getItemId(), 0, 2, 0), true);
            }
        };

        player.getObserveController().attach(moveObserver);
        player.getController().addTask(TaskId.ITEM_USE, ThreadPoolManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                synchronized (player) {
                    player.getController().cancelTask(TaskId.ITEM_USE);
                    player.getObserveController().removeObserver(moveObserver);

                    // ПРОВЕРКА ЧТО ПРЕДМЕТ ВСЕ ЕЩЕ В ИНВЕНТАРЕ!
                    Item tunedItem = player.getInventory().getItemByObjId(itemObjId);
                    if (tunedItem == null) {
                        return;
                    }
                    if (tunedItem.isEquipped()) {
                        return;
                    }
                    if (tunedItem.getRandomCount() >= tunedItem.getItemTemplate().getRandomBonusCount()) {
                        return;
                    }

                    if (tunedItem.getOptionalSocket() != -1) {
                        if (player.getInventory().getKinah() < (long) getReTuningByQuality(tunedItem)) {
                            return;
                        }
                        player.getInventory().decreaseKinah(getReTuningByQuality(tunedItem));
                    }

                    PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), tunedItem.getObjectId(), tunedItem.getItemId(), 0, 1, 1), true);

                    int rndCount = tunedItem.getRandomCount();
                    tunedItem.setRandomStats(null);
                    tunedItem.setBonusNumber(0);
                    tunedItem.setRandomCount(++rndCount);
                    tunedItem.setOptionalSocket(-1);
                    tunedItem.setOptionalSocket(Rnd.get(0, tunedItem.getItemTemplate().getOptionSlotBonus()));
                    tunedItem.setRndBonus();
                    tunedItem.setPersistentState(PersistentState.UPDATE_REQUIRED);
                    player.getInventory().setPersistentState(PersistentState.UPDATE_REQUIRED);

                    PacketSendUtility.sendPacket(player, new SM_INVENTORY_UPDATE_ITEM(player, tunedItem));
                    PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1401626, new DescriptionId(nameId)));
                }
            }
        }, 5000));
    }

    public static int getReTuningByQuality(Item item) {
        int price = 0;
        switch (item.getItemTemplate().getItemQuality()) {
            case ANCIENT: price = 10000; break;
            case RELIC: price = 50000; break;
            case FINALITY: price = 150000; break;
            default: break;
        }
        return price;
    }

    public static ItemTuningService getInstance() {
        return NewSingletonHolder.INSTANCE;
    }

    private static class NewSingletonHolder {
        private static final ItemTuningService INSTANCE = new ItemTuningService();
    }
}