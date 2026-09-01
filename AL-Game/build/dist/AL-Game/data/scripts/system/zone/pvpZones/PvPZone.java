package zone.pvpZones;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.world.zone.ZoneInstance;
import com.aionemu.gameserver.world.zone.handler.ZoneHandler;
import com.aionemu.gameserver.world.zone.handler.ZoneHandlerClass;

@ZoneHandlerClass("PvPZone")
public class PvPZone implements ZoneHandler {

    @Override
    public void onEnterZone(Player player, ZoneInstance zone) {
        player.setInPvPZone(true);
    }

    @Override
    public void onLeaveZone(Player player, ZoneInstance zone) {
        player.setInPvPZone(false);
    }

    @Override
    public void onDie(Player player, ZoneInstance zone) {
    }
}
