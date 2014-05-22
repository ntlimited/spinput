package com.ntlimited.netty.hue.color;

public interface ColorCycle<T extends ColorInstance>
{
    public T cycle(int magnitude);
}
