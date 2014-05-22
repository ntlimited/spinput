package com.ntlimited.spinput.node.command;

/**
 * Command types that are supported by Nodes.
 */
public enum NodeCommandType
{
    WIFI,
    LIGHT;

    /**
     * Get the int value of the enum for serialization.
     *
     * @return an integer value representing the enum
     */
    public int toInt()
    {
        switch (this)
        {
            case WIFI:
                return 1;
            case LIGHT:
                return 0;
            default:
                throw new IllegalStateException(
                    "unknown command type " + this);
        }
    }
}
