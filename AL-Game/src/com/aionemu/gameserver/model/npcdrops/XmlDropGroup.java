
package com.aionemu.gameserver.model.npcdrops;

import java.util.List;
import javax.xml.bind.annotation.*;
import com.aionemu.gameserver.model.Race;

@XmlAccessorType(XmlAccessType.FIELD)
public class XmlDropGroup {

    @XmlAttribute(name = "name")
    private String name;

    @XmlAttribute(name = "min_count")
    private int minCount = 1;

    @XmlAttribute(name = "max_count")
    private int maxCount = 1;

    @XmlAttribute(name = "chance")
    private float chance = 100f;

    @XmlAttribute(name = "use_category")
    private boolean useCategory = false;

    @XmlAttribute(name = "group_name")
    private String groupName;

    @XmlAttribute(name = "race")
    private String raceStr;

    @XmlElement(name = "drop")
    private List<XmlDrop> drops;

    public String getName() { return name != null ? name : (groupName != null ? groupName : "default"); }
    public int getMinCount() { return minCount > 0 ? minCount : 1; }
    public int getMaxCount() { return maxCount >= minCount ? maxCount : minCount; }
    public float getChance() { return chance; }
    public boolean isUseCategory() { return useCategory; }
    public List<XmlDrop> getDrops() { return drops; }
    public List<XmlDrop> getDrop() { return drops; }

    public Race getRace() {
        if (raceStr == null || raceStr.isEmpty()) return Race.PC_ALL;
        try { return Race.valueOf(raceStr); } catch(Exception e) { return Race.PC_ALL; }
    }

    public String getGroupName() { return groupName != null ? groupName : getName(); }
    public boolean isValid() { return drops != null && !drops.isEmpty() && chance > 0; }
}
