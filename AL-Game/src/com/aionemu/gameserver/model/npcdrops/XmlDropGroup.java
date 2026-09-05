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

package com.aionemu.gameserver.model.npcdrops;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import com.aionemu.gameserver.model.Race;

/**
 * @author Falke_34 - fixed for Java 17 retail
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlDropGroup {

        @XmlElement(name = "drop")
        protected List<XmlDrop> drop;
        @XmlAttribute
        protected Race race = Race.PC_ALL;
        @XmlAttribute(name = "use_category")
        protected Boolean useCategory = Boolean.valueOf(true);
        @XmlAttribute(name = "name")
        protected String group_name;

        public List<XmlDrop> getDrop() {
                // JAVA 17 FIX: JAXB 2.3.1 returns null instead of empty list on Java 17
                // Retail behavior: return empty list, not null
                if (this.drop == null) {
                        this.drop = new ArrayList<XmlDrop>();
                }
                return this.drop;
        }

        public Race getRace() {
                return this.race;
        }

        public Boolean isUseCategory() {
                return this.useCategory;
        }

        public String getGroupName() {
                if (this.group_name == null) {
                        return "";
                }
                return this.group_name;
        }
}
