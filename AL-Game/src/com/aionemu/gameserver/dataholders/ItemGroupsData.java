package com.aionemu.gameserver.dataholders;

import java.util.*;
import javax.xml.bind.annotation.*;
import com.aionemu.gameserver.model.templates.itemgroups.*;
import com.aionemu.gameserver.model.templates.rewards.BonusType;
import org.w3c.dom.Element;

@XmlRootElement(name = "item_groups")
@XmlAccessorType(XmlAccessType.FIELD)
public class ItemGroupsData {

    @XmlElement(name = "craft_groups")
    protected List<CraftItemGroup> craftGroups;

    @XmlElement(name = "bonus_groups")
    protected List<BonusItemGroup> bonusGroupsRaw;

    @XmlElement(name = "craft_materials")
    protected List<CraftItemGroup> craftMaterials;
    @XmlElement(name = "craft_shop")
    protected List<CraftItemGroup> craftShop;
    @XmlElement(name = "craft_bundles")
    protected List<CraftItemGroup> craftBundles;
    @XmlElement(name = "craft_recipes")
    protected List<CraftItemGroup> craftRecipes;

    @XmlElement(name = "manastones_common")
    protected List<ManastoneGroup> manastonesCommon;
    @XmlElement(name = "manastones_rare")
    protected List<ManastoneGroup> manastonesRare;
    @XmlElement(name = "medals")
    protected List<MedalGroup> medals;
    @XmlElement(name = "food_common")
    protected List<FoodGroup> foodCommon;
    @XmlElement(name = "food_rare")
    protected List<FoodGroup> foodRare;
    @XmlElement(name = "food_legendary")
    protected List<FoodGroup> foodLegendary;

    @XmlElement(name = "boss")
    protected List<BossGroup> boss;
    @XmlElement(name = "boss_rare")
    protected List<BossGroup> bossRare;
    @XmlElement(name = "boss_legendary")
    protected List<BossGroup> bossLegendary;

    @XmlElement(name = "enchant")
    protected List<EnchantGroup> enchant;
    @XmlElement(name = "food")
    protected List<FoodGroup> food;
    @XmlElement(name = "medicine")
    protected List<MedicineGroup> medicine;
    @XmlElement(name = "ore")
    protected List<OreGroup> ore;
    @XmlElement(name = "gather")
    protected List<GatherGroup> gather;
    @XmlElement(name = "manastone")
    protected List<ManastoneGroup> manastone;

    @XmlElement(name = "feed_groups")
    protected FeedGroups feedGroups;

    // Retail 7.8 feed groups - direct children of item_groups
    // Using Object to avoid missing class errors, they will be captured as DOM or lax
    @XmlElement(name = "feed_fluid")
    protected List<Object> feedFluid;
    @XmlElement(name = "feed_armor")
    protected List<Object> feedArmor;
    @XmlElement(name = "feed_thorn")
    protected List<Object> feedThorn;
    @XmlElement(name = "feed_bone")
    protected List<Object> feedBone;
    @XmlElement(name = "feed_balaur_material")
    protected List<Object> feedBalaurMaterial;
    @XmlElement(name = "feed_soul")
    protected List<Object> feedSoul;
    @XmlElement(name = "feed_exclude")
    protected List<Object> feedExclude;
    @XmlElement(name = "stinking_junk")
    protected List<Object> stinkingJunk;
    @XmlElement(name = "feed_healthy_all")
    protected List<Object> feedHealthyAll;
    @XmlElement(name = "feed_healthy_spicy")
    protected List<Object> feedHealthySpicy;
    @XmlElement(name = "feed_powder_biscuit")
    protected List<Object> feedPowderBiscuit;
    @XmlElement(name = "feed_crystal_biscuit")
    protected List<Object> feedCrystalBiscuit;
    @XmlElement(name = "feed_gem_biscuit")
    protected List<Object> feedGemBiscuit;
    @XmlElement(name = "poppy_snack")
    protected List<Object> poppySnack;
    @XmlElement(name = "tasty_poppy_snack")
    protected List<Object> tastyPoppySnack;
    @XmlElement(name = "nutritious_poppy_snack")
    protected List<Object> nutritiousPoppySnack;
    @XmlElement(name = "feed_shugo_event_coin")
    protected List<Object> feedShugoEventCoin;

