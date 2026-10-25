package instance.makarna_difficult;

import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.StaticDoor;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.world.WorldMapInstance;

@InstanceID(301680000)
public class MakarnaDifficultInstance extends GeneralInstanceHandler {
    private int kills = 0;
    private final int[] BOSSES = { 245900, 245901, 245902 };

    @Override
    public void onInstanceCreate(WorldMapInstance i) {
        super.onInstanceCreate(i);
    }

    @Override
    public void onDie(Npc npc) {
        for (int b : BOSSES)
            if (npc.getNpcId() == b) {
                kills++;
                openDoors();
            }
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
