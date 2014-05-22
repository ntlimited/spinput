package com.ntlimited.netty.hue.brightness;

public interface BrightnessCycle
{
    public int cycle(int magnitude);

    public static int OFF = -1;
    public static int MAX = 255;
    public static int MIN = 0;
}
