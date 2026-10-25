package instance.senekta;

import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.StaticDoor;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * Senekta (302510000) - 7.9. Spawns from client data (247394 gate keepers,
 * 247395/655xxx mobs, 656116 mid-boss, 858650 final boss).
 */
@InstanceID(302510000)
public class SenektaInstance extends GeneralInstanceHandler {

    private static final int GATE_KEEPER = 247394;   // 2 spawns - wing gates
    private static final int MID_BOSS = 656116;      // near final room
    private static final int FINAL_BOSS = 858650;    // Ereshkigal

    private int gateKills = 0;

    @Override
    public void onInstanceCreate(WorldMapInstance instance) {
        super.onInstanceCreate(instance);
        gateKills = 0;
    }

    @Override
    public void onEnterInstance(Player player) {
        super.onEnterInstance(player);
    }

    @Override
    public void onDie(Npc npc) {
        switch (npc.getNpcId()) {
            case GATE_KEEPER:
                gateKills++;
                openDoors();
                if (gateKills >= 2)
                    sendSystemMessage(1401123);
                break;
            case MID_BOSS:
                openDoors();
                sendSystemMessage(1401124);
                break;
            case FINAL_BOSS:
                openDoors();
                sendSystemMessage(1400256);
                onInstanceDestroy();
                break;
            default:
                break;
        }
    }

    private void openDoors() {
        if (instance != null)
            for (StaticDoor d : instance.getDoors().values())
                d.setOpen(true);
    }

    private void sendSystemMessage(int msgId) {
        instance.getPlayersInside().forEach(p -> PacketSendUtility.sendPacket(p, new SM_SYSTEM_MESSAGE(msgId)));
    }

    @Override
    public void onInstanceDestroy() {
        super.onInstanceDestroy();
        gateKills = 0;
    }

    @Override
    public void onLeaveInstance(Player player) {
        super.onLeaveInstance(player);
    }
}
