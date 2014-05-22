package com.ntlimited.spinput.node.event;

import com.ntlimited.spinput.node.Node;

public interface DeviceEvent
{
    public DeviceEventType getEventType();

    public Node getNode();

    public long getTimestamp();

    public int getDeviceId();

    public int getValue();
}
