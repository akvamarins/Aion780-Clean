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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.DescriptionId;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.item.upgrade.ItemUpgradeTemplate;
import com.aionemu.gameserver.model.templates.item.upgrade.SubMaterialItem;
import com.aionemu.gameserver.model.templates.item.upgrade.UpgradeResultItem;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.abyss.AbyssPointsService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.audit.AuditLogger;
import javolution.util.FastMap;

public class ItemUpgradeService {

    private static final Logger log = LoggerFactory.getLogger(ItemUpgradeService.class);

    public static boolean decreaseMaterial(Player player, Item baseItem, int resultItemId) {
        synchronized (player) {
            // Проверяем еще раз что предмет все еще в инвентаре
            if (player.getInventory().getItemByObjId(baseItem.getObjectId()) == null) {
                return false;
            }
            
            FastMap<Integer, UpgradeResultItem> resultItemMap = DataManager.ITEM_UPGRADE_DATA.getResultItemMap(baseItem.getItemId());
            if (resultItemMap == null) return false;
            
            UpgradeResultItem resultItem = resultItemMap.get(resultItemId);
            if (resultItem == null) return false;

            // Проверка и списание суб-материалов АТОМАРНО
            if (resultItem.getNeed_kinah() == null) {
                // Сначала проверяем все маты
                for (SubMaterialItem item : resultItem.getUpgrade_materials().getSubMaterialItem()) {
                    if (player.getInventory().getItemCountByItemId(item.getId()) < item.getCount()) {
                        return false;
                    }
                }
                // Потом списываем
                for (SubMaterialItem item : resultItem.getUpgrade_materials().getSubMaterialItem()) {
                    if (!player.getInventory().decreaseByItemId(item.getId(), item.getCount())) {
                        AuditLogger.info(player, "try item upgrade without sub material");
                        return false;
                    }
                }
            }

            // Кинары - ОДИН РАЗ и БЕЗ МИНУСА!
            if (resultItem.getNeed_kinah() != null) {
                long needKinah = resultItem.getNeed_kinah().getCount();
                if (player.getInventory().getKinah() < needKinah) {
                    return false;
                }
                player.getInventory().decreaseKinah(needKinah);
            }

            if (resultItem.getNeed_abyss_point() != null) {
                long needAp = resultItem.getNeed_abyss_point().getCount();
                if (player.getAbyssRank().getAp() < needAp) {
                    return false;
                }
                AbyssPointsService.setAp(player, (int) -needAp);
            }

            // Базовый предмет списываем в самом конце
            if (!player.getInventory().decreaseByObjectId(baseItem.getObjectId(), 1)) {
                return false;
            }
            return true;
        }
    }

    public static boolean checkItemUpgrade(Player player, Item baseItem, int resultItemId) {
        synchronized (player) {
            ItemUpgradeTemplate itemUpgradeTemplate = DataManager.ITEM_UPGRADE_DATA.getItemUpgradeTemplate(baseItem.getItemId());
            if (itemUpgradeTemplate == null) {
                log.warn(resultItemId + " item's itemupgrade template is null");
                return false;
            }
            FastMap<Integer, UpgradeResultItem> resultItemMap = DataManager.ITEM_UPGRADE_DATA.getResultItemMap(baseItem.getItemId());
            if (resultItemMap == null || !resultItemMap.containsKey(resultItemId)) {
                AuditLogger.info(player, resultItemId + " item's baseItem and resultItem is not matched (possible client modify)");
                return false;
            }
            UpgradeResultItem resultItem = resultItemMap.get(resultItemId);
            if (resultItem.getCheck_enchant_count() > 0) {
                if (baseItem.getEnchantOrAuthorizeLevel() < resultItem.getCheck_enchant_count()) {
                    PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_REGISTER_ITEM_MSG_UPGRADE_CANNOT(new DescriptionId(baseItem.getNameId())));
                    return false;
                }
            }
            if (resultItem.getNeed_abyss_point() != null) {
                if (player.getAbyssRank().getAp() < resultItem.getNeed_abyss_point().getCount()) {
                    PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_REGISTER_ITEM_MSG_UPGRADE_CANNOT_NEED_AP);
                    return false;
                }
            }
            if (resultItem.getNeed_kinah() == null) {
                for (SubMaterialItem sub : resultItem.getUpgrade_materials().getSubMaterialItem()) {
                    if (player.getInventory().getItemCountByItemId(sub.getId()) < sub.getCount()) {
                        return false;
                    }
                }
            } else {
                if (player.getInventory().getKinah() < resultItem.getNeed_kinah().getCount()) {
                    PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_REGISTER_ITEM_MSG_UPGRADE_CANNOT_NEED_QINA);
                    return false;
                }
            }
            return true;
        }
    }
}