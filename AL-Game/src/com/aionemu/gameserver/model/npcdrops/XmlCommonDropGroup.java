package com.aionemu.gameserver.model.npcdrops;

import java.util.List;
import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class XmlCommonDropGroup {

    @XmlAttribute(name = "name")
    private String name;

    @XmlAttribute(name = "common_drop_adjustment")
    private int commonDropAdjustment = 100;

    @XmlElement(name = "group")
    private List<XmlDropGroup> group;

    @XmlElement(name = "item")
    private List<XmlDrop> item;

    @XmlElement(name = "drop")
    private List<XmlDrop> drop;

    @XmlAnyAttribute
    private java.util.Map<javax.xml.namespace.QName, String> otherAttributes;

    @XmlAnyElement(lax = true)
    private List<Object> any;

    public String getName() { return name; }
    public int getCommonDropAdjustment() { return commonDropAdjustment; }
    public List<XmlDropGroup> getGroup() { return group; }
    public List<XmlDrop> getItem() { return item; }
    public List<XmlDrop> getDrop() { return drop; }
}
