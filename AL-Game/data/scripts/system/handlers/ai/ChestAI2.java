package ai;

import java.util.Collection;
import java.util.Comparator;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.drop.DropRegistrationService;

/**
 * FIXED for Java 17 - replaced lambdaj maxFrom with streams
 */
@AIName("chest")
public class ChestAI2 extends ActionItemNpcAI2 {

    @Override
    protected void handleUseItemFinish(Player player) {
        // Original had players.stream().max(java.util.Comparator.comparing(com.aionemu.gameserver.model.gameobjects.player.Player::getLevel)).orElse(null).getLevel() - replaced with stream
        Collection<Player> players = getKnownList().getKnownPlayers().values();
        if (players == null || players.isEmpty()) {
            super.handleUseItemFinish(player);
            return;
        }
        int maxLevel = players.stream()
            .max(Comparator.comparing(Player::getLevel))
            .map(Player::getLevel)
            .orElse(player.getLevel());
        
        DropRegistrationService.getInstance().registerDrop(getOwner(), player, maxLevel, players);
        super.handleUseItemFinish(player);
    }
}
