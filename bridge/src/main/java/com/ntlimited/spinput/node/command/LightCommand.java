package com.ntlimited.spinput.node.command;

import com.ntlimited.spinput.node.Endianness;

/**
 * Command to control the lights on a specific device
 * on a node.
 */
public class LightCommand implements NodeCommand
{
    /**
     * Create an immutable light command with the specified
     * options.
     *
     * @param device the device ID to target on the node
     * @param brightness the brightness to set the light to
     * @param speed the speed to set the light to, used for pulsing
     * @param pulse the pulse table to use
     */
    public LightCommand(int device,
                        int brightness,
                        int speed,
                        PulseTable pulse)
    {
        if (brightness < 0 || brightness > 255)
        {
            throw new IllegalArgumentException("invalid brightness value");
        }
        if (speed < 0 || speed > 510)
        {
            throw new IllegalArgumentException("invalid speed value");
        }

        fDeviceId = device;
        fBrightness = brightness;
        fSpeed = speed;
        fPulse = pulse;
    }

    /** {@inheritDoc} */
    @Override
    public NodeCommandType getCommandType()
    {
        return NodeCommandType.LIGHT;
    }

    /** {@inheritDoc} */
    @Override
    public byte[] serialize(Endianness endian)
    {
        byte[] payload = new byte[6 * 4];

        endian.writeBytes(payload, getDeviceId(), 0, 4);
        endian.writeBytes(payload, getBrightness(), 4, 4);
        endian.writeBytes(payload, getSpeed(), 8, 4);
        endian.writeBytes(payload, getPulseTable().toInt(), 12, 4);
        endian.writeBytes(payload, 0, 16, 4);
        endian.writeBytes(payload, 0, 20, 4);

        return payload;
    }

    /** 
     * Get the device ID targetted by the command.
     *
     * @return the command's device ID
     */
    public int getDeviceId()
    {
        return fDeviceId;
    }

    /**
     * Get the static brightness for the command.
     *
     * @return the brightness
     */
    public int getBrightness()
    {
        return fBrightness;
    }

    /**
     * Get the speed value for the command.
     *
     * @return the speed value
     */
    public int getSpeed()
    {
        return fSpeed;
    }

    /**
     * Get the pulse table value for the command.
     *
     * @retrn the pulse table
     */
    public PulseTable getPulseTable()
    {
        return fPulse;
    }

    /**
     * Create a command to turn on the light on the device with the
     * specified ID.
     *
     * @param deviceId
     * @return a command to turn on the light
     */
    public static LightCommand ON(int deviceId)
    {
        return new LightCommand(deviceId, 255, 0, PulseTable.NO_PULSE);
    }

    /**
     * Create a command to turn the light on the device with the
     * specified ID off.
     *
     * @param deviceId the device to turn off the light on
     * @return a command to turn off the light
     */
    public static final LightCommand OFF(int deviceId)
    {
        return new LightCommand(deviceId, 0, 0, PulseTable.NO_PULSE);
    }

    private final int fDeviceId;
    private final int fBrightness;
    private final int fSpeed;
    private final PulseTable fPulse;
}
