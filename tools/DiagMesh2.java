import java.io.*;
import java.nio.*;
import java.nio.channels.FileChannel;

public class DiagMesh2 {
    public static void main(String[] a) throws Exception {
        File f = new File("geoin/models.mesh");
        try (FileChannel ch = FileChannel.open(f.toPath())) {
            ByteBuffer buf = ch.map(FileChannel.MapMode.READ_ONLY, 0, ch.size());
            int count = 0;
            while (buf.remaining() > 4) {
                int pos = buf.position();
                int nameLen = buf.getShort() & 0xFFFF;
                if (nameLen <= 0 || nameLen > 512) {
                    System.out.println("BREAK at pos " + pos + " after " + count + " models, nameLen=" + nameLen);
                    byte[] dump = new byte[64];
                    buf.position(pos);
                    buf.get(dump);
                    StringBuilder hex = new StringBuilder();
                    for (byte b : dump) hex.append(String.format("%02X ", b));
                    System.out.println(hex);
                    System.out.println(new String(dump, "UTF-8").replaceAll("[^\\x20-\\x7E]", "."));
                    break;
                }
                byte[] nb = new byte[nameLen];
                buf.get(nb);
                int modelCount = buf.get() & 0xFF;
                for (int c = 0; c < modelCount; c++) {
                    int vCount = buf.getShort() & 0xFFFF;
                    buf.position(buf.position() + vCount * 12);
                    int faces = buf.getShort() & 0xFFFF;
                    int idxSize = buf.get() & 0xFF;
                    buf.position(buf.position() + faces * 3 * idxSize + 2);
                }
                count++;
            }
            System.out.println("end, count=" + count + ", filesize=" + f.length());
        }
    }
}
