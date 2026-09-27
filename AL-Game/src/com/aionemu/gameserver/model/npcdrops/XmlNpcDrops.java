package com.aionemu.gameserver.model.npcdrops;

import java.util.List;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
public class XmlNpcDrops {
    @XmlAttribute(name = "npc_id")
    private int npcId;

    @XmlAttribute(name = "id")
    private int id;

    @XmlElement(name = "group")
    private List<XmlDropGroup> group;

    @XmlElement(name = "drop_group")
    private List<XmlDropGroup> dropGroup;

    @XmlElement(name = "common_drop_group")
    private List<XmlCommonDropGroup> commonDropGroup;

    @XmlAnyElement(lax = true)
    private List<Object> any;

    public int getNpcId() {
        if (npcId != 0) return npcId;
        return id;
    }

    public List<XmlDropGroup> getDropGroup() {
        // merge group + drop_group + common_drop_group into one list for backward compat
        java.util.ArrayList<XmlDropGroup> all = new java.util.ArrayList<>();
        if (group != null) all.addAll(group);
        if (dropGroup != null) all.addAll(dropGroup);
        if (commonDropGroup != null) {
            for (XmlCommonDropGroup c : commonDropGroup) {
                // common_drop_group in retail is reference to common_drop_groups.xml
                // We keep it as separate, but for XmlNpcDropData we treat it as empty or as reference
                // The actual drops are resolved via common_drop_groups.xml in DataManager
                // For now, create a dummy group that will be resolved later via name
                // If c contains embedded items, convert them
                if (c != null && c.getGroup() != null) {
                    all.addAll(c.getGroup());
                }
            }
        }
        return all;
    }

    public List<XmlDropGroup> getRawDropGroup() {
        return dropGroup;
    }

    public List<XmlDropGroup> getRawGroup() {
        return group;
    }

    public List<XmlCommonDropGroup> getCommonDropGroup() {
        return commonDropGroup;
    }
}
