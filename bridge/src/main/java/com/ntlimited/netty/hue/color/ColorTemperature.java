package com.ntlimited.netty.hue.color;

import java.util.Collections;
import java.util.Map;

public final class ColorTemperature implements ColorInstance
{
    public ColorTemperature(int temperature)
    {
        if (temperature < MIN || temperature > MAX)
        {
            throw new IllegalArgumentException(
                "Color temperature must be between [153, 500]");
        }

        fTemperature = temperature;
    }

    public int getTemperature()
    {
        return fTemperature;
    }

    @Override
    public Map<String,String> getSetter()
    {
        return Collections.<String,String>singletonMap
            ("temp", "" + getTemperature());
    }

    @Override
    public String toString()
    {
        return "ColorTemperature<" + fTemperature + ">";
    }

    private final int fTemperature;

    public static final int MAX = 500;
    public static final int MIN = 153;
}
