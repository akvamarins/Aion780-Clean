import java.io.*;
import java.nio.*;
import java.nio.channels.FileChannel;
import java.util.*;

public class DiagMesh {
    public static void main(String[] a) throws Exception {
        Map<String, GeoConvert.Model> models = GeoConvert.parseBeyondModels(new File("geoin/models.mesh"));
        System.out.println("models: " + models.size());
        int i = 0;
        for (String k : models.keySet()) {
            System.out.println("key: [" + k + "]");
            if (++i >= 10) break;
        }
        String probe = "levels/common/light/natural/plants/aquatic/na_l_saltfungus_01a.cgf";
        System.out.println("probe exact: " + models.containsKey(probe));
        for (String k : models.keySet())
            if (k.contains("saltfungus"))
                System.out.println("similar key: [" + k + "]");
    }
}
