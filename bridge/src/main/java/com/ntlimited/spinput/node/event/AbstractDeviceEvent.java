package com.ntlimited.spinput.node.event;

import com.ntlimited.spinput.node.Node;

public abstract class AbstractDeviceEvent implements DeviceEvent
{
    public AbstractDeviceEvent(DeviceEventType type,
                               Node node,
                               int deviceId,
                               long timestamp,
                               int value)
    {
        fType = type;
        fNode = node;
        fDeviceId = deviceId;
        fTimestamp = timestamp;
        fValue = value;
    }

    @Override
    public DeviceEventType getEventType()
    {
        return fType;
    }

    @Override
    public long getTimestamp()
    {
        return fTimestamp;
    }

    @Override
    public int getDeviceId()
    {
        return fDeviceId;
    }

    @Override
    public int getValue()
    {
        return fValue;
    }

    @Override
    public Node getNode()
    {
        return fNode;
    }

    @Override
    public String toString()
    {
        return "DeviceEvent(" + getEventType() + ", " + getValue() + ")";
    }

    private final DeviceEventType fType;
    private final Node fNode;
    private final int fDeviceId;
    private final long fTimestamp;
    private final int fValue;
}
