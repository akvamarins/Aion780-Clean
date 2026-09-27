
package com.aionemu.gameserver.model.npcdrops;

import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class XmlDrop {

    @XmlAttribute(name = "item_id", required = true)
    private int itemId;
    @XmlAttribute(name = "min_count")
    private int minCount = 1;
    @XmlAttribute(name = "max_count")
    private int maxCount = 1;
    @XmlAttribute(name = "chance")
    private float chance = 100f;
    @XmlAttribute(name = "no_reduce")
    private boolean noReduce = false;
    @XmlAttribute(name = "eachmember")
    private boolean eachMember = false;

    public int getItemId() { return itemId; }
    public int getMinCount() { return minCount; }
    public int getMaxCount() { return maxCount; }
    public float getChance() { return chance; }
    public boolean isNoReduce() { return noReduce; }
    public boolean isEachMember() { return eachMember; }
    public int getMinAmount() { return getMinCount(); }
    public int getMaxAmount() { return getMaxCount(); }
    public boolean isNoReduction() { return isNoReduce(); }
    public boolean isValid() { return itemId > 0 && chance > 0 && maxCount >= minCount; }
}
