package com.aionemu.gameserver.dataholders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.aionemu.gameserver.model.drop.Drop;
import com.aionemu.gameserver.model.drop.DropGroup;
import com.aionemu.gameserver.model.npcdrops.XmlDrop;
import com.aionemu.gameserver.model.npcdrops.XmlDropGroup;
import com.aionemu.gameserver.model.npcdrops.XmlNpcDrops;

@XmlRootElement(name = "npc_drops")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlNpcDropData {

    static Logger log = LoggerFactory.getLogger(XmlNpcDropData.class);
    @XmlElement(name = "npc_drop")
    private List<XmlNpcDrops> nds;

    @XmlElement(name = "group")
    private List<XmlDropGroup> groups;

    @XmlElement(name = "drop_group")
    private List<XmlDropGroup> dropGroups;

    @XmlElement(name = "common_drop_group")
    private List<com.aionemu.gameserver.model.npcdrops.XmlCommonDropGroup> commonDropGroupsTop;

    @XmlAnyElement(lax = true)
    private List<Object> any;

    private HashMap<Integer, ArrayList<DropGroup>> drops;

    void afterUnmarshal(Unmarshaller u, Object parent) {
        this.drops = new HashMap<Integer, ArrayList<DropGroup>>();
        if (this.nds == null) { 
            // could be common_drop_groups.xml merged - ignore
            if ((groups == null || groups.isEmpty()) && (dropGroups == null || dropGroups.isEmpty())) {
                log.warn("[JAVA17 FIX] XmlNpcDropData nds is null, but top-level groups present - skipping");
            }
            return; 
        }
        for (XmlNpcDrops nd : this.nds) {
            if (nd == null || nd.getNpcId() == 0) continue;
            List<XmlDropGroup> dropGroups = nd.getDropGroup();
            if (dropGroups == null) continue;
            List<DropGroup> newDg = new ArrayList<DropGroup>();
            for (XmlDropGroup dg : dropGroups) {
                if (dg == null) continue;
                List<XmlDrop> xmlDrops = dg.getDrops();
                if (xmlDrops == null) xmlDrops = dg.getDrop();
                if (xmlDrops == null) continue;
                List<Drop> dr = new ArrayList<Drop>();
                for (XmlDrop xd : xmlDrops) {
                    if (xd == null) continue;
                    Drop datDg = new Drop(xd.getItemId(), xd.getMinAmount(), xd.getMaxAmount(), xd.getChance(), xd.isNoReduction(), xd.isEachMember());
                    dr.add(datDg);
                }
                if (dr.isEmpty()) continue;
                DropGroup datDg = new DropGroup(dr, dg.getRace(), dg.isUseCategory(), dg.getGroupName());
                newDg.add(datDg);
            }
            if (this.drops.containsKey(Integer.valueOf(nd.getNpcId()))) {
                log.warn("Drop NPC duplicate List ID: " + nd.getNpcId());
            } else {
                this.drops.put(nd.getNpcId(), new ArrayList<DropGroup>());
            }
            this.drops.get(nd.getNpcId()).addAll(newDg);
        }
    }

    public int size() { return this.nds != null ? this.nds.size() : 0; }
    public HashMap<Integer, ArrayList<DropGroup>> getDrops() { return this.drops; }
    public void clear() { if (this.drops != null) this.drops.clear(); this.drops = null; }
}
