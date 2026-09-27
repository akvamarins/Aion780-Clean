# Aion 7.8 Retail CLEAN - Working Core

## This is PURE RETAIL implementation

### Fixes for Retail:
1. QuestEngineLoader - loads ALL quests dynamically via ScriptManager (5000+ quests), not 11 hardcoded
2. XmlNpcDrops - pure retail structure, supports drop_group with chance, min/max count
3. XmlDropGroup - retail with name, chance, use_category
4. XmlDrop - retail with item_id, chance, min/max count, no_reduce, eachmember
5. ScriptCompilerImpl - clean Java 17, no --release 8 hack
6. SiegeBossDeathListener - retail clean, stops siege on boss death

### Retail Functionality:
- Quests: All retail quests from Poeta to Dumaha (5000+)
- Drops: Retail drop rates with categories
- Sieges: Retail siege timers and boss handling
- AI: All AI scripts compile on Java 17
- No custom V2 loaders, no hardcoded lists

### Build:
mvn clean install -DskipTests

### Start (Java 17):
java --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED --add-opens java.base/java.io=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED -cp "./lib/*;." com.aionemu.gameserver.GameServer

### Verification:
After start, check logs:
- [QuestEngine RETAIL] Loaded X handlers (should be 1000+)
- [QuestEngine RETAIL] RETAIL CHECK PASSED
- No "skipping death callback"
- Sieges stop on boss death
