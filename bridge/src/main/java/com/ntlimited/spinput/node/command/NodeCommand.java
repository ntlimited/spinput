package com.ntlimited.spinput.node.command;

import com.ntlimited.spinput.node.Endianness;

/**
 * A NodeCommand is a serializable instruction for a
 * node.
 */
public interface NodeCommand
{
    /**
     * Get the command type for this command instance.
     *
     * @return the command type
     */
    NodeCommandType getCommandType();

    /**
     * Serialize the command to a byte array, formatted
     * with the specified endianness.
     *
     * @param endian the byte ordering to use while
     *  performing binary serialization
     */
    byte[] serialize(Endianness endian);
}
