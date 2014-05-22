package com.ntlimited.netty.hue.color;

public class HueMonoCycle implements ColorCycle<ColorHue>
{
    public HueMonoCycle(ColorHue start, int increments)
    {
        this(start, increments, increments);
    }

    public HueMonoCycle(ColorHue start, int incrementsHue, int incrementsSat)
    {
        fIntervalHue = 65535/incrementsHue;
        fIntervalSat = 255/incrementsSat;
        fHue = start.getHue();
        fSat = start.getSaturation();
    }

    public ColorHue cycle(int increments)
    {
        if (increments > 0)
        {
            return cycleHue(increments);
        }
        return cycleSat(-increments);
    }

    private ColorHue cycleHue(int increments)
    {
        fHue = fHue + (fIntervalHue * increments);
        if (fHue > 65535)
        {
            fHue %= 65536;
        }

        return new ColorHue(fHue, fSat);
    }

    private ColorHue cycleSat(int increments)
    {
        fSat += (fIntervalSat * increments);
        if (fSat > 255)
        {
            fSat %= 256;
        }

        return new ColorHue(fHue, fSat);
    }

    private int fHue;
    private int fSat;
    private final int fIntervalHue;
    private final int fIntervalSat;
}

