package com.ntlimited.spinput.node;

import java.util.UUID;

/**
 * A unique identifier for spinput Nodes. This is effectively
 * a UUID but is wrapped to allow for convenience methods.
 */
public final class NodeIdentifier
{
    /**
     * Create a new NodeIdentifier from the given byte
     * array.
     *
     * @param bytes
     */
    public NodeIdentifier(byte[] bytes)
    {
        if (bytes == null || bytes.length != 16)
        {
            throw new IllegalArgumentException(
                "NodeIdentifier is a 128 bit UUID");
        }

        fUUID = createUUID(bytes);
    }

    /**
     * Create a NodeIdentifier wrapping the given UUID
     *
     * @param uuid
     */
    public NodeIdentifier(UUID uuid)
    {
        fUUID = uuid;
    }

    /**
     * Retrieve the UUID representing the NodeIdentifier.
     */
    public UUID getUUID()
    {
        return fUUID;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode()
    {
        return getUUID().hashCode();
    }

    /**
     * Determine whether this is the same identifier as anohter
     * identifier based on the UUID representations.
     */
    @Override
    public boolean equals(Object other)
    {
        if (other instanceof NodeIdentifier)
        {
            return ((NodeIdentifier)other).getUUID().equals(getUUID());
        }

        return false;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return getUUID().toString();
    }

    /**
     * Create a UUID instance from 16 bytes.
     */
    private static UUID createUUID(byte[] bytes)
    {
        long lo = 0;
        long hi = 0;

        for (int i = 0 ; i < 8 ; i++)
        {
            lo = (lo<<8) + bytes[i];
            hi = (hi<<8) + bytes[i + 8];
        }

        return new UUID(hi, lo);
    }

    private final UUID fUUID;
}
