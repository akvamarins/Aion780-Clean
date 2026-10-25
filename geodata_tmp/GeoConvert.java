import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferUShort;
import java.io.*;
import java.nio.*;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.*;

/**
 * Converts beyond-aion geo (models.mesh + <mapId>.geo + <mapId>.png heightmap)
 * to AionLightning/AionGermany geo (meshs.geo entries + <mapId>.geo).
 * beyond = BIG endian, AL = LITTLE endian.
 */
public class GeoConvert {

    static class Part {
        float[] verts;
        int[] indices;
        int materialId;
        int intentions;
    }

    static class Model {
        String name;
        List<Part> parts = new ArrayList<>();
    }

    static class Instance {
        String name;
        float[] loc = new float[3];
        float[] matrix = new float[9];
        float scale;
        byte type;
    }

    public static void main(String[] args) throws Exception {
        String srcDir = args[0];
        String existingMeshs = args[1];
        String outMeshs = args[2];
        String outGeoDir = args[3];
        String[] mapIds = Arrays.copyOfRange(args, 4, args.length);

        System.out.println("Parsing beyond models.mesh...");
        Map<String, Model> models = parseBeyondModels(new File(srcDir, "models.mesh"));
        System.out.println("  models: " + models.size());

        System.out.println("Scanning existing meshs.geo...");
        Set<String> existingNames = scanAlMeshNames(new File(existingMeshs));
        System.out.println("  existing models: " + existingNames.size());

        Map<String, Model> neededModels = new LinkedHashMap<>();
        int found = 0, missing = 0;
        List<String> missingSample = new ArrayList<>();
        for (String mapId : mapIds) {
            List<Instance> instances = parseBeyondWorld(new File(srcDir, mapId + ".geo"));
            short[] terrain = readHeightmap(new File(srcDir, mapId + ".png"));
            writeAlWorld(new File(outGeoDir, mapId + ".geo"), terrain, instances);
            for (Instance inst : instances) {
                Model m = models.get(inst.name);
                if (m == null) {
                    m = models.get(inst.name.toLowerCase());
                    if (m == null) {
                        missing++;
                        if (missingSample.size() < 5 && !missingSample.contains(inst.name))
                            missingSample.add(inst.name);
                        continue;
                    }
                }
                found++;
                for (String alias : m.name.split("\\|")) {
                    String lc = alias.toLowerCase();
                    if (!existingNames.contains(lc) && !neededModels.containsKey(lc))
                        neededModels.put(lc, m);
                }
            }
            System.out.println("map " + mapId + ": " + instances.size() + " instances, terrain " + terrain.length + " pts");
        }
        System.out.println("instance->model matches: " + found + ", missing: " + missing);
        for (String s : missingSample)
            System.out.println("  missing sample: " + s);

        System.out.println("Models to append: " + neededModels.size());
        appendAlModels(new File(existingMeshs), new File(outMeshs), neededModels);
        System.out.println("DONE");
    }

    static Map<String, Model> parseBeyondModels(File f) throws IOException {
        Map<String, Model> models = new HashMap<>();
        try (FileChannel ch = FileChannel.open(f.toPath())) {
            ByteBuffer buf = ch.map(FileChannel.MapMode.READ_ONLY, 0, ch.size());
            while (buf.remaining() > 4) {
                int nameLen = buf.getShort() & 0xFFFF;
                if (nameLen <= 0 || nameLen > 60000)
                    break;
                byte[] nb = new byte[nameLen];
                buf.get(nb);
                String name = new String(nb, "UTF-8");
                Model model = new Model();
                model.name = name;
                int modelCount = buf.get() & 0xFF;
                for (int c = 0; c < modelCount; c++) {
                    Part p = new Part();
                    int vCount = buf.getShort() & 0xFFFF;
                    p.verts = new float[vCount * 3];
                    for (int i = 0; i < p.verts.length; i++)
                        p.verts[i] = buf.getFloat();
                    int faces = buf.getShort() & 0xFFFF;
                    int idxSize = buf.get() & 0xFF;
                    p.indices = new int[faces * 3];
                    for (int i = 0; i < p.indices.length; i++)
                        p.indices[i] = (idxSize == 1) ? (buf.get() & 0xFF) : (buf.getShort() & 0xFFFF);
                    p.materialId = buf.get() & 0xFF;
                    p.intentions = buf.get() & 0xFF;
                    model.parts.add(p);
                }
                for (String alias : name.split("\\|"))
                    models.put(alias, model);
            }
        }
        return models;
    }

    static List<Instance> parseBeyondWorld(File f) throws IOException {
        List<Instance> list = new ArrayList<>();
        try (FileChannel ch = FileChannel.open(f.toPath())) {
            ByteBuffer buf = ch.map(FileChannel.MapMode.READ_ONLY, 0, ch.size());
            while (buf.remaining() > 4) {
                int nameLen = buf.getShort() & 0xFFFF;
                if (nameLen <= 0 || nameLen > 60000)
                    break;
                Instance inst = new Instance();
                byte[] nb = new byte[nameLen];
                buf.get(nb);
                inst.name = new String(nb, "UTF-8");
                for (int i = 0; i < 3; i++)
                    inst.loc[i] = buf.getFloat();
                for (int i = 0; i < 9; i++)
                    inst.matrix[i] = buf.getFloat();
                float sx = buf.getFloat();
                buf.getFloat();
                buf.getFloat();
                inst.scale = sx;
                inst.type = buf.get();
                buf.getShort();
                buf.get();
                list.add(inst);
            }
        }
        return list;
    }

