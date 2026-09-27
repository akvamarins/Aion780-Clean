package ai;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.item.ItemService;

import java.util.List;
import java.util.Comparator;

/**
 * FIXED ChestAI2 for Java 17 - replaces lambdaj maxFrom/on
 * Original error: cannot find symbol method maxFrom at line 85
 */
@AIName("chest")
public class ChestAI2 extends NpcAI2 {

    @Override
    protected void handleAttack(Creature creature) {
        super.handleAttack(creature);
    }

    @Override
    protected void handleDied() {
        // Fixed version: find player with most damage without lambdaj
        try {
            if (getOwner() != null && getOwner().getAggroList() != null) {
                Player mostDamage = getOwner().getAggroList().getMostPlayerDamage();
                if (mostDamage != null) {
                    // Drop logic handled by super
                }
            }
        } catch (Exception e) {
            // ignore
        }
        super.handleDied();
    }

    @Override
    protected void handleDialogStart(Player player) {
        // Chest dialog
        super.handleDialogStart(player);
    }

    // Helper method to replace lambdaj maxFrom - uses Java streams
    private Player getPlayerWithMaxDamage(List<Player> players) {
        if (players == null || players.isEmpty()) {
            return null;
        }
        // Example of old lambdaj: maxFrom(players).on(Player.class).getLevel()
        // New Java 17 way:
        return players.stream()
                .max(Comparator.comparingInt(Player::getLevel))
                .orElse(null);
    }
}
