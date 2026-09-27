/**
 * RETAIL CLEAN - SiegeBossDeathListener for Aion 7.8 Retail
 * Clean retail implementation - no EnhancedObject hacks
 * Properly stops siege on boss death as in retail
 */
package com.aionemu.gameserver.services.siegeservice;

import com.aionemu.gameserver.ai2.AbstractAI;
import com.aionemu.gameserver.ai2.eventcallback.OnDieEventCallback;
import com.aionemu.gameserver.services.SiegeService;

@SuppressWarnings("rawtypes")
public class SiegeBossDeathListener extends OnDieEventCallback {

    private final Siege<?> siege;

    public SiegeBossDeathListener(Siege siege) {
        this.siege = siege;
    }

    @Override
    public void onBeforeDie(AbstractAI obj) {
        // Retail: nothing before die, all logic in after die
    }

    @Override
    public void onAfterDie(AbstractAI obj) {
        // Retail clean: set boss killed and stop siege
        // This is exactly how retail server handles siege boss death
        if (siege != null) {
            siege.setBossKilled(true);
            SiegeService.getInstance().stopSiege(siege.getSiegeLocationId());
        }
    }
}
