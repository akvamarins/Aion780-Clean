
package com.aionemu.commons.utils.internal.chmv8;

import java.lang.reflect.Field;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import sun.misc.Unsafe;

final class PlatformDependent0 {

    private static final Unsafe UNSAFE;
    private static final long ADDRESS_FIELD_OFFSET;
    private static final long BYTE_ARRAY_BASE_OFFSET;

    static {
        Unsafe unsafe = null;
        long addressOffset = -1;
        long byteArrayBase = -1;
        try {
            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            unsafe = (Unsafe) unsafeField.get(null);
            Field addressField = Buffer.class.getDeclaredField("address");
            addressOffset = unsafe.objectFieldOffset(addressField);
            byteArrayBase = unsafe.arrayBaseOffset(byte[].class);
        } catch (Throwable t) {}
        UNSAFE = unsafe;
        ADDRESS_FIELD_OFFSET = addressOffset;
        BYTE_ARRAY_BASE_OFFSET = byteArrayBase;
    }

    static boolean hasUnsafe() { return UNSAFE != null; }
    static long directBufferAddress(ByteBuffer buffer) { 
        try { return UNSAFE.getLong(buffer, ADDRESS_FIELD_OFFSET); } catch (Throwable t) { return -1; }
    }
    static long objectFieldOffset(Field field) { return UNSAFE.objectFieldOffset(field); }
    static Object getObject(Object obj, long offset) { return UNSAFE.getObject(obj, offset); }
    static int getInt(Object obj, long offset) { return UNSAFE.getInt(obj, offset); }
    static void throwException(Throwable t) { UNSAFE.throwException(t); }
    static byte getByte(long address) { return UNSAFE.getByte(address); }
    static void putByte(long address, byte b) { UNSAFE.putByte(address, b); }
    static short getShort(long address) { return UNSAFE.getShort(address); }
    static void putShort(long address, short s) { UNSAFE.putShort(address, s); }
    static int getInt(long address) { return UNSAFE.getInt(address); }
    static void putInt(long address, int i) { UNSAFE.putInt(address, i); }
    static long getLong(long address) { return UNSAFE.getLong(address); }
    static void putLong(long address, long l) { UNSAFE.putLong(address, l); }
    static byte getByte(byte[] data, int index) { return UNSAFE.getByte(data, BYTE_ARRAY_BASE_OFFSET + index); }
    static short getShort(byte[] data, int index) { return UNSAFE.getShort(data, BYTE_ARRAY_BASE_OFFSET + index); }
    static int getInt(byte[] data, int index) { return UNSAFE.getInt(data, BYTE_ARRAY_BASE_OFFSET + index); }
    static long getLong(byte[] data, int index) { return UNSAFE.getLong(data, BYTE_ARRAY_BASE_OFFSET + index); }
    static void copyMemory(long srcAddr, long dstAddr, long length) { UNSAFE.copyMemory(srcAddr, dstAddr, length); }
    static void copyMemory(byte[] src, long srcOffset, Object dst, long dstOffset, long length) { UNSAFE.copyMemory(src, srcOffset, dst, dstOffset, length); }
    static void copyMemory(Object src, long srcOffset, byte[] dst, long dstOffset, long length) { UNSAFE.copyMemory(src, srcOffset, dst, dstOffset, length); }
    static void copyMemory(byte[] src, long srcOffset, byte[] dst, long dstOffset, long length) { UNSAFE.copyMemory(src, srcOffset, dst, dstOffset, length); }
    static int arrayBaseOffset() { return (int) BYTE_ARRAY_BASE_OFFSET; }
    static void freeDirectBuffer(ByteBuffer buffer) {
        if (UNSAFE != null) {
            try { UNSAFE.invokeCleaner(buffer); } catch (Throwable t) {}
        }
    }
    private PlatformDependent0() {}
}
