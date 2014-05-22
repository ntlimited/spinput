package com.ntlimited.netty.hue.throttle;

import com.ntlimited.netty.hue.command.HueCommand;

public interface Throttle
{
    public void queue(HueCommand command);
}
