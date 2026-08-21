\# Aion 7.8 Clean - Retail Fix Pack v1



Base: Aion 7.8 Clean Emulator

Date: 20.08.2026

Author: xsayberx



\## Fixed Critical Issues



\### 1. NPC - Tursin Big Boss \& low lvl mobs \[FIXED]

\- File: `AL-Game/data/static\_data/npcs/npc\_templates/`

\- Was: Hp 5000+ / Atk 200+

\- Now: Hp 943 / Atk 44 / Pdef 67 / Mdef 42 - RETAIL

\- Status: ✅



\### 2. Weapons 1-10 lvl \[FIXED - 3420 items]

\- File: `AL-Game/data/static\_data/items/`

\- Was: PHYSICAL\_ATTACK 80-120 on lvl 1

\- Now: 18 + quality (Common 18 / Uncommon 20 / Rare 23)

\- Total fixed: 3420

\- Status: ✅



\### 3. Armor 1-10 lvl \[FIXED - 2 items]

\- File: `AL-Game/data/static\_data/items/item/item\_etc\_templates.xml`

\- Was: id=187055001 pdef=450 / id=187055002 pdef=450 at lvl 10

\- Now: pdef=42 (lvl\*3+12)

\- Status: ✅



\### 4. Manastone / Godstone \[CHECKED]

\- Check: `Check-Stones.py` -> Total inflated stones: 0

\- Status: ✅ Clean



\### 5. Dupe Fixes - 4 critical \[FIXED]

1\. Trade Dupe - double add item on lag - TradeService.java

2\. Mail Dupe - kinah dupe via cancel attachment - MailService.java

3\. Warehouse Dupe - item dupe on crash - WarehouseService.java

4\. Enchant Dupe - rollback dupe - EnchantService.java

\- Fix: DB transaction + item exist check

\- Status: ✅



\## Verify

```bat

python Check-Critical-Aion.py

python Check-Stones.py

python Check-Armor.py
   # TODO - Lightning Portal Fixes

Portal fixes from `Teleports.txt`. All coordinates moved to `portal\_loc.xml` + `portal\_template2.xml`.

## Files in this folder (were broken)

|File|World ID|Issue|Fix|
|-|-|-|-|
|`301520000\_Makarna.xml`|301520000|No entry/exit|Added loc\_id `3015200` entry, `2200804`/`2100704` exit|
|`301520000\_Makarna\_Elyos-462420420.xml`|301520000|Elyos duplicate|Same fix with race split|
|`302340000\_Narakkalli\_1.xml`|302340000|No exit|Added exit `3023401` -> Lakrum `600200000`|
|`302340000\_Narakkalli\_2.xml`|302340000|No exit|Same|
|`302440000\_Silentera Canyon\_Asmodians.xml`|302440000|No inner ports|Asmo port `3024400`|
|`302440000\_Silentera Canyon\_Elyos.xml`|302440000|No inner ports|Elyos port `3024401`|
|`302460000\_Kubrinerk's Cube Laboratory.xml`|302460000|Entry only|Entry `3024600`|
|`Teleports.txt`|-|Reference coords|Source for `portal\_loc.xml`|

## Temporary NPCs (added in portal\_template2.xml)

|NPC ID|Purpose|Loc ID|Spawn command|
|-|-|-|-|
|`730500`|Makarna Entry|`3015200`|`//spawn 730500`|
|`730501`|Makarna Exit Asmo/Elyos|`2200804` / `2100704`|`//spawn 730501`|
|`730502`|Narakkalli Exit|`3023401`|`//spawn 730502`|
|`730503`|Silentera Port Asmo|`3024400`|`//spawn 730503`|
|`730504`|Silentera Port Elyos|`3024401`|`//spawn 730504`|
|`730505`|Kubrinerk Lab Entry|`3024600`|`//spawn 730505`|
|`730506-730512`|Other instances (Kromede, Fire Temple, etc)|see portal\_loc|`//spawn 730506` etc|

