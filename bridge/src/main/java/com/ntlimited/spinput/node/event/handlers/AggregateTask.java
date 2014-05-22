package com.ntlimited.spinput.node.event.handlers;

import io.netty.util.Timeout;
import io.netty.util.TimerTask;

import com.ntlimited.spinput.node.Node;

public class AggregateTask implements TimerTask
{
    public AggregateTask(HueHandler handler, Node node, int deviceId)
    {
        fHandler = handler;
        fNode = node;
        fDeviceId = deviceId;
    }

    @Override
    public void run(Timeout t)
    {
        fHandler.aggregateState(fNode, fDeviceId);
        fHandler.clearAggregationTimeout();
    }

    private final HueHandler fHandler;
    private final Node fNode;
    private final int fDeviceId;
}
