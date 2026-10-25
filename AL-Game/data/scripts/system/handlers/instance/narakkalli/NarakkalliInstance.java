package instance.narakkalli;

import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.StaticDoor;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.world.WorldMapInstance;

@InstanceID(302340000)
public class NarakkalliInstance extends GeneralInstanceHandler {
    private final int FINAL = 244320;
    private final int[] BOSSES = { 244318, 244319, 244320 };

    @Override
    public void onInstanceCreate(WorldMapInstance i) {
        super.onInstanceCreate(i);
    }

    @Override
    public void onDie(Npc npc) {
        for (int b : BOSSES)
            if (npc.getNpcId() == b) {
                openDoors();
                if (npc.getNpcId() == FINAL) {
                    onInstanceDestroy();
                }
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
