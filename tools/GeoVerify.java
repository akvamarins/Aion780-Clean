import com.aionemu.gameserver.geoEngine.GeoWorldLoader;
import com.aionemu.gameserver.geoEngine.models.GeoMap;
import com.aionemu.gameserver.geoEngine.scene.Spatial;

import java.io.File;
import java.util.Map;

public class GeoVerify {
    public static void main(String[] a) throws Exception {
        String meshsFile = a[0];
        // init empty MATERIAL_DATA so loader's material lookup doesn't NPE
        java.lang.reflect.Field f = com.aionemu.gameserver.dataholders.DataManager.class.getField("MATERIAL_DATA");
        f.set(null, new com.aionemu.gameserver.dataholders.MaterialData());

        System.out.println("Loading merged meshs (may take a while)...");
        long t0 = System.currentTimeMillis();
        Map<String, Spatial> models = GeoWorldLoader.loadMeshs(meshsFile);
        System.out.println("  " + models.size() + " models in " + (System.currentTimeMillis() - t0) / 1000 + "s");

        String[][] probes = {
            { "210060000", "2295.6", "2234.8" },  // Theobomos Atropos ~62
            { "210030000", "1640.8", "1500.3" },  // Verteron teleport ~120
            { "220050000", "2917.3", "2417.4" },  // Brusthonin teleport ~15
            { "210020000", "344.5", "2723.5" },   // Eltnen teleport ~264
            { "220020000", "526.7", "800.0" },    // Morheim-ish
            { "220030000", "2917.0", "2417.0" },  // Altgard-ish
        };
        for (String[] p : probes) {
            String mapId = p[0];
            GeoMap map = new GeoMap(mapId, 3072);
            boolean ok = GeoWorldLoader.loadWorld(Integer.parseInt(mapId), models, map);
            float z = map.getZ(Float.parseFloat(p[1]), Float.parseFloat(p[2]));
            System.out.println("map " + mapId + ": loaded=" + ok + " children=" + map.getChildren().size()
                + " getZ(" + p[1] + "," + p[2] + ")=" + z);
        }
        System.out.println("VERIFY DONE");
    }
}
