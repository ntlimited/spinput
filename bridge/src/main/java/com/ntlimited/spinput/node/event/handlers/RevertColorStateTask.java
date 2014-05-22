package com.ntlimited.spinput.node.event.handlers;

import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ntlimited.spinput.node.Node;
import com.ntlimited.spinput.node.command.LightCommand;

public class RevertColorStateTask implements TimerTask
{
    public RevertColorStateTask(HueHandler handler, Node node, int deviceId)
    {
        fHandler = handler;
        fNode = node;
        fDeviceId = deviceId;
    }

    public void run(Timeout timeout)
    {
        fHandler.setColorMode(false);
        fNode.getConnection().sendCommand(
            LightCommand.OFF(fDeviceId));

        fHandler.clearColorTimeout();

        log.info("sent off");
    }

    private final HueHandler fHandler;
    private final Node fNode;
    private final int fDeviceId;

    private static final Logger log = LoggerFactory.getLogger(RevertColorStateTask.class);
}
