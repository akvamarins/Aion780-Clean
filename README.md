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

