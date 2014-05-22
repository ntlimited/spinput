package com.ntlimited.spinput.node.command;

/**
 * Options for setting pulse types on node devices.
 */
public enum PulseTable
{
    NO_PULSE,
    PULSE_SLOW,
    PULSE_FAST;

    /**
     * Get an integer representation for serialization
     * purposes.
     *
     * @return an integer representation of the enum
     */
    public int toInt()
    {
        switch(this)
        {
            case NO_PULSE:
                return 0;
            case PULSE_SLOW:
                return 1;
            case PULSE_FAST:
                return 2;
            default:
                throw new IllegalStateException(
                    "unknown pulse table value: " + this);
        }
    }
};
