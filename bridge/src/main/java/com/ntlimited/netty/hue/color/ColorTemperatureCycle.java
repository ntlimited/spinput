package com.ntlimited.netty.hue.color;

public class ColorTemperatureCycle implements ColorCycle<ColorTemperature>
{
    public ColorTemperatureCycle(ColorTemperature start, int increments)
    {
        this(start, increments, true);
    }

    public ColorTemperatureCycle(ColorTemperature start, int increments, boolean wrap)
    {
        fCurrent = start.getTemperature() - ColorTemperature.MIN;
        fWrap = wrap;

        fMin = ColorTemperature.MIN;
        fMax = ColorTemperature.MAX;
        fRange = fMax - fMin;
        fInterval = fRange/increments;
    }

    @Override
    public ColorTemperature cycle(int increments)
    {
        fCurrent += increments * fInterval;
        if (fCurrent > fRange)
        {
            if (fWrap)
            {
                fCurrent = fCurrent % fRange;
            }
            else
            {
                fCurrent = fRange;
            }
        }
        else if (fCurrent < 0)
        {
            if (fWrap)
            {
                while (fCurrent < 0)
                {
                    fCurrent += fRange;
                }
            }
            else
            {
                fCurrent = 0;
            }
        }

        return new ColorTemperature(fMin + fCurrent);
    }

    private int fCurrent;

    private final int fInterval;
    private final boolean fWrap;

    private final int fMin;
    private final int fMax;
    private final int fRange;
}
