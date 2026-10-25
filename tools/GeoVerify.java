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

        String[][] probes = { { "300160000", "200.0", "800.0" }, { "300190000", "200.0", "300.0" }, { "320130000", "367.0", "643.0" } };
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

