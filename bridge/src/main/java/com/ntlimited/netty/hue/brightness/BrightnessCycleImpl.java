package com.ntlimited.netty.hue.brightness;

/**
 * 
 */
public class BrightnessCycleImpl implements BrightnessCycle
{
    public BrightnessCycleImpl(int start, int increments)
    {
        this(start, increments, false);
    }

    public BrightnessCycleImpl(int start, int increments, boolean wrap)
    {
        if (fCurrent > MAX)
        {
            throw new IllegalArgumentException(
                "Starting brightness cannot be above " + MAX);
        }
        if (fCurrent < 0)
        {
            fCurrent = OFF;
        }

        fCurrent = start;
        fInterval = 256/increments;
        fWrap = wrap;
    }

    public int cycle(int increments)
    {
        fCurrent += increments * fInterval;

        if (!fWrap)
        {
            if (fCurrent > 255)
            {
                fCurrent = 255;
            }
            else if (fCurrent < 0)
            {
                fCurrent = -1;
            }
        }
        else
        {
            if (fCurrent > 255)
            {
                fCurrent = fCurrent % 255;
            }
            else if (fCurrent < 0)
            {
                while (fCurrent < 0)
                {
                    fCurrent += 255;
                }
            }
        }

        return fCurrent;
    }

    private int fCurrent;
    private final int fInterval;
    private final boolean fWrap;
}
