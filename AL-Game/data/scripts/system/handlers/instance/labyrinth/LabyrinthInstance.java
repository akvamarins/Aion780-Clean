package instance.labyrinth;

import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.StaticDoor;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.world.WorldMapInstance;

@InstanceID(301540000)
public class LabyrinthInstance extends GeneralInstanceHandler {
    private int kills = 0;
    private final int[] BOSSES = { 244634, 244635, 244636, 244637 };

    @Override
    public void onInstanceCreate(WorldMapInstance instance) {
        super.onInstanceCreate(instance);
    }

    @Override
    public void onEnterInstance(Player player) {
        super.onEnterInstance(player);
    }

    @Override
    public void onDie(Npc npc) {
        for (int b : BOSSES) {
            if (npc.getNpcId() == b) {
                kills++;
                openDoors();
                break;
            }
        }
    }

    private void openDoors() {
        if (instance != null)
            for (StaticDoor d : instance.getDoors().values())
                d.setOpen(true);
    }
}
