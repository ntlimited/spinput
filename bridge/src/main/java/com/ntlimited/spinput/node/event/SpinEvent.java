package com.ntlimited.spinput.node.event;

import com.ntlimited.spinput.node.Node;

public final class SpinEvent extends AbstractDeviceEvent
{
    public SpinEvent(Node node, int deviceId, int data, int duration, long timestamp)
    {
        super(DeviceEventType.SPIN, node, deviceId, timestamp, data);
        fDuration = duration;
    }

    public int getDuration()
    {
        return fDuration;
    }

    private final int fDuration;
}
