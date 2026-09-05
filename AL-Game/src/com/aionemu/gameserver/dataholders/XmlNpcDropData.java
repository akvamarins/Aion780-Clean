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

package com.aionemu.gameserver.dataholders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.drop.Drop;
import com.aionemu.gameserver.model.drop.DropGroup;
import com.aionemu.gameserver.model.npcdrops.XmlDrop;
import com.aionemu.gameserver.model.npcdrops.XmlDropGroup;
import com.aionemu.gameserver.model.npcdrops.XmlNpcDrops;

/**
 * @author Falke_34 - fixed for Java 17 retail
 */
@XmlRootElement(name = "npc_drops")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlNpcDropData {

        static Logger log = LoggerFactory.getLogger(XmlNpcDropData.class);
        @XmlElement(name = "npc_drop")
        private List<XmlNpcDrops> nds;
        private HashMap<Integer, ArrayList<DropGroup>> drops;

        void afterUnmarshal(Unmarshaller u, Object parent) {
                this.drops = new HashMap<Integer, ArrayList<DropGroup>>();
                // JAVA 17 FIX: nds can be null if validation failed
                if (this.nds == null) {
                        log.warn("[JAVA17 FIX] XmlNpcDropData nds is null after unmarshal - no drops loaded (check XML validation)");
                        return;
                }
                for (XmlNpcDrops nd : this.nds) {
                        if (nd == null) continue;
                        // JAVA 17 FIX: getDropGroup can return null on JAXB 2.3.1 Java 17
                        List<XmlDropGroup> dropGroups = nd.getDropGroup();
                        if (dropGroups == null) {
                                continue;
                        }
                        List<DropGroup> newDg = new ArrayList<DropGroup>();
                        for (XmlDropGroup dg : dropGroups) {
                                if (dg == null) continue;
                                // JAVA 17 FIX: getDrop() now returns empty list instead of null (fixed in XmlDropGroup)
                                // But keep extra null check for safety
                                List<XmlDrop> xmlDrops = dg.getDrop();
                                if (xmlDrops == null) {
                                        continue;
                                }
                                List<Drop> dr = new ArrayList<Drop>();
                                for (XmlDrop xd : xmlDrops) {
                                        if (xd == null) continue;
                                        Drop datDg = new Drop(xd.getItemId(), xd.getMinAmount(), xd.getMaxAmount(), xd.getChance(), xd.isNoReduction(), xd.isEachMember());
                                        dr.add(datDg);
                                }
                                // Skip empty groups - retail does this
                                if (dr.isEmpty()) continue;
                                DropGroup datDg = new DropGroup(dr, dg.getRace(), dg.isUseCategory(), dg.getGroupName());
                                newDg.add(datDg);
                        }
                        if (this.drops.containsKey(Integer.valueOf(nd.getNpcId()))) {
                                log.warn("Drop NPC duplicate List ID: " + nd.getNpcId());
                        }
                        else {
                                this.drops.put(nd.getNpcId(), new ArrayList<DropGroup>());
                        }
                        this.drops.get(nd.getNpcId()).addAll(newDg);
                }
        }

        public int size() {
                return this.nds != null ? this.nds.size() : 0;
        }

        public HashMap<Integer, ArrayList<DropGroup>> getDrops() {
                return this.drops;
        }

        public void clear() {
                if (this.drops != null) {
                        this.drops.clear();
                }
                this.drops = null;
        }
}