    // Catch-all for any other unexpected retail groups
    @XmlAnyElement(lax = true)
    protected List<Object> any;

    private List<BonusItemGroup> allBonusGroups;

    void afterUnmarshal(javax.xml.bind.Unmarshaller u, Object parent) {
        allBonusGroups = new ArrayList<>();
        if (bonusGroupsRaw!= null) allBonusGroups.addAll(bonusGroupsRaw);
        if (craftMaterials!= null) for (CraftItemGroup g : craftMaterials) allBonusGroups.add(g);
        if (craftShop!= null) for (CraftItemGroup g : craftShop) allBonusGroups.add(g);
        if (craftBundles!= null) for (CraftItemGroup g : craftBundles) allBonusGroups.add(g);
        if (craftRecipes!= null) for (CraftItemGroup g : craftRecipes) allBonusGroups.add(g);
        if (manastonesCommon!= null) allBonusGroups.addAll(manastonesCommon);
        if (manastonesRare!= null) allBonusGroups.addAll(manastonesRare);
        if (medals!= null) allBonusGroups.addAll(medals);
        if (foodCommon!= null) allBonusGroups.addAll(foodCommon);
        if (foodRare!= null) allBonusGroups.addAll(foodRare);
        if (foodLegendary!= null) allBonusGroups.addAll(foodLegendary);
        if (boss!= null) allBonusGroups.addAll(boss);
        if (bossRare!= null) allBonusGroups.addAll(bossRare);
        if (bossLegendary!= null) allBonusGroups.addAll(bossLegendary);
        if (enchant!= null) allBonusGroups.addAll(enchant);
        if (food!= null) allBonusGroups.addAll(food);
        if (medicine!= null) allBonusGroups.addAll(medicine);
        if (ore!= null) allBonusGroups.addAll(ore);
        if (gather!= null) allBonusGroups.addAll(gather);
        if (manastone!= null) allBonusGroups.addAll(manastone);
    }

    public BonusItemGroup[] getBonusGroups() {
        if (allBonusGroups == null) return new BonusItemGroup[0];
        return allBonusGroups.toArray(new BonusItemGroup[0]);
    }
    public BonusItemGroup[] getCraftGroups() { return new BonusItemGroup[0]; }
    public BonusItemGroup[] getBossGroups() { return filterByType(BonusType.BOSS); }
    public BonusItemGroup[] getEnchantGroups() { return filterByType(BonusType.ENCHANT); }
    public BonusItemGroup[] getFoodGroups() { return filterByType(BonusType.FOOD); }
    public BonusItemGroup[] getGatherGroups() { return filterByType(BonusType.GATHER); }
    public BonusItemGroup[] getManastoneGroups() { return filterByType(BonusType.MANASTONE); }
    public BonusItemGroup[] getMedicineGroups() { return filterByType(BonusType.MEDICINE); }
    public BonusItemGroup[] getMedalGroups() { return filterByType(BonusType.MEDAL); }
    public BonusItemGroup[] getOreGroups() { return new BonusItemGroup[0]; }

    private BonusItemGroup[] filterByType(BonusType type) {
        if (allBonusGroups == null) return new BonusItemGroup[0];
        List<BonusItemGroup> list = new ArrayList<>();
        for (BonusItemGroup bg : allBonusGroups) {
            if (bg.getBonusType() == type) list.add(bg);
        }
        return list.toArray(new BonusItemGroup[0]);
    }
    public int size() { return allBonusGroups!= null? allBonusGroups.size() : 0; }
    public int bonusSize() { return size(); }
    public int petFoodSize() { return 1; }
    public boolean isFood(int itemId, com.aionemu.gameserver.model.templates.pet.FoodType foodType) { return true; }
}
