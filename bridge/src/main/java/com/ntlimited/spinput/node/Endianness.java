package com.ntlimited.spinput.node;

import java.nio.ByteOrder;

/** 
 * As the network protocol for the spinput daemon involves
 * writing structs across the wire in C, the endianness of
 * the node is significant to properly parse and send
 * messages.
 */
public enum Endianness
{
    BIG,
    LITTLE;

    public ByteOrder getByteOrder()
    {
        switch (this)
        {
            case BIG:
                return ByteOrder.BIG_ENDIAN;
            case LITTLE:
                return ByteOrder.LITTLE_ENDIAN;
        }

        return null;
    }

    /**
     * Write integer data to the byte array based on
     * the Endianness value.
     *
     * @param b the byte array to write to
     * @param v the value to write out
     * @param o the offset in the array to start writing at
     * @param l the number of bytes to write out
     */
    public void writeBytes(byte[] b, long v, int o, int l)
    {
        writeBytes(this, b, v, o, l);
    }

    /**
     * Parse bytes from a byte array into a long.
     *
     * @param b the byte array to read from
     * @param o the offset in the array to start at
     * @param l the number of bytes to parse
     * @return the parsed value
     */
    public long parseBytes(byte[] b, int o, int l)
    {
        return parseBytes(this, b, o, l);
    }

    /**
     * Converts a numeric value to the opposite endianness.
     *
     * @param l the value to reverse
     * @param s the actual length, in bytes, of the value
     * @return the reversed value
     */
    public static long reverseEndianness(long l, int s)
    {
        long value = 0;
        for (int i = 0 ; i < s ; i++)
        {
            value = (value<<8) + ((l>>(i*8))&0xFF);
        }
        return value;
    }

    /**
     * Write out bytes to a byte array according to the given
     * endianness.
     *
     * @param e the endianness to use
     * @param b the array to write to
     * @param v the value to write out
     * @param o the offset in the array
     * @param l the number of bytes to write out
     */
    public static void writeBytes(Endianness e,
                                  byte[] b,
                                  long v,
                                  int o,
                                  int l)
    {
        int start;
        int end;
        int inc;

        switch (e)
        {
            case LITTLE:
                start = o;
                end = o + l;
                inc = 1;
                break;
            case BIG:
                start = o + l - 1;
                end = o - 1;
                inc = -1;
                break;
            default:
                throw new IllegalStateException("invalid endianness value");
        }

        for (int i = start ; i != end ; i += inc)
        {
            b[i] = (byte)(v&0xFF);
            v >>= 8;
        }
    }

    /** 
     * Parse bytes to a long according to the given endianness.
     *
     * @param e the endianness to use
     * @param b the byte array to parse
     * @param o the offset to start at
     * @param l the number of bytes to parse
     * @return the bytes represented as a long value
     */
    public static long parseBytes(Endianness e, byte[] b, int o, int l)
    {
        long value = 0;
        int start = -1;
        int end = -1;
        int inc = -1;

        switch (e)
        {
            case BIG:
                start = o;
                inc = 1;
                end = o + l;
                break;
            case LITTLE:
                start = o + l - 1;
                inc = -1;
                end = o - 1;
                break;
        }

        for (int i = start ; i != end ; i += inc)
        {
            value = (value<<8) + (b[i]&0xFF);
        }

        return value;
    }
}