    static short[] readHeightmap(File f) throws IOException {
        BufferedImage img = ImageIO.read(f);
        if (!(img.getRaster().getDataBuffer() instanceof DataBufferUShort))
            throw new IOException(f + " is not a 16-bit heightmap");
        short[] data = ((DataBufferUShort) img.getRaster().getDataBuffer()).getData();
        if (img.getWidth() != img.getHeight())
            System.out.println("  WARN: heightmap not square: " + img.getWidth() + "x" + img.getHeight());
        return data;
    }

    static Set<String> scanAlMeshNames(File f) throws IOException {
        Set<String> names = new HashSet<>();
        try (FileChannel ch = FileChannel.open(f.toPath())) {
            ByteBuffer buf = ch.map(FileChannel.MapMode.READ_ONLY, 0, ch.size());
            buf.order(ByteOrder.LITTLE_ENDIAN);
            while (buf.remaining() > 6) {
                int nameLen = buf.getShort() & 0xFFFF;
                if (nameLen <= 0 || nameLen > 60000 || buf.remaining() < nameLen + 2)
                    break;
                byte[] nb = new byte[nameLen];
                buf.get(nb);
                names.add(new String(nb, "UTF-8").toLowerCase());
                int modelCount = buf.getShort() & 0xFFFF;
                for (int c = 0; c < modelCount; c++) {
                    int vCount = buf.getInt();
                    buf.position(buf.position() + vCount * 3 * 4);
                    int iCount = buf.getInt();
                    buf.position(buf.position() + iCount * 2 + 2);
                }
            }
        }
        return names;
    }

    static void writeAlWorld(File out, short[] terrain, List<Instance> instances) throws IOException {
        try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(out)))) {
            ByteBuffer le = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
            dos.writeByte(1); // hasTerrain
            writeLeInt(dos, terrain.length);
            ByteBuffer tb = ByteBuffer.allocate(terrain.length * 2).order(ByteOrder.LITTLE_ENDIAN);
            tb.asShortBuffer().put(terrain);
            dos.write(tb.array());
            writeLeInt(dos, 0); // cutouts
            for (Instance inst : instances) {
                byte[] nb = inst.name.getBytes("UTF-8");
                writeLeShort(dos, nb.length);
                dos.write(nb);
                for (float v : inst.loc)
                    writeLeFloat(dos, v);
                for (float v : inst.matrix)
                    writeLeFloat(dos, v);
                writeLeFloat(dos, inst.scale);
                dos.writeByte(inst.type);
            }
        }
    }

    static void appendAlModels(File existing, File out, Map<String, Model> models) throws IOException {
        try (FileChannel in = FileChannel.open(existing.toPath());
             FileChannel oc = FileChannel.open(out.toPath(), StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            long transferred = 0;
            long size = in.size();
            while (transferred < size)
                transferred += in.transferTo(transferred, Math.min(size - transferred, 64L * 1024 * 1024), oc);
            ByteBuffer buf = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
            for (Map.Entry<String, Model> e : models.entrySet()) {
                Model m = e.getValue();
                byte[] nb = e.getKey().getBytes("UTF-8"); // lowercase alias as name (AL lookups are lowercased)
                buf.clear();
                buf.putShort((short) nb.length).flip();
                oc.write(buf);
                oc.write(ByteBuffer.wrap(nb));
                buf.clear();
                buf.putShort((short) m.parts.size()).flip();
                oc.write(buf);
                for (Part p : m.parts) {
                    ByteBuffer vb = ByteBuffer.allocate(4 + p.verts.length * 4).order(ByteOrder.LITTLE_ENDIAN);
                    vb.putInt(p.verts.length / 3);
                    for (float v : p.verts)
                        vb.putFloat(v);
                    vb.flip();
                    oc.write(vb);
                    ByteBuffer ib = ByteBuffer.allocate(4 + p.indices.length * 2 + 2).order(ByteOrder.LITTLE_ENDIAN);
                    ib.putInt(p.indices.length);
                    for (int idx : p.indices)
                        ib.putShort((short) idx);
                    ib.putShort((short) ((p.intentions << 8) | (p.materialId & 0xFF)));
                    ib.flip();
                    oc.write(ib);
                }
            }
        }
    }

    static void writeLeShort(DataOutputStream dos, int v) throws IOException {
        dos.writeByte(v & 0xFF);
        dos.writeByte((v >> 8) & 0xFF);
    }

    static void writeLeInt(DataOutputStream dos, int v) throws IOException {
        dos.writeByte(v & 0xFF);
        dos.writeByte((v >> 8) & 0xFF);
        dos.writeByte((v >> 16) & 0xFF);
        dos.writeByte((v >> 24) & 0xFF);
    }

    static void writeLeFloat(DataOutputStream dos, float f) throws IOException {
        writeLeInt(dos, Float.floatToIntBits(f));
    }
}

