import java.io.*;
import java.nio.file.*;
import java.util.zip.Inflater;

/**
 * Aion PAK extractor.
 * Record layout (little endian):
 *   AF B4 FC FB | uint32 version | uint32 flags | uint32 crc | uint16 unk | uint32 csize | uint32 usize | uint32 nameLen | name | data[csize]
 * Data is raw deflate (falls back to zlib wrapper) or stored.
 */
public class PakExtract {
    public static void main(String[] a) throws Exception {
        File pak = new File(a[0]);
        File outDir = new File(a[1]);
        outDir.mkdirs();
        byte[] b = Files.readAllBytes(pak.toPath());
        int pos = 0, count = 0;
        while (pos + 30 <= b.length) {
            if ((b[pos] & 0xFF) != 0xAF || (b[pos + 1] & 0xFF) != 0xB4 || (b[pos + 2] & 0xFF) != 0xFC || (b[pos + 3] & 0xFF) != 0xFB) {
                pos++;
                continue;
            }
            int flags = le32(b, pos + 8);
            int csize = le32(b, pos + 18);
            int usize = le32(b, pos + 22);
            int nlen = le32(b, pos + 26);
            if (nlen <= 0 || nlen > 300 || pos + 30 + nlen + csize > b.length) {
                pos++;
                continue;
            }
            String name = new String(b, pos + 30, nlen, "UTF-8");
            if (!name.matches("[\\x20-\\x7E]+")) {
                pos++;
                continue;
            }
            int dstart = pos + 30 + nlen;
            byte[] raw;
            if (csize == 0 || csize == usize) {
                raw = new byte[usize];
                System.arraycopy(b, dstart, raw, 0, usize);
            } else {
                raw = inflate(b, dstart, csize, usize);
            }
            if (raw == null) {
                System.out.println("SKIP(inflate fail): " + name);
                pos = dstart + csize;
                continue;
            }
            File out = new File(outDir, name.replace('/', File.separatorChar));
            out.getParentFile().mkdirs();
            Files.write(out.toPath(), raw);
            count++;
            System.out.println("OK " + name + " (" + usize + " bytes" + (csize != usize ? ", from " + csize : "") + ")");
            pos = dstart + csize;
        }
        System.out.println("extracted: " + count);
    }

    static int le32(byte[] b, int p) {
        return (b[p] & 0xFF) | ((b[p + 1] & 0xFF) << 8) | ((b[p + 2] & 0xFF) << 16) | ((b[p + 3] & 0xFF) << 24);
    }

    static byte[] inflate(byte[] b, int off, int csize, int usize) {
        for (boolean nowrap : new boolean[] { true, false }) {
            try {
                Inflater inf = new Inflater(nowrap);
                inf.setInput(b, off, csize);
                byte[] out = new byte[usize];
                int n = inf.inflate(out);
                inf.end();
                if (n == usize)
                    return out;
            } catch (Exception e) {
            }
        }
        return null;
    }
}
