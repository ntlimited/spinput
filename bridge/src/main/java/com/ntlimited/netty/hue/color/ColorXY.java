package com.ntlimited.netty.hue.color;

import java.util.Collections;
import java.util.Map;

public class ColorXY implements ColorInstance
{
    public ColorXY(double x, double y)
    {
        if (x < 0. || x > 1.)
        {
            throw new IllegalStateException("x must be between [0, 1]");
        }
        if (y < 0. || y > 1.)
        {
            throw new IllegalStateException("y must be between [0, 1]");
        }

        fX = x;
        fY = y;
    }

    @Override
    public Map<String,String> getSetter()
    {
        return Collections.<String,String>singletonMap
            ("xy", "[" + fX + "," + fY + "]");
    }

    @Override
    public String toString()
    {
        return "ColorXY<" + fX + "," + fY + ">";
    }

    private final double fX;
    private final double fY;
}
