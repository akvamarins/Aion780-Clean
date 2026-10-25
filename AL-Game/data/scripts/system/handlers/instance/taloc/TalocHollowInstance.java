package instance.taloc;

import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.StaticDoor;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.world.WorldMapInstance;

@InstanceID(300190000)
public class TalocHollowInstance extends GeneralInstanceHandler {
    private final int[] BOSSES = { 653391, 653401, 653420 }; // Ancient Octanus, Kinquid, Celestius (7.x ids)

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
