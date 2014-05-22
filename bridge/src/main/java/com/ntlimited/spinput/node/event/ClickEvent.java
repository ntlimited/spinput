package com.ntlimited.spinput.node.event;

import com.ntlimited.spinput.node.Node;

public final class ClickEvent extends AbstractDeviceEvent
{
    public ClickEvent(Node node, int deviceId, int data, long timestamp)
    {
        super(DeviceEventType.CLICK, node, deviceId, timestamp, data);
    }
}
