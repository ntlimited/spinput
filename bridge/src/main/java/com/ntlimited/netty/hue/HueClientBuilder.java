package com.ntlimited.netty.hue;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

import com.ntlimited.netty.hue.throttle.NonThrottle;
import com.ntlimited.netty.hue.throttle.Throttle;

public final class HueClientBuilder
{
    public HueClientBuilder() {}

    public HueClientBuilder setAddress(InetAddress addr)
    {
        fAddress = addr;

        return this;
    }

    public HueClientBuilder setUsername(String username)
    {
        fUsername = username;

        return this;
    }

    public HueClientBuilder setUpdateInterval(int interval, TimeUnit unit)
    {
        fRefreshInterval = interval;
        fRefreshUnit = unit;

        return this;
    }

    public HueClientBuilder setThrottle(Throttle throttle)
    {
        fThrottle = throttle;

        return this;
    }

    public HueClient build()
    {
        if (fUsername == null)
        {
            throw new IllegalStateException("Username may not be null");
        }
        if (fAddress == null)
        {
            throw new IllegalStateException("Address may not be null");
        }

        return null;
    }

    private InetAddress fAddress = null;
    private String fUsername = null;
    private Throttle fThrottle = new NonThrottle();

    private int fRefreshInterval = 30;
    private TimeUnit fRefreshUnit = TimeUnit.SECONDS;
}