> Later replace `730500-730505` with retail portal models (usually `730318`, `730322` type).

## Testing

* Enter Makarna via `//spawn 730500` -> should teleport to `301520000`
* Inside Makarna `//spawn 730501` -> exit to `220080000` (Asmo) / `210070000` (Elyos)
* Same for Narakkalli, Silentera, Kubrinerk

## Lightning TODO

* \[x] Makarna entry/exit
* \[x] Narakkalli exit
* \[x] Silentera Canyon ports
* \[x] Kubrinerk Cube Lab
* \[ ] Garden of Knowledge bosses (in Teleports.txt, needs separate NPCs)
* \[ ] Prometun Grappling Hook `3024301` - already exists

Fix date: 2026
# Aion 7.8 Lightning - Classic Poeta Fixes

This server had 3 common issues on a fresh install. Fixed on 2026-05-13.

## 1. Insane Physical Attack (1284 at level 7)

**Symptom:**
- Level 5 naked: 30 attack (correct)
- Level 7 with starter gear: 1284-1396 attack, 1277 damage to Smallhorn Kerub
- After fix: 54 attack, 42-45 auto-attack, 169 with Ferocious Strike to Tursin Big Boss (retail-like)

**Root cause:**
`AL-Game/data/static_data/items/item_etc_templates.xml`

Item `187055001` - `[10th Anniversary] Superior Wings of War` level 10 had:
```xml
<add name="PHYSICAL_ATTACK" value="1200"/>
<add name="PHYSICAL_ATTACK" value="264" bonus="true"/>
```
Total 1464 attack at level 10. If this wing is given in starter kit, you get 1284 attack at level 7.

Also affected:
- `120003270`, `121003270`, `122003271`, `123003266` - Ultimate Ambush War accessories level 10 with 206 attack
- `100050226`, `100250223`, `100950221`, `101350221`, `101550223`, `101750220` - Legionary's Simple weapons level 15 with 368 attack

**Fix:**
In `item_etc_templates.xml` replace high PHYSICAL_ATTACK for low level (<=15) items to 20.
Fixed file provided: `item_etc_templates_FIXED.xml`

After replace, delete:
```
AL-Game/cache/
```

## 2. Huge XP - 3772 XP from Kerub level 3

**Symptom:**
`You have gained 3,772 XP from Bigfoot Kerubar` at level 7. Normal is ~300.

**Root cause:**
`AL-Game/configs/main/rates.properties`

```properties
gameserver.rate.regular.xp = 1
gameserver.rate.premium.xp = 2
gameserver.rate.holiday.enable = true
gameserver.rate.holiday.days = 1,7
gameserver.rate.holiday.regular = 1
gameserver.rate.holiday.premium = 2
```

Comment: `The value added, not multiplied`

On Saturday/Sunday (day 7 and 1) you get:
- Regular: 1 + 1 = x2
- Premium: 2 + 2 = x4

Test account was Premium, so 900 * 4 = ~3600 XP.

**Fix:**
```properties
gameserver.rate.holiday.enable = false
gameserver.rate.premium.xp = 1
```

## 3. Aggro "not working"

**Symptom:**
Mobs only attack after you hit them, they don't see you.

**Explanation:**
This is retail behavior for Poeta.

- `Smallhorn Kerub`, `Bigfoot Kerubar`, `Brownbristle Brax` - are **passive**. They never aggro by sight.
- `Tursin Big Boss`, `Mau`, `Sparkie` - are **aggressive** with `Pre-emptive Attack` label. They aggro by sight.

Test aggro on Tursin, not on Kerub.

If aggressive mobs still don't aggro, check:

```properties
# geo.properties
gameserver.geo.enable = true
gameserver.geo.npc.can.see = true

# ai.properties
gameserver.ai2.enable = true
gameserver.ai2.npc.aggro = true
```

And ensure GeoData for map `110010000` is loaded: log should show `Loaded geo for map 110010000`




