package zone.pvpZones;

import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.player.PlayerReviveService;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.knownlist.Visitor;
import com.aionemu.gameserver.world.zone.SiegeZoneInstance;
import com.aionemu.gameserver.world.zone.ZoneInstance;
import com.aionemu.gameserver.world.zone.ZoneName;
import com.aionemu.gameserver.world.zone.handler.AdvencedZoneHandler;
import com.aionemu.gameserver.world.zone.handler.ZoneNameAnnotation;

/**
 * @author MrPoke (Aion-Lightning), flattened to standalone class for SaYberAioN
 *         (script engine cannot resolve script-to-script inheritance)
 */
@ZoneNameAnnotation(value = "LC1_PVP_SUB_C DC1_PVP_ZONE")
public class PvPAreaZone implements AdvencedZoneHandler {

    @Override
    public void onEnterZone(Creature player, ZoneInstance zone) {
    }

    @Override
    public void onLeaveZone(Creature player, ZoneInstance zone) {
    }

    @Override
    public boolean onDie(final Creature lastAttacker, Creature target, final ZoneInstance zone) {
        if (!(target instanceof Player)) {
            return false;
        }

        final Player player = (Player) target;

        PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.DIE, 0, player.equals(lastAttacker) ? 0 : lastAttacker.getObjectId()), true);
        if (zone instanceof SiegeZoneInstance) {
            ((SiegeZoneInstance) zone).doOnAllPlayers(new Visitor<Player>() {

                @Override
                public void visit(Player p) {
                    PacketSendUtility.sendPacket(p, SM_SYSTEM_MESSAGE.STR_PvPZONE_OUT_MESSAGE(player.getName()));
                }
            });

            ThreadPoolManager.getInstance().schedule(new Runnable() {

                @Override
                public void run() {
                    PlayerReviveService.duelRevive(player);
                    doTeleport(player, zone.getZoneTemplate().getName());
                    PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_PvPZONE_MY_DEATH_TO_B(lastAttacker.getName()));
                }
            }, 5000);
        }
        return true;
    }

    protected void doTeleport(Player player, ZoneName zoneName) {
        if (zoneName == ZoneName.get("LC1_PVP_SUB_C")) {
            TeleportService2.teleportTo(player, 110010000, 1, 1470.3f, 1343.5f, 563.7f);
        } else if (zoneName == ZoneName.get("DC1_PVP_ZONE")) {
            TeleportService2.teleportTo(player, 120010000, 1, 1005.1f, 1528.9f, 222.1f);
        }
    }
}
