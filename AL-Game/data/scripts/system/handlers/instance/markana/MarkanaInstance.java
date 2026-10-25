package instance.markana;

import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.StaticDoor;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.world.WorldMapInstance;

@InstanceID(301520000)
public class MarkanaInstance extends GeneralInstanceHandler {
    private final int[] BOSSES = { 654713 }; // Fire Tamer (7.x id)

    @Override
    public void onInstanceCreate(WorldMapInstance i) {
        super.onInstanceCreate(i);
    }

    @Override
    public void onDie(Npc npc) {
        for (int b : BOSSES)
            if (npc.getNpcId() == b)
                openDoors();
    }

    @Override
    public void onEnterInstance(Player p) {
        super.onEnterInstance(p);
    }

    private void openDoors() {
        if (instance != null)
            for (StaticDoor d : instance.getDoors().values())
                d.setOpen(true);
    }
}
