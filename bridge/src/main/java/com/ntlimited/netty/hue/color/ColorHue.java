package com.ntlimited.netty.hue.color;

import java.util.HashMap;
import java.util.Map;

public final class ColorHue implements ColorInstance
{
    public ColorHue(int hue, int saturation)
    {
        if (hue < 0 || hue > 65535)
        {
            throw new IllegalArgumentException(
                "Hue must be between [0, 65535]");
        }

        if (saturation < 0 || saturation > 255)
        {
            throw new IllegalArgumentException(
                "Saturation must be between [0, 255]");
        }

        fHue = hue;
        fSaturation = saturation;
    }

    public int getHue()
    {
        return fHue;
    }

    public int getSaturation()
    {
        return fSaturation;
    }

    @Override
    public Map<String, String> getSetter()
    {
        Map<String,String> setter = new HashMap<>();
        setter.put("hue", "" + getHue());
        setter.put("sat", "" + getSaturation());

        return setter;
    }

    @Override
    public String toString()
    {
        return "ColorHue<" + fHue + ", " + fSaturation + ">";
    }

    public static final ColorHue RED = new ColorHue(0, 255);
    public static final ColorHue GREEN = new ColorHue(25500, 255);
    public static final ColorHue BLUE = new ColorHue(46920, 255);
    public static final ColorHue WHITE = new ColorHue(0, 0);

    private final int fHue;
    private final int fSaturation;
}
