package com.ntlimited.netty.hue.throttle;

import com.ntlimited.netty.hue.command.HueCommand;

public class NonThrottle implements Throttle
{
    @Override
    public void queue(HueCommand cmd)
    {
    }
}
